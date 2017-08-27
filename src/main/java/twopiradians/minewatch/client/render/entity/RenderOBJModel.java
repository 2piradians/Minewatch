package twopiradians.minewatch.client.render.entity;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;

public abstract class RenderOBJModel<T extends Entity> extends Render<T> {

	private IBakedModel bakedModel;
	private VertexLighterFlat lighter;

	public RenderOBJModel(RenderManager renderManager) {
		super(renderManager);
		this.lighter = new VertexLighterFlat(Minecraft.getMinecraft().getBlockColors());
		IModel model = ModelLoaderRegistry.getModelOrMissing(this.getEntityModel());
		this.bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
	}

	@Override
	protected abstract ResourceLocation getEntityTexture(T entity);
	protected abstract ResourceLocation getEntityModel();

	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.bindEntityTexture(entity);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.rotate(180, 0, 0, 1);
		GlStateManager.translate((float)-x, (float)-y, (float)z);
		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, -1.0F, 0.0F);
	    GlStateManager.rotate(entity.rotationPitch, 1.0F, 0.0F, 0.0F);
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer VertexBuffer = tessellator.getBuffer();
		VertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		VertexBuffer.setTranslation(0, -0.05d, 0);

		lighter.setParent(new VertexBufferConsumer(VertexBuffer));
		lighter.setWorld(entity.world);
		lighter.setState(Blocks.AIR.getDefaultState());
		lighter.setBlockPos(new BlockPos(entity.posX, 255, entity.posZ));
		boolean empty = true;
		List<BakedQuad> quads = bakedModel.getQuads(null, null, 0);
		if(!quads.isEmpty()) {
			lighter.updateBlockInfo();
			empty = false;
			for(BakedQuad quad : quads)
				quad.pipe(lighter);
		}
		for(EnumFacing side : EnumFacing.values()) {
			quads = bakedModel.getQuads(null, side, 0);
			if(!quads.isEmpty()) {
				if(empty) 
					lighter.updateBlockInfo();
				empty = false;
				for(BakedQuad quad : quads)
					quad.pipe(lighter);
			}
		}

		VertexBuffer.setTranslation(0, 0, 0);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
	}
}
