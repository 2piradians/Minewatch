package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.client.model.ModelReaperPellet;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityReaperPellet;

public class RenderReaperPellet extends Render<EntityReaperPellet>
{
	private final ModelReaperPellet REAPER_PELLET_MODEL = new ModelReaperPellet();
	
	public RenderReaperPellet(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityReaperPellet entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x-0.025f, (float)y+0.025f, (float)z-0.05f);
		GlStateManager.scale(0.1F, 0.1F, 0.1F);
		double velX = entity.motionX;
		double velY = entity.motionY;
		double velZ = entity.motionZ;
		double velocityXZ = Math.sqrt(velX*velX + velZ*velZ);
		double velocityXYZ = Math.sqrt(velX*velX + velY*velY + velZ*velZ);
		if (velZ >= 0 && velX >= 0) {
			GlStateManager.rotate((float) (Math.asin(velX/velocityXZ)*180/Math.PI), (float)0, (float)y, (float)0);
			GlStateManager.rotate((float) (-Math.asin(velY/velocityXYZ)*180/Math.PI), (float)x, (float)0, (float)0);
		}
		else if (velZ >= 0) {
			GlStateManager.rotate((float) (Math.asin(velX/velocityXZ)*180/Math.PI), (float)0, (float)y, (float)0);
			GlStateManager.rotate((float) (Math.asin(velY/velocityXYZ)*180/Math.PI), (float)x, (float)0, (float)0);
		}
		else if (velZ < 0 && velX >= 0) {
			GlStateManager.rotate((float) (-Math.asin(velX/velocityXZ)*180/Math.PI), (float)0, (float)y, (float)0);
			GlStateManager.rotate((float) (Math.asin(velY/velocityXYZ)*180/Math.PI), (float)x, (float)0, (float)0);
		}
		else {
			GlStateManager.rotate((float) (-Math.asin(velX/velocityXZ)*180/Math.PI), (float)0, (float)y, (float)0);
			GlStateManager.rotate((float) (-Math.asin(velY/velocityXYZ)*180/Math.PI), (float)x, (float)0, (float)0);
		}
		this.bindEntityTexture(entity);
		this.REAPER_PELLET_MODEL.render(entity, 0, 0, 0, 0, 0, 0.5f);
		GlStateManager.popMatrix();
		
		super.doRender((EntityReaperPellet)entity, x, y, z, entityYaw, partialTicks);
    }

	@Override
	protected ResourceLocation getEntityTexture(EntityReaperPellet entity) {
		return new ResourceLocation(Minewatch.MODID, "textures/entity/reaper_pellet.png");
	}
}
