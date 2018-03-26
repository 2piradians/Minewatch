package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.util.Texture;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

@Mod.EventBusSubscriber
public class UltimateManager {

	public static TextureAtlasSprite ready;
	public static Texture gif;
	public static final ResourceLocation GIF = new ResourceLocation(Minewatch.MODID, "textures/gui/ultimate.gif");
	public static final ResourceLocation CIRCLE = new ResourceLocation(Minewatch.MODID, "textures/gui/ultimate_circle.png");
	public static final ResourceLocation READY = new ResourceLocation(Minewatch.MODID, "textures/gui/ultimate_ready.png");
	public static final ResourceLocation BACKGROUND = new ResourceLocation(Minewatch.MODID, "textures/gui/ultimate_background.png");
	public static final ResourceLocation FOREGROUND = new ResourceLocation(Minewatch.MODID, "textures/gui/ultimate_foreground.png");
	public static final ResourceLocation STENCIL = new ResourceLocation(Minewatch.MODID, "textures/gui/ultimate_stencil.png");
	public static final ResourceLocation KEYBIND_0 = new ResourceLocation(Minewatch.MODID, "textures/gui/ultimate_keybind_0.png");
	public static final ResourceLocation KEYBIND_1 = new ResourceLocation(Minewatch.MODID, "textures/gui/ultimate_keybind_1.png");

	public static final Handler PREVENT_CHARGE = new Handler(Identifier.ULTIMATE_PREVENT_CHARGE, false) {};
	public static final int SYNC_DELAY = 5;

	public enum AttackType {
		DAMAGE, HEAL, SELF_HEAL
	}

	private static float playerChargeClient;
	/**Map of player UUIDs and their current ult charge*/
	private static HashMap<UUID, Float> playerChargesServer = Maps.newHashMap();

	/**Set of EntityPlayerMPs that need to have their charges synced to their clients*/
	private static HashSet<EntityPlayerMP> needToBeSynced = new HashSet();
	/**Tick timer to when charges should be synced*/
	private static int syncTickTimer;

	/**Does this entity have enough charge to use ult*/
	public static boolean canUseUltimate(Entity entity) {
		EnumHero hero = SetManager.getWornSet(entity);
		return hero != null && getCurrentCharge(entity) >= getMaxCharge(hero);
	}

	/**Get EntityHero or EntityPlayer's current ult charge*/
	public static float getCurrentCharge(Entity entity) {
		if (entity instanceof EntityHero)
			return ((EntityHero)entity).ultCharge;
		else if (entity instanceof EntityPlayer) {
			if (!entity.world.isRemote && playerChargesServer.containsKey(entity.getPersistentID()))
				return playerChargesServer.get(entity.getPersistentID());
			else if (entity.world.isRemote && entity == Minewatch.proxy.getClientPlayer()) 
				return playerChargeClient;
		}
		return 0;
	}

	/**Get an entity's max charge to use ultimate*/
	public static float getMaxCharge(Entity entity) {
		EnumHero hero = SetManager.getWornSet(entity);
		if (hero != null)
			return getMaxCharge(hero);
		return 0;
	}

	/**Get a hero's max charge to use ultimate*/
	public static float getMaxCharge(EnumHero hero) {
		if (hero != null)
			return hero.ultimateChargeRequired;
		return 0;
	}

	/**Add ultimate charge for an entity - amount should be unscaled*/
	public static void addCharge(Entity entity, float amount, boolean syncToClient) {
		if (entity instanceof EntityHero)
			amount += ((EntityHero)entity).ultCharge;
		else if (entity instanceof EntityPlayer) {
			if (!entity.world.isRemote && playerChargesServer.containsKey(entity.getPersistentID())) 
				amount +=  playerChargesServer.get(entity.getPersistentID());
			else if (entity.world.isRemote && entity == Minewatch.proxy.getClientPlayer()) 
				amount += playerChargeClient;
		}
		setCharge(entity, amount, syncToClient);
	}

	/**Set ultimate charge for an entity - amount should be unscaled*/
	public static void setCharge(Entity entity, float amount, boolean syncToClient) {
		if (entity instanceof EntityHero)
			((EntityHero)entity).ultCharge = amount;
		else if (entity instanceof EntityPlayer) {
			if (!entity.world.isRemote) {
				playerChargesServer.put(entity.getPersistentID(), amount);
				// set to be synced to client
				if (syncToClient && entity instanceof EntityPlayerMP)
					needToBeSynced.add((EntityPlayerMP) entity);
			}
			else if (entity.world.isRemote && entity == Minewatch.proxy.getClientPlayer()) {
				playerChargeClient = amount;
			}
		}
	}

