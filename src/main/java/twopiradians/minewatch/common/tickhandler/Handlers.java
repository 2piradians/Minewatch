package twopiradians.minewatch.common.tickhandler;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

public class Handlers {

	/**Locks entity's current pitch/yaw*/
	public static final Handler PREVENT_ROTATION = new Handler(Identifier.PREVENT_ROTATION, true) {
		@Override
		public Handler setEntity(Entity entity) {
			super.setEntity(entity);
			if (entityLiving != null)
				rotations.put(entityLiving, 
						Triple.of(entity.rotationPitch, entity.rotationYaw, entity.getRotationYawHead()));
			return this;
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			this.setRotations();
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			this.setRotations();
			return super.onServerTick();
		}
		public void setRotations() {
			if (entityLiving != null && rotations.containsKey(entityLiving)) {
				Triple<Float, Float, Float> triple = rotations.get(entityLiving);
				entityLiving.rotationPitch = triple.getLeft();
				entityLiving.rotationYaw = triple.getMiddle();
				entityLiving.rotationYawHead = triple.getRight();
			}
		}
		@Override
		public Handler onRemove() {
			rotations.remove(entityLiving);
			return this;
		}
	};

	private static HashMap<EntityLivingBase, Triple<Float, Float, Float>> rotations = Maps.newHashMap();

	/**Stop player from moving camera*/
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void viewEvent(EntityViewRenderEvent.CameraSetup event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player != null && rotations.containsKey(player) &&
				TickHandler.hasHandler(player, Identifier.PREVENT_ROTATION)) {
			Triple<Float, Float, Float> triple = rotations.get(player);
			player.rotationPitch = triple.getLeft();
			player.rotationYaw = triple.getMiddle();
			player.rotationYawHead = triple.getRight();
			event.setPitch(triple.getLeft());
			event.setYaw(triple.getMiddle() + 180.0F);
		}
	}

	/**Stop player from using mouse buttons while frozen*/
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void mouseEvent(MouseEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		// prevent rotations
		if ((event.getDx() != 0 || event.getDy() != 0) && player != null && 
				TickHandler.hasHandler(player, Identifier.PREVENT_ROTATION))
			event.setCanceled(true);
		// prevent clicking
		else if ((event.isButtonstate() || event.getDwheel() != 0) && player != null && 
				TickHandler.hasHandler(player, Identifier.PREVENT_INPUT))
			event.setCanceled(true);
	}

	/**Prevents attacking, using abilities, targeting, clicking, and scrolling*/
	public static final Handler PREVENT_INPUT = new Handler(Identifier.PREVENT_INPUT, true) {};

	@SubscribeEvent
	public void preventAttacking(LivingAttackEvent event) {
		if (event.getSource().getEntity() instanceof EntityLivingBase &&
				TickHandler.hasHandler(event.getSource().getEntity(), Identifier.PREVENT_INPUT)) {
			if (event.getEntity() instanceof EntityLiving)
				((EntityLiving)event.getSource().getEntity()).setAttackTarget(null);
			((EntityLivingBase) event.getSource().getEntity()).setRevengeTarget(null);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void preventTargeting(LivingSetAttackTargetEvent event) {
		if (event.getTarget() != null && event.getEntity() instanceof EntityLivingBase &&
				TickHandler.hasHandler(event.getEntity(), Identifier.PREVENT_INPUT)) {
			if (event.getEntity() instanceof EntityLiving)
				((EntityLiving)event.getEntity()).setAttackTarget(null);
			((EntityLivingBase) event.getEntity()).setRevengeTarget(null);
		}
	}

	/**Prevents moving, jumping, flying, and ender teleporting*/
	public static final Handler PREVENT_MOVEMENT = new Handler(Identifier.PREVENT_MOVEMENT, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// prevent flying
			entity.onGround = true;
			if (player != null)
				player.capabilities.isFlying = false;
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			// slowness
			if (this.entityLiving != null) {
				PotionEffect effect = this.entityLiving.getActivePotionEffect(MobEffects.SLOWNESS);
				if (effect == null || effect.getDuration() <= 0 || effect.getAmplifier() < 200)
					entityLiving.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, this.ticksLeft, 200, true, false));
			}
			return super.onServerTick();
		}
		@Override
		public Handler reset() {
			if (this.entityLiving != null && !this.entityLiving.world.isRemote)
				entityLiving.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, this.ticksLeft, 200, true, false));
			return super.reset();
		}
	};

	@SubscribeEvent
	public void preventJumping(LivingJumpEvent event) {
		if (event.getEntity() instanceof EntityLivingBase &&
				TickHandler.hasHandler(event.getEntity(), Identifier.PREVENT_MOVEMENT))  {
			event.getEntity().motionX = 0;
			event.getEntity().motionY = 0;
			event.getEntity().motionZ = 0;
			event.getEntity().isAirBorne = false;
			event.getEntity().onGround = true;
		}
	}

	@SubscribeEvent
	public void preventTeleporting(EnderTeleportEvent event) {
		if (event.getEntity() instanceof EntityLivingBase &&
				TickHandler.hasHandler(event.getEntity(), Identifier.PREVENT_MOVEMENT))
			event.setCanceled(true);
	}

}