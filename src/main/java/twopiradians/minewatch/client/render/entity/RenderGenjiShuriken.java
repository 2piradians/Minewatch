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
import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.packet.SPacketSyncSpawningEntity;

public class RenderGenjiShuriken extends Render<EntityGenjiShuriken>
{	
	private final RenderItem itemRenderer;

	public RenderGenjiShuriken(RenderManager renderManager) {
		super(renderManager);
		this.itemRenderer = Minecraft.getMinecraft().getRenderItem();
	}

	public ItemStack getStackToRender(EntityGenjiShuriken entityIn) {
		return new ItemStack(ModItems.genji_shuriken_single);
	}

	protected ResourceLocation getEntityTexture(EntityGenjiShuriken entity) {
		return new ResourceLocation(Minewatch.MODID, "textures/entity/genji_shuriken.png");   
	}

	@Override
	public void doRender(EntityGenjiShuriken entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// correct trajectories of fast entities
		if (ModEntities.spawningEntities.containsKey(entity.getPersistentID())) {
			SPacketSyncSpawningEntity packet = ModEntities.spawningEntities.get(entity.getPersistentID());
			entity.rotationPitch = packet.pitch;
			entity.prevRotationPitch = packet.pitch;
			entity.rotationYaw = packet.yaw;
			entity.prevRotationYaw = packet.yaw;
			entity.motionX = packet.motionX;
			entity.motionY = packet.motionY;
			entity.motionZ = packet.motionZ;
			ModEntities.spawningEntities.remove(entity.getPersistentID());
		}
		
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