	/**Handles ultimate charge for attacks / abilities / healing - see https://overwatch.gamepedia.com/Ultimate_ability*/
	public static void handleAbilityCharge(Entity actualThrower, Entity damageSource, float amount, AttackType type) {
		EnumHero hero = SetManager.getWornSet(actualThrower);
		if (hero != null && !TickHandler.hasHandler(actualThrower, Identifier.ULTIMATE_PREVENT_CHARGE)) {
			// specific hero / ability modifiers
			switch(hero) {
			case LUCIO:
				if (type == AttackType.HEAL || type == AttackType.SELF_HEAL)
					amount *= 19/15f;
				break;
			case MERCY:
				if (type == AttackType.SELF_HEAL)
					amount = 0;
				else if (type == AttackType.HEAL)
					amount *= 4/5f;
				break;
			case MOIRA:
				if (type == AttackType.HEAL || type == AttackType.SELF_HEAL)
					amount *= 6/5f;
				break;
			case ZENYATTA:
				if (type == AttackType.HEAL) 
					amount *= 33/25f;
				break;
			}

			amount *= 10f; // 1 damage in mc = 10 damage in ow

			if (amount > 0)
				addCharge(actualThrower, amount, true);
		}
	}

	@SubscribeEvent
	public static void handleNormalCharge(TickEvent.PlayerTickEvent event) {
		if (event.phase == Phase.END) {
			handleNormalCharge(event.player);
		}
	}

	@SubscribeEvent
	public static void syncToClients(TickEvent.ServerTickEvent event) {
		if (event.phase == Phase.END && --syncTickTimer <= 0 && !needToBeSynced.isEmpty()) {
			syncTickTimer = SYNC_DELAY;
			for (EntityPlayerMP player : needToBeSynced)
				Minewatch.network.sendTo(new SPacketSimple(79, false, player, getCurrentCharge(player), syncTickTimer, 0), player);
			needToBeSynced.clear();
		}
	}

