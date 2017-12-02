package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityMeiCrystal;

public class RenderMeiCrystal extends RenderOBJModel<EntityMeiCrystal> { 

	public RenderMeiCrystal(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {new ResourceLocation(Minewatch.MODID, "entity/mei_crystal.obj")};
	}

	@Override
	protected boolean preRender(EntityMeiCrystal entity, int model, VertexBuffer buffer, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.scale(1.33f, 1.33f, 1.33f);
		
		return true;
	}
}
