package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityJunkratGrenade;
import twopiradians.minewatch.common.entity.ModEntities;

public class RenderJunkratGrenade extends RenderOBJModel<EntityJunkratGrenade> {

	public RenderJunkratGrenade(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	protected ResourceLocation getEntityModel() {
		return new ResourceLocation(Minewatch.MODID, "entity/junkrat_grenade.obj");
	}
	
	@Override
	protected void preRender(EntityJunkratGrenade entity, VertexBuffer buffer, double x, double y, double z, float entityYaw, float partialTicks) {
		if (entity.ticksExisted == 0 && entity.getPersistentID().equals(ModEntities.spawningEntityUUID)) 
			entity.updateFromPacket();
		
		GlStateManager.translate(0, -0.06d, 0);
		GlStateManager.scale(2, 2, 2);	
	}
}
