package twopiradians.minewatch.client.render.entity;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.particle.ParticleCustom;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityMercyBeam;
import twopiradians.minewatch.common.util.EntityHelper;

public class RenderMercyBeam extends Render<EntityMercyBeam> {

	private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation(Minewatch.MODID, "textures/entity/mercy_beam.png");
	private static final Color COLOR_HEAL = new Color(0xCFC77F);
	private static final Color COLOR_DAMAGE = new Color(0x47A4E9);

	public RenderMercyBeam(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMercyBeam entity) {
		return null;
	}

	@Override
	public boolean shouldRender(EntityMercyBeam livingEntity, ICamera camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void doRender(EntityMercyBeam entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (entity.player == null || (!KeyBind.RMB.isKeyDown(entity.player) && !KeyBind.LMB.isKeyDown(entity.player) && entity.player == Minecraft.getMinecraft().player))
			return;

		Color color = entity.isHealing() ? COLOR_HEAL : COLOR_DAMAGE;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(BEAM_TEXTURE);
		GlStateManager.pushAttrib();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 1);

		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
		double scale = entity.player == Minecraft.getMinecraft().player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 ? 0.02d : 0.05d;

		Vec3d vec = EntityHelper.getShootingPos(entity.player, entity.player.rotationPitch, entity.player.rotationYaw, EnumHand.MAIN_HAND, 12.5f, 0.34f);
		double d10 = vec.x - (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks);
		double d11 = vec.y - (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks);
		double d12 = vec.z - (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks);

		// right
		this.renderBeam(entity, vertexbuffer, 0, scale, 0, x+scale, y, z, d10, d11, d12);
		this.renderBeam(entity, vertexbuffer, 0, -scale, 0, x+scale, y, z, d10, d11, d12);
		// left
		this.renderBeam(entity, vertexbuffer, 0, scale, 0, x-scale, y, z, d10, d11, d12);
		this.renderBeam(entity, vertexbuffer, 0, -scale, 0, x-scale, y, z, d10, d11, d12);
		// top
		this.renderBeam(entity, vertexbuffer, scale, 0, 0, x, y+scale, z, d10, d11, d12);
		this.renderBeam(entity, vertexbuffer, -scale, 0, 0, x, y+scale, z, d10, d11, d12);
		// bottom
		this.renderBeam(entity, vertexbuffer, scale, 0, 0, x, y-scale, z, d10, d11, d12);
		this.renderBeam(entity, vertexbuffer, -scale, 0, 0, x, y-scale, z, d10, d11, d12);
		tessellator.draw();

		// spawn/position particles
		EntityPlayer player = Minecraft.getMinecraft().player;
		double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
		double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
		double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
		if (entity.particleStaff == null || !entity.particleStaff.isAlive()) {
			entity.particleStaff = new ParticleCustom(EnumParticle.CIRCLE, entity.player.world, vec.x, vec.y+0.05d, vec.z, 
					0, 0, 0, 0xFFFFFF, color.getRGB(), 0.97f, Integer.MAX_VALUE, 1f, 0.9f, 0, 0.1f, 0, null, false);
			Minecraft.getMinecraft().effectRenderer.addEffect(entity.particleStaff);
		}
		if (entity.particleTarget == null || !entity.particleTarget.isAlive()) {
			float size = entity.target != null ? Math.min(entity.target.height, entity.target.width)*8f : 5f;
			entity.particleTarget = new ParticleCustom(EnumParticle.CIRCLE, entity.player.world, posX+x, posY+y, posZ+z, 
					0, 0, 0, 0xFFFFFF, color.getRGB(), 0.8f, Integer.MAX_VALUE, size * 1.3f, size, 0, 0.1f, 0, null, false);
			Minecraft.getMinecraft().effectRenderer.addEffect(entity.particleTarget);
		}
		float rate = 30;
		float pulse = (entity.ticksExisted % rate)/(rate*2) - 0.25f;
		if (entity.ticksExisted % rate > rate/2)
			pulse *= -1;
		entity.particleStaff.setRBGColorF(MathHelper.clamp(color.getRed()/255f+pulse, 0, 1),
				MathHelper.clamp(color.getGreen()/255f+pulse, 0, 1), 
				MathHelper.clamp(color.getBlue()/255f+pulse, 0, 1));
		entity.particleStaff.setPosition(vec.x, vec.y+0.05d, vec.z);
		entity.particleStaff.oneTickToLive();
		entity.particleTarget.setRBGColorF(MathHelper.clamp(color.getRed()/255f+pulse, 0, 1),
				MathHelper.clamp(color.getGreen()/255f+pulse, 0, 1), 
				MathHelper.clamp(color.getBlue()/255f+pulse, 0, 1));
		entity.particleTarget.setPosition(posX+x, posY+y, posZ+z);
		entity.particleTarget.oneTickToLive();

		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		GlStateManager.popAttrib();
	}

	private void renderBeam(Entity entity, BufferBuilder vertexbuffer, double scaleX, double scaleY, double scaleZ, double x, double y, double z, double d10, double d11, double d12) {
		double pan = -(entity.ticksExisted % 100)/15d;
		for (int i = 0; i <= 15; ++i) {	
			float f = (float)i / 16;
			double posX = x + d10 * (double)f;
			double posY = y + d11 * (double)(f * f + f) * 0.5D + 0.05D;
			double posZ = z + d12 * (double)f;
			vertexbuffer.pos(posX+scaleX, posY+scaleY, posZ+scaleZ).tex(1.0, i/15d+pan).normal(0.0F, 1.0F, 0.0F).endVertex();
			vertexbuffer.pos(posX-scaleX, posY-scaleY, posZ-scaleZ).tex(0.0, i/15d+pan).normal(0.0F, 1.0F, 0.0F).endVertex();
			f = (i+1f) / 16;
			posX = x + d10 * (double)f;
			posY = y + d11 * (double)(f * f + f) * 0.5D + 0.05D;
			posZ = z + d12 * (double)f;
			vertexbuffer.pos(posX-scaleX, posY-scaleY, posZ-scaleZ).tex(0.0, pan).normal(0.0F, 1.0F, 0.0F).endVertex();
			vertexbuffer.pos(posX+scaleX, posY+scaleY, posZ+scaleZ).tex(1.0, pan).normal(0.0F, 1.0F, 0.0F).endVertex();
		}
	}
}
