package twopiradians.minewatch.client.render.tileentity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.client.render.TileEntityOBJRenderer;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.block.ModBlocks;
import twopiradians.minewatch.common.tileentity.TileEntityHealthPack;

public class TileEntityHealthPackRenderer extends TileEntityOBJRenderer<TileEntityHealthPack> {

	public TileEntityHealthPackRenderer() {
		super(new ResourceLocation(Minewatch.MODID, "block/health_pack.obj"));
	}

	@Override
	protected String[] getModelParts() {
		return new String[] {
				"base", "glow", "small", "large", "small", "large"
		};
	}

	@Override
	protected boolean preRender(TileEntityHealthPack te, int model, BufferBuilder buffer, double x, double y, double z, float partialTicks) {
		GlStateManager.translate(0.5d, 0, 0.5d);

		boolean friendly = (!te.isHacked() || te.hackedTeam == null || 
				(te.hackedTeam == Minewatch.proxy.getRenderViewEntity().getTeam() && (!te.hackedTeam.getAllowFriendlyFire() || Minewatch.proxy.getRenderViewEntity().getPersistentID().equals(te.hacker))));

		// hacked see through walls
		if (te.isHacked() && friendly && (model == 2 || model == 3)) {
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			GlStateManager.colorMask(true, false, true, true);
			GlStateManager.disableTexture2D();
		}

		// pack rotating / bouncing
		if ((te.getBlockType() == ModBlocks.healthPackSmall && (model == 2 || model == 4)) || 
				(te.getBlockType() == ModBlocks.healthPackLarge && (model == 3 || model == 5))) {
			GlStateManager.translate(-0.01d, Math.sin((Minecraft.getMinecraft().player.ticksExisted+partialTicks)/5f)/10f+0.2d, -0.2d);
			GlStateManager.rotate(25, 1, 0, 0);
			GlStateManager.rotate((Minecraft.getMinecraft().player.ticksExisted+partialTicks)*4f, 0, 1, 0);
		}

		if (model == 1) {
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
			GlStateManager.depthMask(false);
		}
		else
			GlStateManager.depthMask(true);

		return (te.getBlockType() == ModBlocks.healthPackLarge && (model == 3 || (model == 5)) && te.getCooldown() <= 0) || 
				(te.getBlockType() == ModBlocks.healthPackSmall && (model == 2 || (model == 4)) && te.getCooldown() <= 0) || model == 0 || 
				(model == 1 && te.getCooldown() <= 0);
	}

	@Override
	protected void postRender(TileEntityHealthPack te, int model, BufferBuilder buffer, double x, double y, double z, float partialTicks) {
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.enableLighting();
		GlStateManager.colorMask(true, true, true, true);

		// respawn progress circle (modified from EntityRenderer#drawNameplate)
		if (model == 0 && (te.getCooldown() > 0 || te.isHacked())) { 
			boolean isThirdPersonFrontal = Minecraft.getMinecraft().getRenderManager().options.thirdPersonView == 2;
			float viewerYaw = Minecraft.getMinecraft().getRenderManager().playerViewY;
			float viewerPitch = Minecraft.getMinecraft().getRenderManager().playerViewX;

			float size = 11;
			float scale = 0.025f;
			double progress = 1f - (te.getCooldown() / ((float)te.getResetCooldown()));

			GlStateManager.pushMatrix();
			GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();

			// cooldown icon
			if (te.getCooldown() > 0) {
				GlStateManager.pushMatrix();
				// translate / rotate
				GlStateManager.translate(0, 0.5f, 0);
				GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate((float)(isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
				GlStateManager.scale(-scale, -scale, scale);

				// outside rings
				GlStateManager.enableTexture2D();
				this.bindTexture(new ResourceLocation(Minewatch.MODID, "textures/blocks/health_pack_recharge.png"));
				Gui.drawModalRectWithCustomSizedTexture((int) -size, (int) -size, 0, 0, (int) size*2, (int) size*2, size*2, size*2);

				size -= 3.15f;
				GlStateManager.disableTexture2D();

				// inside progress circle
				vertexbuffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				double deg_to_rad = 0.0174532925d;
				double precision = 1;
				double angle_from = 270;
				double angle_to = angle_from + progress * 360d;
				double angle_diff=angle_to-angle_from;
				double steps=Math.round(angle_diff*precision);
				double angle=angle_from;
				vertexbuffer.pos(0, 0, -0.001D).color(143/255f, 157/255f, 227/255f, 0.7F).endVertex();
				vertexbuffer.pos(0, -size, -0.001D).color(143/255f, 157/255f, 227/255f, 0.7F).endVertex();
				for (int i=1; i<=steps; i++) {
					angle=angle_from+angle_diff/steps*i;
					vertexbuffer.pos(size*Math.cos(angle*deg_to_rad), size*Math.sin(angle*deg_to_rad), -0.001D).color(143/255f, 157/255f, 227/255f, 0.7F).endVertex();
				}
				tessellator.draw();

				GlStateManager.popMatrix();
			}

			// hacked icon
			if (te.isHacked() && te.hackedIconTime <= 0) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				TextureAtlasSprite sprite = EnumParticle.SOMBRA_HACK.sprite;
				double yOffset = -15;
				boolean friendly = (!te.isHacked() || te.hackedTeam == null || 
						(te.hackedTeam == Minewatch.proxy.getRenderViewEntity().getTeam() && (!te.hackedTeam.getAllowFriendlyFire() || Minewatch.proxy.getRenderViewEntity().getPersistentID().equals(te.hacker))));
				
				GlStateManager.pushMatrix();
				GlStateManager.enableTexture2D();
				// translate / rotate
				GlStateManager.translate(0, 1.6f, 0);
				GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate((float)(isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
				GlStateManager.scale(-scale, -scale, scale);
				scale = 30;

				// background
				if (friendly)
					GlStateManager.color(0.7F, 0.5F, 0.8F, 1.0F);
				else
					GlStateManager.color(0.7F, 0.3F, 0.3F, 0.7F);
				vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				vertexbuffer.pos(-scale/2d, yOffset, 0).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
				vertexbuffer.pos(-scale/2d, yOffset+scale, 0).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
				vertexbuffer.pos(scale/2d, yOffset+scale, 0).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
				vertexbuffer.pos(scale/2d, yOffset, 0).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
				tessellator.draw();

				// progress layer
				progress = MathHelper.clamp(0.92f -(float) te.hackedTime / TileEntityHealthPack.HACK_TIME, 0, 1);
				if (friendly)
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				else
					GlStateManager.color(0.9F, 0.6F, 0.5F, 0.6F);
				vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				vertexbuffer.pos(-scale/2d, yOffset+scale*progress, -0.1D).tex(sprite.getMinU(), sprite.getMinV()+(sprite.getMaxV()-sprite.getMinV())*progress).endVertex();
				vertexbuffer.pos(-scale/2d, yOffset+scale, -0.1D).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
				vertexbuffer.pos(scale/2d, yOffset+scale, -0.1D).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
				vertexbuffer.pos(scale/2d, yOffset+scale*progress, -0.1D).tex(sprite.getMaxU(), sprite.getMinV()+(sprite.getMaxV()-sprite.getMinV())*progress).endVertex();
				tessellator.draw();

				GlStateManager.popMatrix();
			}

			GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
			GlStateManager.disableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.popMatrix();
		}
	}

}