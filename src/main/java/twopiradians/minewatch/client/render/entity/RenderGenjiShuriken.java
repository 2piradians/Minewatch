package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.item.ModItems;

public class RenderGenjiShuriken extends RenderSimple<EntityGenjiShuriken> {
	
	private final RenderItem itemRenderer;

	public RenderGenjiShuriken(RenderManager manager) {
		super(manager, null, "", 0, 0, 0);
		this.itemRenderer = Minecraft.getMinecraft().getRenderItem();
	}

	public ItemStack getStackToRender(EntityGenjiShuriken entityIn) {
		return new ItemStack(ModItems.genji_shuriken_single);
	}

	@Override
	public void doRender(EntityGenjiShuriken entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y+.05f, (float)z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(0.5d, 0.5d, 0.5d);
        GlStateManager.rotate(entity.ticksExisted*10, 0.2F, 0.0F, 1.0F);
        GlStateManager.rotate(entityYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90, 1, 0, 0);

        this.itemRenderer.renderItem(this.getStackToRender(entity), ItemCameraTransforms.TransformType.GROUND);

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
	}
}