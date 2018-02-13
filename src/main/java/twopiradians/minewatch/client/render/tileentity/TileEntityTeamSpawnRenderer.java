package twopiradians.minewatch.client.render.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;

public class TileEntityTeamSpawnRenderer extends TileEntitySpecialRenderer<TileEntityTeamSpawn> {

	public TileEntityTeamSpawnRenderer() {
		super();
	}

	@Override
	public void renderTileEntityAt(TileEntityTeamSpawn te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
/*
		Entity entity = Minewatch.proxy.getRenderViewEntity();
 // Render <team> spawn text inside?
		if (entity != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			float width = (float) MathHelper.clamp(15-entity.getDistanceSqToCenter(te.getPos())*0.158f, 1, 10);
			System.out.println(width);
			GlStateManager.glLineWidth(width);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(true);
			GlStateManager.enableDepth();

			double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
			double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
			double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
			RenderGlobal.drawSelectionBoundingBox(te.getRenderBoundingBox().expandXyz(0.001d).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 1F);        

			//GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.glLineWidth(2.0F);
			GlStateManager.popMatrix();
		}*/
	}

	@Override
	public void renderTileEntityFast(TileEntityTeamSpawn te, double x, double y, double z, float partialTicks, int destroyStage, net.minecraft.client.renderer.VertexBuffer buffer) {
		//if (te.getPos() == TileEntityTeamSpawn.teamSpawnPositions.toArray()[0])
		//GlStateManager.colorMask(false, false, false, false);
	}

}