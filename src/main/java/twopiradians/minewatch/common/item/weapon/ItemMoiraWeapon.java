package twopiradians.minewatch.common.item.weapon;

import java.util.HashMap;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.projectile.EntityMoiraHealEnergy;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemMoiraWeapon extends ItemMWWeapon {

	// TODO make 1st person left hand from 1st person right hand in json
	// TODO there's a little gap in the model - see if you can cover the gap by merging the edges together or something and fixing the uv to look nice
	// https://gyazo.com/6fe95e2f171bf0f5bccd3079566c6afc ^ (not super noticeable in-game, but would be nice to fix)

	public static HashMap<EntityPlayer, Boolean> fadeViewBobbing = Maps.newHashMap();
	public static Handler FADE = new Handler(Identifier.MOIRA_FADE, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (player == Minecraft.getMinecraft().player) {
				entityLiving.motionY += 0.038f;
				if (this.ticksLeft > 1)
					Minecraft.getMinecraft().gameSettings.viewBobbing = false;
				else if (fadeViewBobbing.containsKey(player)) {
					Minecraft.getMinecraft().gameSettings.viewBobbing = fadeViewBobbing.get(player);
					fadeViewBobbing.remove(player);
				}
			}
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			// set resistance high to prevent hurt sound/animations 
			// (entityLiving.hurtResistantTime > entityLiving.maxHurtResistantTime / 2.0F)
			entityLiving.hurtResistantTime = (int) (entityLiving.maxHurtResistantTime*2.1f); 
			entityLiving.motionY += 0.038f;
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.REAPER_TELEPORT_BASE_0, entityLiving.world, 
					entityLiving.posX, entityLiving.posY+entityLiving.height/2f, entityLiving.posZ, 0, 0, 0, 0xFFFFFF, 0xAAAAAA, 1, 5, 5, 17, entityLiving.world.rand.nextFloat(), 0.3f);
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			EnumHero.MOIRA.ability3.keybind.setCooldown(entityLiving, 12, false); // TODO 
			entityLiving.hurtResistantTime = 0;
			return super.onServerRemove();
		}
	};

	public ItemMoiraWeapon() {
		super(0);
		this.maxCharge = 180;
		this.rechargeRate = 1/5f;
		this.hasOffhand = true;
		this.showHealthParticles = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 		
		if (hand == EnumHand.OFF_HAND && this.canUse(player, true, hand, false) && this.getCurrentCharge(player) >= 1) {
			this.subtractFromCurrentCharge(player, 1, player.ticksExisted % 10 == 0);
			if (!world.isRemote) {
				EntityMoiraHealEnergy energy = new EntityMoiraHealEnergy(world, player, hand.ordinal());
				EntityHelper.setAim(energy, player, player.rotationPitch, player.rotationYawHead, 30, 0,  
						hand, 20, 0.6f);
				world.spawnEntity(energy);
				if (KeyBind.LMB.isKeyPressed(player))
					ModSoundEvents.MOIRA_HEAL_START.playFollowingSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f, false);
				else if (player.ticksExisted % 10 == 0)
					ModSoundEvents.MOIRA_HEAL_DURING.playFollowingSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f, false);
				if (world.rand.nextInt(100) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHeldItemMainhand() == stack &&
				((EntityLivingBase)entity).getActiveItemStack() != stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// fade
			if (hero.ability3.isSelected(player) && !world.isRemote &&
					this.canUse((EntityLivingBase) entity, true, EnumHand.MAIN_HAND, true)) {
				TickHandler.register(false, Ability.ABILITY_USING.setEntity(player).setTicks(16).setAbility(hero.ability3),
						FADE.setEntity(player).setTicks(16)); // 340%
				Minewatch.network.sendToAll(new SPacketSimple(47, player, false));
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 16, 16, true, false));
				player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 16, 0, true, false));
				player.extinguish();
				ModSoundEvents.MOIRA_FADE.playFollowingSound(player, 1, 1, false);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.REAPER_TELEPORT_BASE_0, world, 
						player.posX, player.posY+player.height/2f, player.posZ, 0, 0, 0, 0xFFFFFF, 0xAAAAAA, 1, 5, 17, 5, world.rand.nextFloat(), 0.3f);
			}
		}
	}	

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return (Minewatch.proxy.getClientPlayer() != null && 
				TickHandler.hasHandler(Minewatch.proxy.getClientPlayer(), Identifier.MOIRA_FADE)) ? 
						true : super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@SubscribeEvent
	public void preventFadeDamage(LivingAttackEvent event) {
		if (TickHandler.hasHandler(event.getEntity(), Identifier.MOIRA_FADE) &&
				!event.getSource().canHarmInCreative()) 
			event.setCanceled(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean preRenderArmor(EntityLivingBase entity, ModelMWArmor model) { 
		// fade
		if (TickHandler.hasHandler(entity, Identifier.MOIRA_FADE)) {
			GlStateManager.enableCull();
			GlStateManager.color(1, 1, 1, 0);
			return true;
		}
		else
			return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType transform, Pair<? extends IBakedModel, Matrix4f> ret) {
		// fade
		if (transform != TransformType.GUI && TickHandler.hasHandler(entity, Identifier.MOIRA_FADE)) 
			ret.getRight().setScale(0);
		return ret;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height, EnumHand hand) {
		super.preRenderGameOverlay(event, player, width, height, hand);

		// heal/damage orb overlay
		if (hand == EnumHand.MAIN_HAND && event.getType() == ElementType.CROSSHAIRS && 
				TickHandler.getHandler(player, Identifier.MOIRA_ORB) != null) {
			GlStateManager.enableBlend();

			double scale = 0.8d*Config.guiScale;
			GlStateManager.scale(scale, scale, 1);
			GlStateManager.translate((int) ((width - 256*scale)/2d / scale), (int) ((height - 256*scale)/2d / scale), 0);
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/moira_orb.png"));
			GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);

			GlStateManager.disableBlend();
		}
		// moira fade
		else if (event.getType() == ElementType.ALL && TickHandler.hasHandler(player, Identifier.MOIRA_FADE)) {
			Handler handler = TickHandler.getHandler(player, Identifier.MOIRA_FADE);
			float ticks = handler.initialTicks - handler.ticksLeft+Minecraft.getMinecraft().getRenderPartialTicks();

			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			//PORT scale x event.getResolution().getScaleFactor()
			GlStateManager.scale(width/256d, height/256d, 1);
			int firstImage = (int) (ticks / 10);
			int secondImage = firstImage + 1;
			if (firstImage < 6) {
				GlStateManager.color(1, 1, 1, 1.1f-((ticks) % 10)/10f);
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/reaper_wraith_"+firstImage+".png"));
				GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);
			}
			if (secondImage < 6) {
				GlStateManager.color(1, 1, 1, (ticks % 10)/10f);
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/reaper_wraith_"+secondImage+".png"));
				GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);
			}
			GlStateManager.popMatrix();
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderArms(RenderSpecificHandEvent event) {
		// render arms while holding weapons - modified from ItemRenderer#renderArmFirstPerson
		if (event.getItemStack() != null && event.getItemStack().getItem() == this && 
				!TickHandler.hasHandler(Minecraft.getMinecraft().player, Identifier.MOIRA_FADE)) {
			GlStateManager.pushMatrix();
			Minecraft mc = Minecraft.getMinecraft();
			AbstractClientPlayer player = mc.player;
			float partialTicks = mc.getRenderPartialTicks();
			float swing = player.getSwingProgress(partialTicks);	
			float f7 = event.getHand() == EnumHand.MAIN_HAND ? swing : 0.0F;
			// would move hand to follow item - but equippedProgress is private
			float mainProgress = 0.0F;// - (mc.getItemRenderer().prevEquippedProgressMainHand + (this.equippedProgressMainHand - this.prevEquippedProgressMainHand) * partialTicks);
			float offProgress = 0.0F;// - (mc.getItemRenderer().prevEquippedProgressOffHand + (this.equippedProgressOffHand - this.prevEquippedProgressOffHand) * partialTicks);
			float progress = event.getHand() == EnumHand.MAIN_HAND ? mainProgress : offProgress;
			EnumHandSide side = event.getHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
			boolean flag = side != EnumHandSide.LEFT;
			float f = flag ? 1.0F : -1.0F;
			float f1 = MathHelper.sqrt(f7);
			float f2 = -0.3F * MathHelper.sin(f1 * (float)Math.PI);
			float f3 = 0.4F * MathHelper.sin(f1 * ((float)Math.PI * 2F));
			float f4 = -0.4F * MathHelper.sin(f3 * (float)Math.PI);
			GlStateManager.translate(f * (f2 + 0.64000005F), f3 + -0.6F + progress * -0.6F, f4 + -0.71999997F);
			GlStateManager.rotate(f * 45.0F, 0.0F, 1.0F, 0.0F);
			float f5 = MathHelper.sin(f3 * f3 * (float)Math.PI);
			float f6 = MathHelper.sin(f1 * (float)Math.PI);
			GlStateManager.rotate(f * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(f * f5 * -20.0F, 0.0F, 0.0F, 1.0F);
			AbstractClientPlayer abstractclientplayer = mc.player;
			mc.getTextureManager().bindTexture(abstractclientplayer.getLocationSkin());
			GlStateManager.translate(f * -1.0F, 3.6F, 3.5F);
			GlStateManager.rotate(f * 120.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(f * -135.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(f * 5.6F, 0.0F, 0.0F);
			RenderPlayer renderplayer = (RenderPlayer)mc.getRenderManager().getEntityRenderObject(abstractclientplayer);
			GlStateManager.disableCull();

			if (flag)
				renderplayer.renderRightArm(abstractclientplayer);
			else
				renderplayer.renderLeftArm(abstractclientplayer);

			GlStateManager.enableCull();
			GlStateManager.popMatrix();
		}
	}

}