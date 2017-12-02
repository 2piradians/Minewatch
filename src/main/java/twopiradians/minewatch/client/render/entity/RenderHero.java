package twopiradians.minewatch.client.render.entity;

import java.util.Random;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.hero.EntityHero;

@SideOnly(Side.CLIENT)
public class RenderHero extends RenderLivingBase<EntityHero> {

	public RenderHero(RenderManager manager) {
		super(manager, new ModelBiped(), 0.5f);
		this.addLayer(new LayerBipedArmor(this));
		this.addLayer(new LayerHeldItem(this));
		this.addLayer(new LayerHeroArrow(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHero entity) {
		return null;
	}
	
	@Override
    protected boolean canRenderName(EntityHero entity) {
        return super.canRenderName(entity) && (entity.getAlwaysRenderNameTagForRender() || entity.hasCustomName() && entity == this.renderManager.pointedEntity);
    }

	@Override
	public void doRender(EntityHero entity, double x, double y, double z, float entityYaw, float partialTicks) {		
		// set arm poses
		ModelBiped model = (ModelBiped) this.getMainModel();
		ItemStack itemstack = entity.getHeldItemMainhand();
		ItemStack itemstack1 = entity.getHeldItemOffhand();
		model.isSneak = entity.isSneaking();
		ModelBiped.ArmPose mainPose = ModelBiped.ArmPose.EMPTY;
		ModelBiped.ArmPose offPose = ModelBiped.ArmPose.EMPTY;

		if (itemstack != null) {
			mainPose = ModelBiped.ArmPose.ITEM;

			if (entity.getItemInUseCount() > 0) {
				EnumAction enumaction = itemstack.getItemUseAction();

				if (enumaction == EnumAction.BLOCK)
					mainPose = ModelBiped.ArmPose.BLOCK;
				else if (enumaction == EnumAction.BOW)
					mainPose = ModelBiped.ArmPose.BOW_AND_ARROW;
			}
		}

		if (itemstack1 != null) {
			offPose = ModelBiped.ArmPose.ITEM;

			if (entity.getItemInUseCount() > 0) {
				EnumAction enumaction1 = itemstack1.getItemUseAction();

				if (enumaction1 == EnumAction.BLOCK)
					offPose = ModelBiped.ArmPose.BLOCK;
				// FORGE: fix MC-88356 allow offhand to use bow and arrow animation
				else if (enumaction1 == EnumAction.BOW)
					offPose = ModelBiped.ArmPose.BOW_AND_ARROW;
			}
		}

		if (entity.getPrimaryHand() == EnumHandSide.RIGHT) {
			model.rightArmPose = mainPose;
			model.leftArmPose = offPose;
		}
		else {
			model.rightArmPose = offPose;
			model.leftArmPose = mainPose;
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
	// copied from LayerArrow with standard item lighting removed to prevent lighting bugs
	public static class LayerHeroArrow extends LayerArrow
	{
	    private final RenderLivingBase<?> renderer;

	    public LayerHeroArrow(RenderLivingBase<?> rendererIn)
	    {
	       super(rendererIn);
	       this.renderer = rendererIn;
	    }

	    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	    {
	    	 int i = entitylivingbaseIn.getArrowCountInEntity();

	        if (i > 0)
	        {
	            Entity entity = new EntityTippedArrow(entitylivingbaseIn.worldObj, entitylivingbaseIn.posX, entitylivingbaseIn.posY, entitylivingbaseIn.posZ);
	            Random random = new Random((long)entitylivingbaseIn.getEntityId());
	           // RenderHelper.disableStandardItemLighting(); these cause lighting bugs

	            for (int j = 0; j < i; ++j)
	            {
	                GlStateManager.pushMatrix();
	                ModelRenderer modelrenderer = this.renderer.getMainModel().getRandomModelBox(random);
	                ModelBox modelbox = (ModelBox)modelrenderer.cubeList.get(random.nextInt(modelrenderer.cubeList.size()));
	                modelrenderer.postRender(0.0625F);
	                float f = random.nextFloat();
	                float f1 = random.nextFloat();
	                float f2 = random.nextFloat();
	                float f3 = (modelbox.posX1 + (modelbox.posX2 - modelbox.posX1) * f) / 16.0F;
	                float f4 = (modelbox.posY1 + (modelbox.posY2 - modelbox.posY1) * f1) / 16.0F;
	                float f5 = (modelbox.posZ1 + (modelbox.posZ2 - modelbox.posZ1) * f2) / 16.0F;
	                GlStateManager.translate(f3, f4, f5);
	                f = f * 2.0F - 1.0F;
	                f1 = f1 * 2.0F - 1.0F;
	                f2 = f2 * 2.0F - 1.0F;
	                f = f * -1.0F;
	                f1 = f1 * -1.0F;
	                f2 = f2 * -1.0F;
	                float f6 = MathHelper.sqrt_float(f * f + f2 * f2);
	                entity.rotationYaw = (float)(Math.atan2((double)f, (double)f2) * (180D / Math.PI));
	                entity.rotationPitch = (float)(Math.atan2((double)f1, (double)f6) * (180D / Math.PI));
	                entity.prevRotationYaw = entity.rotationYaw;
	                entity.prevRotationPitch = entity.rotationPitch;
	                this.renderer.getRenderManager().doRenderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);
	                GlStateManager.popMatrix();
	            }

	            //RenderHelper.enableStandardItemLighting(); these cause lighting bugs
	        }
	    }
	}

}