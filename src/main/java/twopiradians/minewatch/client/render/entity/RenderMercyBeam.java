package twopiradians.minewatch.client.render.entity;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import twopiradians.minewatch.client.particle.ParticleCustom;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMercyBeam;

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
		if (entity.player == null || (!Minewatch.keys.rmb(entity.player) && !Minewatch.keys.lmb(entity.player) && entity.player == Minecraft.getMinecraft().player))
			return;

		Color color = entity.isHealing() ? COLOR_HEAL : COLOR_DAMAGE;
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(BEAM_TEXTURE);
		GlStateManager.pushAttrib();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 1);

		int k = entity.player.getPrimaryHand() == EnumHandSide.RIGHT ? 1 : -1;
		float f7 = entity.player.getSwingProgress(partialTicks);
		float f8 = MathHelper.sin(MathHelper.sqrt(f7) * (float)Math.PI);

		float f10 = this.renderManager.options.fovSetting;
		f10 = f10 / 100.0F;
		double multiplier = -0.1D - entity.player.rotationPitch*0.001d;
		if (entity.player == Minecraft.getMinecraft().player)
			multiplier -= Minecraft.getMinecraft().player.getFovModifier()*0.1f;
		Vec3d vec3d = new Vec3d((double)k * multiplier * (double)f10, 0.03D * (double)f10, 0.4D);
		float pitch = -(entity.player.prevRotationPitch + (entity.player.rotationPitch - entity.player.prevRotationPitch) * partialTicks) * 0.02F;
		if (entity.player.rotationPitch < 10)
			pitch *= 0.8F;
		vec3d = vec3d.rotatePitch(pitch);
		vec3d = vec3d.rotateYaw(-(entity.player.prevRotationYaw + (entity.player.rotationYaw - entity.player.prevRotationYaw) * partialTicks) * 0.017453292F);
		vec3d = vec3d.rotateYaw(f8 * 0.5F);
		vec3d = vec3d.rotatePitch(-f8 * 0.7F);
		double d4 = entity.player.prevPosX + (entity.player.posX - entity.player.prevPosX) * (double)partialTicks + vec3d.xCoord;
		double d5 = entity.player.prevPosY + (entity.player.posY - entity.player.prevPosY) * (double)partialTicks + vec3d.yCoord+0.1d;
		double d6 = entity.player.prevPosZ + (entity.player.posZ - entity.player.prevPosZ) * (double)partialTicks + vec3d.zCoord;
		double d7 = (double)entity.player.getEyeHeight();

		double d13 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
		double d8 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + 0.25D;
		double d9 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;
		double d10 = (double)((float)(d4 - d13));
		double d11 = (double)((float)(d5 - d8)) + d7;
		double d12 = (double)((float)(d6 - d9));
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
		double scale = entity.player == Minecraft.getMinecraft().player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 ? 0.02d : 0.05d;

		// counter viewBobbing (copied from EntityRenderer#applyBobbing)
		float offsetX = 0;
		float offsetY = 0;
		if (Minecraft.getMinecraft().gameSettings.viewBobbing && Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
			float f = player.distanceWalkedModified - player.prevDistanceWalkedModified;
			float f1 = -(player.distanceWalkedModified + f * partialTicks);
			float f2 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
			float f3 = player.prevCameraPitch + (player.cameraPitch - player.prevCameraPitch) * partialTicks;
			offsetX = MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F;
			if (player.posZ > entity.posZ)
				offsetX *= -1;
			offsetY = Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2);
			GlStateManager.translate(offsetX, offsetY, 0.0F);
			GlStateManager.rotate(MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
		}

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
		float f11 = (float)16 / 16;
		double posX = x + d10 * (double)f11;
		double posY = y + d11 * (double)(f11 * f11 + f11) * 0.5D + 0.05D;
		double posZ = z + d12 * (double)f11;
		if (entity.particleStaff == null || !entity.particleStaff.isAlive()) {
			entity.particleStaff = new ParticleCustom(EnumParticle.CIRCLE, entity.player.world, entity.player.posX+posX, entity.player.posY+posY, entity.player.posZ+posZ, 
					0, 0, 0, 0xFFFFFF, color.getRGB(), 0.95f, Integer.MAX_VALUE, 0.5f, 0.4f, 0, 0.1f);
			Minecraft.getMinecraft().effectRenderer.addEffect(entity.particleStaff);
		}
		if (entity.particleTarget == null || !entity.particleTarget.isAlive()) {
			entity.particleTarget = new ParticleCustom(EnumParticle.CIRCLE, entity.player.world, entity.player.posX+x, entity.player.posY+y, entity.player.posZ+z, 
					0, 0, 0, 0xFFFFFF, color.getRGB(), 0.8f, Integer.MAX_VALUE, 7, 5.1f, 0, 0.1f);
			Minecraft.getMinecraft().effectRenderer.addEffect(entity.particleTarget);
		}
		float rate = 30;
		float pulse = (entity.ticksExisted % rate)/(rate*2) - 0.25f;
		if (entity.ticksExisted % rate > rate/2)
			pulse *= -1;
		entity.particleStaff.oneTickToLive();
		entity.particleStaff.setRBGColorF(MathHelper.clamp(color.getRed()/255f+pulse, 0, 1),
				MathHelper.clamp(color.getGreen()/255f+pulse, 0, 1), 
				MathHelper.clamp(color.getBlue()/255f+pulse, 0, 1));
		entity.particleStaff.setPosition(entity.player.posX+posX+offsetX, entity.player.posY+posY+offsetY, entity.player.posZ+posZ);
		entity.particleTarget.oneTickToLive();
		entity.particleTarget.setRBGColorF(MathHelper.clamp(color.getRed()/255f+pulse, 0, 1),
				MathHelper.clamp(color.getGreen()/255f+pulse, 0, 1), 
				MathHelper.clamp(color.getBlue()/255f+pulse, 0, 1));
		entity.particleTarget.setPosition(entity.player.posX+x+offsetX, entity.player.posY+y+offsetY, entity.player.posZ+z);

		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		GlStateManager.popAttrib();
	}

	private void renderBeam(Entity entity, VertexBuffer vertexbuffer, double scaleX, double scaleY, double scaleZ, double x, double y, double z, double d10, double d11, double d12) {
		double pan = -(entity.ticksExisted % 100)/15d;
		for (int i1 = 0; i1 <= 15; ++i1) {	
			float f11 = (float)i1 / 16;
			double posX = x + d10 * (double)f11;
			double posY = y + d11 * (double)(f11 * f11 + f11) * 0.5D + 0.05D;
			double posZ = z + d12 * (double)f11;
			vertexbuffer.pos(posX+scaleX, posY+scaleY, posZ+scaleZ).tex(1.0, i1/15d+pan).normal(0.0F, 1.0F, 0.0F).endVertex();
			vertexbuffer.pos(posX-scaleX, posY-scaleY, posZ-scaleZ).tex(0.0, i1/15d+pan).normal(0.0F, 1.0F, 0.0F).endVertex();
			f11 = (i1+1f) / 16;
			posX = x + d10 * (double)f11;
			posY = y + d11 * (double)(f11 * f11 + f11) * 0.5D + 0.05D;
			posZ = z + d12 * (double)f11;
			vertexbuffer.pos(posX-scaleX, posY-scaleY, posZ-scaleZ).tex(0.0, pan).normal(0.0F, 1.0F, 0.0F).endVertex();
			vertexbuffer.pos(posX+scaleX, posY+scaleY, posZ+scaleZ).tex(1.0, pan).normal(0.0F, 1.0F, 0.0F).endVertex();
		}
	}
}
