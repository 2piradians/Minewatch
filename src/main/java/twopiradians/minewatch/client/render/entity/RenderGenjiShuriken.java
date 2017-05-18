package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.item.ModItems;

public class RenderGenjiShuriken extends Render<EntityGenjiShuriken>
{	
	private final RenderItem itemRenderer;

	public RenderGenjiShuriken(RenderManager renderManager) {
		super(renderManager);
		this.itemRenderer = Minecraft.getMinecraft().getRenderItem();
	}

	public ItemStack getStackToRender(EntityGenjiShuriken entityIn) {
		return new ItemStack(ModItems.genji_shuriken);
	}

	protected ResourceLocation getEntityTexture(EntityGenjiShuriken entity) {
		return new ResourceLocation(Minewatch.MODID, "textures/entity/genji_shuriken.png");   
	}

	@Override
	public void doRender(EntityGenjiShuriken entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y+.05f, (float)z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(0.5d, 0.5d, 0.5d);
        GlStateManager.rotate(entity.ticksExisted*60, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90, 1, 0, 0);

        this.itemRenderer.renderItem(this.getStackToRender(entity), ItemCameraTransforms.TransformType.GROUND);

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
	}
}