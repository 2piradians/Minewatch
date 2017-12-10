package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import twopiradians.minewatch.common.entity.projectile.EntityZenyattaOrb;
import twopiradians.minewatch.common.hero.EnumHero;

public class RenderZenyattaOrb extends RenderSimple<EntityZenyattaOrb> {
	
	private final RenderItem itemRenderer;

	public RenderZenyattaOrb(RenderManager manager) {
		super(manager, null, "", 0, 0, 0);
		this.itemRenderer = Minecraft.getMinecraft().getRenderItem();
	}

	public ItemStack getStackToRender(EntityZenyattaOrb entityIn) {
		return new ItemStack(EnumHero.ZENYATTA.weapon);
	}

	@Override
	public void doRender(EntityZenyattaOrb entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		GlStateManager.pushMatrix();
        GlStateManager.translate(x+0.15d, y+0.07d, z);
        
        // render white
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.SET);
		
        this.itemRenderer.renderItem(this.getStackToRender(entity), ItemCameraTransforms.TransformType.FIXED);

        GlStateManager.disableColorLogic();
        GlStateManager.popMatrix();
	}
}