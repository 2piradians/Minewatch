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
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
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
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
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
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.particle.ParticleCustom;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

@Mod.EventBusSubscriber
public class RenderManager {

	public static final ResourceLocation ABILITY_OVERLAY = new ResourceLocation(Minewatch.MODID, "textures/gui/ability_overlay.png");
	public static Handler SNEAKING = new Handler(Identifier.HERO_SNEAKING, true) {};
	public static HashMap<EntityLivingBase, HashMap<UUID, Tuple<Float, Integer>>> entityDamage = Maps.newHashMap();
	public static Handler MESSAGES = new Handler(Identifier.HERO_MESSAGES, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			ArrayList<Handler> handlers = TickHandler.getHandlers(entity, Identifier.HERO_MESSAGES);
			return handlers.indexOf(this) <= 6 ? --this.ticksLeft <= 0 : false;
		}
	};
	public static Handler HIT_OVERLAY = new Handler(Identifier.HIT_OVERLAY, false) {};
	public static Handler KILL_OVERLAY = new Handler(Identifier.KILL_OVERLAY, false) {};
	public static Handler MULTIKILL = new Handler(Identifier.HERO_MULTIKILL, false) {};
	/**Used for debugging*/
	public static CopyOnWriteArrayList<AxisAlignedBB> boundingBoxesToRender = new CopyOnWriteArrayList<AxisAlignedBB>();

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void hideEntityWearingArmor(RenderLivingEvent.Pre<EntityLivingBase> event) {	
		// make entity body follow head
		if (event.getEntity() instanceof EntityLivingBase && event.getEntity().getHeldItemMainhand() != null && 
				event.getEntity().getHeldItemMainhand().getItem() instanceof ItemMWWeapon &&
				(KeyBind.LMB.isKeyDown((EntityLivingBase) event.getEntity()) || KeyBind.RMB.isKeyDown((EntityLivingBase) event.getEntity()))) {
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
			model.setInvisible(true);
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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void renderWorldLast(RenderWorldLastEvent event) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player != null && player.getHeldItemMainhand() != null && 
				player.getHeldItemMainhand().getItem() instanceof ItemMWWeapon && 
				((ItemMWWeapon)player.getHeldItemMainhand().getItem()).hero == SetManager.getWornSet(player)) {
			GlStateManager.color(1, 1, 1, 1f);
			GlStateManager.pushMatrix();
			((ItemMWWeapon)player.getHeldItemMainhand().getItem()).renderWorldLast(event, player);
			GlStateManager.popMatrix();
		}

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
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer vertexbuffer = tessellator.getBuffer();
				vertexbuffer.begin(3, DefaultVertexFormats.POSITION_NORMAL);
				RenderGlobal.drawBoundingBox(vertexbuffer, aabb.minX + vec.xCoord, aabb.minY + vec.yCoord, aabb.minZ + vec.zCoord, aabb.maxX + vec.xCoord, aabb.maxY + vec.yCoord, aabb.maxZ + vec.zCoord, 
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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void renderCrosshairs(RenderGameOverlayEvent.Pre event) {
		if (Config.guiScale > 0) {
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

			if (weapon != null && !KeyBind.HERO_INFORMATION.isKeyDown(player)) {
				GlStateManager.color(1, 1, 1, 1f);
				GlStateManager.pushMatrix();
				weapon.preRenderGameOverlay(event, player, width, height, hand);
				GlStateManager.popMatrix();
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

				// eliminate/assist text overlay
				double yOffset = 0;
				ArrayList<Handler> handlers = TickHandler.getHandlers(player, Identifier.HERO_MESSAGES);
				for (int i=0; i<Math.min(6, handlers.size()); ++i) {
					handler = handlers.get(i);
					if (handler != null && handler.string != null) {
						float alpha = 0.7f;
						if (handler.ticksLeft < 15)
							alpha -= (1f-(handler.ticksLeft-10)/5f)*alpha;
						else if (handler.ticksLeft > handler.initialTicks-8)
							alpha -= (1f-(handler.initialTicks-handler.ticksLeft+1)/8f)*alpha;
						if (alpha > 0) {
							double scale = handler.bool ? 0.8f :1f;
							GlStateManager.scale(scale, scale, 1);
							FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
							font.drawString(handler.string, (float)((width/2/scale) - font.getStringWidth(handler.string)/2), (float) (height/1.6f/scale+yOffset+(handler.bool ? 4 : 0)), new Color(1, 1, 1, alpha).getRGB(), false);
							GlStateManager.scale(1/scale, 1/scale, 1);
						}
						yOffset += handler.ticksLeft >= 10 ? 11 : handler.ticksLeft/10f*11f;
					}
				}

				GlStateManager.disableBlend();
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
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void renderOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() == ElementType.HELMET && Config.guiScale > 0) {			
			EntityPlayer player = Minecraft.getMinecraft().player;
			EnumHero hero = SetManager.getWornSet(player);
			ItemMWWeapon weapon = player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemMWWeapon ? (ItemMWWeapon)player.getHeldItemMainhand().getItem() : null;

			// hero information screen
			if (hero != null && KeyBind.HERO_INFORMATION.isKeyDown(player))
				hero.displayInfoScreen(event.getResolution());
			else {
				if (hero != null) {
					// display icon
					GlStateManager.pushMatrix();
					GlStateManager.color(1, 1, 1, 1);
					GlStateManager.enableDepth();
					GlStateManager.enableAlpha();

					double scale = 0.25d*Config.guiScale;
					GlStateManager.scale(scale, scale, 1);
					GlStateManager.translate(40-scale*120, (int) ((event.getResolution().getScaledHeight() - 256*scale) / scale) - 35+scale*110, 0);
					Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/icon_background.png"));
					GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 240, 230, 0);
					Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+hero.name.toLowerCase()+"_icon.png"));
					GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 240, 230, 0);

					GlStateManager.popMatrix();
				}

				// display abilities/weapon
				if (weapon != null) {
					GlStateManager.pushMatrix();
					GlStateManager.enableDepth();
					GlStateManager.enableAlpha();

					double scale = 2.7d*Config.guiScale;
					GlStateManager.scale(scale, scale, 1);
					GlStateManager.translate((int) (event.getResolution().getScaledWidth()/scale)-35, ((int)event.getResolution().getScaledHeight()/scale)-25+scale*2, 0);
					Minecraft.getMinecraft().getTextureManager().bindTexture(ABILITY_OVERLAY);
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
							Minecraft.getMinecraft().getTextureManager().bindTexture(ABILITY_OVERLAY);
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
								GuiUtils.drawTexturedModalRect(-i*9+1, 6, 16, 246, 5, 5, 0);
							}
							GlStateManager.color(1, 1, 1);
							GlStateManager.popMatrix();

							// keybinds 
							GlStateManager.pushMatrix();
							GlStateManager.translate(-i*0.2f, -i*0.7f, 0);
							// background
							if (ability.showKeybind(player)) {
								if (ability.keybind.getKeyName() != "")
									GuiUtils.drawTexturedModalRect(-i*9-6, 9, 0, 247, 11, 6, 0);
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
							int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(ability.keybind.getKeyName());
							GlStateManager.scale(0.25d, 0.25d, 1);
							GlStateManager.rotate(4.5f, 0, 0, 1);
							// keybind text
							if (ability.showKeybind(player)) 
								Minecraft.getMinecraft().fontRendererObj.drawString(ability.keybind.getKeyName(), 3-i*36-width/2, 43+i*3, 0);
							// multi-use number
							if (ability.maxUses > 0)
								Minecraft.getMinecraft().fontRendererObj.drawString(String.valueOf(ability.getUses(player)), 16-i*36, -8+i*3, 0);
							// cooldown
							scale = 2d;
							GlStateManager.scale(scale, scale, 1);
							if (ability.getCooldown(player) > 0) { 
								String num = String.valueOf((int)Math.ceil(ability.getCooldown(player)/20d));
								width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(num);
								Minecraft.getMinecraft().fontRendererObj.drawString(num, 6-i*18-width/2, 8+i, 0xFFFFFF);
							}
							GlStateManager.color(1, 1, 1);
							GlStateManager.popMatrix();
						}
					}
					// ammo
					if (weapon.getMaxAmmo(player) > 0) {
						scale = 0.45d;
						GlStateManager.scale(scale, scale, 1);
						int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(
								String.valueOf(weapon.getCurrentAmmo(player)));
						Minecraft.getMinecraft().fontRendererObj.drawString(
								String.valueOf(weapon.getCurrentAmmo(player)), 31-width, -10, 0xFFFFFF);
						scale = 0.6d;
						GlStateManager.scale(scale, scale, 1);
						Minecraft.getMinecraft().fontRendererObj.drawString("/", 53, -10, 0x00D5FF);
						Minecraft.getMinecraft().fontRendererObj.drawString(
								String.valueOf(weapon.getMaxAmmo(player)), 59, -10, 0xFFFFFF);
					}

					GlStateManager.popMatrix();
				}
			}
		}
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
		if (event.getSource().getSourceOfDamage() instanceof EntityPlayerMP)
			player = ((EntityPlayerMP)event.getSource().getSourceOfDamage());
		else if (event.getSource().getEntity() instanceof EntityPlayerMP)
			player = ((EntityPlayerMP)event.getSource().getEntity());
		else if (event.getSource().getSourceOfDamage() instanceof IThrowableEntity && 
				((IThrowableEntity) event.getSource().getSourceOfDamage()).getThrower() instanceof EntityPlayerMP)
			player = (EntityPlayerMP) ((IThrowableEntity) event.getSource().getSourceOfDamage()).getThrower();

		if (player != null && event.getEntityLiving() != null && player != event.getEntityLiving()) {
			if (!player.world.isRemote && SetManager.getWornSet(player) != null) {
				try {
					float damage = event.getAmount();
					damage = CombatRules.getDamageAfterAbsorb(damage, (float)event.getEntityLiving().getTotalArmorValue(), (float)event.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
					damage = applyPotionDamageCalculations(player, event.getSource(), damage);
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
						if (SetManager.getWornSet(uuid) == EnumHero.GENJI) {
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
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.depthMask(false);
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();

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
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(enumParticle.facingLoc);
				for (ParticleCustom particle : enumParticle.facingParticles)
					particle.renderOnBlocks(buffer);
				tessellator.draw();
			}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
	}

	/**Render a texture (must be bound before calling this) on blocks
	 * entityVec should be INSIDE the block that the texture will be rendered on*/
	@SideOnly(Side.CLIENT)
	public static void renderOnBlocks(World world, VertexBuffer buffer, float red, float green, float blue, float alpha, double size, Vec3d entityVec, EnumFacing facing, boolean particle) {
		Entity player = Minewatch.proxy.getRenderViewEntity();
		Vec3d playerVec = EntityHelper.getEntityPartialPos(player);
		Vec3d diffVec = entityVec.subtract(playerVec);
		Vec3d offsetVec = new Vec3d(facing.getDirectionVec()).scale(0.001d);

		double minX = MathHelper.floor(entityVec.xCoord - (facing.getAxis() == Axis.X ? 0 : size));
		double maxX = MathHelper.floor(entityVec.xCoord + (facing.getAxis() == Axis.X ? 0 : size));
		double minY = MathHelper.floor(entityVec.yCoord - (facing.getAxis() == Axis.Y ? 0 : size));
		double maxY = MathHelper.floor(entityVec.yCoord + (facing.getAxis() == Axis.Y ? 0 : size));
		double minZ = MathHelper.floor(entityVec.zCoord - (facing.getAxis() == Axis.Z ? 0 : size));
		double maxZ = MathHelper.floor(entityVec.zCoord + (facing.getAxis() == Axis.Z ? 0 : size));

		for (BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ))) {
			BlockPos[] positions = particle ? new BlockPos[] {blockpos} : new BlockPos[] {blockpos.up(), blockpos, blockpos.down()};
			for (BlockPos pos : positions) {
				
				if (facing == EnumFacing.DOWN || facing == EnumFacing.NORTH || facing == EnumFacing.WEST)
					pos = pos.add(facing.getDirectionVec());
				
				IBlockState state = world.getBlockState(pos);
				if (state.getRenderType() != EnumBlockRenderType.INVISIBLE && state.getRenderType() != EnumBlockRenderType.LIQUID) {
					if (!particle)
						alpha = (float) ((1d - (diffVec.yCoord - ((double)pos.getY() - playerVec.yCoord + 0)) / 2.0D) * 0.5D);
					if (alpha >= 0.0D) {
						if (alpha > 1.0f)
							alpha = 1.0f;
												
						AxisAlignedBB aabb = state.getBoundingBox(world, pos);
						minX = (double)pos.getX() + aabb.minX - playerVec.xCoord + offsetVec.xCoord;
						maxX = (double)pos.getX() + aabb.maxX - playerVec.xCoord + offsetVec.xCoord;
						minY = (double)pos.getY() + aabb.minY - playerVec.yCoord + offsetVec.yCoord;
						maxY = (double)pos.getY() + aabb.maxY - playerVec.yCoord + offsetVec.yCoord;
						minZ = (double)pos.getZ() + aabb.minZ - playerVec.zCoord + offsetVec.zCoord;
						maxZ = (double)pos.getZ() + aabb.maxZ - playerVec.zCoord + offsetVec.zCoord;
						switch(facing.getAxis()) {
						case Y:
							double y = facing == EnumFacing.UP ? maxY : minY;
							double f = MathHelper.clamp(((diffVec.xCoord - minX) / 2.0D / size + 0.5D), 0, 1);
							double f1 = MathHelper.clamp(((diffVec.xCoord - maxX) / 2.0D / size + 0.5D), 0, 1);
							double f2 = MathHelper.clamp(((diffVec.zCoord - minZ) / 2.0D / size + 0.5D), 0, 1);
							double f3 = MathHelper.clamp(((diffVec.zCoord - maxZ) / 2.0D / size + 0.5D), 0, 1);
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
							f = MathHelper.clamp(((diffVec.xCoord - minX) / 2.0D / size + 0.5D), 0, 1);
							f1 = MathHelper.clamp(((diffVec.xCoord - maxX) / 2.0D / size + 0.5D), 0, 1);
							f2 = MathHelper.clamp(((diffVec.yCoord - minY) / 2.0D / size + 0.5D), 0, 1);
							f3 = MathHelper.clamp(((diffVec.yCoord - maxY) / 2.0D / size + 0.5D), 0, 1);
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
							f = MathHelper.clamp(((diffVec.zCoord - minZ) / 2.0D / size + 0.5D), 0, 1);
							f1 = MathHelper.clamp(((diffVec.zCoord - maxZ) / 2.0D / size + 0.5D), 0, 1);
							f2 = MathHelper.clamp(((diffVec.yCoord - minY) / 2.0D / size + 0.5D), 0, 1);
							f3 = MathHelper.clamp(((diffVec.yCoord - maxY) / 2.0D / size + 0.5D), 0, 1);
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

}