package twopiradians.minewatch.common.entity;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.packet.SPacketSyncSpawningEntity;

public class ModEntities {
	
	public static HashMap<UUID, SPacketSyncSpawningEntity> spawningEntities = Maps.newHashMap();
	
	public static void registerEntities() {
		int id = 0;
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "reaper_pellet"), EntityReaperBullet.class, "reaper_pellet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoSonicArrow.class, "hanzo_sonic_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoScatterArrow.class, "hanzo_scatter_arrow", id++, Minewatch.instance, 64, 1, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "ana_bullet"), EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "genji_shuriken"), EntityGenjiShuriken.class, "genji_shuriken", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "tracer_bullet"), EntityTracerBullet.class, "tracer_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mccree_bullet"), EntityMcCreeBullet.class, "mccree_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "soldier76_bullet"), EntitySoldier76Bullet.class, "soldier76_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "soldier76_helix_rocket"), EntitySoldier76HelixRocket.class, "soldier76_helix_rocket", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "bastion_bullet"), EntityBastionBullet.class, "bastion_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mei_blast"), EntityMeiBlast.class, "mei_blast", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mei_icicle"), EntityMeiIcicle.class, "mei_icicle", id++, Minewatch.instance, 64, 20, false);
	}
}
