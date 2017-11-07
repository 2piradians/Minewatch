package twopiradians.minewatch.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;

@SideOnly(Side.CLIENT)
public class ModelMWArmor extends ModelPlayer {

	public ModelMWArmor(float modelSize, boolean smallArmsIn) {
		super(modelSize, smallArmsIn);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);

		super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.disableBlend();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setLivingAnimations(EntityLivingBase entityIn, float limbSwing, float limbSwingAmount, float partialTickTime) {
		if (((RenderLivingBase)Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entityIn)).getMainModel() instanceof ModelBiped) {
			ModelBiped model = (ModelBiped) ((RenderLivingBase)Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entityIn)).getMainModel();
			model.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTickTime);
			copyModelAngles(model.bipedHead, this.bipedHead);
			copyModelAngles(this.bipedHead, this.bipedHeadwear);
			copyModelAngles(model.bipedBody, this.bipedBody);
			copyModelAngles(this.bipedBody, this.bipedBodyWear);
			copyModelAngles(model.bipedLeftArm, this.bipedLeftArm);
			copyModelAngles(model.bipedRightArm, this.bipedRightArm);
			copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
			copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
			copyModelAngles(model.bipedLeftLeg, this.bipedLeftLeg);
			copyModelAngles(model.bipedRightLeg, this.bipedRightLeg);
			copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear);
			copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear);
		}
    }

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		EnumHero hero = ItemMWArmor.SetManager.entitiesWearingSets.get(entityIn.getPersistentID());
		if (hero != null && entityIn instanceof EntityLivingBase)
			hero.weapon.preRenderArmor((EntityLivingBase) entityIn, this);
	}

}