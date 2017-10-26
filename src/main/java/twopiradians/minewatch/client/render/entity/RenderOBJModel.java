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
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;
import twopiradians.minewatch.common.entity.EntityWidowmakerMine;

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
	protected abstract boolean preRender(T entity, int model, VertexBuffer buffer, double x, double y, double z, float entityYaw, float partialTicks);
	protected IModel retexture(int i, IModel model) {return model;}

	/**Adapted from ForgeBlockModelRenderer#render*/
	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {	
		if (this.lighter == null) {
			this.lighter = new VertexLighterSmoothAo(Minecraft.getMinecraft().getBlockColors());
			this.bakedModels = new IBakedModel[this.getEntityModels().length];
			for (int i=0; i<this.getEntityModels().length; ++i) {
				IModel model = ModelLoaderRegistry.getModelOrLogError(this.getEntityModels()[i], "Minewatch is missing a model. Please report this to the mod authors.");
				model = this.retexture(i, model);
				this.bakedModels[i] = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
			}
		}

		for (int i=0; i<this.bakedModels.length; ++i) {
			RenderHelper.disableStandardItemLighting();
			GlStateManager.pushMatrix();
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.disableBlend();
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

			GlStateManager.rotate(180, 0, 0, 1);
			GlStateManager.translate((float)-x, (float)-y, (float)z);
			GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);
			if (this.preRender(entity, i, buffer, x, y, z, entityYaw, partialTicks)) {
				GlStateManager.rotate(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);

				lighter.setParent(new VertexBufferConsumer(buffer));
				lighter.setWorld(entity.world);
				lighter.setState(Blocks.AIR.getDefaultState());
				BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
				if (entity instanceof EntityWidowmakerMine && ((EntityWidowmakerMine)entity).facing != null) {
					pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
					double adjustZ = ((EntityWidowmakerMine)entity).facing == EnumFacing.SOUTH ? -0.5d : 0;
					double adjustX = ((EntityWidowmakerMine)entity).facing == EnumFacing.EAST ? -0.5d : 0;
					pos = pos.add(adjustX, 0, adjustZ).offset(((EntityWidowmakerMine)entity).facing.getOpposite());
				}
				lighter.setBlockPos(pos);

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
			}
			buffer.setTranslation(0, 0, 0);
			tessellator.draw();	

			GlStateManager.popMatrix();
			RenderHelper.enableStandardItemLighting();
		}
	}

}