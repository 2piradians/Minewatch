package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;

public class RenderHanzoArrow extends RenderArrow {

	public RenderHanzoArrow(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return new ResourceLocation(Minewatch.MODID, "textures/entity/hanzo_arrow.png");
	}
	
}
