package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMWThrowable;

public class RenderInvisible extends Render<EntityMWThrowable> {
		
	public RenderInvisible(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMWThrowable entity) {
		return new ResourceLocation(Minewatch.MODID, "");
	}
	
	@Override
	public void doRender(EntityMWThrowable entity, double x, double y, double z, float entityYaw, float partialTicks) {}
}
