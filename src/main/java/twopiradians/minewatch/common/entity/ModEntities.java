package twopiradians.minewatch.common.entity;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.packet.PacketSyncSpawningEntity;

public class ModEntities {
	
	public static HashMap<UUID, PacketSyncSpawningEntity> spawningEntities = Maps.newHashMap();
	
	public static void registerEntities() {
		int id = 0;
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "reaper_pellet"), EntityReaperBullet.class, "reaper_pellet", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 64, 1, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoSonicArrow.class, "hanzo_sonic_arrow", id++, Minewatch.instance, 64, 1, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoScatterArrow.class, "hanzo_scatter_arrow", id++, Minewatch.instance, 64, 1, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "ana_bullet"), EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 32, 20, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "genji_shuriken"), EntityGenjiShuriken.class, "genji_shuriken", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "tracer_bullet"), EntityTracerBullet.class, "tracer_bullet", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mccree_bullet"), EntityMcCreeBullet.class, "mccree_bullet", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "soldier_bullet"), EntitySoldier76Bullet.class, "soldier_bullet", id++, Minewatch.instance, 64, 1, true);
	}
}
