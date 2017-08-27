package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMeiIcicle;

public class RenderMeiIcicle extends RenderOBJModel<EntityMeiIcicle> {

	public RenderMeiIcicle(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMeiIcicle entity) {
		return new ResourceLocation(Minewatch.MODID, "textures/entity/mei_icicle.png");
	}

	@Override
	protected ResourceLocation getEntityModel() {
		return new ResourceLocation(Minewatch.MODID, "entity/mei_icicle.obj");
	}
}
