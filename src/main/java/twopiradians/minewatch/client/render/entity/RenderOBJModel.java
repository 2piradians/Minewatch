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
import net.minecraft.client.renderer.texture.TextureMap;
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

	// Note: Make sure to register new textures in ClientProxy#stitchEventPre
	private IBakedModel[] bakedModels;
	private VertexLighterFlat lighter;

	protected RenderOBJModel(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity) {
		return null;
	}

	protected abstract ResourceLocation[] getEntityModels();
	protected abstract void preRender(T entity, int model, VertexBuffer buffer, double x, double y, double z, float entityYaw, float partialTicks);

	/**Adapted from ForgeBlockModelRenderer#render*/
	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {	
		if (this.lighter == null) {
			this.lighter = new VertexLighterFlat(Minecraft.getMinecraft().getBlockColors());
			this.bakedModels = new IBakedModel[this.getEntityModels().length];
			for (int i=0; i<this.getEntityModels().length; ++i) {
				IModel model = ModelLoaderRegistry.getModelOrLogError(this.getEntityModels()[i], "Minewatch is missing a model. Please report this to the mod authors.");
				this.bakedModels[i] = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
			}
		}
		
		for (int i=0; i<this.bakedModels.length; ++i) {
			RenderHelper.disableStandardItemLighting();
			GlStateManager.pushMatrix();
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

			GlStateManager.rotate(180, 0, 0, 1);
			GlStateManager.translate((float)-x, (float)-y, (float)z);
			GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);
			this.preRender(entity, i, buffer, x, y, z, entityYaw, partialTicks);
			GlStateManager.rotate(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);

			lighter.setParent(new VertexBufferConsumer(buffer));
			lighter.setWorld(entity.world);
			lighter.setState(Blocks.AIR.getDefaultState());
			lighter.setBlockPos(new BlockPos(entity.posX, entity.posY, entity.posZ));

			boolean empty = true;
			List<BakedQuad> quads = this.bakedModels[i].getQuads(null, null, 0);
			if(!quads.isEmpty()) {
				lighter.updateBlockInfo();
				empty = false;
				for(BakedQuad quad : quads)
					quad.pipe(lighter);
			}
			for(EnumFacing side : EnumFacing.values()) {
				quads = this.bakedModels[i].getQuads(null, side, 0);
				if(!quads.isEmpty()) {
					if(empty) 
						lighter.updateBlockInfo();
					empty = false;
					for(BakedQuad quad : quads)
						quad.pipe(lighter);
				}
			}

			buffer.setTranslation(0, 0, 0);
			tessellator.draw();	
			GlStateManager.popMatrix();
			RenderHelper.enableStandardItemLighting();
		}
	}

}