package twopiradians.minewatch.common.item.weapon;

import java.util.HashMap;
import java.util.LinkedList;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityTracerBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.RenderManager;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemTracerPistol extends ItemMWWeapon {

	/**Entities with the last tick that their armor was rendered (used to make sure particles only rendered once per tick)*/
	private static HashMap<EntityLivingBase, Integer> tickRendered = Maps.newHashMap();
	
	public static final Handler RECOLOR = new Handler(Identifier.TRACER_RECOLOR, false) {};
	public static final Handler RECALL = new Handler(Identifier.TRACER_RECALL, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (entityLiving == Minecraft.getMinecraft().thePlayer && this.ticksLeft > 20) 
				SavedState.applyState(entityLiving);
			if (this.ticksLeft == 5)
				TickHandler.register(true, RECOLOR.setEntity(entity).setTicks(25));
			if (this.ticksLeft <= 6 && this.ticksLeft % 2 == 0) {
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_2, entity.worldObj, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0x63B8E8, 0x4478AD, 1, 10, 0, 20, 0, 0.5f);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_3, entity.worldObj, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0x63B8E8, 0x4478AD, 1, 10, 0, 20, 0, 0.5f);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_2, entity.worldObj, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0x63B8E8, 0x4478AD, 1, 10, 0, 20, 0, 0.5f);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_3, entity.worldObj, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0xD2FFFF, 0xEAFFFF, 1, 10, 0, 20, 0, 0.5f);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, entity.worldObj, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0x63B8E8, 0x63B8E8, 1, 7, 15, 5, 0, 0.5f);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, entity.worldObj, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0xD2FFFF, 0xEAFFFF, 1, 7, 10, 5, 0, 0.8f);
			}
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (this.ticksLeft > 5)
				SavedState.applyState(entityLiving);
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			entity.extinguish();
			TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.ANA_GRENADE_DAMAGE));
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			entity.extinguish();
			TickHandler.unregister(false, TickHandler.getHandler(entity, Identifier.ANA_GRENADE_DAMAGE));
			ModSoundEvents.TRACER_RECALL_VOICE.playFollowingSound(entity, 1, 1, false);
			return super.onServerRemove();
		}
	};

	public ItemTracerPistol() {
		super(20);
		this.hasOffhand = true;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World worldObj, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && !worldObj.isRemote) {
			EntityTracerBullet bullet = new EntityTracerBullet(player.worldObj, player, hand.ordinal());
			EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, -1, 2, hand, 7, 0.58f);
			player.worldObj.spawnEntityInWorld(bullet);
			ModSoundEvents.TRACER_SHOOT.playSound(player, 1.0f, player.worldObj.rand.nextFloat()/20+0.95f);
			this.subtractFromCurrentAmmo(player, 1);
			if (worldObj.rand.nextInt(40) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World worldObj, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, worldObj, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHeldItemMainhand() == stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// dash
			if ((hero.ability2.isSelected(player, true) || hero.ability2.isSelected(player, true, Keys.KeyBind.RMB)) &&
					!worldObj.isRemote && this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				player.setSneaking(false);
				ModSoundEvents.TRACER_BLINK.playSound(player, 1, worldObj.rand.nextFloat()/2f+0.75f);
				ModSoundEvents.TRACER_BLINK_VOICE.playFollowingSound(player, 1, 1, false);
				Minewatch.network.sendToDimension(new SPacketSimple(0, player, false), player.worldObj.provider.getDimension());
				if (player instanceof EntityHero)
					SPacketSimple.move(player, 9, false, true);
				hero.ability2.keybind.setCooldown(player, 3, true); 
				hero.ability2.subtractUse(player);
			}

			// save recall state
			if (entity.ticksExisted % SavedState.SAVE_INTERVAL == 0 && 
					!TickHandler.hasHandler(player, Identifier.TRACER_RECALL))
				SavedState.create(player);

			// recall
			if (!worldObj.isRemote && hero.ability1.isSelected(player, true) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 30, 0, false, false));
				ModSoundEvents.TRACER_RECALL.playFollowingSound(player, 1, 1, false);
				entity.extinguish();
				TickHandler.unregister(false, TickHandler.getHandler(entity, Identifier.ANA_GRENADE_DAMAGE));
				TickHandler.register(false, ItemTracerPistol.RECALL.setEntity(entity).setTicks(35), 
						Handlers.PREVENT_INPUT.setEntity(entity).setTicks(30),
						Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks(30),
						Ability.ABILITY_USING.setEntity(entity).setTicks(30).setAbility(hero.ability1),
						Handlers.INVULNERABLE.setEntity(entity).setTicks(30));
				Minewatch.network.sendToDimension(new SPacketSimple(54, player, false), player.worldObj.provider.getDimension());
				hero.ability1.keybind.setCooldown(player, 240, false); 
				this.setCurrentAmmo(player, this.getMaxAmmo(player), EnumHand.values());
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean preRenderArmor(EntityLivingBase entity, ModelMWArmor model) { 
		super.preRenderArmor(entity, model);

		// chestplate particles
		if (model.slot == EntityEquipmentSlot.CHEST && entity.isEntityAlive() && !Minecraft.getMinecraft().isGamePaused() && 
				(entity != Minecraft.getMinecraft().thePlayer || !Minewatch.proxy.isPlayerInFirstPerson()) && 
				entity.ticksExisted > 3 && 
				(!tickRendered.containsKey(entity) || tickRendered.get(entity) != entity.ticksExisted) &&
				!TickHandler.hasHandler(entity, Identifier.TRACER_RECALL)) {
			tickRendered.put(entity, entity.ticksExisted);
			EntityHelper.spawnTrailParticles(entity, 10, 0, 0x5EDCE5, 0x007acc, 1, 2, 1, entity.getPositionVector().addVector(0, 0.3f, 0), EntityHelper.getPrevPositionVector(entity).addVector(0, 0.3f, 0));
		}
		
		// recolor
		Handler handler = TickHandler.getHandler(entity, Identifier.TRACER_RECOLOR);
		if (handler != null) {
			float percent = ((float) handler.ticksLeft) / handler.initialTicks;
			GlStateManager.color((255f-172f*percent)/255f, (255f-62f*percent)/255f, (255f-38f*percent)/255f, 1);
			return true;
		}

		return false; 
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height, EnumHand hand) {
		// tracer's dash
		if (hand == EnumHand.MAIN_HAND && event.getType() == ElementType.CROSSHAIRS && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
			GlStateManager.enableBlend();

			double scale = 3d*Config.guiScale;
			GlStateManager.translate(width/2, height/2, 0);
			GlStateManager.scale(scale, scale, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(RenderManager.ABILITY_OVERLAY);
			int uses = this.hero.ability2.getUses(player);
			GuiUtils.drawTexturedModalRect(-5, 8, 1, uses > 2 ? 239 : 243, 10, 4, 0);
			GlStateManager.scale(0.75f, 0.75f, 1);
			GuiUtils.drawTexturedModalRect(-5, 8, 1, uses > 1 ? 239 : 243, 10, 4, 0);
			GlStateManager.scale(0.75f, 0.75f, 1);
			GuiUtils.drawTexturedModalRect(-5, 8, 1, uses > 0 ? 239 : 243, 10, 4, 0);

			GlStateManager.disableBlend();
		}
	}

	public static class SavedState {

		public static final int SAVE_INTERVAL = 2;

		private static HashMap<EntityLivingBase, LinkedList<SavedState>> statesClient = Maps.newHashMap();
		private static HashMap<EntityLivingBase, LinkedList<SavedState>> statesServer = Maps.newHashMap();

		public int ticksExisted;
		public int dimensionId;
		public float health;
		public float rotationYaw;
		public float rotationPitch;
		public double posX;
		public double posY;
		public double posZ;

		private SavedState(EntityLivingBase entity) {
			// copy data to state
			this.ticksExisted = entity.ticksExisted;
			this.dimensionId = entity.worldObj.provider.getDimension();
			this.health = entity.getHealth();
			this.rotationYaw = MathHelper.wrapDegrees(entity.rotationYaw);
			this.rotationPitch = MathHelper.wrapDegrees(entity.rotationPitch);
			this.posX = entity.posX;
			this.posY = entity.posY;
			this.posZ = entity.posZ;
		}

		public static void create(EntityLivingBase entity) {
			SavedState state = new SavedState(entity);
			// add this state to the list of states
			HashMap<EntityLivingBase, LinkedList<SavedState>> states = getStates(entity.worldObj.isRemote);
			LinkedList<SavedState> list = states.containsKey(entity) ? states.get(entity) : new LinkedList<SavedState>();
			list.add(state);
			if (list.size() > 60 / SAVE_INTERVAL)
				list.removeFirst();
			states.put(entity, list);
		}

		/**Can this state be applied to the entity*/
		public boolean canApply(EntityLivingBase entity) {
			return this.dimensionId == entity.worldObj.provider.getDimension() && 
					entity.ticksExisted - this.ticksExisted < 100 && 
					entity.getDistance(posX, posY, posZ) < 50;
		}

		/**Apply this entity's latest save state*/
		public static void applyState(EntityLivingBase entity) {
			HashMap<EntityLivingBase, LinkedList<SavedState>> states = getStates(entity.worldObj.isRemote);
			SavedState state = states.containsKey(entity) ? states.get(entity).pollLast() : null;
			if (state != null && state.canApply(entity)) {
				entity.prevPosX = entity.posX;
				entity.prevPosY = entity.posY;
				entity.prevPosZ = entity.posZ;
				entity.prevRotationYaw = entity.rotationYaw;
				entity.prevRotationPitch = entity.rotationPitch;
				entity.prevRenderYawOffset = entity.renderYawOffset;
				entity.prevRotationYawHead = entity.rotationYawHead;
				entity.rotationYaw = state.rotationYaw;
				entity.rotationPitch = state.rotationPitch;
				entity.renderYawOffset = state.rotationYaw;
				entity.rotationYawHead = state.rotationYaw;
				entity.fallDistance = 0;
				if (!entity.worldObj.isRemote)
					EntityHelper.attemptTeleport(entity, state.posX, state.posY, state.posZ);

				if (state.health > entity.getHealth())
					entity.setHealth(state.health);
			}
		}

		public static HashMap<EntityLivingBase, LinkedList<SavedState>> getStates(boolean isRemote) {
			return isRemote ? statesClient : statesServer;
		}

	}

}