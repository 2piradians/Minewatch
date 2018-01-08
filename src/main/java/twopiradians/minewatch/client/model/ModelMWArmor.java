package twopiradians.minewatch.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.Profile;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

@SideOnly(Side.CLIENT)
public class ModelMWArmor extends ModelPlayer {

	private boolean renderingEnchantment;

	public ModelMWArmor(float modelSize, boolean smallArmsIn) {
		super(modelSize, smallArmsIn);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		GlStateManager.pushMatrix();
		
		if (!this.renderingEnchantment) // renders black if used while rendering enchanted armor
			GlStateManager.enableBlendProfile(Profile.PLAYER_SKIN);

		super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		if (!this.renderingEnchantment)
			GlStateManager.disableBlendProfile(Profile.PLAYER_SKIN);
		
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.popMatrix();

		this.renderingEnchantment = true;
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

		this.renderingEnchantment = false;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		if (entityIn instanceof EntityLivingBase) {
			EntityLivingBase entity = (EntityLivingBase) entityIn;
			EnumHero hero = SetManager.getWornSet(entityIn);
			// only do more coloring if preRenderArmor returns false or hero is null
			if (hero == null || !hero.weapon.preRenderArmor((EntityLivingBase) entityIn, this)) {
				// hurt coloring
				if (entity.hurtTime > 0 || entity.deathTime > 0)
					GlStateManager.color(1, 0.7f, 0.7f);
				// frozen coloring
				else if (TickHandler.hasHandler(entity, Identifier.POTION_FROZEN) || 
						(entity != null && entity.getActivePotionEffect(ModPotions.frozen) != null && 
						entity.getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) {
					int freeze = TickHandler.getHandler(entity, Identifier.POTION_FROZEN) != null ? 
							TickHandler.getHandler(entity, Identifier.POTION_FROZEN).ticksLeft : 30;
							entity.maxHurtTime = -1;
							entity.hurtTime = -1;
							GlStateManager.color(1f-freeze/30f, 1f-freeze/120f, 1f);
				}
			}

		}
	}

}