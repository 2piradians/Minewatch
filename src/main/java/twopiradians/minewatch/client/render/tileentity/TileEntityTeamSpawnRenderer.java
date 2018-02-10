package twopiradians.minewatch.client.render.tileentity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.block.ModBlocks;
import twopiradians.minewatch.common.tileentity.TileEntityHealthPack;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;

public class TileEntityTeamSpawnRenderer extends TileEntitySpecialRenderer<TileEntityTeamSpawn> {

	public TileEntityTeamSpawnRenderer() {
		super();
	}

	@Override
	public void renderTileEntityAt(TileEntityTeamSpawn te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
		//GlStateManager.colorMask(false, false, false, false);
	}

	@Override
	public void renderTileEntityFast(TileEntityTeamSpawn te, double x, double y, double z, float partialTicks, int destroyStage, net.minecraft.client.renderer.VertexBuffer buffer) {
		//if (te.getPos() == TileEntityTeamSpawn.teamSpawnPositions.toArray()[0])
		//GlStateManager.colorMask(false, false, false, false);
	}

}