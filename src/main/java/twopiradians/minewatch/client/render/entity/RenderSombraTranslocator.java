package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntitySombraTranslocator;

public class RenderSombraTranslocator extends RenderOBJModel<EntitySombraTranslocator> {

	public RenderSombraTranslocator(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/sombra_translocator.obj")
		};
	}

	@Override
	protected boolean preRender(EntitySombraTranslocator entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks) {		
		GlStateManager.scale(2.5f, 2.5f, 2.5f);
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.translate(0, 0.045f, 0);

		// rotate while thrown
		if (!entity.onGround)
			GlStateManager.rotate((float) Math.sqrt(entity.motionX*entity.motionX+entity.motionY*entity.motionY+entity.motionZ*entity.motionZ)*1000f, 1, 0, 0);
		
		return true;
	}
}
