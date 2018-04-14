package twopiradians.minewatch.client.render.attachments;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.client.attachment.Attachment;
import twopiradians.minewatch.client.render.EntityOBJModel;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.EntityHelper;

public class RenderSombraUltimateDome extends EntityOBJModel<EntityLivingBase> {

	public RenderSombraUltimateDome(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "attachment/sombra_ultimate_dome.obj")
		};
	}

	@Override
	protected int getColor(int i, EntityLivingBase entity) {
		return EntityHelper.shouldHit(entity, Minecraft.getMinecraft().getRenderViewEntity(), false) ? 0xFF6666 : 0xDC89FE;
	}

	@Override
	protected boolean preRender(EntityLivingBase entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks, @Nullable Attachment att) {	
		GlStateManager.translate(0, entity.height/2f, 0);
		GlStateManager.color(1, 1, 1, 0.5f);
		double percent = att.ticksExisted/40d;
		double scale = 0d + Math.sin(percent)*41d;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.disableLighting();
		return true;
	}
}
