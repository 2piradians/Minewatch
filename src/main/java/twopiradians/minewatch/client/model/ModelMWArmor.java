package twopiradians.minewatch.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.Profile;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.display.EntityGuiPlayer;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

@SideOnly(Side.CLIENT)
public class ModelMWArmor extends ModelPlayer {

	private boolean renderingEnchantment;
	public EntityEquipmentSlot slot;

	public ModelMWArmor(float modelSize, boolean smallArmsIn) {
		super(modelSize, smallArmsIn);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		boolean preventColoring = true;
		// TEST coloring / preRenderArmor
		if (entityIn instanceof EntityLivingBase) {
			EntityLivingBase entity = (EntityLivingBase) entityIn;
			EnumHero hero = entity.getHeldItemMainhand() != null && entity.getHeldItemMainhand().getItem() instanceof ItemMWWeapon ? 
					((ItemMWWeapon)entity.getHeldItemMainhand().getItem()).hero : null;//SetManager.getWornSet(entityIn);
			preventColoring = hero != null && hero.weapon.preRenderArmor((EntityLivingBase) entityIn, this);
			// only do more coloring if preRenderArmor returns false or hero is null
			if (!preventColoring) {
				// frozen coloring
				if (TickHandler.hasHandler(entity, Identifier.POTION_FROZEN) || 
						(entity != null && entity.getActivePotionEffect(ModPotions.frozen) != null && 
						entity.getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) {
					int freeze = TickHandler.getHandler(entity, Identifier.POTION_FROZEN) != null ? 
							TickHandler.getHandler(entity, Identifier.POTION_FROZEN).ticksLeft : 30;
							entity.maxHurtTime = -1;
							entity.hurtTime = -1;
							GlStateManager.color(1f-freeze/30f, 1f-freeze/120f, 1f);
				}
				// hurt coloring
				else if (entity.hurtTime > 0 || entity.deathTime > 0)
					GlStateManager.color(1, 0.6f, 0.6f);
			}
		}
		
		// don't render when invisible
		if (entityIn.isInvisible()) {
			if (entityIn instanceof EntityLivingBase)
				((EntityLivingBase)entityIn).setArrowCountInEntity(0);
			return;
		}
		
		boolean renderOutline = !preventColoring && Config.renderOutlines && 
				!(entityIn instanceof EntityGuiPlayer) && !(entityIn instanceof EntityArmorStand) && 
				!this.renderingEnchantment && !entityIn.isGlowing() && 
				EntityHelper.shouldTarget(entityIn, Minewatch.proxy.getClientPlayer(), false); 

		for (int i= renderOutline ? 0 : 1; i<2; ++i) {
			GlStateManager.pushMatrix();
			
			// render outline
			if (i==0) {
				GlStateManager.enableColorMaterial();
				GlStateManager.enableOutlineMode(0xCD200F);
				float outlineScale = 1.04f;
				GlStateManager.scale(outlineScale, outlineScale, outlineScale);
				scale = 0.0625f/outlineScale;
				GlStateManager.depthMask(false);
			}
			// render normal
			else {
				GlStateManager.depthMask(true);
				scale = 0.0625f; // default
			}
			
			if (!this.renderingEnchantment) // renders black if used while rendering enchanted armor
				GlStateManager.enableBlendProfile(Profile.PLAYER_SKIN);

			super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

			if (!this.renderingEnchantment)
				GlStateManager.disableBlendProfile(Profile.PLAYER_SKIN);

			if (i == 0)
				GlStateManager.disableOutlineMode();
			GlStateManager.popMatrix();
		}

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
		/*if (entityIn instanceof EntityLivingBase) {
			EntityLivingBase entity = (EntityLivingBase) entityIn;
			EnumHero hero = SetManager.getWornSet(entityIn);
			this.preventColoring = hero.weapon.preRenderArmor((EntityLivingBase) entityIn, this);
			// only do more coloring if preRenderArmor returns false or hero is null
			if (hero == null || !preventColoring) {
				// frozen coloring
				if (TickHandler.hasHandler(entity, Identifier.POTION_FROZEN) || 
						(entity != null && entity.getActivePotionEffect(ModPotions.frozen) != null && 
						entity.getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) {
					int freeze = TickHandler.getHandler(entity, Identifier.POTION_FROZEN) != null ? 
							TickHandler.getHandler(entity, Identifier.POTION_FROZEN).ticksLeft : 30;
							entity.maxHurtTime = -1;
							entity.hurtTime = -1;
							GlStateManager.color(1f-freeze/30f, 1f-freeze/120f, 1f);
				}
				// hurt coloring
				else if (entity.hurtTime > 0 || entity.deathTime > 0)
					GlStateManager.color(1, 0.6f, 0.6f);
			}

		}*/
	}

}