	/**Called once per tick for players and heroes - manages normal ult charge*/
	public static void handleNormalCharge(Entity entity) {
		if (entity != null && entity.ticksExisted % 4 == 0) {
			EnumHero hero = SetManager.getWornSet(entity);
			if (hero != null) {
				addCharge(entity, 1, false);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static void renderUltimateMeter(EntityPlayer player, EnumHero hero, double width, double height) {
		Config.guiScale += 0.01d; // TODO, move up h to change hero
		if (Config.guiScale > 1.3d)
			Config.guiScale = 0.001d;
		Config.guiScale = 0.75d;

		Minecraft mc = Minecraft.getMinecraft();
		double percent = MathHelper.clamp(Math.floor((getCurrentCharge(player)/getMaxCharge(player)*100d)), 0d, 100d);
		boolean ready = percent >= 100;
		double scale, scale2;
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.translate(width/2f, height-80f, 0);

		if (!ready) {
			// background
			GlStateManager.pushMatrix();
			mc.getTextureManager().bindTexture(BACKGROUND);
			scale = 0.22d*Config.guiScale;
			GlStateManager.scale(scale, scale, 1);
			Gui.drawModalRectWithCustomSizedTexture(-128, -128, 0, 0, 256, 256, 256, 256);		
			// stencil
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			GL11.glDepthMask(false);
			GL11.glStencilMask(0xFF); // writing on
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT); // flush old data
			GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF); // always add to buffer
			GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_REPLACE); // replace on success
			GlStateManager.colorMask(false, false, false, false); // don't draw this
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder buffer = tess.getBuffer();
			buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
			double deg_to_rad = 0.0174532925d;
			double precision = 1d;
			double angle_from = 270;
			double angle_to = angle_from + (percent/100f) * 360d;
			double angle_diff=angle_to-angle_from;
			double steps=Math.round(angle_diff*precision);
			double angle=angle_from;
			double size = 200;
			buffer.pos(0, 0, 0.001D).endVertex();
			buffer.pos(0, 0, 0.001D).endVertex();
			for (int i=(int) steps; i>0; i--) {
				angle=angle_from+angle_diff/steps*i;
				buffer.pos(size*Math.cos(angle*deg_to_rad), size*Math.sin(angle*deg_to_rad), 0.001D).endVertex();
			}
			tess.draw();
			GlStateManager.colorMask(true, true, true, true);
			GL11.glStencilFunc(GL11.GL_LEQUAL, 1, 0xFF); // anything written to buffer will be drawn
			GL11.glStencilMask(0x00); // writing off
			GlStateManager.enableTexture2D();
			// foreground
			mc.getTextureManager().bindTexture(FOREGROUND);
			GlStateManager.translate(0, 0, 5d);
			Gui.drawModalRectWithCustomSizedTexture(-128, -128, 0, 0, 256, 256, 256, 256);
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			GL11.glDepthMask(true);
			GlStateManager.popMatrix();
			// keybind background
			GlStateManager.pushMatrix();
			mc.getTextureManager().bindTexture(KEYBIND_0);
			scale = 0.19d*Config.guiScale;
			GlStateManager.scale(scale, scale, 1);
			Gui.drawModalRectWithCustomSizedTexture(-256, -256, 0, 0, 512, 512, 512, 512);
			GlStateManager.popMatrix();
		}
		else {
			// circle background
			GlStateManager.pushMatrix();
			scale = 0.57d*Config.guiScale;
			GlStateManager.scale(scale, scale, 1);
			GlStateManager.color(1, 1, 1, 0.6f);
			mc.getTextureManager().bindTexture(CIRCLE);
			Gui.drawModalRectWithCustomSizedTexture(-32, -32, 0, 0, 64, 64, 64, 64);
			GlStateManager.popMatrix();
			// animation
			GlStateManager.pushMatrix();
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
			scale = 0.88d*Config.guiScale;
			GlStateManager.scale(scale, scale, 1);
			GlStateManager.color(1, 1, 1, 1);
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			double xCoord = -106.5d;
			double yCoord = -92.5d;
			double heightIn = 256;
			double widthIn = 256;
			double z = 0;
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			for (int i=0; i<1; ++i) {
				bufferbuilder.pos((double)(xCoord + 0), (double)(yCoord + heightIn), (double)z).tex((double)UltimateManager.ready.getMinU(), (double)UltimateManager.ready.getMaxV()).endVertex();
				bufferbuilder.pos((double)(xCoord + widthIn), (double)(yCoord + heightIn), (double)z).tex((double)UltimateManager.ready.getMaxU(), (double)UltimateManager.ready.getMaxV()).endVertex();
				bufferbuilder.pos((double)(xCoord + widthIn), (double)(yCoord + 0), (double)z).tex((double)UltimateManager.ready.getMaxU(), (double)UltimateManager.ready.getMinV()).endVertex();
				bufferbuilder.pos((double)(xCoord + 0), (double)(yCoord + 0), (double)z).tex((double)UltimateManager.ready.getMinU(), (double)UltimateManager.ready.getMinV()).endVertex();
			}
			tessellator.draw();
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			GlStateManager.popMatrix();
			
			// icon
			GlStateManager.pushMatrix();
			GlStateManager.color(1, 1, 1, 0.9f);
			mc.getTextureManager().bindTexture(hero.ultimate.getTexture());
			scale = 0.20d*Config.guiScale;
			GlStateManager.scale(scale, scale, 1);
			Gui.drawModalRectWithCustomSizedTexture(-64, -64, 0, 0, 128, 128, 128, 128);
			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.popMatrix();
		}

		// keybind
		GlStateManager.pushMatrix();
		scale = 1.05d*Config.guiScale;
		GlStateManager.scale(scale, scale, 1);
		String keybind = (ready ? TextFormatting.BOLD : "") + KeyBind.ULTIMATE.getKeyName();
		float widthString = mc.fontRenderer.getStringWidth(keybind);
		mc.fontRenderer.drawString(keybind, -widthString/2f, 31f, ready ? 0xE0000000 : 0xE0757575, !ready);		
		GlStateManager.popMatrix();

		if (!ready) {
			// charge
			GlStateManager.color(1, 1, 1, 0.1f);
			double stretchY = 1.6d;
			GlStateManager.pushMatrix();
			scale = 1.35d*Config.guiScale;
			GlStateManager.scale(scale, scale*stretchY, 1);
			String charge = TextFormatting.ITALIC+String.valueOf(MathHelper.clamp(MathHelper.floor(percent), 0, 99));
			widthString = mc.fontRenderer.getStringWidth(charge);
			mc.fontRenderer.drawString(charge, -widthString/2f, -mc.fontRenderer.FONT_HEIGHT/2, 0xE0FFFFFF, true);
			GlStateManager.popMatrix();

			// %
			GlStateManager.pushMatrix();
			scale2 = 0.70d*Config.guiScale;
			GlStateManager.scale(scale2, scale2*stretchY, 1);
			mc.fontRenderer.drawString(TextFormatting.GOLD+""+TextFormatting.ITALIC+"%", (float) (widthString*(scale-scale2)/2f+9), -1, 0xE0FFFFFF, true);
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}

}