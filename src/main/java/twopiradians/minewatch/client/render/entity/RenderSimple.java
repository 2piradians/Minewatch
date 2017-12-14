package twopiradians.minewatch.client.render.entity;

import java.awt.Color;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.client.model.ModelSimple;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityJunkratMine;

public class RenderSimple<T extends Entity> extends Render<T> {

	private final ModelSimple MODEL;
	private final ResourceLocation TEXTURE;
	private final Color COLOR;

	public RenderSimple(RenderManager manager, Color color, String texture, int width, int height, int depth) {
		super(manager);
		this.COLOR = color;
		this.TEXTURE = !texture.equals("") ? new ResourceLocation(Minewatch.MODID, "textures/entity/"+texture+".png") : null;
		this.MODEL = width+height+depth > 0 ? new ModelSimple(width, height, depth) : null;
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {	
		if (entity.ticksExisted <= 2 && entity.hasNoGravity())
			return;

		if (this.MODEL != null && (entity.ticksExisted > 1 || !entity.hasNoGravity())) {
			float scale = 0.05f;
			GlStateManager.pushMatrix();
			GlStateManager.translate((float)x, (float)y + scale, (float)z);
			GlStateManager.rotate(-(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks), 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1.0F, 0.0F, 0.0F);
			this.bindEntityTexture(entity);
			if (this.COLOR != null)
				GlStateManager.color(COLOR.getRed()/255f, COLOR.getGreen()/255f, COLOR.getBlue()/255f, 1f);
			this.MODEL.render(entity, 0, 0, entity.ticksExisted, entity.getRotationYawHead(), entity.rotationPitch, scale);
			GlStateManager.popMatrix();
		}
	}

}
