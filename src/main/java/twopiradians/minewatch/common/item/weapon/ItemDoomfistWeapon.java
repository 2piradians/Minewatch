package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.projectile.EntityDoomfistBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.RenderManager;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemDoomfistWeapon extends ItemMWWeapon {

	public static HashMap<UUID, Integer> punchAnimations = Maps.newHashMap();
	public static final ResourceLocation OVERLAY = new ResourceLocation(Minewatch.MODID, "textures/gui/doomfist_overlay.png");
	public static final ResourceLocation PUNCH_OVERLAY = new ResourceLocation(Minewatch.MODID, "textures/gui/doomfist_punch.png");
	public static final Handler RELOAD = new Handler(Identifier.DOOMFIST_RELOAD, false) {
		@Override
		public Handler onServerRemove() {
			if (entityLiving != null && entityLiving.getHeldItemMainhand() != null && 
					entityLiving.getHeldItemMainhand().getItem() == EnumHero.DOOMFIST.weapon) {
				EnumHero.DOOMFIST.weapon.setCurrentAmmo(entityLiving, EnumHero.DOOMFIST.weapon.getCurrentAmmo(entityLiving)+1, EnumHand.MAIN_HAND);
				ModSoundEvents.DOOMFIST_RELOAD.playFollowingSound(entityLiving, 0.7f, player.world.rand.nextFloat()/2+0.75f, false);
			}
			return super.onServerRemove();
		}
	};
	public static final Handler SLAM = new Handler(Identifier.DOOMFIST_SLAM, true) {
		public void move() {
			if (this.ticksLeft == this.initialTicks) { // moving up+forward
				entity.onGround = false;
				entity.setSneaking(false);
				entity.motionY += 1.2d;
				entity.moveRelative(0, 0.8f, 1);
			}
			else if (this.ticksLeft > this.initialTicks-2) // moving forward
				entity.moveRelative(0, 0.8f, 1);
			else if (!entity.onGround && number < 40) { // moving down
				entity.motionY = Math.max(entity.motionY-0.3d, -1.2d);
				this.ticksLeft = 2;
			}
			else  // on ground
				this.ticksLeft = 1;
			++number;
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			move();
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			move();
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.ABILITY_USING));
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			if (entity instanceof EntityLivingBase) {
				if (entity.onGround)
					ModSoundEvents.DOOMFIST_SLAM_STOP.playFollowingSound(entity, 1, 1, false);
				EnumHero.DOOMFIST.ability2.keybind.setCooldown((EntityLivingBase) entity, 140, false);	
				TickHandler.unregister(false, TickHandler.getHandler(entity, Identifier.ABILITY_USING));
			}	
			return super.onServerRemove();
		}
	};
	public static final Handler UPPERCUT = new Handler(Identifier.DOOMFIST_UPPERCUT, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			moveUppercut(this);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			moveUppercut(this);
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {

			return super.onServerRemove();
		}
	};
	public static final Handler UPPERCUTTING = new Handler(Identifier.DOOMFIST_UPPERCUTTING, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			moveUppercut(this);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			moveUppercut(this);
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			return super.onServerRemove();
		}
	};
	private static void moveUppercut(Handler handler) {
		if (handler.ticksLeft > 1) { // moving up
			handler.entity.setSneaking(false);
			handler.entity.onGround = false;
			handler.entity.motionY = Math.max(handler.entity.motionY, 1);
			if (handler.ticksLeft == handler.initialTicks)
				handler.entity.moveRelative(0, 0.3f, 1);
		}
		else if (!handler.entity.onGround && handler.number < 40) { // done moving up, slowly fall down
			if (handler.entity.motionY < 0)
				handler.entity.motionY = Math.min(handler.entity.motionY+0.03d, 0);
			handler.ticksLeft = 2;
		}
		else // on ground, stop handler
			handler.ticksLeft = 1;
		handler.entity.fallDistance = 0;
		if (++handler.number == 4 && !handler.entity.world.isRemote &&
				handler.entity instanceof EntityLivingBase)
			EnumHero.DOOMFIST.ability3.keybind.setCooldown((EntityLivingBase) handler.entity, 140, false);

		// check for entities to knockback
		if (!handler.entity.world.isRemote && handler.identifier == Identifier.DOOMFIST_UPPERCUTTING &&
				handler.ticksLeft > 2) {
			Vec3d look = handler.entity.getLookVec().scale(1d);
			AxisAlignedBB aabb = handler.entity.getEntityBoundingBox().expandXyz(1d).move(look);
			for (Entity target : handler.entity.world.getEntitiesWithinAABBExcludingEntity(handler.entity, aabb)) 
				if (target != handler.entityLiving && target != handler.entity && 
				target instanceof EntityLivingBase && 
				EntityHelper.shouldHit(handler.entity, target, false)) {
					if (target.isEntityAlive() && !TickHandler.hasHandler(target, Identifier.DOOMFIST_UPPERCUT) && 
							EntityHelper.attemptDamage(handler.entity, target, 50, false)) {
						TickHandler.interrupt(target);
						Minewatch.network.sendToDimension(new SPacketSimple(63, handler.entity, true, target, handler.ticksLeft+1, 0, 0), handler.entity.world.provider.getDimension());
						TickHandler.register(false, UPPERCUT.setEntity(target).setEntityLiving((EntityLivingBase) handler.entity).setTicks(handler.ticksLeft+1));
					}
				}
		}
	}
	/**number = punch power 0-1, number2 = yaw of punch, entity = getting punched, entityLiving = doomfist*/
	public static final Handler PUNCHED = new Handler(Identifier.DOOMFIST_PUNCHED, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			movePunch(this);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			movePunch(this);
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			if (entity instanceof EntityLivingBase) {
				entity.motionX = 0;
				entity.motionZ = 0;
				entity.velocityChanged = true;
			}
			if (entity instanceof EntityLivingBase) {
				// spawn particle
				RayTraceResult result = EntityHelper.getMouseOverBlock((EntityLivingBase) entity, 5, 0, (float) number2);
				if (result != null) {
					entity.motionX = 0;
					entity.motionZ = 0;
					if (!bool)
						ModSoundEvents.REINHARDT_CHARGE_HIT.playFollowingSound(entity, 1, 1, false);
					double x = result.hitVec.xCoord; 
					double y = result.hitVec.yCoord;
					double z = result.hitVec.zCoord;
					if (result.sideHit == EnumFacing.SOUTH)
						z = Math.ceil(z);
					else if (result.sideHit == EnumFacing.EAST)
						x = Math.ceil(x);
					else if (result.sideHit == EnumFacing.UP)
						y = Math.ceil(y);
					Vec3d pos = new Vec3d(x, y, z);
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_PUNCH_0, entity.world, pos.xCoord, pos.yCoord, pos.zCoord, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1.0f, 300, 15, 15, entity.world.rand.nextFloat(), 0, result.sideHit, true);
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_PUNCH_1, entity.world, pos.xCoord, pos.yCoord, pos.zCoord, 0, 0, 0, 0xFFFFFF, 0x62B4FE, 0.7f, 10, 15, 15, entity.world.rand.nextFloat(), 0, result.sideHit, true);
				}
			}
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			if (entity instanceof EntityLivingBase) {
				entity.motionX = 0;
				entity.motionZ = 0;
				entity.velocityChanged = true;
			}
			return super.onServerRemove();
		}
	};
	/**number = punch power 0-1*/
	public static final Handler PUNCH = new Handler(Identifier.DOOMFIST_PUNCH, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			movePunch(this);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			movePunch(this);
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {			
			if (entity instanceof EntityLivingBase) {
				entity.motionX *= ((EntityLivingBase)entity).moveForward;
				entity.motionZ *= ((EntityLivingBase)entity).moveForward;
				entity.velocityChanged = true;
			}
			if (entity instanceof EntityLivingBase) {
				// spawn particle
				RayTraceResult result = EntityHelper.getMouseOverBlock((EntityLivingBase) entity, 5, 0, entity.rotationYaw);
				if (result != null) {
					entity.motionX = 0;
					entity.motionZ = 0;
					if (!bool) {
						ModSoundEvents.REINHARDT_CHARGE_HIT.playFollowingSound(entity, 1, 1, false);
						punchAnimations.put(entity.getPersistentID(), 10);
					}
					double x = result.hitVec.xCoord; 
					double y = result.hitVec.yCoord;
					double z = result.hitVec.zCoord;
					if (result.sideHit == EnumFacing.SOUTH)
						z = Math.ceil(z);
					else if (result.sideHit == EnumFacing.EAST)
						x = Math.ceil(x);
					else if (result.sideHit == EnumFacing.UP)
						y = Math.ceil(y);
					Vec3d pos = new Vec3d(x, y, z);
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_PUNCH_0, entity.world, pos.xCoord, pos.yCoord, pos.zCoord, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1.0f, 300, 15, 15, entity.world.rand.nextFloat(), 0, result.sideHit, true);
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_PUNCH_1, entity.world, pos.xCoord, pos.yCoord, pos.zCoord, 0, 0, 0, 0xFFFFFF, 0x62B4FE, 0.7f, 10, 15, 15, entity.world.rand.nextFloat(), 0, result.sideHit, true);
				}
			}
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			EnumHero.DOOMFIST.ability1.keybind.setCooldown(entityLiving, 80, false);
			ModSoundEvents.DOOMFIST_PUNCH_DURING.stopFollowingSound(entity);
			return super.onServerRemove();
		}
	};
	private static void movePunch(Handler handler) {
		handler.entity.setSneaking(false);
		float prev = handler.entity.rotationYaw;
		if (handler.identifier == Identifier.DOOMFIST_PUNCHED)
			handler.entity.rotationYaw = (float) handler.number2;
		handler.entity.moveRelative(0, 1, 1);
		handler.entity.rotationYaw = prev;
		Vec3d motion = new Vec3d(handler.entity.motionX, 0, handler.entity.motionZ).normalize().scale((22d+(37d*handler.number))/20d);
		handler.entity.motionX = motion.xCoord;
		handler.entity.motionZ = motion.zCoord;

		// check for entities to pin / knockback
		if (handler.identifier == Identifier.DOOMFIST_PUNCH) {
			Vec3d look = handler.entity.getLookVec().scale(1d);
			AxisAlignedBB aabb = handler.entity.getEntityBoundingBox().expandXyz(1d).move(look);
			for (Entity target : handler.entity.world.getEntitiesWithinAABBExcludingEntity(handler.entity, aabb)) 
				if (target != handler.entityLiving && target != handler.entity && 
				target instanceof EntityLivingBase && 
				EntityHelper.shouldHit(handler.entity, target, false)) {
					/*if (target.isEntityAlive() && (TickHandler.hasHandler(target, Identifier.REINHARDT_CHARGE) ||
							TickHandler.hasHandler(target, Identifier.DOOMFIST_PUNCH))) {
						handler.ticksLeft = 20; // TODO
						TickHandler.unregister(false, TickHandler.getHandler(handler.entity, Identifier.HERO_SNEAKING));
						TickHandler.register(false, Handlers.PREVENT_INPUT.setEntity(handler.entity).setTicks(handler.ticksLeft),
								Handlers.PREVENT_ROTATION.setEntity(handler.entity).setTicks(handler.ticksLeft), 
								Handlers.PREVENT_MOVEMENT.setEntity(handler.entity).setTicks(handler.ticksLeft));
						TickHandler.register(false, Handlers.PREVENT_INPUT.setEntity(target).setTicks(handler.ticksLeft),
								Handlers.PREVENT_ROTATION.setEntity(target).setTicks(handler.ticksLeft), 
								Handlers.PREVENT_MOVEMENT.setEntity(target).setTicks(handler.ticksLeft));
						TickHandler.getHandler(handler.entity, Identifier.PREVENT_MOVEMENT).setBoolean(false);
						Minewatch.network.sendToDimension(new SPacketSimple(58, handler.entity, true, target), handler.entity.world.provider.getDimension());
					}
					else */if (target.isEntityAlive() && EntityHelper.attemptDamage(handler.entity, target, (float) (49f+(51f*handler.number)), true)) {
						TickHandler.interrupt(target);
						Minewatch.network.sendToDimension(new SPacketSimple(62, handler.entity, false, target, handler.initialTicks, handler.number, handler.entity.rotationYaw), handler.entity.world.provider.getDimension());
						TickHandler.register(false, Handlers.PREVENT_INPUT.setEntity(target).setTicks(handler.initialTicks),
								Handlers.PREVENT_ROTATION.setEntity(target).setTicks(handler.initialTicks), 
								Handlers.PREVENT_MOVEMENT.setEntity(target).setTicks(handler.initialTicks).setBoolean(true),
								PUNCHED.setEntity(target).setEntityLiving((EntityLivingBase) handler.entity).setTicks(handler.initialTicks).setNumber(handler.number).setNumber2(handler.entity.rotationYaw));
					}
					handler.ticksLeft = 1;
					handler.entity.motionX = 0;
					handler.entity.motionZ = 0;
					handler.entity.velocityChanged = true;
					if (handler.entity.world.isRemote)
						punchAnimations.put(handler.entity.getPersistentID(), 10);
					break;
				}
		}

		// check for wall impact
		float pitch = handler.entity.rotationPitch;
		handler.entity.rotationPitch = 0;
		AxisAlignedBB aabb = handler.entity.getEntityBoundingBox().contract(0, 0.1d, 0).
				move(EntityHelper.getLook(0, (float) (handler.identifier == Identifier.DOOMFIST_PUNCH ? handler.entity.getRotationYawHead() : handler.number2)).scale(1));
		handler.entity.rotationPitch = pitch;
		if (handler.entity.world.collidesWithAnyBlock(aabb)) {
			handler.ticksLeft = 1;
			handler.entity.motionX = 0;
			handler.entity.motionZ = 0;
			handler.entity.velocityChanged = true;
			if (handler.entity.world.isRemote && handler.identifier == Identifier.DOOMFIST_PUNCH) {
				handler.bool = true;
				ModSoundEvents.DOOMFIST_PUNCH_HIT.playFollowingSound(handler.entity, 1, 1, false);
				punchAnimations.put(handler.entity.getPersistentID(), 10);
			}
			else if (!handler.entity.world.isRemote && handler.identifier == Identifier.DOOMFIST_PUNCHED && 
					handler.entity instanceof EntityLivingBase && handler.entityLiving != null) {
				EntityHelper.attemptDamage(handler.entityLiving, handler.entity, (float) (49f+(101f*handler.number)), true);
			}
		}
	}

	public ItemDoomfistWeapon() {
		super(15);
		this.saveEntityToNBT = true;
	}

	@Override
	public void reload(Entity player) {}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return oldStack != newStack || slotChanged;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase player) {
		player.stopActiveHand();
		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack)	{
		return EnumAction.BOW;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// stop charging punch
		if (this.getCharge(player) != -1) {
			player.resetActiveHand();
			ModSoundEvents.DOOMFIST_PUNCH_CHARGE.stopFollowingSound(player);
			ModSoundEvents.DOOMFIST_PUNCH_CHARGE_VOICE.stopFollowingSound(player);
		}
		// shoot
		else if (this.canUse(player, true, hand, false) && !world.isRemote) {
			for (int i=0; i<6; ++i) {
				EntityDoomfistBullet projectile = new EntityDoomfistBullet(world, player, EnumHand.OFF_HAND.ordinal());
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 80, 4F, EnumHand.OFF_HAND, 18, 0.6f);
				world.spawnEntity(projectile);
			}
			ModSoundEvents.DOOMFIST_SHOOT.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);
			this.subtractFromCurrentAmmo(player, 1, hand);
			this.setCooldown(player, 7);
			if (world.rand.nextInt(6) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int timeLeft) {
		// release punch
		float charge = getCharge(player);
		if (!world.isRemote && charge > 0) {
			if (!world.isRemote) {
				ModSoundEvents.DOOMFIST_PUNCH_CHARGE.stopFollowingSound(player);
				ModSoundEvents.DOOMFIST_PUNCH_CHARGE_VOICE.stopFollowingSound(player);
				ModSoundEvents.DOOMFIST_PUNCH_DURING.playFollowingSound(player, 1, 1, false);
				ModSoundEvents.DOOMFIST_PUNCH_DURING_VOICE.playFollowingSound(player, 1, 1, false);
			}
			player.renderYawOffset = player.rotationYawHead;
			int ticks = 8;
			Minewatch.network.sendToDimension(new SPacketSimple(62, player, true, ticks, 0, 0), world.provider.getDimension());
			TickHandler.register(false, PUNCH.setEntity(player).setTicks(ticks).setNumber(charge),
					Ability.ABILITY_USING.setEntity(player).setTicks(ticks).setAbility(hero.ability1),
					Handlers.PREVENT_ROTATION.setEntity(player).setTicks(ticks));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);	

		// charge punch
		if (hand == EnumHand.MAIN_HAND && this.canUse(player, true, hand, true) && 
				hero.ability1.isSelected(player, true)) {
			if (!world.isRemote) {
				ModSoundEvents.DOOMFIST_PUNCH_CHARGE.playFollowingSound(player, 1, 1, false);
				if (world.rand.nextBoolean())
					ModSoundEvents.DOOMFIST_PUNCH_CHARGE_VOICE.playFollowingSound(player, 1, 1, false);
			}
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHeldItemMainhand() == stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// animation
			if (world.isRemote && punchAnimations.containsKey(entity.getPersistentID())) {
				if (punchAnimations.get(entity.getPersistentID()) > 1)
					punchAnimations.put(entity.getPersistentID(), punchAnimations.get(entity.getPersistentID())-1);
				else
					punchAnimations.remove(entity.getPersistentID());
			}

			// reload automatically
			if (!world.isRemote && this.getCurrentAmmo(player) < this.getMaxAmmo(player) && !TickHandler.hasHandler(player, Identifier.DOOMFIST_RELOAD))
				TickHandler.register(false, RELOAD.setEntity(player).setTicks(this.reloadTime));

			// seismic slam
			if (hero.ability2.isSelected(player) &&
					!world.isRemote && this.canUse(player, true, getHand(player, stack), true)) {
				ModSoundEvents.DOOMFIST_SLAM_START.playFollowingSound(player, 1, 1, false);				
				Minewatch.network.sendToDimension(new SPacketSimple(64, player, true), world.provider.getDimension());
				TickHandler.register(false, SLAM.setEntity(player).setTicks(5),
						Ability.ABILITY_USING.setEntity(player).setTicks(50).setAbility(hero.ability2));
			}

			// uppercut
			if (hero.ability3.isSelected(player) &&
					!world.isRemote && this.canUse(player, true, getHand(player, stack), true)) {
				ModSoundEvents.DOOMFIST_UPPERCUT_START.playFollowingSound(player, 1, 1, false);		
				ModSoundEvents.DOOMFIST_UPPERCUT_VOICE.playFollowingSound(player, 1, 1, false);	
				int ticks = 4;
				Minewatch.network.sendToDimension(new SPacketSimple(63, player, true, ticks, 0, 0), world.provider.getDimension());
				TickHandler.register(false, UPPERCUTTING.setEntity(player).setTicks(ticks),
						Ability.ABILITY_USING.setEntity(player).setTicks(ticks).setAbility(hero.ability3));
			}
		}
	}

	/**Gets current punch charge 0-1 or -1 if not charging*/
	public float getCharge(EntityLivingBase entity) {
		if (entity != null && entity.getActiveItemStack() != null && entity.getActiveItemStack().getItem() == this) {
			float charge = MathHelper.clamp(2f - (float) entity.getItemInUseCount() / 20f, 0, 1);
			return charge;
		}
		return -1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		return -1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderHand(AbstractClientPlayer player, EnumHand hand) {
		return player != null && hand == EnumHand.OFF_HAND && 
				((getModel(player) == 0 && this.getCurrentAmmo(player) < this.getMaxAmmo(player)) || getModel(player) == 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height, EnumHand hand) {
		super.preRenderGameOverlay(event, player, width, height, hand);
		
		Minecraft mc = Minecraft.getMinecraft();

		if (hand == EnumHand.MAIN_HAND && event.getType() == ElementType.CROSSHAIRS && mc.gameSettings.thirdPersonView == 0) {
			GlStateManager.enableBlend();

			float charge = getCharge(player);

			if (charge != -1) { // charging punch
				GlStateManager.pushMatrix();
				GlStateManager.translate(width/2, height/2, 0);
				mc.getTextureManager().bindTexture(PUNCH_OVERLAY);
				GlStateManager.color(1, 1, 1, 1);
				GuiUtils.drawTexturedModalRect(-128, -128, 0, 0, 256, 256, 0);

				double scale = 3d*Config.guiScale;
				GlStateManager.scale(scale, scale, 1);
				mc.getTextureManager().bindTexture(RenderManager.ABILITY_OVERLAY);
				GlStateManager.color(0.5f, 0.8f, 0.8f, 0.5f);
				GuiUtils.drawTexturedModalRect(-9, 8, 19, 239, 20, 8, 0);
				GlStateManager.color(1, 1, 1, 1);
				GuiUtils.drawTexturedModalRect(-9, 8+(int) (8d*(1d-charge)), 19, (int) (239d+(8d*(1d-charge))), 20, (int) (8d*charge), 0);
				GlStateManager.popMatrix();
			}
			else { // normal ammo overlay
				GlStateManager.pushMatrix();
				double scale = 0.65d*Config.guiScale;
				GlStateManager.translate(width/2, height/2, 0);
				GlStateManager.scale(scale, scale, 1);
				mc.getTextureManager().bindTexture(OVERLAY);
				GlStateManager.color(1, 1, 1, 1);
				GuiUtils.drawTexturedModalRect(-128, -128, 0, 0, 256, 256, 0);

				scale = 4.1d*Config.guiScale;
				GlStateManager.scale(scale, scale, 1);
				mc.getTextureManager().bindTexture(RenderManager.ABILITY_OVERLAY);
				int ammo = this.getCurrentAmmo(player);
				for (int i=0; i<4; ++i) {
					if (ammo > i)
						GlStateManager.color(1, 1, 1, 1);
					else
						GlStateManager.color(0.5f, 0.8f, 0.8f, 0.6f);

					switch(i) {
					case 3:
						GuiUtils.drawTexturedModalRect(-11, 13, 12, 244, 7, 2, 0);
						break;
					case 2:
						GuiUtils.drawTexturedModalRect(-6, 12, 12, 244, 7, 2, 0);
						break;
					case 1:
						GuiUtils.drawTexturedModalRect(-1, 12, 12, 244, 7, 2, 0);
						break;
					case 0:
						GuiUtils.drawTexturedModalRect(4, 13, 12, 244, 7, 2, 0);
						break;
					}
				}
				GlStateManager.popMatrix();
				
				Handler handler = TickHandler.getHandler(player, Identifier.DOOMFIST_SLAM);
				/*if (handler != null)*/ {
					GlStateManager.pushMatrix();
					scale = 0.8d*Config.guiScale;
					GlStateManager.translate(width/2, height/2, 0);
					GlStateManager.scale(scale, scale*1.3d, 1);
					int number = 100;
					String brackets = TextFormatting.GRAY+""+TextFormatting.ITALIC+""+TextFormatting.BOLD;
					String text = brackets+"[ "+TextFormatting.WHITE+String.valueOf(number)+brackets+" ]";
					mc.fontRendererObj.drawString(text, -mc.fontRendererObj.getStringWidth(text)/2, 10, 0xFFFFFF);
					GlStateManager.popMatrix();
				}
			}

			GlStateManager.disableBlend();
		}
	}

	/**Returns the current model
	 * 0 = normal
	 * 1 = charging punch
	 * 2 = punching
	 * 3 = uppercutting
	 * 4 = slam*/
	public int getModel(EntityLivingBase entity) {
		if (entity == null) // normal
			return 0;
		else if (getCharge(entity) > 0) // charging punch
			return 1;
		else if (TickHandler.hasHandler(entity, Identifier.DOOMFIST_PUNCH) || // punching
				(entity != null && punchAnimations.containsKey(entity.getPersistentID())))
			return 2;
		else if (TickHandler.hasHandler(entity, Identifier.DOOMFIST_UPPERCUTTING) && // uppercut
				TickHandler.getHandler(entity, Identifier.DOOMFIST_UPPERCUTTING).number < 8)
			return 3;
		else if (TickHandler.hasHandler(entity, Identifier.DOOMFIST_SLAM)) // slam
			return 4;
		else // normal
			return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType transform, Pair<? extends IBakedModel, Matrix4f> ret) {
		if (entity != null && punchAnimations.containsKey(entity.getPersistentID())) {
			float percent = (punchAnimations.get(entity.getPersistentID()) + Minewatch.proxy.getRenderPartialTicks()) / 10f;
		}
		return ret;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getAllModelLocations(ArrayList<String> locs) {
		for (int i=0; i<5; ++i)
			locs.add("_"+i);
		return locs;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getModelLocation(ItemStack stack, @Nullable EntityLivingBase entity) {
		return "_"+getModel(entity);
	}

}