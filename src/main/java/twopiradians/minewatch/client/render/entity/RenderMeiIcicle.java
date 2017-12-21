package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityMeiIcicle;

public class RenderMeiIcicle extends RenderOBJModel<EntityMeiIcicle> { 

	public RenderMeiIcicle(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {new ResourceLocation(Minewatch.MODID, "entity/mei_icicle.obj")};
	}

	@Override
	protected boolean preRender(EntityMeiIcicle entity, int model, VertexBuffer buffer, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.scale(2, 2, 2);		
		buffer.setTranslation(0, -0.02d, 0);
		
		return true;
	}
}
