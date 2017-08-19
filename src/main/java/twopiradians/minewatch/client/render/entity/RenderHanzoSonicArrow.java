package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;

public class RenderHanzoSonicArrow extends RenderHanzoArrow {

	public RenderHanzoSonicArrow(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return new ResourceLocation(Minewatch.MODID, "textures/entity/hanzo_sonic_arrow.png");
	}
	
}
