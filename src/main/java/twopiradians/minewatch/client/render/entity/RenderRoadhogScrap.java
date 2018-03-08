package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityRoadhogScrap;

public class RenderRoadhogScrap extends RenderOBJModel<EntityRoadhogScrap> {

	public RenderRoadhogScrap(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/roadhog_scrap.obj")
		};
	}

	@Override
	protected boolean preRender(EntityRoadhogScrap entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks) {	
		GlStateManager.translate(0, -entity.height/2f, 0);
		GlStateManager.rotate(-entity.ticksExisted*25, 1, 0, 0);

		return true;
	}

	@Override
	public void doRender(EntityRoadhogScrap entity, double x, double y, double z, float entityYaw, float partialTicks) {	
		super.doRender(entity, x, y, z, entityYaw, partialTicks);

	}
}
