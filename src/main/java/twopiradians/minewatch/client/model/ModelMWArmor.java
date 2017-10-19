package twopiradians.minewatch.client.model;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

@SideOnly(Side.CLIENT)
public class ModelMWArmor extends ModelPlayer {

	public ModelMWArmor(float modelSize, boolean smallArmsIn) {
		super(modelSize, smallArmsIn);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
		Handler handler = null;
		EntityLivingBase entityLiving = entityIn instanceof EntityLivingBase ? (EntityLivingBase) entityIn : null;
		// Reaper's wraith
		if (TickHandler.hasHandler(entityIn, Identifier.REAPER_WRAITH)) { 
			handler = TickHandler.getHandler(entityIn, Identifier.REAPER_WRAITH);
			float delay = 10;
			float color = handler.ticksLeft > (60-delay) ? 1f-(1f-(handler.ticksLeft-60+delay)/delay)*0.6f : 
				handler.ticksLeft < delay ? 1f-handler.ticksLeft/delay*0.6f : 0.4f;
			GlStateManager.color(color-0.1f, color-0.1f, color-0.1f, color);
		}
		// Reaper's teleport
		else if (TickHandler.hasHandler(entityIn, Identifier.REAPER_TELEPORT)) { 
			handler = TickHandler.getHandler(entityIn, Identifier.REAPER_TELEPORT);
			float delay = 10;
			float color = (handler.ticksLeft > (40-delay) && handler.ticksLeft < (40+delay)) ? 
					Math.abs((handler.ticksLeft-40)/(delay*2f)) : 1f;
					GlStateManager.color(color, color, color, color);
		}
		// Mei's frozen coloring
		else if (TickHandler.hasHandler(entityIn, Identifier.POTION_FROZEN) || 
				(entityLiving != null && entityLiving.getActivePotionEffect(ModPotions.frozen) != null && 
				entityLiving.getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) {
			int freeze = TickHandler.getHandler(entityIn, Identifier.POTION_FROZEN) != null ? 
					TickHandler.getHandler(entityIn, Identifier.POTION_FROZEN).ticksLeft : 30;
					entityLiving.maxHurtTime = -1;
					entityLiving.hurtTime = -1;
					GlStateManager.color(1f-freeze/30f, 1f-freeze/120f, 1f);
		}

		super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.disableBlend();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.popMatrix();
	}

}