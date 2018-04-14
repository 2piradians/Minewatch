package twopiradians.minewatch.client.render.entity;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.client.attachment.Attachment;
import twopiradians.minewatch.client.render.EntityOBJModel;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityJunkratGrenade;

public class RenderJunkratGrenade extends EntityOBJModel<EntityJunkratGrenade> {

	public RenderJunkratGrenade(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {new ResourceLocation(Minewatch.MODID, "entity/junkrat_grenade.obj")};
	}
	
	@Override
	protected boolean preRender(EntityJunkratGrenade entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks, @Nullable Attachment att) {
		GlStateManager.translate(0, -0.06d, 0);
		GlStateManager.scale(2, 2, 2);	
		return true;
	}
}
