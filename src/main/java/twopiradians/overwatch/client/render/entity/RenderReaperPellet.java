package twopiradians.overwatch.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.overwatch.client.model.ModelReaperPellet;
import twopiradians.overwatch.common.Overwatch;
import twopiradians.overwatch.common.entity.EntityReaperPellet;

public class RenderReaperPellet extends Render<EntityReaperPellet>
{
	private final ModelReaperPellet REAPER_PELLET_MODEL = new ModelReaperPellet();
	
	public RenderReaperPellet(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityReaperPellet entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x-0.05f, (float)y, (float)z-0.05f);
		GlStateManager.scale(0.1F, 0.1F, 0.1F);
		this.bindEntityTexture(entity);
		this.REAPER_PELLET_MODEL.render(entity, 0, 0, 0, 0, 0, 1f);
		GlStateManager.popMatrix();
		
		super.doRender((EntityReaperPellet)entity, x, y, z, entityYaw, partialTicks);
    }

	@Override
	protected ResourceLocation getEntityTexture(EntityReaperPellet entity) {
		return new ResourceLocation(Overwatch.MODID, "textures/entity/reaper_pellet.png");
	}
}
