package twopiradians.minewatch.client.render.entity;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.client.attachment.Attachment;
import twopiradians.minewatch.client.render.EntityOBJModel;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityRoadhogHook;

public class RenderRoadhogHook extends EntityOBJModel<EntityRoadhogHook> {

	public RenderRoadhogHook(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/roadhog_hook.obj")
		};
	}

	@Override
	protected boolean preRender(EntityRoadhogHook entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks, @Nullable Attachment att) {	
		GlStateManager.translate(0, -entity.height/2f, 0);
		GlStateManager.rotate(90, 1, 0, 0);

		return true;
	}

	@Override
	public void doRender(EntityRoadhogHook entity, double x, double y, double z, float entityYaw, float partialTicks) {	
		super.doRender(entity, x, y, z, entityYaw, partialTicks);

	}
}
