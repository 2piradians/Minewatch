package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityMcCreeStun;

public class RenderMcCreeStun extends RenderOBJModel<EntityMcCreeStun> {

	public RenderMcCreeStun(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/mccree_stun.obj")
		};
	}

	@Override
	protected boolean preRender(EntityMcCreeStun entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks) {	
		GlStateManager.translate(0, -entity.height/2f, 0);
		
		return true;
	}
}
