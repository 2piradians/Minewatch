package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityJunkratGrenade;

public class RenderJunkratGrenade extends RenderOBJModel<EntityJunkratGrenade> {

	public RenderJunkratGrenade(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {new ResourceLocation(Minewatch.MODID, "entity/junkrat_grenade.obj")};
	}
	
	@Override
	protected void preRender(EntityJunkratGrenade entity, int model, VertexBuffer buffer, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.translate(0, -0.06d, 0);
		GlStateManager.scale(2, 2, 2);	
	}
}
