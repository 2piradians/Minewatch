package twopiradians.minewatch.client.render.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;

public class TileEntityHealthPackRenderer extends TileEntityOBJRenderer {

	public TileEntityHealthPackRenderer() {
		super(new ResourceLocation(Minewatch.MODID, "block/health_pack.obj"));
	}

	@Override
	protected String[] getModelParts() {
		return new String[] {
				"base", "glow", "small", "large"
		};
	}

	@Override
	protected boolean preRender(TileEntity te, int model, VertexBuffer buffer, double x, double y, double z, float partialTicks) {
		GlStateManager.translate(0.5d, 0, 0.5d);
		
		// pack rotating / bouncing
		if (model == 2 || model == 3) {
			GlStateManager.translate(-0.01d, Math.sin((Minecraft.getMinecraft().player.ticksExisted+partialTicks)/5f)/10f+0.2d, -0.2d);
			GlStateManager.rotate(25, 1, 0, 0);
			GlStateManager.rotate((Minecraft.getMinecraft().player.ticksExisted+partialTicks)*4f, 0, 1, 0);
		}
		
		return model != 2 && model != 1;
	}
	
}