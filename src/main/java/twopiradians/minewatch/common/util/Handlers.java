package twopiradians.minewatch.common.util;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

public class Handlers {

	/**Set mainhand active (for item use action)*/
	public static final Handler ACTIVE_HAND = new Handler(Identifier.ACTIVE_HAND, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (player != null && player.getHeldItemMainhand() != null && 
					player.getHeldItemMainhand().getItem() instanceof ItemMWWeapon &&
					!player.isHandActive()) 
				player.setActiveHand(EnumHand.MAIN_HAND);
			return super.onClientTick();
		}
		@Override
		public Handler onRemove() {
			player.resetActiveHand();
			return this;
		}
	};

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
		public boolean onServerTick() {
			Handlers.setRotations(entityLiving);
			return super.onServerTick();
		}
		@Override
		public Handler onRemove() {
			rotations.remove(entityLiving);
			return this;
		}
	};

	public static void setRotations(EntityLivingBase entityLiving) {
		if (entityLiving != null && rotations.containsKey(entityLiving)) {
			Triple<Float, Float, Float> triple = rotations.get(entityLiving);
			entityLiving.prevRotationPitch = triple.getLeft();
			entityLiving.prevRotationYaw = triple.getMiddle();
			entityLiving.prevRotationYawHead = triple.getRight();
			entityLiving.rotationPitch = triple.getLeft();
			entityLiving.rotationYaw = triple.getMiddle();
			entityLiving.rotationYawHead = triple.getRight();
			entityLiving.prevRenderYawOffset = triple.getMiddle();
			entityLiving.renderYawOffset = triple.getMiddle();
		}
	}

	private float prevRotationPitch;
	private float prevRotationYaw;
	private float prevRotationYawHead;
	private float rotationPitch;
	private float rotationYaw;
	private float rotationYawHead;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void rotate(RenderLivingEvent.Pre<EntityLivingBase> event) {
		Handler handler = TickHandler.getHandler(event.getEntity(), Identifier.PREVENT_ROTATION);
		if (handler != null) {
			// save rotations to allow camera rotation
			if (event.getEntity() == Minecraft.getMinecraft().getRenderViewEntity() &&
					Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) {
				this.prevRotationPitch = event.getEntity().prevRotationPitch;
				this.prevRotationYaw = event.getEntity().prevRotationYaw;
				this.prevRotationYawHead = event.getEntity().prevRotationYawHead;
				this.rotationPitch = event.getEntity().rotationPitch;
				this.rotationYaw = event.getEntity().rotationYaw;
				this.rotationYawHead = event.getEntity().rotationYawHead;
			}
			// rotate to locked position only for render
			Handlers.setRotations(event.getEntity());
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void rotate(RenderLivingEvent.Post<EntityLivingBase> event) {
		Handler handler = TickHandler.getHandler(event.getEntity(), Identifier.PREVENT_ROTATION);
		if (handler != null && event.getEntity() == Minecraft.getMinecraft().getRenderViewEntity() &&
				Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) {
			// restore rotations to allow camera rotation
			event.getEntity().prevRotationPitch = this.prevRotationPitch;
			event.getEntity().prevRotationYaw = this.prevRotationYaw;
			event.getEntity().prevRotationYawHead = this.prevRotationYawHead;
			event.getEntity().rotationPitch = this.rotationPitch;
			event.getEntity().rotationYaw = this.rotationYaw;
			event.getEntity().rotationYawHead = this.rotationYawHead;
		}
	}

	public static HashMap<EntityLivingBase, Triple<Float, Float, Float>> rotations = Maps.newHashMap();

	/**Stop player from moving camera in first person*/
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void viewEvent(EntityViewRenderEvent.CameraSetup event) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (player != null && rotations.containsKey(player) &&
				TickHandler.hasHandler(player, Identifier.PREVENT_ROTATION) &&
				Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
			Triple<Float, Float, Float> triple = rotations.get(player);
			player.rotationPitch = triple.getLeft();
			player.rotationYaw = triple.getMiddle();
			player.rotationYawHead = triple.getRight();
			event.setPitch(triple.getLeft());
			event.setYaw(triple.getMiddle() + 180.0F);
		}
	}

	/**Stop player from using mouse buttons*/
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void mouseEvent(MouseEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		// prevent clicking / scrolling
		if ((event.getDx() != 0 || event.getDy() != 0 ||
				event.isButtonstate() || event.getDwheel() != 0) && player != null && 
				TickHandler.hasHandler(player, Identifier.PREVENT_INPUT))
			event.setCanceled(true);
	}

	/**Prevents attacking, using abilities, targeting, clicking, and scrolling*/
	public static final Handler PREVENT_INPUT = new Handler(Identifier.PREVENT_INPUT, true) {};

	@SubscribeEvent
	public void preventAttacking(LivingAttackEvent event) {
		if (event.getSource().getEntity() instanceof EntityLivingBase &&
				TickHandler.hasHandler(event.getSource().getEntity(), Identifier.PREVENT_INPUT)) {
			if (event.getSource().getEntity() instanceof EntityLiving)
				((EntityLiving)event.getSource().getEntity()).setAttackTarget((EntityLivingBase) event.getSource().getEntity());
			((EntityLivingBase) event.getSource().getEntity()).setRevengeTarget((EntityLivingBase) event.getSource().getEntity());
			event.setCanceled(true); 
		}
	}

	@SubscribeEvent
	public void preventTargeting(LivingSetAttackTargetEvent event) {
		if (event.getTarget() != event.getEntity() && event.getEntity() instanceof EntityLivingBase &&
				TickHandler.hasHandler(event.getEntity(), Identifier.PREVENT_INPUT)) {
			if (event.getEntity() instanceof EntityLiving)
				((EntityLiving)event.getEntity()).setAttackTarget((EntityLivingBase) event.getEntity());
			((EntityLivingBase) event.getEntity()).setRevengeTarget((EntityLivingBase) event.getEntity());
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
			entity.motionX = 0;
			entity.motionY = player != null && (entity.isInWater() || entity.isInLava()) ? 0.05d : Math.min(0, entity.motionY);
			entity.motionZ = 0;
			entity.motionY = Math.min(0, entity.motionY);
			entity.fallDistance *= 0.5f;
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			// prevent jumping
			if (entity instanceof EntitySlime)
				entity.onGround = false;
			entity.motionX = 0;
			entity.motionY = player != null && (entity.isInWater() || entity.isInLava()) ? 0.05d : Math.min(0, entity.motionY);
			entity.motionZ = 0;
			entity.motionY = Math.min(0, entity.motionY);
			entity.fallDistance *= 0.5f;
			// slowness
			if (this.entityLiving != null) {
				PotionEffect effect = this.entityLiving.getActivePotionEffect(MobEffects.SLOWNESS);
				if (effect == null || effect.getDuration() <= 0 || effect.getAmplifier() < 200)
					entityLiving.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, this.ticksLeft, 200, true, false));
			}
			return super.onServerTick();
		}
		@Override
		public Handler onRemove() {
			// remove slowness
			if (this.entityLiving != null && !this.entityLiving.worldObj.isRemote)
				entityLiving.removePotionEffect(MobEffects.SLOWNESS);
			return super.onRemove();
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