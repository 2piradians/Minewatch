package twopiradians.minewatch.common.entity;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;

public class ModEntities {

	public static void registerEntities() {
		int id = 0;
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "reaper_bullet"), EntityReaperBullet.class, "reaper_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoSonicArrow.class, "hanzo_sonic_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoScatterArrow.class, "hanzo_scatter_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "ana_bullet"), EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "ana_sleep_dart"), EntityAnaSleepDart.class, "ana_sleep_dart", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "genji_shuriken"), EntityGenjiShuriken.class, "genji_shuriken", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "tracer_bullet"), EntityTracerBullet.class, "tracer_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mccree_bullet"), EntityMcCreeBullet.class, "mccree_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "soldier76_bullet"), EntitySoldier76Bullet.class, "soldier76_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "soldier76_helix_rocket"), EntitySoldier76HelixRocket.class, "soldier76_helix_rocket", id++, Minewatch.instance, 64, 200, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "bastion_bullet"), EntityBastionBullet.class, "bastion_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mei_blast"), EntityMeiBlast.class, "mei_blast", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mei_icicle"), EntityMeiIcicle.class, "mei_icicle", id++, Minewatch.instance, 64, Integer.MAX_VALUE, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "widowmaker_bullet"), EntityWidowmakerBullet.class, "widowmaker_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "widowmaker_mine"), EntityWidowmakerMine.class, "widowmaker_mine", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mercy_bullet"), EntityMercyBullet.class, "mercy_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mercy_beam"), EntityMercyBeam.class, "mercy_beam", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "junkrat_grenade"), EntityJunkratGrenade.class, "junkrat_grenade", id++, Minewatch.instance, 64, 30, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "junkrat_trap"), EntityJunkratTrap.class, "junkrat_trap", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "junkrat_mine"), EntityJunkratMine.class, "junkrat_mine", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "sombra_bullet"), EntitySombraBullet.class, "sombra_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "sombra_translocator"), EntitySombraTranslocator.class, "sombra_translocator", id++, Minewatch.instance, 64, 20, true);
	}
}
