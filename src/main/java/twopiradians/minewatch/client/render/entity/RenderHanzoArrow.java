package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;

public class RenderHanzoArrow extends RenderArrow {
	
	private final ResourceLocation TEXTURE;
	
	public RenderHanzoArrow(RenderManager manager, String texture) {
		super(manager);
		this.TEXTURE = new ResourceLocation(Minewatch.MODID, "textures/entity/"+texture+".png");
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TEXTURE;
	}
	
}