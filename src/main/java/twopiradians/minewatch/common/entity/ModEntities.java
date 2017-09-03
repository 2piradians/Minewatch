package twopiradians.minewatch.common.entity;

import java.util.UUID;

import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.packet.SPacketSyncSpawningEntity;

public class ModEntities {
	
	public static UUID spawningEntityUUID;
	public static SPacketSyncSpawningEntity spawningEntityPacket;
	
	public static void registerEntities() {
		int id = 0;
		EntityRegistry.registerModEntity(EntityReaperBullet.class, "reaper_pellet", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityHanzoSonicArrow.class, "hanzo_sonic_arrow", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityHanzoScatterArrow.class, "hanzo_scatter_arrow", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityGenjiShuriken.class, "genji_shuriken", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityTracerBullet.class, "tracer_bullet", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityMcCreeBullet.class, "mccree_bullet", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntitySoldier76Bullet.class, "soldier76_bullet", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntitySoldier76HelixRocket.class, "soldier76_helix_rocket", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityBastionBullet.class, "bastion_bullet", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityMeiBlast.class, "mei_blast", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityMeiIcicle.class, "mei_icicle", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityWidowmakerBullet.class, "widowmaker_bullet", id++, Minewatch.instance, 64, 20, true);
	}
}
