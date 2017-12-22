package twopiradians.minewatch.client.render.entity;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import twopiradians.minewatch.client.ClientProxy;
import twopiradians.minewatch.client.model.BakedMWItem;
import twopiradians.minewatch.common.entity.ability.EntityJunkratMine;
import twopiradians.minewatch.common.entity.ability.EntityReinhardtStrike;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerMine;

public abstract class RenderOBJModel<T extends Entity> extends Render<T> {

	// Note: Make sure to register new textures in ClientProxy#stitchEventPre
	private IBakedModel[] bakedModels;

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
	protected int getColor(int i, T entity) {return -1;}

	/**Adapted from ForgeBlockModelRenderer#render*/
	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {	
		if (this.bakedModels == null) {
			this.bakedModels = new IBakedModel[this.getEntityModels().length];
			for (int i=0; i<this.getEntityModels().length; ++i) {
				IModel model = ModelLoaderRegistry.getModelOrLogError(this.getEntityModels()[i], "Minewatch is missing a model. Please report this to the mod authors.");
				model = this.retexture(i, model);
				IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
				if (bakedModel instanceof OBJBakedModel && model instanceof OBJModel)
					this.bakedModels[i] = new BakedMWItem((OBJModel) model, ((OBJBakedModel) bakedModel).getState(), DefaultVertexFormats.ITEM, ClientProxy.getTextures((OBJModel) model));
			}
		}

		for (int i=0; i<this.bakedModels.length; ++i) {
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableRescaleNormal();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.pushMatrix();

			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

			GlStateManager.rotate(180, 0, 0, 1);
			GlStateManager.translate((float)-x, (float)-y, (float)z);
			GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);
			if (this.preRender(entity, i, buffer, x, y, z, entityYaw, partialTicks)) {
				int color = this.getColor(i, entity);
				GlStateManager.rotate(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);

				BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
				EnumFacing facing = entity instanceof EntityWidowmakerMine ? ((EntityWidowmakerMine)entity).facing :
					entity instanceof EntityJunkratMine ? ((EntityJunkratMine)entity).facing : null;
					if (facing != null) {
						pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
						double adjustZ = facing == EnumFacing.SOUTH ? -0.5d : 0;
						double adjustX = facing == EnumFacing.EAST ? -0.5d : 0;
						pos = pos.add(adjustX, 0, adjustZ).offset(facing.getOpposite());
					}
					else if (entity instanceof EntityReinhardtStrike)
						pos = pos.add(0, 255, 0);

					for(EnumFacing side : EnumFacing.values()) {
						List<BakedQuad> quads = this.bakedModels[i].getQuads(null, side, 0);
						if(!quads.isEmpty()) 
							for(BakedQuad quad : quads)
								LightUtil.renderQuadColor(buffer, quad, color == -1 ? color : color | -16777216);
					}
					List<BakedQuad> quads = this.bakedModels[i].getQuads(null, null, 0);
					if(!quads.isEmpty()) {
						for(BakedQuad quad : quads) 
							LightUtil.renderQuadColor(buffer, quad, color == -1 ? color : color | -16777216);
					}
			}
			buffer.setTranslation(0, 0, 0);
			tessellator.draw();	

			GlStateManager.cullFace(GlStateManager.CullFace.BACK);
			GlStateManager.popMatrix();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableBlend();
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}
	}

}