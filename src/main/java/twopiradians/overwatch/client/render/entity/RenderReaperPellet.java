package twopiradians.overwatch.client.render.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import twopiradians.overwatch.common.Overwatch;
import twopiradians.overwatch.common.entity.EntityReaperPellet;

public class RenderReaperPellet extends Render
{
	public RenderReaperPellet(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return new ResourceLocation(Overwatch.MODID, "textures/entity/reaper_pellet.png");
	}
	
	public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        this.bindEntityTexture(entity);
    	super.doRender((EntityReaperPellet)entity, x, y, z, entityYaw, partialTicks);
    }
}
