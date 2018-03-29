package twopiradians.minewatch.common.hero;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.heroSelect.GuiHeroSelect;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.particle.ParticleCustom;
import twopiradians.minewatch.client.render.entity.RenderHero;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.hero.HealthManager.Type;
import twopiradians.minewatch.common.item.IChangingModel;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.RenderHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

@Mod.EventBusSubscriber
public class RenderManager {

	public enum MessageTypes {
		TOP, MIDDLE
	} 

	public static final ResourceLocation ABILITY_OVERLAY = new ResourceLocation(Minewatch.MODID, "textures/gui/ability_overlay.png");
	public static final Handler SNEAKING = new Handler(Identifier.HERO_SNEAKING, true) {};
	public static HashMap<EntityLivingBase, HashMap<UUID, Tuple<Float, Integer>>> entityDamage = Maps.newHashMap();
	/**Text overlay - number is MessageTypes.ordinal(), bool == small text*/
	public static final Handler MESSAGES = new Handler(Identifier.HERO_MESSAGES, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (entity != Minewatch.proxy.getRenderViewEntity())
				return true;

			ArrayList<Handler> handlers = TickHandler.getHandlers(entity, handler->handler.identifier == Identifier.HERO_MESSAGES && handler.number == MessageTypes.MIDDLE.ordinal());
			return handlers.indexOf(this) <= 6 ? --this.ticksLeft <= 0 : false;
		}
	};
	public static final Handler HIT_OVERLAY = new Handler(Identifier.HIT_OVERLAY, false) {};
	public static final Handler KILL_OVERLAY = new Handler(Identifier.KILL_OVERLAY, false) {};
	public static final Handler MULTIKILL = new Handler(Identifier.HERO_MULTIKILL, false) {};
	/**Used for debugging*/
	public static CopyOnWriteArrayList<AxisAlignedBB> boundingBoxesToRender = new CopyOnWriteArrayList<AxisAlignedBB>();

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void hideEntityWearingArmor(RenderLivingEvent.Pre<EntityLivingBase> event) {	
		// make entity body follow head
		if (event.getEntity() instanceof EntityLivingBase && event.getEntity().getHeldItemMainhand() != null && 
				event.getEntity().getHeldItemMainhand().getItem() instanceof ItemMWWeapon &&
				(event.getEntity() != Minewatch.proxy.getClientPlayer() || 
				KeyBind.LMB.isKeyDown((EntityLivingBase) event.getEntity()) || 
				KeyBind.RMB.isKeyDown((EntityLivingBase) event.getEntity()))) {
			event.getEntity().renderYawOffset = event.getEntity().rotationYawHead;
		}

		// hide ModelBipeds with armor layer that are wearing armor
		if (event.getRenderer().getMainModel() instanceof ModelBiped && 
				ItemMWArmor.classesWithArmor.contains(event.getEntity().getClass())) {
			ModelBiped model = (ModelBiped) event.getRenderer().getMainModel();
			// block when running with soldier's gun
			ItemStack stack = event.getEntity().getHeldItemMainhand();
			if (event.getEntity().isSprinting() && stack != null && stack.getItem() == EnumHero.SOLDIER76.weapon) 
				model.rightArmPose = ArmPose.BLOCK;
			// sneak
			if (TickHandler.hasHandler(event.getEntity(), Identifier.HERO_SNEAKING))
				model.isSneak = true;
			model.setVisible(true);
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
				stack = event.getEntity().getItemStackFromSlot(slot);
				if (stack != null && stack.getItem() instanceof ItemMWArmor) {
					if (slot == EntityEquipmentSlot.LEGS && 
							event.getEntity().getItemStackFromSlot(EntityEquipmentSlot.FEET) != null && 
							event.getEntity().getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemMWArmor) {
						model.bipedLeftLeg.showModel = false;
						model.bipedRightLeg.showModel = false;
						if (model instanceof ModelPlayer) {
							((ModelPlayer)model).bipedLeftLegwear.showModel = false;
							((ModelPlayer)model).bipedRightLegwear.showModel = false;
						}
					}
					else if (slot == EntityEquipmentSlot.CHEST) {
						model.bipedBody.showModel = false;
						if (model instanceof ModelPlayer) {
							model.bipedLeftArm.showModel = false;
							model.bipedRightArm.showModel = false;
							((ModelPlayer)model).bipedRightArmwear.showModel = false;
							((ModelPlayer)model).bipedLeftArmwear.showModel = false;
							((ModelPlayer)model).bipedBodyWear.showModel = false;
						}
					}
					else if (slot == EntityEquipmentSlot.HEAD) {
						model.bipedHeadwear.showModel = false;
						model.bipedHead.showModel = false;
					}
				}
			}
		}

		// ItemMWWeapon#preRenderEntity
		ItemStack stack = EntityHelper.getHeldItem(event.getEntity(), ItemMWWeapon.class, EnumHand.MAIN_HAND);
		if (stack != null && ((ItemMWWeapon)stack.getItem()).hero == SetManager.getWornSet(event.getEntity())) {
			GlStateManager.color(1, 1, 1, 1f);
			GlStateManager.pushMatrix();
			((ItemMWWeapon)stack.getItem()).preRenderEntity(event);
			GlStateManager.popMatrix();
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void postRender(RenderLivingEvent.Post<EntityLivingBase> event) {
		// ItemMWWeapon#postRenderEntity
		ItemStack stack = EntityHelper.getHeldItem(event.getEntity(), ItemMWWeapon.class, EnumHand.MAIN_HAND);
		if (stack != null && ((ItemMWWeapon)stack.getItem()).hero == SetManager.getWornSet(event.getEntity())) {
			GlStateManager.color(1, 1, 1, 1f);
			GlStateManager.pushMatrix();
			((ItemMWWeapon)stack.getItem()).postRenderEntity(event);
			GlStateManager.popMatrix();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean hideHealthArmor(EntityPlayer player) {
		return Config.hideHealthArmor && SetManager.getWornSet(player) != null;
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean hideFood(EntityPlayer player) {
		return Config.hideHunger && SetManager.getWornSet(player) != null && !player.getFoodStats().needFood();
	}

	@SideOnly(Side.CLIENT)
	public static boolean hideHotBar(EntityPlayer player) {
		if (Config.hideHotbar) {
			EnumHero hero = SetManager.getWornSet(player);
			if (hero != null && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == hero.weapon)
				return true;
		}
		return false;
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void renderCrosshairs(RenderGameOverlayEvent.Pre event) {
		Minecraft mc = Minecraft.getMinecraft();

		// hide health / armor
		if ((event.getType() == ElementType.HEALTH || event.getType() == ElementType.ARMOR) &&
				hideHealthArmor(mc.player))
			event.setCanceled(true);
		// hide food
		else if (event.getType() == ElementType.FOOD &&
				hideFood(mc.player)) 
				event.setCanceled(true);
		// hide hotbar
		else if ((event.getType() == ElementType.HOTBAR || event.getType() == ElementType.EXPERIENCE) &&
				hideHotBar(mc.player)) 
				event.setCanceled(true);

		if (Minecraft.getMinecraft().currentScreen instanceof GuiHeroSelect)
			event.setCanceled(true);
		else if (Config.guiScale > 0) {
			double height = event.getResolution().getScaledHeight_double();
			double width = event.getResolution().getScaledWidth_double();
			int imageSize = 256;
			EntityPlayer player = Minecraft.getMinecraft().player;
			EnumHero hero = SetManager.getWornSet(player);
			EnumHand hand = null;
			for (EnumHand hand2 : EnumHand.values())
				if (player.getHeldItem(hand2) != null && player.getHeldItem(hand2).getItem() instanceof ItemMWWeapon && (((ItemMWWeapon)player.getHeldItem(hand2).getItem()).hero == hero || hand == null || ((ItemMWWeapon)player.getHeldItem(hand).getItem()).hero != hero)) {
					hand = hand2;
					break;
				}
			ItemMWWeapon weapon = hand == null ? null : (ItemMWWeapon) player.getHeldItem(hand).getItem();

			if (!Minewatch.proxy.getClientPlayer().isSpectator()) {
				if (weapon != null && !KeyBind.HERO_INFORMATION.isKeyDown(player)) {
					GlStateManager.color(1, 1, 1, 1f);
					GlStateManager.pushMatrix();
					GlStateManager.enableAlpha();
					weapon.preRenderGameOverlay(event, player, width, height, hand);
					GlStateManager.popMatrix();
				}

				if (event.getType() == ElementType.CROSSHAIRS && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
					GlStateManager.color(1, 1, 1, 1f);

					if (weapon != null && !KeyBind.HERO_INFORMATION.isKeyDown(player)) {
						GlStateManager.pushMatrix();
						GlStateManager.enableBlend();

						// render crosshair
						double scale = 0.2d*Config.guiScale;
						GlStateManager.scale(scale, scale, 1);
						GlStateManager.translate((int) ((event.getResolution().getScaledWidth_double() - 256*scale)/2d / scale), (int) ((event.getResolution().getScaledHeight_double() - 256*scale)/2d / scale), 0);
						if (Config.customCrosshairs) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(weapon.hero.crosshair.loc);
							GuiUtils.drawTexturedModalRect(3, 3, 0, 0, 256, 256, 0);
						}

						GlStateManager.disableBlend();
						GlStateManager.popMatrix();
					}

					if (weapon != null && Config.customCrosshairs || hero != null && KeyBind.HERO_INFORMATION.isKeyDown(player))
						event.setCanceled(true);
				}
			}

			if (event.getType() == ElementType.CROSSHAIRS && hero != null) {
				GlStateManager.pushMatrix();
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.enableBlend();
				GlStateManager.enableAlpha();

				// hit overlay
				Handler handler = TickHandler.getHandler(player, Identifier.HIT_OVERLAY);
				if (handler != null &&
						Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
					GlStateManager.color(1, 1, 1, 0.7f-(handler.ticksLeft >= 3 ? 0 : (1f-handler.ticksLeft/3f)*0.7f));
					double scale = MathHelper.clamp(0.014f*handler.number, 0.03f, 0.25f);
					GlStateManager.scale(scale, scale, 1);
					Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/hit_overlay.png"));
					GuiUtils.drawTexturedModalRect((int) ((width/2/scale-imageSize/2)), (int) ((height/2/scale-imageSize/2)), 0, 0, imageSize, imageSize, 0);
					GlStateManager.scale(1/scale, 1/scale, 1);
				}

				// kill overlay
				handler = TickHandler.getHandler(player, Identifier.KILL_OVERLAY);
				if (handler != null &&
						Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
					GlStateManager.color(1, 1, 1, 0.7f-(handler.ticksLeft >= 5 ? 0 : (1f-handler.ticksLeft/5f)*0.7f));
					double scale = 0.1f;
					GlStateManager.scale(scale, scale, 1);
					Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/kill_overlay.png"));
					GuiUtils.drawTexturedModalRect((int) (width/2/scale-imageSize/2), (int) (height/2/scale-imageSize/2), 0, 0, imageSize, imageSize, 0);
					GlStateManager.scale(1/scale, 1/scale, 1);
				}

				// elimination / assist text overlay
				double yOffset = 0;
				ArrayList<Handler> handlers = TickHandler.getHandlers(player, handler2->handler2.identifier == Identifier.HERO_MESSAGES && handler2.number == MessageTypes.MIDDLE.ordinal());
				for (int i=0; i<Math.min(6, handlers.size()); ++i) {
					handler = handlers.get(i);
					if (handler != null && handler.string != null && handler.entity == Minewatch.proxy.getRenderViewEntity()) {
						float alpha = 0.7f;
						if (handler.ticksLeft < 15)
							alpha -= (1f-(handler.ticksLeft-10)/5f)*alpha;
						else if (handler.ticksLeft > handler.initialTicks-8)
							alpha -= (1f-(handler.initialTicks-handler.ticksLeft+1)/8f)*alpha;
						if (alpha > 0) {
							double scale = handler.bool ? 0.8f :1f;
							GlStateManager.scale(scale, scale, 1);
							FontRenderer font = Minecraft.getMinecraft().fontRenderer;
							font.drawString(handler.string, (float)((width/2/scale) - font.getStringWidth(handler.string)/2), (float) (height/1.6f/scale+yOffset+(handler.bool ? 4 : 0)), new Color(1, 1, 1, alpha).getRGB(), false);
							GlStateManager.scale(1/scale, 1/scale, 1);
						}
						yOffset += handler.ticksLeft >= 10 ? 11 : handler.ticksLeft/10f*11f;
					}
				}

				// top text overlay
				handler = TickHandler.getHandler(handler2->handler2.identifier == Identifier.HERO_MESSAGES && handler2.number == MessageTypes.TOP.ordinal(), true);
				if (handler != null && handler.entity == Minewatch.proxy.getRenderViewEntity()) {
					double scale = handler.bool ? 1.3f :2.3f;
					if (handler.initialTicks-handler.ticksLeft <= 6)
						scale += 1.8d * (1d-((handler.initialTicks-handler.ticksLeft) / 6d));
					float alpha = 1f;
					if (handler.initialTicks-handler.ticksLeft < 5)
						alpha = (handler.initialTicks-handler.ticksLeft)/5f;
					else if (handler.ticksLeft < 3)
						alpha = handler.ticksLeft/3f;
					GlStateManager.scale(scale, scale, 1);
					mc.fontRenderer.drawString(handler.string, (float)((width/2/scale) - mc.fontRenderer.getStringWidth(handler.string)/2), (float) (height/4f/scale+(handler.bool ? 4 : 0)), new Color(1, 1, 1, alpha).getRGB(), true);
				}

				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void renderOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() == ElementType.HELMET && Config.guiScale > 0) {				
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			EnumHero hero = SetManager.getWornSet(player);
			ItemMWWeapon weapon = player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemMWWeapon ? (ItemMWWeapon)player.getHeldItemMainhand().getItem() : null;
			double width = event.getResolution().getScaledWidth_double();
			double height = event.getResolution().getScaledHeight_double();

			// hero information screen
			if (hero != null && KeyBind.HERO_INFORMATION.isKeyDown(player))
				hero.displayInfoScreen(width, height);
			else {
				// team spawn hero selection
				if (TickHandler.hasHandler(player, Identifier.TEAM_SPAWN_IN_RANGE)) {
					GlStateManager.pushMatrix();
					GlStateManager.color(1, 1, 1, 0.5f);
					GlStateManager.enableDepth();
					double scale = 0.8d*Config.guiScale;
					GlStateManager.scale(scale, scale, 0);

					String text = TextFormatting.BLACK+""+TextFormatting.BOLD+String.format(Minewatch.translate("overlay.change_hero"), KeyBind.CHANGE_HERO.keyBind.getDisplayName()).toUpperCase();
					int textWidth = mc.fontRenderer.getStringWidth(text);
					int x = (int) (width/scale/2d-textWidth/2d);
					int y = (int) (height/scale-190f);
					GuiUtils.drawGradientRect(0, x-10, y, x+textWidth+10, y+mc.fontRenderer.FONT_HEIGHT+10, 0xCAFFDA56, 0xCAFFDA56);
					mc.fontRenderer.drawString(text, x, y+6, 0xFFFFFF);

					GlStateManager.enableBlend();
					GlStateManager.popMatrix();
				}

				if (hero != null && weapon != null && hero == weapon.hero && !player.isSpectator()) {
					// ultimate
					UltimateManager.renderUltimateMeter(player, hero, width, height);
				}
				
				if (hero != null && !player.isSpectator()) {	
					// charge
					ChargeManager.renderChargeOverlay(player, width, height);
					
					// portrait
					GlStateManager.pushMatrix();
					double scale = 0.25d*Config.guiScale;
					GlStateManager.scale(scale, scale, 1);
					EnumHero.displayPortrait(hero, 40-scale*120, (int) ((height - 256*scale) / scale) - 65+scale*110, false, false);
					GlStateManager.popMatrix();

					GlStateManager.pushMatrix();
					scale = 1.5d*Config.guiScale;
					GlStateManager.scale(scale, scale, 1);
					GlStateManager.translate(43, height/scale-28, 0);	
					GlStateManager.rotate(-4f, 0, 0, 1);

					renderHealthBar(player, hero, true, false);

					GlStateManager.popMatrix();

					// current health / max health
					GlStateManager.pushMatrix();

					float maxHealth = HealthManager.getMaxCombinedHealth(player);
					float currentHealth = HealthManager.getCurrentCombinedHealth(player);

					scale = 1.6d*Config.guiScale;
					GlStateManager.scale(scale, scale, 1);
					GlStateManager.translate(40, height/scale-45, 0);
					GlStateManager.rotate(-4f, 0, 0, 1);

					int textWidth = mc.fontRenderer.getStringWidth(TextFormatting.ITALIC+String.valueOf((int)currentHealth))+1;
					mc.fontRenderer.drawString(TextFormatting.ITALIC+String.valueOf((int)currentHealth), 0, 10, 0xFFFFFF, true);
					scale = 0.53d;
					GlStateManager.scale(scale, scale, 1);
					mc.fontRenderer.drawString("/", (int) (textWidth/scale), 24, 0xFFFFFF, true);
					textWidth += mc.fontRenderer.getStringWidth("/")-2;
					mc.fontRenderer.drawString(TextFormatting.ITALIC+String.valueOf((int)maxHealth), (int) (textWidth/scale), 24, 0xFFFFFF, true);

					GlStateManager.popMatrix();
				}

				// display abilities/weapon
				if (weapon != null && !player.isSpectator()) {		
					GlStateManager.pushMatrix();
					GlStateManager.enableDepth();
					GlStateManager.enableAlpha();
					GlStateManager.color(1, 1, 1, 1);

					double scale = 3d*Config.guiScale;
					GlStateManager.scale(scale, scale, 1);
					GlStateManager.translate((int) (width/scale)-35, ((int)height/scale)-25+scale*2, 0);
					mc.getTextureManager().bindTexture(ABILITY_OVERLAY);
					int index = ItemMWWeapon.isAlternate(player.getHeldItemMainhand()) && 
							weapon.hero.hasAltWeapon ? weapon.hero.altWeaponIndex : weapon.hero.overlayIndex;
					// weapon
					int u = 1+85*(index/14);
					int v = 1+17*(index%14);
					GuiUtils.drawTexturedModalRect(0, 0, u, v, 32, 16, 0);

					// infinite ammo
					if (weapon.getMaxAmmo(player) == 0) 
						GuiUtils.drawTexturedModalRect(18, -3, 13, 239, 6, 4, 0);

					if (hero != null && weapon.hero == hero && SetManager.getWornSet(player) != null) {
						for (int i=1; i<=3; ++i) {
							mc.getTextureManager().bindTexture(ABILITY_OVERLAY);
							Ability ability = hero.getAbility(i);

							// weapon / ability icons
							GlStateManager.pushMatrix();
							// on cooldown
							if (ability.getCooldown(player) > 0) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							// selected
							else if (ability.isSelected(player) || (hero == EnumHero.SOMBRA && 
									ability.entities.get(player) != null && ability.entities.get(player).isEntityAlive())) {
								GlStateManager.color(0.8f, 0.6f, 0);
								GlStateManager.translate(0.5f, 0.5f, 0);
							}

							GlStateManager.scale(1/scale, 1/scale, 1);
							GlStateManager.translate(0, -i*1, 0);
							GlStateManager.scale(scale, scale, 1);

							// draw ability icon
							GuiUtils.drawTexturedModalRect(-i*9-4, 1, u+33+13*(i-1), v, 12, 16, 0);
							// not implemented icon
							if (!ability.isEnabled && ability.keybind != KeyBind.NONE) {
								GlStateManager.translate(i*0.3d, -i*0.5d, 0);
								ability.drawNotEnabledIcon(-i*9+1, 6, 0);
								
							}
							GlStateManager.color(1, 1, 1);
							GlStateManager.popMatrix();

							// keybinds 
							GlStateManager.pushMatrix();
							GlStateManager.translate(-i*0.2f, -i*0.7f, 0);
							// background
							if (ability.showKeybind(player)) {
								if (ability.keybind.getKeyName() != "")
									GuiUtils.drawTexturedModalRect(-i*9-6, 9, 0, 247, 11, 5, 0);
								else if (ability.keybind == KeyBind.RMB)		
									GuiUtils.drawTexturedModalRect(-i*9-2, 9, 11, 247, 5, 5, 0);
							}
							// multi-use background
							if (ability.maxUses > 0)
								GuiUtils.drawTexturedModalRect(-i*9+2, -3, 21, 247, 5, 9, 0);
							// ability entity icon
							if (ability.entities.get(player) != null && ability.entities.get(player).isEntityAlive() && !(hero == EnumHero.SOMBRA && 
									ability.entities.get(player) != null && ability.entities.get(player).isEntityAlive())) 
								GuiUtils.drawTexturedModalRect(-i*9+(ability.maxUses > 0 ? 3 : 2), (ability.maxUses > 0 ? -8 : -3), 26, 247, 5, 5, 0);
							// text
							String text = ability.keybind.getKeyName();
							if ("LMENU".equalsIgnoreCase(text))
								text = "LALT";
							int textWidth = mc.fontRenderer.getStringWidth(text);
							GlStateManager.scale(0.25d, 0.25d, 1);
							GlStateManager.rotate(4.5f, 0, 0, 1);
							// keybind text
							if (ability.showKeybind(player)) 
								mc.fontRenderer.drawString(text, 3-i*36-textWidth/2, 43+i*3, 0);
							// multi-use number
							if (ability.maxUses > 0)
								mc.fontRenderer.drawString(String.valueOf(ability.getUses(player)), 16-i*36, -8+i*3, 0);
							// cooldown
							scale = 2d;
							GlStateManager.scale(scale, scale, 1);
							if (ability.getCooldown(player) > 0) { 
								String num = String.valueOf((int)Math.ceil(ability.getCooldown(player)/20d));
								textWidth = mc.fontRenderer.getStringWidth(num);
								mc.fontRenderer.drawString(num, 6-i*18-textWidth/2, 8+i, 0xFFFFFF);
							}
							GlStateManager.color(1, 1, 1);
							GlStateManager.popMatrix();
						}
					}
					// ammo
					if (weapon.getMaxAmmo(player) > 0) {
						scale = 0.45d;
						GlStateManager.scale(scale, scale, 1);
						int textWidth = mc.fontRenderer.getStringWidth(TextFormatting.ITALIC+String.valueOf(weapon.getCurrentAmmo(player)));
						mc.fontRenderer.drawString(TextFormatting.ITALIC+String.valueOf(weapon.getCurrentAmmo(player)), 31-textWidth, -10, 0xFFFFFF, true);
						scale = 0.6d;
						GlStateManager.scale(scale, scale, 1);
						mc.fontRenderer.drawString("/", 53, -10, 0x00D5FF, true);
						mc.fontRenderer.drawString(TextFormatting.ITALIC+String.valueOf(weapon.getMaxAmmo(player)), 59, -10, 0xFFFFFF, true);
					}

					GlStateManager.popMatrix();
				}
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void renderHands(RenderSpecificHandEvent event) {
		// render arms while holding weapons - modified from ItemRenderer#renderArmFirstPerson
		Minecraft mc = Minecraft.getMinecraft();
		AbstractClientPlayer player = mc.player;
		ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		ItemStack held = player.getHeldItem(event.getHand());
		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();
		// ask mainhand if offhand is empty or is another mw item (but not a weapon - like Sombra's hack or Junkrat's trigger)
		ItemStack stack = event.getHand() == EnumHand.MAIN_HAND || off == null || off.isEmpty() || (off.getItem() instanceof IChangingModel && !(off.getItem() instanceof ItemMWWeapon)) ? main : held;

		GlStateManager.pushMatrix();
		if (player != null && !player.isInvisible() && !player.isSpectator() && ((stack != null && stack.getItem() instanceof ItemMWWeapon &&
				((ItemMWWeapon)stack.getItem()).shouldRenderHand(player, event.getHand()))) || 
				(chest != null && chest.getItem() instanceof ItemMWArmor && event.getHand() == EnumHand.MAIN_HAND && 
				(held == null || held.isEmpty()))) {
			float partialTicks = mc.getRenderPartialTicks();
			float swing = player.getSwingProgress(partialTicks);	
			float f7 = event.getHand() == EnumHand.MAIN_HAND ? swing : 0.0F;
			float progress = event.getEquipProgress();
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
			if (chest != null && chest.getItem() instanceof ItemMWArmor) {
				mc.getTextureManager().bindTexture(new ResourceLocation(((ItemMWArmor)chest.getItem()).getArmorTexture(chest, player, EntityEquipmentSlot.CHEST, null)));
				// cancel normal hand render
				if (held == null || held.isEmpty())
					event.setCanceled(true);
			}
			else
				mc.getTextureManager().bindTexture(abstractclientplayer.getLocationSkin());
			GlStateManager.translate(f * -1.0F, 3.6F, 3.5F);
			GlStateManager.rotate(f * 120.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(f * -135.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(f * 5.6F, 0.0F, 0.0F);
			RenderPlayer renderplayer = (RenderPlayer)mc.getRenderManager().<AbstractClientPlayer>getEntityRenderObject(abstractclientplayer);
			GlStateManager.disableCull();

			if (flag)
				renderplayer.renderRightArm(abstractclientplayer);
			else
				renderplayer.renderLeftArm(abstractclientplayer);

			GlStateManager.enableCull();
		}
		GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void hurtTime(TickEvent.RenderTickEvent event) {
		// limit hurt time when wearing full set
		if (Minecraft.getMinecraft().player != null && 
				SetManager.getWornSet(Minecraft.getMinecraft().player) != null)
			Minecraft.getMinecraft().player.hurtTime = Math.min(5, Minecraft.getMinecraft().player.hurtTime);
	}

	@SubscribeEvent
	public static void serverSide(ServerTickEvent event) {
		// decrement timer for damage
		if (event.phase == TickEvent.Phase.END) {
			for (EntityLivingBase entity : entityDamage.keySet()) 
				for (UUID uuid : entityDamage.get(entity).keySet()) {
					Tuple<Float, Integer> tup = entityDamage.get(entity).get(uuid);
					entityDamage.get(entity).put(uuid, new Tuple(tup.getFirst(), tup.getSecond()-1));
				}
		}
	}


	@SubscribeEvent
	public static void damageEntities(LivingHurtEvent event) {
		EntityPlayerMP player = null;
		if (event.getSource().getTrueSource() instanceof EntityPlayerMP)
			player = ((EntityPlayerMP)event.getSource().getTrueSource());
		else if (event.getSource().getTrueSource() instanceof EntityPlayerMP)
			player = ((EntityPlayerMP)event.getSource().getTrueSource());
		else if (event.getSource().getTrueSource() instanceof IThrowableEntity && 
				((IThrowableEntity) event.getSource().getTrueSource()).getThrower() instanceof EntityPlayerMP)
			player = (EntityPlayerMP) ((IThrowableEntity) event.getSource().getTrueSource()).getThrower();

		if (player != null && event.getEntityLiving() != null && player != event.getEntityLiving()) {
			if (!player.world.isRemote && SetManager.getWornSet(player) != null) {
				try {
					float damage = event.getAmount();
					damage = CombatRules.getDamageAfterAbsorb(damage, (float)event.getEntityLiving().getTotalArmorValue(), (float)event.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
					damage = applyPotionDamageCalculations(event.getEntityLiving(), event.getSource(), damage);
					damage = Math.min(damage, event.getEntityLiving().getHealth());
					if (damage > 0) {
						HashMap<UUID, Tuple<Float, Integer>> damageMap = entityDamage.get(event.getEntityLiving()) == null ? Maps.newHashMap() : entityDamage.get(event.getEntityLiving());
						damageMap.put(player.getPersistentID(), new Tuple(damageMap.get(player.getPersistentID()) == null ? damage : damageMap.get(player.getPersistentID()).getFirst() + damage, 200));
						entityDamage.put(event.getEntityLiving(), damageMap);
						Minewatch.network.sendTo(new SPacketSimple(15, false, player, damage, 0, 0), player);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SubscribeEvent
	public static void deathMessages(LivingDeathEvent event) {
		if (event.getEntityLiving() != null && entityDamage.containsKey(event.getEntityLiving())) {
			UUID mostDamage = null;
			float damage = 0;
			// find who dealt most damage
			for (UUID uuid : entityDamage.get(event.getEntityLiving()).keySet()) 
				if ((mostDamage == null || entityDamage.get(event.getEntityLiving()).get(uuid).getFirst() > damage) &&
						entityDamage.get(event.getEntityLiving()).get(uuid).getSecond() > 0) {
					mostDamage = uuid;
					damage = entityDamage.get(event.getEntityLiving()).get(uuid).getFirst();
				}
			for (UUID uuid : entityDamage.get(event.getEntityLiving()).keySet()) {
				EntityPlayer player = event.getEntityLiving().world.getPlayerEntityByUUID(uuid);
				if (player instanceof EntityPlayerMP) {
					int percent = (int) (entityDamage.get(event.getEntityLiving()).get(uuid).getFirst()/event.getEntityLiving().getMaxHealth()*100f+1);
					if (percent >= 10 && entityDamage.get(event.getEntityLiving()).get(uuid).getSecond() > 0) {
						// reset genji strike cooldown
						if (SetManager.getWornSet(uuid, player.world.isRemote) == EnumHero.GENJI) {
							EnumHero.GENJI.ability2.keybind.setCooldown(player, 0, false);
							Handler handler = TickHandler.getHandler(player, Identifier.GENJI_STRIKE);
							if (handler != null)
								handler.setBoolean(true);
						}
						Minewatch.network.sendTo(new SPacketSimple(14, !uuid.equals(mostDamage), player,
								(int)MathHelper.clamp(percent, 0, 100),
								0, 0, event.getEntityLiving()), (EntityPlayerMP) player);
					}
				}
			}
			if (event.getEntityLiving() instanceof EntityPlayerMP && mostDamage != null)
				Minewatch.network.sendTo(new SPacketSimple(14, false, (EntityPlayer) event.getEntityLiving(), -1,
						0, 0, event.getEntityLiving().world.getPlayerEntityByUUID(mostDamage)), (EntityPlayerMP) event.getEntityLiving());
			entityDamage.remove(event.getEntityLiving());
		}
		else if (event.getEntityLiving() instanceof EntityPlayerMP && event.getSource() != null && 
				event.getSource().getTrueSource() instanceof EntityHero) {
			Minewatch.network.sendTo(new SPacketSimple(14, false, (EntityPlayer) event.getEntityLiving(), -1,
					0, 0, event.getSource().getTrueSource()), (EntityPlayerMP) event.getEntityLiving());
		}
	}

	/**Copied from EntityLivingBase bc it's protected*/
	public static float applyPotionDamageCalculations(EntityLivingBase player, DamageSource source, float damage) {
		if (source.isDamageAbsolute())
			return damage;
		else {
			if (player.isPotionActive(MobEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
				int i = (player.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
				int j = 25 - i;
				float f = damage * (float)j;
				damage = f / 25.0F;
			}
			if (damage <= 0.0F)
				return 0.0F;
			else {
				int k = EnchantmentHelper.getEnchantmentModifierDamage(player.getArmorInventoryList(), source);
				if (k > 0)
					damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float)k);
				return damage;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void renderOnBlocks(RenderWorldLastEvent event) {
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.depthMask(false);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// lucio circles
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/lucio_circle.png"));
		for (Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
			if (entity instanceof EntityLivingBase && SetManager.getWornSet(entity) == EnumHero.LUCIO && 
					((EntityLivingBase) entity).getHeldItemMainhand() != null && 
					((EntityLivingBase) entity).getHeldItemMainhand().getItem() == EnumHero.LUCIO.weapon &&
					EntityHelper.shouldTarget(entity, Minecraft.getMinecraft().player, true)) {
				Vec3d entityVec = EntityHelper.getEntityPartialPos(entity);
				boolean heal = ItemMWWeapon.isAlternate(((EntityLivingBase)entity).getHeldItemMainhand());
				renderOnBlocks(entity.world, buffer, 
						heal ? 253f/255f : 9f/255f, heal ? 253f/255f : 222f/255f, heal ? 71f/255f : 123f/255f, -1, 10, entityVec.subtract(0, 1, 0), EnumFacing.UP, false);
			}
		}
		tessellator.draw();

		// custom particles with facing
		for (EnumParticle enumParticle : EnumParticle.values())
			if (!enumParticle.facingParticles.isEmpty()) {
				Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(enumParticle.facingLoc);
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				for (ParticleCustom particle : enumParticle.facingParticles) {
					particle.renderOnBlocks(buffer);
				}
				tessellator.draw();
			}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.popMatrix();

		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player != null && player.getHeldItemMainhand() != null && 
				player.getHeldItemMainhand().getItem() instanceof ItemMWWeapon && 
				((ItemMWWeapon)player.getHeldItemMainhand().getItem()).hero == SetManager.getWornSet(player)) {
			GlStateManager.color(1, 1, 1, 1f);
			GlStateManager.pushMatrix();
			((ItemMWWeapon)player.getHeldItemMainhand().getItem()).renderWorldLast(event, player);
			GlStateManager.popMatrix();
		}

		// render bounding boxes
		if (!boundingBoxesToRender.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.depthMask(false);
			GlStateManager.disableDepth();
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.disableCull();
			GlStateManager.disableBlend();

			Vec3d vec = EntityHelper.getEntityPartialPos(Minewatch.proxy.getRenderViewEntity()).scale(-1);

			for (AxisAlignedBB aabb : boundingBoxesToRender) {
				BufferBuilder vertexbuffer = tessellator.getBuffer();
				vertexbuffer.begin(3, DefaultVertexFormats.POSITION_NORMAL);
				RenderGlobal.drawBoundingBox(vertexbuffer, aabb.minX + vec.x, aabb.minY + vec.y, aabb.minZ + vec.z, aabb.maxX + vec.x, aabb.maxY + vec.y, aabb.maxZ + vec.z, 
						255, 255, 255, 1f);
				tessellator.draw();
			}

			GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.depthMask(true);
			GlStateManager.enableDepth();
			GlStateManager.popMatrix();
		}
	}

	/**Render a texture (must be bound before calling this) on blocks
	 * entityVec should be INSIDE the block that the texture will be rendered on
	 * Would like to have this use particle rotations, but rendering only on blocks would be very complicated*/
	@SideOnly(Side.CLIENT)
	public static void renderOnBlocks(World world, BufferBuilder buffer, float red, float green, float blue, float alpha, double size, Vec3d entityVec, EnumFacing facing, boolean particle) {
		Entity player = Minewatch.proxy.getRenderViewEntity();
		Vec3d playerVec = EntityHelper.getEntityPartialPos(player);
		Vec3d diffVec = entityVec.subtract(playerVec);
		Vec3d offsetVec = new Vec3d(facing.getDirectionVec()).scale(0.001d);

		double minX = MathHelper.floor(entityVec.x - (facing.getAxis() == Axis.X ? 0 : size));
		double maxX = MathHelper.floor(entityVec.x + (facing.getAxis() == Axis.X ? 0 : size));
		double minY = MathHelper.floor(entityVec.y - (facing.getAxis() == Axis.Y ? 0 : size));
		double maxY = MathHelper.floor(entityVec.y + (facing.getAxis() == Axis.Y ? 0 : size));
		double minZ = MathHelper.floor(entityVec.z - (facing.getAxis() == Axis.Z ? 0 : size));
		double maxZ = MathHelper.floor(entityVec.z + (facing.getAxis() == Axis.Z ? 0 : size));

		for (BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ))) {
			BlockPos[] positions = particle ? new BlockPos[] {blockpos, blockpos.offset(facing.getOpposite())} : new BlockPos[] {blockpos.offset(facing), blockpos, blockpos.offset(facing.getOpposite())};
			for (BlockPos pos : positions) {

				if (facing == EnumFacing.DOWN || facing == EnumFacing.NORTH || facing == EnumFacing.WEST)
					pos = pos.add(facing.getDirectionVec());

				IBlockState state = world.getBlockState(pos);
				if (state.getRenderType() != EnumBlockRenderType.INVISIBLE && state.getRenderType() != EnumBlockRenderType.LIQUID) {
					if (!particle)
						alpha = (float) ((1d - (diffVec.y - ((double)pos.getY() - playerVec.y + 0)) / 2.0D) * 0.5D);
					if (alpha >= 0.0D) {
						if (alpha > 1.0f)
							alpha = 1.0f;

						AxisAlignedBB aabb = state.getBoundingBox(world, pos);
						minX = (double)pos.getX() + aabb.minX - playerVec.x + offsetVec.x;
						maxX = (double)pos.getX() + aabb.maxX - playerVec.x + offsetVec.x;
						minY = (double)pos.getY() + aabb.minY - playerVec.y + offsetVec.y;
						maxY = (double)pos.getY() + aabb.maxY - playerVec.y + offsetVec.y;
						minZ = (double)pos.getZ() + aabb.minZ - playerVec.z + offsetVec.z;
						maxZ = (double)pos.getZ() + aabb.maxZ - playerVec.z + offsetVec.z;
						switch(facing.getAxis()) {
						case Y:
							double y = facing == EnumFacing.UP ? maxY : minY;
							double f = MathHelper.clamp(((diffVec.x - minX) / 2.0D / size + 0.5D), 0, 1);
							double f1 = MathHelper.clamp(((diffVec.x - maxX) / 2.0D / size + 0.5D), 0, 1);
							double f2 = MathHelper.clamp(((diffVec.z - minZ) / 2.0D / size + 0.5D), 0, 1);
							double f3 = MathHelper.clamp(((diffVec.z - maxZ) / 2.0D / size + 0.5D), 0, 1);
							buffer.pos(minX, y, minZ).tex(f, f2).color(red, green, blue, alpha).endVertex();
							buffer.pos(minX, y, maxZ).tex(f, f3).color(red, green, blue, alpha).endVertex();
							buffer.pos(maxX, y, maxZ).tex(f1, f3).color(red, green, blue, alpha).endVertex();
							buffer.pos(maxX, y, minZ).tex(f1, f2).color(red, green, blue, alpha).endVertex();

							buffer.pos(minX, y, minZ).tex(f, f2).color(red, green, blue, alpha).endVertex();
							buffer.pos(maxX, y, minZ).tex(f1, f2).color(red, green, blue, alpha).endVertex();
							buffer.pos(maxX, y, maxZ).tex(f1, f3).color(red, green, blue, alpha).endVertex();
							buffer.pos(minX, y, maxZ).tex(f, f3).color(red, green, blue, alpha).endVertex();
							break;
						case Z:
							double z = facing == EnumFacing.SOUTH ? maxZ : minZ;
							f = MathHelper.clamp(((diffVec.x - minX) / 2.0D / size + 0.5D), 0, 1);
							f1 = MathHelper.clamp(((diffVec.x - maxX) / 2.0D / size + 0.5D), 0, 1);
							f2 = MathHelper.clamp(((diffVec.y - minY) / 2.0D / size + 0.5D), 0, 1);
							f3 = MathHelper.clamp(((diffVec.y - maxY) / 2.0D / size + 0.5D), 0, 1);
							buffer.pos(minX, minY, z).tex(f, f2).color(red, green, blue, alpha).endVertex();
							buffer.pos(minX, maxY, z).tex(f, f3).color(red, green, blue, alpha).endVertex();
							buffer.pos(maxX, maxY, z).tex(f1, f3).color(red, green, blue, alpha).endVertex();
							buffer.pos(maxX, minY, z).tex(f1, f2).color(red, green, blue, alpha).endVertex();

							buffer.pos(minX, minY, z).tex(f, f2).color(red, green, blue, alpha).endVertex();
							buffer.pos(maxX, minY, z).tex(f1, f2).color(red, green, blue, alpha).endVertex();
							buffer.pos(maxX, maxY, z).tex(f1, f3).color(red, green, blue, alpha).endVertex();
							buffer.pos(minX, maxY, z).tex(f, f3).color(red, green, blue, alpha).endVertex();
							break;
						case X:
							double x = facing == EnumFacing.EAST ? maxX : minX;
							f = MathHelper.clamp(((diffVec.z - minZ) / 2.0D / size + 0.5D), 0, 1);
							f1 = MathHelper.clamp(((diffVec.z - maxZ) / 2.0D / size + 0.5D), 0, 1);
							f2 = MathHelper.clamp(((diffVec.y - minY) / 2.0D / size + 0.5D), 0, 1);
							f3 = MathHelper.clamp(((diffVec.y - maxY) / 2.0D / size + 0.5D), 0, 1);
							buffer.pos(x, minY, minZ).tex(f, f2).color(red, green, blue, alpha).endVertex();
							buffer.pos(x, minY, maxZ).tex(f1, f2).color(red, green, blue, alpha).endVertex();
							buffer.pos(x, maxY, maxZ).tex(f1, f3).color(red, green, blue, alpha).endVertex();
							buffer.pos(x, maxY, minZ).tex(f, f3).color(red, green, blue, alpha).endVertex();

							buffer.pos(x, minY, minZ).tex(f, f2).color(red, green, blue, alpha).endVertex();
							buffer.pos(x, maxY, minZ).tex(f, f3).color(red, green, blue, alpha).endVertex();
							buffer.pos(x, maxY, maxZ).tex(f1, f3).color(red, green, blue, alpha).endVertex();
							buffer.pos(x, minY, maxZ).tex(f1, f2).color(red, green, blue, alpha).endVertex();
							break;
						}
					}
					break;
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void hideNameTags(RenderLivingEvent.Specials.Pre event) {
		EnumHero hero = SetManager.getWornSet(event.getEntity());
		// hide name tag for invis
		if (event.getEntity() instanceof EntityPlayer && 
				(event.getEntity().isInvisible() || TickHandler.hasHandler(event.getEntity(), Identifier.SOMBRA_INVISIBLE))) 
			event.setCanceled(true);

		// override with custom health bar + name tag
		if (Config.healthBars) {
			event.setCanceled(true);

			if (event.getEntity() != null && event.getEntity().isEntityAlive() && 
					hero != null && canRenderName(event.getRenderer(), event.getEntity())) { 
				boolean enemy = EntityHelper.shouldTarget(Minewatch.proxy.getRenderViewEntity(), event.getEntity(), false);
				float yOffset = event.getEntity().height + 0.5F - (event.getEntity().isSneaking() ? 0.25F : 0.0F);

				GlStateManager.pushMatrix();

				Minecraft mc = Minecraft.getMinecraft();
				double scale = 0.02f + mc.player.getDistanceToEntity(event.getEntity())/1000f;
				boolean isThirdPersonFrontal = mc.getRenderManager().options.thirdPersonView == 2;
				float viewerYaw = mc.getRenderManager().playerViewY;
				float viewerPitch = mc.getRenderManager().playerViewX;
				GlStateManager.translate(event.getX(), event.getY()+yOffset, event.getZ());
				GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate((float)(isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
				GlStateManager.scale(-scale, -scale, scale);
				GlStateManager.disableLighting();
				GlStateManager.depthMask(true);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.enableDepth();

				// translate based on distance
				GlStateManager.translate(0, -mc.player.getDistanceToEntity(event.getEntity())/6f, 0);

				// health bar
				if (!enemy || TickHandler.hasHandler(event.getEntity(), Identifier.HEALTH_SHOW_BAR) ||
						(mc.player.isSpectator() && !TickHandler.hasHandler(mc.player, Identifier.DEAD))) {
					GlStateManager.translate(0, enemy ? -10 : -20, 0);
					renderHealthBar(event.getEntity(), hero, false, enemy);
					GlStateManager.translate(0, enemy ? 10 : 20, 0);
				}

				// icon
				if (!enemy) {
					GlStateManager.pushMatrix();
					// hurt color
					if (event.getEntity().getHealth() < event.getEntity().getMaxHealth() && 
							TickHandler.hasHandler(event.getEntity(), Identifier.HEALTH_FIGHTING))
						GlStateManager.color(0.7f, 0.67f, 0.35f);
					// normal color
					else
						GlStateManager.color(0.19f, 0.84f, 0.78f);
					GlStateManager.enableDepth();
					GL11.glDepthFunc(GL11.GL_ALWAYS);
					mc.getTextureManager().bindTexture(RenderManager.ABILITY_OVERLAY);
					GuiUtils.drawTexturedModalRect(-8, 0, 58, 245, 30, 10, 0);
					GL11.glDepthFunc(GL11.GL_LEQUAL);
					GlStateManager.popMatrix();
				}

				// name plate
				GlStateManager.translate(0, enemy ? -0 : -8, 0);
				drawNameplate(mc.fontRenderer, event.getEntity().getDisplayName().getFormattedText());

				GlStateManager.popMatrix();
			}
		}
	}

	@SideOnly(Side.CLIENT) // copied from RenderLivingBase#canRenderName() to make public
	public static boolean canRenderName(RenderLivingBase renderer, EntityLivingBase entity) {
		if (entity == null || !entity.isEntityAlive())
			return false;
		// render for spectators
		else if (Minewatch.proxy.getClientPlayer().isSpectator() && 
				!(entity instanceof EntityPlayer && ((EntityPlayer)entity).isSpectator()))
			return true;
		// if EntityHero, can check directly
		else if (renderer instanceof RenderHero && entity instanceof EntityHero)
			return ((RenderHero)renderer).canRenderName((EntityHero) entity);

		EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
		boolean flag = !entity.isInvisibleToPlayer(entityplayersp);

		if (entity != entityplayersp)
		{
			Team team = entity.getTeam();
			Team team1 = entityplayersp.getTeam();

			if (team != null)
			{
				Team.EnumVisible team$enumvisible = team.getNameTagVisibility();

				switch (team$enumvisible)
				{
				case ALWAYS:
					return flag;
				case NEVER:
					return false;
				case HIDE_FOR_OTHER_TEAMS:
					return team1 == null ? flag : team.isSameTeam(team1) && (team.getSeeFriendlyInvisiblesEnabled() || flag);
				case HIDE_FOR_OWN_TEAM:
					return team1 == null ? flag : !team.isSameTeam(team1) && flag;
				default:
					return true;
				}
			}
		}

		return Minecraft.isGuiEnabled() && entity != Minecraft.getMinecraft().getRenderManager().renderViewEntity && flag && !entity.isBeingRidden();
	}

	/**Render an entity's health bar*/
	@SideOnly(Side.CLIENT)
	public static void renderHealthBar(EntityLivingBase entity, EnumHero hero, boolean inGui, boolean enemy) {
		GlStateManager.pushMatrix();
		// health		
		float maxHealth = HealthManager.getMaxCombinedHealth(entity);

		HashMap<Type, Float> map = HealthManager.getAllCurrentHealth(entity, hero);
		float health = map.get(Type.HEALTH);
		float armor = map.get(Type.ARMOR) + map.get(Type.ARMOR_ABILITY);
		float shield = map.get(Type.SHIELD);
		float shieldAbility = map.get(Type.SHIELD_ABILITY) + map.get(Type.ABSORPTION);

		float maxWidth = 70f;
		int barWidth = 8;
		int barHeight = 11;
		float scaleX = maxWidth / (maxHealth/25f*(barWidth+0.4f));
		float scaleY = inGui ? 1 : 0.6f;
		float incrementX = barWidth + (inGui ? 0.4f/scaleX : 0);
		float slant = barHeight * maxHealth * 0.0006f;
		float xOffset = inGui ? 0 : maxWidth/scaleX/2f;

		// health bars: health -> armor -> shield -> shieldAbility
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.scale(scaleX, scaleY, 1);

		// ana health / damage bar background
		boolean hasHeal = TickHandler.hasHandler(entity, Identifier.ANA_GRENADE_HEAL);
		boolean hasDamage = TickHandler.hasHandler(entity, Identifier.ANA_GRENADE_DAMAGE);
		if (hasHeal || hasDamage) {
			RenderHelper.drawSlantedRect(-scaleX-xOffset, 1, (int) (maxHealth/25f*incrementX)+scaleX-xOffset, barHeight, hasHeal ? 0x60FFFF63 : 0x60E702FF, inGui ? -0.001D : 0.001D, slant);
			GlStateManager.enableBlend();
			Minecraft.getMinecraft().renderEngine.bindTexture(hasHeal ? EnumParticle.ANA_GRENADE_HEAL.facingLoc : EnumParticle.ANA_GRENADE_DAMAGE.facingLoc);	
			GlStateManager.color(1, 1, 1, 0.7f);
			GlStateManager.scale(1/scaleX, 1/scaleY, 1);
			if (inGui)
				Gui.drawModalRectWithCustomSizedTexture((int) (((maxHealth/25f*incrementX)+10)*scaleX-xOffset+1), -2, 0, 0, 16, 16, 16, 16);
			else
				Gui.drawModalRectWithCustomSizedTexture(-8, -barHeight-7, 0, 0, 16, 16, 16, 16);
			GlStateManager.scale(scaleX, scaleY, 1);
		}

		mc.getTextureManager().bindTexture(ABILITY_OVERLAY);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		// health
		double start = 0;
		double finish = health/25f;
		int red, green, blue, alpha;
		if (enemy) {
			red = 185;
			green = 8;
			blue = 52;
		}
		else
			red = green = blue = 255;
		alpha = inGui ? 180 : 255;
		if (health > 0)
			renderIndividualHealthBar(buffer, start, finish, health, barHeight, barWidth, xOffset, incrementX, red, green, blue, alpha, slant);
		// armor
		start = finish;
		finish = (health+armor)/25f;
		if (enemy) {
			red = 226;
			green = 172;
			blue = 110;
		}
		else {
			red = 255;
			green = 220;
			blue = 49;
		}
		alpha = inGui ? 180 : 255;
		if (armor > 0)
			renderIndividualHealthBar(buffer, start, finish, armor, barHeight, barWidth, xOffset, incrementX, red, green, blue, alpha, slant);
		// shield
		start = finish;
		finish = (health+armor+shield)/25f;
		if (enemy) {
			red = 42;
			green = 145;
			blue = 178;
		}
		else {
			red = 114;
			green = 189;
			blue = 234;
		}
		alpha = inGui ? 180 : 255;
		if (shield > 0)
			renderIndividualHealthBar(buffer, start, finish, shield, barHeight, barWidth, xOffset, incrementX, red, green, blue, alpha, slant);
		// shield ability
		start = finish;
		finish = (health+armor+shield+shieldAbility)/25f;
		if (enemy) {
			red = 50;
			green = 58;
			blue = 207;
		}
		else {
			red = 6;
			green = 58;
			blue = 207;
		}
		alpha = inGui ? 180 : 255;
		if (shieldAbility > 0)
			renderIndividualHealthBar(buffer, start, finish, shieldAbility, barHeight, barWidth, xOffset, incrementX, red, green, blue, alpha, slant);
		// translucent background
		start = finish;
		finish = maxHealth/25f;
		red = green = blue = 150;
		if (enemy) {
			red = 185;
			green = 8;
			blue = 52;
		}
		alpha = inGui ? 160 : 90;
		if (finish > start)
			renderIndividualHealthBar(buffer, start, finish, maxHealth, barHeight, barWidth, xOffset, incrementX, red, green, blue, alpha, slant);

		tessellator.draw();
		GlStateManager.popMatrix(); 
	}

	/**Render health bar for a certain value (background, health, armor, shield, barrier)*/
	@SideOnly(Side.CLIENT)
	public static void renderIndividualHealthBar(BufferBuilder buffer, double start, double finish, float value, int barHeight, int barWidth, float xOffset, float incrementX, int red, int green, int blue, int alpha, float slant) {
		float uScale = 1f / 0x100;
		float vScale = 1f / 0x100;
		int zLevel = 0;
		int v = 245;
		for (int i=(int) start; i<finish; ++i) {
			double currentBarWidth = barWidth;
			double x = incrementX * i - xOffset;
			double y = 0;
			double u = 39;

			if (i == (int) start && start % 1 != 0) { // partial start (pushes u out of bounds a bit.. don't put anything next to icon in texture)
				x += currentBarWidth * ((float)start % 1);
				u += currentBarWidth * ((float)start % 1);
			}
			if (i == Math.ceil(finish)-1 && finish % 1 != 0) // partial end
				currentBarWidth *= ((float)finish % 1);

			buffer.pos(x-slant, y + barHeight, zLevel).tex(u * uScale, ((v + barHeight) * vScale)).color(red, green, blue, alpha).endVertex();
			buffer.pos(x-slant + currentBarWidth, y + barHeight, zLevel).tex((u + currentBarWidth) * uScale, ((v + barHeight) * vScale)).color(red, green, blue, alpha).endVertex();
			buffer.pos(x+slant + currentBarWidth, y, zLevel).tex((u + currentBarWidth) * uScale, (v * vScale)).color(red, green, blue, alpha).endVertex();
			buffer.pos(x+slant, y, zLevel).tex(u * uScale, (v * vScale)).color(red, green, blue, alpha).endVertex();
		}
	}

	/**Modified from EntityRenderer#drawNamePlate()*/
	@SideOnly(Side.CLIENT)
	public static void drawNameplate(FontRenderer fontRendererIn, String str) {
		GlStateManager.pushMatrix();
		fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, 0, -1, true);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

}