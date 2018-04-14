package twopiradians.minewatch.client.render.entity;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.client.attachment.Attachment;
import twopiradians.minewatch.client.render.EntityOBJModel;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityMeiCrystal;

public class RenderMeiCrystal extends EntityOBJModel<EntityMeiCrystal> { 

	public RenderMeiCrystal(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {new ResourceLocation(Minewatch.MODID, "entity/mei_crystal.obj")};
	}

	@Override
	protected boolean preRender(EntityMeiCrystal entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks, @Nullable Attachment att) {
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.scale(1.33f, 1.33f, 1.33f);
		
		return true;
	}
}
