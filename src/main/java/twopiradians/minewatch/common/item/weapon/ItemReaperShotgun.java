package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
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
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMWThrowable;
import twopiradians.minewatch.common.entity.EntityReaperBullet;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.SPacketPotionEffect;
import twopiradians.minewatch.packet.SPacketSpawnParticle;

public class ItemReaperShotgun extends ItemMWWeapon {

	public static HashMap<EntityPlayer, Tuple<Integer, Vec3d>> clientTps = Maps.newHashMap();
	public static HashMap<EntityPlayer, Tuple<Integer, Vec3d>> serverTps = Maps.newHashMap();

	public ItemReaperShotgun() {//TODO 20% healing and 3rd person (behind) during tp
		super(30);
		this.hasOffhand = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (this.canUse(player, true) && !world.isRemote && !serverTps.containsKey(player) &&
				!hero.ability1.isSelected(player)) {
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

	@SuppressWarnings("deprecation")
	@Nullable
	private Vec3d getTeleportPos(EntityPlayer player) {
		try {
			RayTraceResult result = player.world.rayTraceBlocks(player.getPositionEyes(1), 
					player.getLookVec().scale(Integer.MAX_VALUE), false, false, true);
			if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.hitVec != null) {
				BlockPos pos = new BlockPos(result.hitVec.xCoord, result.getBlockPos().getY(), result.hitVec.zCoord);
				if (player.world.isAirBlock(pos.up()) && player.world.isAirBlock(pos.up(2)) && 
						!player.world.isAirBlock(pos) && 
						player.world.getBlockState(pos).getBlock().getCollisionBoundingBox(player.world.getBlockState(pos), player.world, pos) != null &&
						player.world.getBlockState(pos).getBlock().getCollisionBoundingBox(player.world.getBlockState(pos), player.world, pos) != Block.NULL_AABB &&
						Math.sqrt(result.getBlockPos().distanceSq(player.posX, player.posY, player.posZ)) <= 35)
					return new Vec3d(result.hitVec.xCoord, result.getBlockPos().getY()+1, result.hitVec.zCoord);
			}
		}
		catch (Exception e) {}
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
							Minewatch.proxy.spawnParticlesReaperTeleport(world, player, false, 0);
							if (Minewatch.keys.ability2(player))
								player.playSound(ModSoundEvents.reaperTeleportStart, 1.0f, 1.0f);
						}
						else
							clientTps.put(player, new Tuple(clientTps.get(player).getFirst(), tpVec));
					}
					else if (clientTps.containsKey(player) && clientTps.get(player).getFirst() == -1)
						clientTps.remove(player);
				}
				if (Minewatch.keys.lmb(player)) {
					Vec3d tpVec = this.getTeleportPos(player);
					if (tpVec != null) {
						if (world.isRemote) {
							clientTps.put(player, new Tuple(70, new Vec3d(Math.floor(tpVec.xCoord)+0.5d, tpVec.yCoord, Math.floor(tpVec.zCoord)+0.5d)));
							Minewatch.proxy.spawnParticlesReaperTeleport(world, player, true, 0);
						}
						else {
							serverTps.put(player, new Tuple(70, new Vec3d(Math.floor(tpVec.xCoord)+0.5d, tpVec.yCoord, Math.floor(tpVec.zCoord)+0.5d)));
							world.playSound(null, player.getPosition(), ModSoundEvents.reaperTeleportFinal, SoundCategory.PLAYERS, 3.0f, 1.0f);
							hero.ability1.keybind.setCooldown(player, 20, false); //TODO
							PotionEffect effect = new PotionEffect(ModPotions.frozen, 60, 1, false, false);
							player.addPotionEffect(effect);
							Minewatch.network.sendToAll(new SPacketPotionEffect(player, effect));
						}
					}
				}

				if (Minewatch.keys.rmb(player))
					hero.ability1.toggled.remove(player.getPersistentID());
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void clientSide(PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT) {
			ArrayList<EntityPlayer> toRemove = new ArrayList<EntityPlayer>();
			for (EntityPlayer player : clientTps.keySet()) {
				if ((player.getHeldItemMainhand() == null || player.getHeldItemMainhand().getItem() != this ||
						!hero.ability1.isSelected(player) || !this.canUse(player, true)) && clientTps.get(player).getFirst() == -1) {
					toRemove.add(player);
					player.playSound(ModSoundEvents.reaperTeleportStop, 1.0f, 1.0f);
				}
				else {		
					if (clientTps.get(player).getFirst() != -1) {
						if (Minecraft.getMinecraft().gameSettings.thirdPersonView != 1)
							Minecraft.getMinecraft().gameSettings.thirdPersonView = 1;

						if (clientTps.get(player).getFirst() > 1)
							clientTps.put(player, new Tuple(clientTps.get(player).getFirst()-1, clientTps.get(player).getSecond()));
						else
							toRemove.add(player);
					}

					if (player.ticksExisted % 2 == 0)
						Minewatch.proxy.spawnParticlesReaperTeleport(event.player.world, event.player, false, 1);
					else if (player.ticksExisted % 3 == 0)
						Minewatch.proxy.spawnParticlesReaperTeleport(event.player.world, event.player, false, 3);
					Minewatch.proxy.spawnParticlesReaperTeleport(event.player.world, event.player, false, 2);
					if (player.ticksExisted % 13 == 0 && clientTps.get(player).getFirst() == -1)
						player.playSound(ModSoundEvents.reaperTeleportDuring, player.world.rand.nextFloat()*0.5f+0.3f, player.world.rand.nextFloat()*0.5f+0.75f);

					if (clientTps.get(player).getFirst() > 40 && clientTps.get(player).getFirst() != -1) {
						if (player.ticksExisted % 2 == 0)
							Minewatch.proxy.spawnParticlesReaperTeleport(event.player.world, event.player, true, 1);
						else if (player.ticksExisted % 3 == 0)
							Minewatch.proxy.spawnParticlesReaperTeleport(event.player.world, event.player, true, 3);
						Minewatch.proxy.spawnParticlesReaperTeleport(event.player.world, event.player, true, 2);
					}
				}
			}
			for (EntityPlayer player : toRemove) {
				if (clientTps.get(player).getFirst() != -1)
					Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
				clientTps.remove(player);
			}
		}
	}

	@SubscribeEvent
	public void serverSide(WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.world.getTotalWorldTime() % 3 == 0) {
			ArrayList<EntityPlayer> toRemove = new ArrayList<EntityPlayer>();
			for (EntityPlayer player : serverTps.keySet()) {
				if (serverTps.get(player).getFirst() > 1) {
					serverTps.put(player, new Tuple(serverTps.get(player).getFirst()-1, serverTps.get(player).getSecond()));

					if (serverTps.get(player).getFirst() == 40) {
						if (player.isRiding())
							player.dismountRidingEntity();
						player.setPositionAndUpdate(serverTps.get(player).getSecond().xCoord, 
								serverTps.get(player).getSecond().yCoord, 
								serverTps.get(player).getSecond().zCoord);
						if (player.world.rand.nextBoolean())
							player.world.playSound(null, player.getPosition(), ModSoundEvents.reaperTeleportVoice, SoundCategory.PLAYERS, 1.0f, 1.0f);
					}
				}
				else
					toRemove.add(player);
			}
			for (EntityPlayer player : toRemove)
				serverTps.remove(player);
		}
	}

}
