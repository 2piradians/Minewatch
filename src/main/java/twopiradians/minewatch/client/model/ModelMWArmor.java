package twopiradians.minewatch.client.model;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
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
		
		EnumHero hero = ItemMWArmor.SetManager.entitiesWearingSets.get(entityIn.getPersistentID());
		if (hero != null && entityIn instanceof EntityLivingBase)
			hero.weapon.preRenderArmor((EntityLivingBase) entityIn, this);

		super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.disableBlend();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.popMatrix();
	}

}