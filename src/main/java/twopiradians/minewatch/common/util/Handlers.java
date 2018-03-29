package twopiradians.minewatch.common.util;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
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
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class Handlers {

	/**Clientside glowing effect*/
	public static final Handler CLIENT_GLOWING = new Handler(Identifier.GLOWING, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (this.entity != null && !this.entity.isGlowing() && !(entity instanceof EntityLivingBaseMW))
				this.entity.setGlowing(true);

			return super.onClientTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			if (this.entity instanceof EntityLivingBase && (((EntityLivingBase) this.entity).getActivePotionEffect(MobEffects.GLOWING) == null ||
					((EntityLivingBase) this.entity).getActivePotionEffect(MobEffects.GLOWING).getDuration() <= 0))
				this.entity.setGlowing(false);

			return super.onClientRemove();
		}
	};

	/**Set mainhand active (for item use action)*/
	public static final Handler ACTIVE_HAND = new Handler(Identifier.ACTIVE_HAND, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (entityLiving != null && entityLiving.getHeldItemMainhand() != null && 
					entityLiving.getHeldItemMainhand().getItem() instanceof ItemMWWeapon &&
					!entityLiving.isHandActive()) 
				entityLiving.setActiveHand(EnumHand.MAIN_HAND);
			return super.onClientTick();
		}
		@SideOnly(Side.CLIENT)
		@Override
		public Handler onClientRemove() {
			entityLiving.resetActiveHand();
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			entityLiving.resetActiveHand();
			return super.onServerRemove();
		}
	};

	/**Locks entity's current pitch/yaw*/
	public static final Handler PREVENT_ROTATION = new Handler(Identifier.PREVENT_ROTATION, true) {
		@Override
		public Handler setEntity(Entity entity) {
			super.setEntity(entity);
			copyRotations(entityLiving);
			return this;
		}
		@Override
		public boolean onServerTick() {
			Handlers.setRotations(entityLiving);
			return super.onServerTick();
		}
		@Override
		public Handler onServerRemove() {
			rotations.remove(entityLiving);
			return super.onServerRemove();
		}
	};

	public static void copyRotations(EntityLivingBase entity) {
		if (entity != null)
			rotations.put(entity, 
					Triple.of(entity.rotationPitch, entity.rotationYaw, entity.getRotationYawHead()));
	}

	public static void setRotations(EntityLivingBase entityLiving) {
		if (entityLiving != null && rotations.containsKey(entityLiving)) {
			Triple<Float, Float, Float> triple = rotations.get(entityLiving);
			entityLiving.prevRotationPitch = triple.getLeft();
			entityLiving.prevRotationYaw = triple.getMiddle();
			entityLiving.prevRotationYawHead = triple.getRight();
			entityLiving.rotationPitch = triple.getLeft();
			entityLiving.rotationYaw = triple.getMiddle();
			entityLiving.rotationYawHead = triple.getRight();
			entityLiving.prevRenderYawOffset = triple.getRight();
			entityLiving.renderYawOffset = triple.getRight();
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
		EntityPlayer player = Minecraft.getMinecraft().player;
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
		EntityPlayer player = Minecraft.getMinecraft().player;
		// prevent clicking / scrolling
		if ((event.getDx() != 0 || event.getDy() != 0 ||
				event.isButtonstate() || event.getDwheel() != 0) && player != null && 
				TickHandler.hasHandler(player, Identifier.PREVENT_INPUT) && 
				!(TickHandler.hasHandler(player, Identifier.MEI_CRYSTAL) && event.getButton() != -1)) {
			event.setCanceled(true);
		}
	}

	/**Prevents attacking, using abilities, clicking, reloading, and scrolling*/
	public static final Handler PREVENT_INPUT = new Handler(Identifier.PREVENT_INPUT, true) {};

	@SubscribeEvent
	public void preventAttacking(LivingAttackEvent event) {
		if (event.getSource().getTrueSource() instanceof EntityLivingBase &&
				TickHandler.hasHandler(event.getSource().getTrueSource(), Identifier.PREVENT_INPUT)) 
			event.setCanceled(true); 
	}

	/**Prevents moving, jumping, flying, and ender teleporting
	 * bool is if it should allow x and z motion (used with Reinhardt's Charge)*/
	public static final Handler PREVENT_MOVEMENT = new Handler(Identifier.PREVENT_MOVEMENT, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (this.ticksLeft <= 0) // needed for stupid slowness not removing properly
				return true;

			// prevent flying
			entity.onGround = true;
			if (player != null)
				player.capabilities.isFlying = false;
			if (!bool) {
				entity.motionX = 0;
				entity.motionZ = 0;
			}
			entity.motionY = player != null && !bool && (entity.isInWater() || entity.isInLava()) ? 0.05d : Math.min(0, entity.motionY);
			entity.motionY = Math.min(0, entity.motionY);
			if (entityLiving != null) {
				this.entityLiving.moveForward = 0;
				this.entityLiving.moveStrafing = 0;
			}
			entity.fallDistance *= 0.5f;
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (this.ticksLeft <= 0) // needed for stupid slowness not removing properly
				return true;

			// prevent jumping
			if (entity instanceof EntitySlime)
				entity.onGround = false;
			if (!bool) {
				entity.motionX = 0;
				entity.motionZ = 0;
			}
			entity.motionY = player != null && !bool && (entity.isInWater() || entity.isInLava()) ? 0.05d : Math.min(0, entity.motionY);
			entity.motionY = Math.min(0, entity.motionY);
			if (entityLiving != null) {
				this.entityLiving.moveForward = 0;
				this.entityLiving.moveStrafing = 0;
			}
			entity.fallDistance *= 0.5f;
			// slowness
			if (this.entityLiving != null) {
				PotionEffect effect = this.entityLiving.getActivePotionEffect(MobEffects.SLOWNESS);
				if (effect == null || effect.getDuration() <= 0 || effect.getAmplifier() < 254)
					entityLiving.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, this.ticksLeft, 254, true, false));
			}
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			Minewatch.proxy.updateFOV();
			// remove slowness
			if (this.entityLiving != null) {
				entityLiving.removePotionEffect(MobEffects.SLOWNESS);
				this.ticksLeft = 0;
			}
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			// remove slowness
			if (this.entityLiving != null) {
				entityLiving.removePotionEffect(MobEffects.SLOWNESS);
				this.ticksLeft = 0;
			}
			return super.onServerRemove();
		}
	};

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientSide(ClientTickEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		Handler handler = TickHandler.getHandler(player, Identifier.PREVENT_MOVEMENT);
		if (event.side == Side.CLIENT && event.phase == Phase.START &&
				handler != null) {
			if (!handler.bool) {
				player.motionX = 0;
				player.motionZ = 0;
			}
			player.motionY = player != null && !handler.bool && (player.isInWater() || player.isInLava()) ? 0.05d : Math.min(0, player.motionY);
			player.motionY = Math.min(0, player.motionY);
		}
	}

	@SubscribeEvent
	public void preventJumping(LivingJumpEvent event) {
		Handler handler = TickHandler.getHandler(event.getEntity(), Identifier.PREVENT_MOVEMENT);
		if (event.getEntity() instanceof EntityLivingBase &&
				handler != null) {
			if (!handler.bool) {
				event.getEntity().motionX = 0;
				event.getEntity().motionZ = 0;
			}
			event.getEntity().motionY = 0;
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

	public static final Handler INVULNERABLE = new Handler(Identifier.INVULNERABLE, false) {};

	@SubscribeEvent
	public void preventAttack(LivingAttackEvent event) {
		if (!event.getSource().canHarmInCreative() && 
				TickHandler.hasHandler(event.getEntity(), Identifier.INVULNERABLE)) 
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void preventDamage(LivingHurtEvent event) {
		if (!event.getSource().canHarmInCreative() && 
				TickHandler.hasHandler(event.getEntityLiving(), Identifier.INVULNERABLE)) 
			event.setCanceled(true);
	}

	public static final Handler FORCE_VIEW = new Handler(Identifier.FORCE_VIEW, false) {
		@Override
		public Handler setNumber(double number) {
			this.number2 = Minecraft.getMinecraft().gameSettings.thirdPersonView;
			return super.setNumber(number);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (entity instanceof EntityPlayerSP)
				Minecraft.getMinecraft().gameSettings.thirdPersonView = (int) number;
			return super.onClientTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			if (entity instanceof EntityPlayerSP)
				Minecraft.getMinecraft().gameSettings.thirdPersonView = (int) number2;
			return super.onClientRemove();
		}
	};
	
	/**bool = view bobbing to force, bool2 = prev view bobbing*/
	public static final Handler VIEW_BOBBING = new Handler(Identifier.VIEW_BOBBING, false) {
		@Override
		public Handler setBoolean(Boolean bool) {
			this.bool2 = Minecraft.getMinecraft().gameSettings.viewBobbing;
			if (entity instanceof EntityPlayerSP)
				Minecraft.getMinecraft().gameSettings.viewBobbing = bool;
			return super.setBoolean(bool);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			return super.onClientTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			if (entity instanceof EntityPlayerSP)
				Minecraft.getMinecraft().gameSettings.viewBobbing = bool2;
			return super.onClientRemove();
		}
	};

}