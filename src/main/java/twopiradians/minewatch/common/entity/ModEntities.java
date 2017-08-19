package twopiradians.minewatch.common.entity;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.packet.PacketSyncSpawningEntity;

public class ModEntities {
	
	public static HashMap<UUID, PacketSyncSpawningEntity> spawningEntities = Maps.newHashMap();
	
	public static void registerEntities() {
		int id = 0;
		EntityRegistry.registerModEntity(EntityReaperBullet.class, "reaper_pellet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityHanzoSonicArrow.class, "hanzo_sonic_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityHanzoScatterArrow.class, "hanzo_scatter_arrow", id++, Minewatch.instance, 64, 1, false);
		EntityRegistry.registerModEntity(EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityGenjiShuriken.class, "genji_shuriken", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityTracerBullet.class, "tracer_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMcCreeBullet.class, "mccree_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntitySoldier76Bullet.class, "soldier76_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntitySoldier76HelixRocket.class, "soldier76_helix_rocket", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityBastionBullet.class, "bastion_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMeiBlast.class, "mei_blast", id++, Minewatch.instance, 64, 20, false);
	}
}
