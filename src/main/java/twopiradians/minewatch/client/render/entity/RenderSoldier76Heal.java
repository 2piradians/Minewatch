package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntitySoldier76Heal;

public class RenderSoldier76Heal extends RenderOBJModel<EntitySoldier76Heal> {

	public RenderSoldier76Heal(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/soldier76_heal.obj")
		};
	}

	@Override
	protected boolean preRender(EntitySoldier76Heal entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks) {	
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.rotate(entity.ticksExisted*3f, 0, 1, 0);
		
		return true;
	}
}
