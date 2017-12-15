package twopiradians.minewatch.client.render.tileentity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;

public class TileEntityHealthPackRenderer extends TileEntityOBJRenderer {

	public TileEntityHealthPackRenderer() {

	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "block/health_pack.obj")
		};
	}

	@Override
	protected boolean preRender(TileEntity te, int model, VertexBuffer buffer, double x, double y, double z, float partialTicks) {
		GlStateManager.translate(0.5d, 0, 0.5d);
		GlStateManager.scale(0.6d, 0.6d, 0.6d);
		return true;
	}
	
}