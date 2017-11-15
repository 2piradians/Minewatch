package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.hero.EntityHero;

@SideOnly(Side.CLIENT)
public class RenderHero extends RenderLiving<EntityHero> {

	public RenderHero(RenderManager manager) {
		super(manager, new ModelPlayer(0, false), 0.5f);
		this.addLayer(new LayerBipedArmor(this));
		this.addLayer(new LayerHeldItem(this));
		this.addLayer(new LayerArrow(this));
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityHero entity) {
		return null;
	}
	
	@Override
	public void doRender(EntityHero entity, double x, double y, double z, float entityYaw, float partialTicks) {		
		// set arm poses
		ModelPlayer model = (ModelPlayer) this.getMainModel();
		ItemStack itemstack = entity.getHeldItemMainhand();
        ItemStack itemstack1 = entity.getHeldItemOffhand();
        model.isSneak = entity.isSneaking();
        ModelBiped.ArmPose mainPose = ModelBiped.ArmPose.EMPTY;
        ModelBiped.ArmPose offPose = ModelBiped.ArmPose.EMPTY;
		
		if (!itemstack.isEmpty()) {
            mainPose = ModelBiped.ArmPose.ITEM;

            if (entity.getItemInUseCount() > 0) {
                EnumAction enumaction = itemstack.getItemUseAction();

                if (enumaction == EnumAction.BLOCK)
                    mainPose = ModelBiped.ArmPose.BLOCK;
                else if (enumaction == EnumAction.BOW)
                    mainPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
        }

        if (!itemstack1.isEmpty()) {
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

}