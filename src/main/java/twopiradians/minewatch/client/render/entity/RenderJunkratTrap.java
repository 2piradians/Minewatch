package twopiradians.minewatch.client.render.entity;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.client.attachment.Attachment;
import twopiradians.minewatch.client.render.EntityOBJModel;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityJunkratTrap;
import twopiradians.minewatch.common.util.EntityHelper;

public class RenderJunkratTrap extends EntityOBJModel<EntityJunkratTrap> {

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
	protected boolean preRender(EntityJunkratTrap entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks, @Nullable Attachment att) {		
		GlStateManager.rotate(180, 1, 0, 0);
		
		if (entity.onGround && entity.trappedTicks == 0 && 
				EntityHelper.shouldTarget(entity, Minecraft.getMinecraft().player, false))
			GlStateManager.translate(0, -0.2d, 0);
		
		// rotate while thrown
		if (!entity.onGround)
			GlStateManager.rotate((float) Math.sqrt(entity.motionX*entity.motionX+entity.motionY*entity.motionY+entity.motionZ*entity.motionZ)*1000f, 1, 0, 0);
		
		float angle = entity.onGround ? entity.trappedTicks <= 7 ? entity.trappedTicks * 7.5f % 60 : 60 : 30;
		if (model == 0) {
			GlStateManager.translate(0, 0, -angle / 800);
			GlStateManager.rotate(angle, 1, 0, 0);
		}
		else if (model == 2) {
			GlStateManager.translate(0, 0, angle / 800);
			GlStateManager.rotate(angle, -1, 0, 0);
		}
		
		return true;
	}
}
