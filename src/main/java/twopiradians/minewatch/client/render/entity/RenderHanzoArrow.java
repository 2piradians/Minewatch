package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.packet.SPacketSyncSpawningEntity;

public class RenderHanzoArrow extends RenderArrow {

	public RenderHanzoArrow(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return new ResourceLocation(Minewatch.MODID, "textures/entity/hanzo_arrow.png");
	}
	
	@Override
    public void doRender(EntityArrow entity, double x, double y, double z, float entityYaw, float partialTicks) {
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
			entity.posX = packet.posX;
			entity.posY = packet.posY;
			entity.posZ = packet.posZ;
			ModEntities.spawningEntities.remove(entity.getPersistentID());
		}
		
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
}
