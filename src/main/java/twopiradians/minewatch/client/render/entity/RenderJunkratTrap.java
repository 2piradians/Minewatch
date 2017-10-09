package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityJunkratTrap;

public class RenderJunkratTrap extends RenderOBJModel<EntityJunkratTrap> {

	public RenderJunkratTrap(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/junkrat_trap_top.obj"),
				new ResourceLocation(Minewatch.MODID, "entity/junkrat_trap_middle.obj"),
				new ResourceLocation(Minewatch.MODID, "entity/junkrat_trap_bottom.obj")
		};
	}

	@Override
	protected void preRender(EntityJunkratTrap entity, int model, VertexBuffer buffer, double x, double y, double z, float entityYaw, float partialTicks) {
		// rotate while thrown
		if (!entity.onGround)
			GlStateManager.rotate((float) Math.sqrt(entity.motionX*entity.motionX+entity.motionY*entity.motionY+entity.motionZ*entity.motionZ)*1000f, 1, 0, 0);
		
		float angle = entity.onGround ? entity.trappedTicks <= 7 ? entity.trappedTicks * 7.5f % 60 : 60 : 30;
		if (model == 0) {
			GlStateManager.translate(0, 0, angle / 800);
			GlStateManager.rotate(angle, 1, 0, 0);
		}
		else if (model == 2) {
			GlStateManager.translate(0, 0, -angle / 800);
			GlStateManager.rotate(angle, -1, 0, 0);
		}
	}
}
