package twopiradians.minewatch.client.render.entity;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelProcessingHelper;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityReinhardtStrike;

public class RenderReinhardtStrike extends RenderOBJModel<EntityReinhardtStrike> { 

	public RenderReinhardtStrike(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0;
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/reinhardt_strike.obj"),
				new ResourceLocation(Minewatch.MODID, "entity/reinhardt_strike.obj"),
				new ResourceLocation(Minewatch.MODID, "entity/reinhardt_strike.obj"),
				new ResourceLocation(Minewatch.MODID, "entity/reinhardt_strike.obj"),
				new ResourceLocation(Minewatch.MODID, "entity/reinhardt_strike.obj"),
				new ResourceLocation(Minewatch.MODID, "entity/reinhardt_strike.obj")
				};
	}
	
	@Override
	protected IModel retexture(int i, IModel model) {
			HashMap<String, String> map = Maps.newHashMap();
			map.put("#None", new ResourceLocation(Minewatch.MODID, "entity/reinhardt_strike_"+i).toString());
			return ModelProcessingHelper.retexture(model, ImmutableMap.copyOf(map));
	}

	@Override
	protected boolean preRender(EntityReinhardtStrike entity, int model, VertexBuffer buffer, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.translate(0, -entity.height/2f, 0);

		return entity.ticksExisted % 6 == model;
	}
}
