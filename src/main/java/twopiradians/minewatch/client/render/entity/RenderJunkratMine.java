package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityJunkratMine;

public class RenderJunkratMine extends RenderOBJModel<EntityJunkratMine> {

	public RenderJunkratMine(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/junkrat_mine.obj")
		};
	}

	@Override
	protected boolean preRender(EntityJunkratMine entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks) {	
		// rotate / position based on attached block
		if (entity.facing != null) {
			Vec3i vec = entity.facing.getDirectionVec();
			if (entity.facing == EnumFacing.DOWN) 
				GlStateManager.rotate(180, 0, 0, 1);
			else {
				if (entity.facing == EnumFacing.UP)
					GlStateManager.translate(0, -0.2f, 0);
				GlStateManager.translate(0, -0.3f, 0);
				GlStateManager.rotate(-90, vec.getZ(), vec.getY(), vec.getX());
			}
		}		

		if (!entity.onGround) // rotate while thrown
			GlStateManager.rotate((float) Math.sqrt(entity.motionX*entity.motionX+entity.motionY*entity.motionY+entity.motionZ*entity.motionZ)*5000f, 1, 0, 0);

		return true;
	}
}
