package twopiradians.minewatch.client.render.tileentity;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.UnmodifiableIterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Group;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.common.model.IModelPart;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.Models;
import net.minecraftforge.common.model.TRSRTransformation;
import twopiradians.minewatch.client.ClientProxy;
import twopiradians.minewatch.client.model.BakedMWItem;

@SuppressWarnings("deprecation")
public abstract class TileEntityOBJRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

	// Note: Make sure to register new textures in ClientProxy#stitchEventPre
	private IBakedModel[] bakedModels;
	private ResourceLocation loc;

	protected TileEntityOBJRenderer(ResourceLocation loc) {
		super();
		this.loc = loc;
	}

	protected abstract String[] getModelParts();
	protected abstract boolean preRender(T te, int model, BufferBuilder buffer, double x, double y, double z, float partialTicks);
	protected abstract void postRender(T te, int model, BufferBuilder buffer, double x, double y, double z, float partialTicks);
	protected int getColor(int i, T entity) {return -1;}

	// PORT 1.12 render
	@Override
	public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (this.bakedModels == null) {
			this.bakedModels = new IBakedModel[this.getModelParts().length];
			IModel model = ModelLoaderRegistry.getModelOrLogError(loc, "Minewatch is missing a model. Please report this to the mod authors.");
			IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
			if (bakedModel instanceof OBJBakedModel && model instanceof OBJModel) 
				for (int i=0; i<this.getModelParts().length; ++i) 
					this.bakedModels[i] = new BakedMWItem((OBJModel) model, new OBJModelState(this.getModelParts()[i]), DefaultVertexFormats.ITEM, ClientProxy.getTextures((OBJModel) model));
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
			GlStateManager.disableCull();
			GlStateManager.enableDepth();

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

			GlStateManager.rotate(180, 0, 0, 1);
			GlStateManager.translate((float)-x, (float)-y, (float)z);
			GlStateManager.rotate(180, 0, 0, 1);
			if (this.preRender(te, i, buffer, x, y, z, partialTicks)) {
				int color = this.getColor(i, te);

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

			this.postRender(te, i, buffer, x, y, z, partialTicks); 

			GlStateManager.cullFace(GlStateManager.CullFace.BACK);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			GlStateManager.popMatrix();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableBlend();
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}
	}

	/**Model state with just the specified part name ('o partName' in .obj)*/
	public static class OBJModelState implements IModelState {

		private static final Optional<TRSRTransformation> IDENTITY = Optional.of(TRSRTransformation.identity());
		private String[] partNames = new String[0];
		@Nullable
		private IModelState state;

		public OBJModelState(String... partNames) {
			this(null, partNames);
		}

		public OBJModelState(@Nullable IModelState state, String... partNames) {
			this.state = state;
			this.partNames = partNames;
		}

		@Override
		public java.util.Optional<TRSRTransformation> apply(java.util.Optional<? extends IModelPart> part) {
			if(part.isPresent()) {
				// This whole thing is subject to change, but should do for now.
				UnmodifiableIterator<String> parts = Models.getParts(part.get());
				if(parts.hasNext()) {
					String name = parts.next();
					boolean contains = false;
					for (String partName : partNames)
						if (partName.equalsIgnoreCase(name))
							contains = true;
					// only interested in the root level
					if(!parts.hasNext()) // OBJModel.Default.Element.Name
						if (state != null && name.equals(Group.DEFAULT_NAME))
							return state.apply(part);
						else if (!contains)
							return IDENTITY;
				}
				else if (state != null)
					return state.apply(part);
			}

			return Optional.empty();
		}
	}
}