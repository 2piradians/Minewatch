package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMWThrowable;
import twopiradians.minewatch.common.entity.EntityReaperBullet;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.SPacketSpawnParticle;

public class ItemReaperShotgun extends ItemMWWeapon {

	public static HashMap<EntityPlayer, Tuple<Integer, Vec3d>> clientTps = Maps.newHashMap();
	public static HashMap<EntityPlayer, Tuple<Integer, Vec3d>> serverTps = Maps.newHashMap();

	public ItemReaperShotgun() {
		super(30);
		this.hasOffhand = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (this.canUse(player, true) && !world.isRemote) {
			for (int i=0; i<20; i++) {
				EntityReaperBullet bullet = new EntityReaperBullet(world, player, hand);
				bullet.setAim(player, player.rotationPitch, player.rotationYaw, 3.0F, 4F, 1F, hand, false);
				world.spawnEntity(bullet);
			}
			world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.reaperShoot, SoundCategory.PLAYERS, 
					world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);	
			Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
			Minewatch.network.sendToAllAround(new SPacketSpawnParticle(0, vec.xCoord, vec.yCoord, vec.zCoord, 0xD93B1A, 0x510D30, 5, 5), 
					new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 128));

			this.subtractFromCurrentAmmo(player, 1, hand);
			if (!player.getCooldownTracker().hasCooldown(this))
				player.getCooldownTracker().setCooldown(this, 11);
			if (world.rand.nextInt(25) == 0 && ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) != hero)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Nullable
	private Vec3d getTeleportPos(EntityPlayer player) {
		RayTraceResult result = player.world.rayTraceBlocks(player.getPositionEyes(1), 
				player.getLookVec().scale(Integer.MAX_VALUE), false, false, true);
		if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.hitVec != null && 
				player.world.isAirBlock(result.getBlockPos().up()) && player.world.isAirBlock(result.getBlockPos().up(2)) && 
				Math.sqrt(result.getBlockPos().distanceSq(player.posX, player.posY, player.posZ)) <= 35)
			return new Vec3d(result.hitVec.xCoord, result.getBlockPos().getY()+1, result.hitVec.zCoord);
		return null;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, world, entity, itemSlot, isSelected);

		// teleport
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entity;
			if (isSelected && hero.ability1.isSelected(player) && this.canUse(player, true)) {   
				if (world.isRemote) {
					Vec3d tpVec = this.getTeleportPos(player);
					if (tpVec != null) {
						if (!clientTps.containsKey(player)) {
							clientTps.put(player, new Tuple(-1, tpVec));
							Minewatch.proxy.spawnParticlesReaperTeleport(world, player, false, true);
						}
						else
							clientTps.put(player, new Tuple(-1, tpVec));
						//for (int i=0; i<5; ++i)
						//world.spawnParticle(EnumParticleTypes.HEART, tpVec.xCoord, tpVec.yCoord+world.rand.nextDouble()*2, tpVec.zCoord, 0, 0, 0);
					}
					else if (clientTps.containsKey(player) && clientTps.get(player).getFirst() == -1)
						clientTps.remove(player);
				}
				else if (Minewatch.keys.rmb(player)) {
					Vec3d tpVec = this.getTeleportPos(player);
					if (tpVec != null) {
						if (player.attemptTeleport(tpVec.xCoord, tpVec.yCoord, tpVec.zCoord)) {
							if (player.isRiding())
								player.dismountRidingEntity();
							hero.ability1.keybind.setCooldown(player, 20, false); 
						}
						else // otherwise holding rmb on place you can't tp will freeze player in air
							player.getCooldownTracker().setCooldown(this, 20);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void clientSide(PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT) {
			ArrayList<EntityPlayer> toRemove = new ArrayList<EntityPlayer>();
			for (EntityPlayer player : clientTps.keySet()) {
				if (player.getHeldItemMainhand() == null || player.getHeldItemMainhand().getItem() != this ||
						!hero.ability1.isSelected(player) || !this.canUse(player, true))
					toRemove.add(player);
				else {				
					if (event.player.ticksExisted % 2 == 0 && clientTps.get(player).getFirst() != -1) {
						// decrement
					}

					if (clientTps.get(player).getFirst() == -1)
						Minewatch.proxy.spawnParticlesReaperTeleport(event.player.world, event.player, false, false);
				}
			}
			for (EntityPlayer player : toRemove)
				clientTps.remove(player);
		}
	}

	@SubscribeEvent
	public void serverSide(WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.world.getTotalWorldTime() % 6 == 0) {
			ArrayList<EntityPlayer> toRemove = new ArrayList<EntityPlayer>();
			for (EntityPlayer player : serverTps.keySet()) {

			}
			for (EntityPlayer player : toRemove)
				serverTps.remove(player);
		}
	}

}
