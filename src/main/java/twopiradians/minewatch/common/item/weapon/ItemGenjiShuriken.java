package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Maps;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.SPacketTriggerAbility;

public class ItemGenjiShuriken extends ItemMWWeapon {

	private static HashMap<EntityPlayer, Integer> deflecting = Maps.newHashMap();
	public static HashMap<EntityPlayer, Integer> clientStriking = Maps.newHashMap();
	private static HashMap<EntityPlayerMP, Integer> serverStriking = Maps.newHashMap();
	public static HashMap<EntityPlayer, Integer> useSword = Maps.newHashMap();

	public ItemGenjiShuriken() {
		super(40);
		this.savePlayerToNBT = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 10;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (!player.world.isRemote && this.canUse(player, true, hand) && player.ticksExisted % 3 == 0 && 
				!deflecting.containsKey(player)) {
			EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player);
			shuriken.setAim(player, player.rotationPitch, player.rotationYaw, 3F, 1.0F, 1F, hand, false);
			player.world.spawnEntity(shuriken);
			player.world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.genjiShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
					player.world.rand.nextFloat()/2+0.75f);	
			this.subtractFromCurrentAmmo(player, 1, hand);
			if (!player.getCooldownTracker().hasCooldown(this) && this.getCurrentAmmo(player) % 3 == 0 &&
					this.getCurrentAmmo(player) != this.getMaxAmmo(player))
				player.getCooldownTracker().setCooldown(this, 15);
			if (player.world.rand.nextInt(24) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (!player.world.isRemote && this.canUse(player, true, hand) && !deflecting.containsKey(player)) {
			for (int i = 0; i < Math.min(3, this.getCurrentAmmo(player)); i++) {
				EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player);
				shuriken.setAim(player, player.rotationPitch, player.rotationYaw + (1 - i)*8, 3F, 1.0F, 0F, hand, false);
				player.world.spawnEntity(shuriken);
			}
			player.world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.genjiShoot, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/2+0.75f);
			this.subtractFromCurrentAmmo(player, 3, hand);
			if (world.rand.nextInt(8) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			if (!player.getCooldownTracker().hasCooldown(this))
				player.getCooldownTracker().setCooldown(this, 15);
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		// don't untoggle while active
		if (isSelected && serverStriking.containsKey(entity))
			hero.ability2.toggled.put(entity.getPersistentID(), true);
		else if (isSelected && deflecting.containsKey(entity))
			hero.ability1.toggled.put(entity.getPersistentID(), true);
		
		// block while striking
		if (isSelected && entity instanceof EntityPlayer && serverStriking.containsKey(entity) &&
				((EntityPlayer)entity).getActiveItemStack() != stack) 
			((EntityPlayer)entity).setActiveHand(EnumHand.MAIN_HAND);

		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entity;

			// deflect
			if (isSelected && !world.isRemote && hero.ability1.isSelected((EntityPlayer) entity) &&
					!deflecting.containsKey(entity) && entity instanceof EntityPlayerMP) {
				Minewatch.network.sendToAll(new SPacketTriggerAbility(4, (EntityPlayer) entity, 40, 0, 0));
				deflecting.put(player, 40);
				world.playSound(null, entity.getPosition(), ModSoundEvents.genjiDeflect, SoundCategory.PLAYERS, 1.0f, 1.0f);
			}

			// strike
			if (isSelected && !world.isRemote && hero.ability2.isSelected((EntityPlayer) entity) &&
					!serverStriking.containsKey(entity) && entity instanceof EntityPlayerMP) {
				serverStriking.put((EntityPlayerMP) player, 8);
				Minewatch.network.sendToAll(new SPacketTriggerAbility(3, true, (EntityPlayer) entity, 0, 0, 0));
				world.playSound(null, entity.getPosition(), ModSoundEvents.genjiStrike, SoundCategory.PLAYERS, 2f, 1.0f);
			}
		}
	}	

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void clientSide(PlayerTickEvent event) {
		if (event.phase == Phase.END && event.player.world.isRemote) {
			// clientStriking
			ArrayList<EntityPlayer> toRemove = new ArrayList<EntityPlayer>();
			for (EntityPlayer player : clientStriking.keySet()) {
				if (player == event.player && player instanceof EntityPlayerSP) {
					SPacketTriggerAbility.move(player, 0.9f, true);
					((EntityPlayerSP)player).movementInput.sneak = false;
					int numParticles = (int) ((Math.abs(player.chasingPosX-player.prevChasingPosX)+Math.abs(player.chasingPosY-player.prevChasingPosY)+Math.abs(player.chasingPosZ-player.prevChasingPosZ))*20d);
					for (int i=0; i<numParticles; ++i) {
						Minewatch.proxy.spawnParticlesTrail(player.world, 
								player.prevChasingPosX+(player.chasingPosX-player.prevChasingPosX)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d-0.2d, 
								player.prevChasingPosY+(player.chasingPosY-player.prevChasingPosY)*i/numParticles+player.height/2+(player.world.rand.nextDouble()-0.5d)*0.1d-0.2d, 
								player.prevChasingPosZ+(player.chasingPosZ-player.prevChasingPosZ)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d, 
								0, 0, 0, 0xAAB85A, 0xF4FCB6, 1, 7, 1);
						Minewatch.proxy.spawnParticlesTrail(player.world, 
								player.prevChasingPosX+(player.chasingPosX-player.prevChasingPosX)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d+0.2d, 
								player.prevChasingPosY+(player.chasingPosY-player.prevChasingPosY)*i/numParticles+player.height/2+(player.world.rand.nextDouble()-0.5d)*0.1d+0.2d, 
								player.prevChasingPosZ+(player.chasingPosZ-player.prevChasingPosZ)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d, 
								0, 0, 0, 0xAAB85A, 0xF4FCB6, 1, 7, 1);
						player.chasingPosX = player.prevPosX;
						player.chasingPosY = player.prevPosY;
						player.chasingPosZ = player.prevPosZ;
					}
					if (event.player.ticksExisted % 2 == 0) {
						if (clientStriking.get(player) > 1)
							clientStriking.put(player, clientStriking.get(player)-1);
						else {
							player.resetActiveHand();
							toRemove.add(player);
						}
					}
				}
			}
			for (EntityPlayer player : toRemove)
				clientStriking.remove(player);
			// useSword
			toRemove = new ArrayList<EntityPlayer>();
			for (EntityPlayer player : useSword.keySet()) {
				if (player == event.player) {
					if (event.player.ticksExisted % 2 == 0) {
						if (useSword.get(player) > 1)
							useSword.put(player, useSword.get(player)-1);
						else 
							toRemove.add(player);
					}
				}
			}
			for (EntityPlayer player : toRemove)
				useSword.remove(player);
		}
	}

	@SubscribeEvent
	public void serverSide(WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.world.getTotalWorldTime() % 6 == 0) {
			// strike
			ArrayList<EntityPlayer> toRemove = new ArrayList<EntityPlayer>();
			for (EntityPlayerMP player : serverStriking.keySet()) {
				AxisAlignedBB aabb = player.getEntityBoundingBox().expandXyz(1.3d);
				List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, aabb);

				for (Entity entityCollided : list) {
					if (entityCollided instanceof EntityLivingBase) {
						if (((EntityLivingBase)entityCollided).attackEntityFrom(DamageSource.causePlayerDamage(player), 50F*ItemMWWeapon.damageScale))
							entityCollided.world.playSound(null, entityCollided.getPosition(), ModSoundEvents.hurt, SoundCategory.PLAYERS, 0.3f, entityCollided.world.rand.nextFloat()/2+0.75f);
					}
				}

				if (serverStriking.get(player) > 1)
					serverStriking.put(player, serverStriking.get(player)-1);
				else {
					player.resetActiveHand();
					toRemove.add(player);
					hero.ability2.keybind.setCooldown(player, 160, false);
				}
			}
			for (EntityPlayer player : toRemove)
				serverStriking.remove(player);

			// deflect
			toRemove = new ArrayList<EntityPlayer>();
			for (EntityPlayer player : deflecting.keySet()) {
				AxisAlignedBB aabb = player.getEntityBoundingBox().expandXyz(4);
				List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, aabb);
				for (Entity entity : list) 
					if (!(entity instanceof EntityArrow))
						this.deflect(player, entity);
				if (deflecting.get(player) > 1)
					deflecting.put(player, deflecting.get(player)-1);
				else {
					toRemove.add(player);
					hero.ability1.keybind.setCooldown(player, 160, false);
				}
			}
			for (EntityPlayer player : toRemove)
				deflecting.remove(player);
		}
	}

	private boolean deflect(EntityPlayer player, Entity entity) {
		if ((entity instanceof EntityArrow || entity instanceof EntityThrowable || 
				entity instanceof IThrowableEntity ||entity instanceof EntityFireball ||
				entity instanceof EntityTNTPrimed) &&
				player.getLookVec().dotProduct(new Vec3d(entity.motionX, entity.motionY, entity.motionZ)) < -0.1d) {
			double velScale = Math.sqrt(entity.motionX*entity.motionX + 
					entity.motionY*entity.motionY + entity.motionZ*entity.motionZ)*1.2d;
			entity.motionX = player.getLookVec().xCoord*velScale;	
			entity.motionY = player.getLookVec().yCoord*velScale;	
			entity.motionZ = player.getLookVec().zCoord*velScale;		
			entity.velocityChanged = true;

			if (entity instanceof EntityArrow) { 
				EntityArrow ent = (EntityArrow) entity;
				ent.shootingEntity = player;
				// undo motion slowing done after event in EntityArrow#onHit
				ent.motionX *= -1/0.10000000149011612D;
				ent.motionY *= -1/0.10000000149011612D;
				ent.motionZ *= -1/0.10000000149011612D;
			}
			if (entity instanceof EntityFireball) {
				EntityFireball ent = (EntityFireball) entity;
				ent.accelerationX = ent.motionX * 0.05d;
				ent.accelerationY = ent.motionY * 0.05d;
				ent.accelerationZ = ent.motionZ * 0.05d;
				ent.shootingEntity = player;
			}
			if (entity instanceof IThrowableEntity)
				((IThrowableEntity) entity).setThrower(player);

			player.world.playSound(null, player.getPosition(), ModSoundEvents.genjiDeflectHit, SoundCategory.PLAYERS, 0.6f, player.world.rand.nextFloat()/6f+0.9f);
			player.setActiveHand(EnumHand.MAIN_HAND);
			return true;
		}
		return false;
	}

	@SubscribeEvent
	public void deflectAttack(LivingAttackEvent event) {
		if (event.getEntity() instanceof EntityPlayer && !event.getEntity().world.isRemote && 
				deflecting.containsKey(event.getEntity())) {
			if (this.deflect((EntityPlayer) event.getEntity(), event.getSource().getSourceOfDamage()))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onKill(LivingDeathEvent event) {
		// remove strike cooldown if killed by Genji
		if (event.getEntityLiving() != null && !event.getEntityLiving().world.isRemote && 
				event.getSource().getEntity() instanceof EntityPlayer) 
			hero.ability2.keybind.setCooldown((EntityPlayer) event.getSource().getEntity(), 0, false);
	}

}
