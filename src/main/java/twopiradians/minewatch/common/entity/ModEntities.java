package twopiradians.minewatch.common.entity;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;

public class ModEntities {

	public static void registerEntities() {
		int id = 0;
		EntityRegistry.registerModEntity(EntityReaperBullet.class, "reaper_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityHanzoSonicArrow.class, "hanzo_sonic_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityHanzoScatterArrow.class, "hanzo_scatter_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityAnaSleepDart.class, "ana_sleep_dart", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityGenjiShuriken.class, "genji_shuriken", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityTracerBullet.class, "tracer_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMcCreeBullet.class, "mccree_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntitySoldier76Bullet.class, "soldier76_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntitySoldier76HelixRocket.class, "soldier76_helix_rocket", id++, Minewatch.instance, 64, 200, false);
		EntityRegistry.registerModEntity(EntityBastionBullet.class, "bastion_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMeiBlast.class, "mei_blast", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMeiIcicle.class, "mei_icicle", id++, Minewatch.instance, 64, Integer.MAX_VALUE, false);
		EntityRegistry.registerModEntity(EntityMeiCrystal.class, "mei_crystal", id++, Minewatch.instance, 64, Integer.MAX_VALUE, false);
		EntityRegistry.registerModEntity(EntityWidowmakerBullet.class, "widowmaker_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityWidowmakerMine.class, "widowmaker_mine", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityMercyBullet.class, "mercy_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMercyBeam.class, "mercy_beam", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityJunkratGrenade.class, "junkrat_grenade", id++, Minewatch.instance, 64, 30, true);
		EntityRegistry.registerModEntity(EntityJunkratTrap.class, "junkrat_trap", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityJunkratMine.class, "junkrat_mine", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntitySombraBullet.class, "sombra_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntitySombraTranslocator.class, "sombra_translocator", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityReinhardtStrike.class, "reinhardt_strike", id++, Minewatch.instance, 64, 20, false);
	}
}
