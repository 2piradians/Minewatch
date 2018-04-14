package twopiradians.minewatch.client.render.entity;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.client.attachment.Attachment;
import twopiradians.minewatch.client.render.EntityOBJModel;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityLucioSonic;
import twopiradians.minewatch.common.util.EntityHelper;

public class RenderLucioSonic extends EntityOBJModel<EntityLucioSonic> { 

	public RenderLucioSonic(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0;
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/lucio_sonic.obj")
		};
	}

	@Override
	protected int getColor(int i, EntityLucioSonic entity) {
		return EntityHelper.shouldHit(entity, Minecraft.getMinecraft().getRenderViewEntity(), false) ? 0xFF6666 : 0x93B964;
	}

	@Override
	protected boolean preRender(EntityLucioSonic entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks, @Nullable Attachment att) {
		GlStateManager.translate(0, -entity.height/2d, 0);
		GlStateManager.rotate(90, 1, 0, 0);
		double scale = 0.5d;
		if ((entity.ticksExisted+partialTicks) <= 2)
			scale *= (entity.ticksExisted+partialTicks) / 2d;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.disableCull();
		return true;
	}
}
