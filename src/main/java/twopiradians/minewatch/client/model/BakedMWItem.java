package twopiradians.minewatch.client.model;

import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import twopiradians.minewatch.client.render.tileentity.TileEntityOBJRenderer;
import twopiradians.minewatch.common.item.IChangingModel;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class BakedMWItem extends OBJBakedModel {

	public ItemStack stack;
	public EntityLivingBase entity;
	@Nullable
	private TextureAtlasSprite particleTexture;

	public BakedMWItem(OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures) {
		model.super(model, state, format, textures);
		// set health pack particle texture to base texture
		if (state instanceof TileEntityOBJRenderer.OBJModelState && textures.containsKey("None"))
			this.particleTexture = textures.get("None");
	}
	
	@Override
    public TextureAtlasSprite getParticleTexture() {
        return this.particleTexture != null ? this.particleTexture : super.getParticleTexture();
    }

	@Override
	public ItemOverrideList getOverrides() {
		return BakedMWItemOverrideHandler.INSTANCE;
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {			
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
	
		Pair<? extends IBakedModel, Matrix4f> ret = IPerspectiveAwareModel.MapWrapper.handlePerspective(this, this.getState(), type);

		if (stack != null && stack.getItem() instanceof ItemMWWeapon)
			ret = ((ItemMWWeapon)stack.getItem()).preRenderWeapon(entity, stack, type, ret);
		
		// don't render when invisible
		if (entity != null && entity.isInvisible() && (type == TransformType.FIRST_PERSON_LEFT_HAND || 
				type == TransformType.FIRST_PERSON_RIGHT_HAND || type == TransformType.THIRD_PERSON_LEFT_HAND ||
				type == TransformType.THIRD_PERSON_RIGHT_HAND))
			ret.getRight().setScale(0);
		
		return ret;
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState blockState, EnumFacing side, long rand) {
		// set tint index for quads
		List<BakedQuad> ret = super.getQuads(blockState, side, rand);
				for (BakedQuad quad : ret)
					if (!quad.hasTintIndex() && stack != null && (!(stack.getItem() instanceof IChangingModel) ||
					((IChangingModel)stack.getItem()).shouldRecolor(this, quad))) 
						ReflectionHelper.setPrivateValue(BakedQuad.class, quad, 1, 1); // PORT double check the index

				return ret;
	}

	private static class BakedMWItemOverrideHandler extends ItemOverrideList {

		public static final BakedMWItemOverrideHandler INSTANCE = new BakedMWItemOverrideHandler();

		private BakedMWItemOverrideHandler() {
			super(ImmutableList.<ItemOverride>of());
		}

		/**Called every tick*/
		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World worldObj, EntityLivingBase entity) {
			// save stack and entity to BakedMWItem so it's available in handlePerspective
			IBakedModel model = super.handleItemState(originalModel, stack, worldObj, entity);
			if (model instanceof BakedMWItem) {
				((BakedMWItem)model).stack = stack;
				((BakedMWItem)model).entity = entity;
			}
			return model;
		}
	}

}