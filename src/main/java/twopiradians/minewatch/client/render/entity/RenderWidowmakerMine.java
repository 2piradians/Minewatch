package twopiradians.minewatch.client.render.entity;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelProcessingHelper;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityWidowmakerMine;
import twopiradians.minewatch.common.util.EntityHelper;

public class RenderWidowmakerMine extends RenderOBJModel<EntityWidowmakerMine> {

	public RenderWidowmakerMine(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/widowmaker_mine_bottom.obj"),
				new ResourceLocation(Minewatch.MODID, "entity/widowmaker_mine_top.obj"), // blue
				new ResourceLocation(Minewatch.MODID, "entity/widowmaker_mine_top.obj") // red
		};
	}

	@Override
	protected IModel retexture(int i, IModel model) {
		if (i == 2) {
			HashMap<String, String> map = Maps.newHashMap();
			map.put("#material", new ResourceLocation(Minewatch.MODID, "entity/widowmaker_mine_red").toString());
			return ModelProcessingHelper.retexture(model, ImmutableMap.copyOf(map));
		}
		else
			return model;
	}

	@Override
	protected boolean preRender(EntityWidowmakerMine entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks) {		
		// rotate / position based on attached block
		if (entity.facing != null) {
			Vec3i vec = entity.facing.getDirectionVec();
			if (entity.facing == EnumFacing.DOWN) 
				GlStateManager.rotate(180, 0, 0, 1);
			else {
				if (entity.facing == EnumFacing.UP)
					GlStateManager.translate(0, -0.2f, 0);
				GlStateManager.translate(0, -0.2f, 0);
				GlStateManager.rotate(-90, vec.getZ(), vec.getY(), vec.getX());
			}
		}

		// rotate while thrown
		if (!entity.onGround)
			GlStateManager.rotate((float) Math.sqrt(entity.motionX*entity.motionX+entity.motionY*entity.motionY+entity.motionZ*entity.motionZ)*1000f, 1, 0, 0);
		else if (model == 1 || model == 2) {
			GlStateManager.translate(0, 0.02d, 0);
			GlStateManager.rotate((entity.ticksExisted+partialTicks)*2, 0, 1, 0);
		}
		return (model == 0 && entity.onGround) || (model == 1 && !EntityHelper.shouldTarget(entity, null, false)) ||
				(model == 2 && EntityHelper.shouldTarget(entity, null, false));
	}
}
