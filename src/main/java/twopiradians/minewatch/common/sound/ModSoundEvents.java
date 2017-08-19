package twopiradians.minewatch.common.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

public class ModSoundEvents {

	public static SoundEvent anaShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ana_shoot")).setRegistryName("ana_shoot");
	public static SoundEvent anaReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ana_reload")).setRegistryName("ana_reload");
	public static SoundEvent reaperShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_shoot")).setRegistryName("reaper_shoot");
	public static SoundEvent reaperReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_reload")).setRegistryName("reaper_reload");
	public static SoundEvent hanzoShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hanzo_shoot")).setRegistryName("hanzo_shoot");
	public static SoundEvent hanzoDraw = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hanzo_draw")).setRegistryName("hanzo_draw");
	public static SoundEvent reinhardtWeapon = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reinhardt_rocket_hammer")).setRegistryName("reinhardt_rocket_hammer");
	public static SoundEvent genjiShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "genji_shoot")).setRegistryName("genji_shoot");
	public static SoundEvent genjiReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "genji_reload")).setRegistryName("genji_reload");
	public static SoundEvent tracerShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "tracer_shoot")).setRegistryName("tracer_shoot");
	public static SoundEvent tracerReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "tracer_reload")).setRegistryName("tracer_reload");
	public static SoundEvent mccreeShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mccree_shoot")).setRegistryName("mccree_shoot");
	public static SoundEvent mccreeReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mccree_reload")).setRegistryName("mccree_reload");
	public static SoundEvent soldier76Shoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "soldier76_shoot")).setRegistryName("soldier76_shoot");
	public static SoundEvent soldier76Reload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "soldier76_reload")).setRegistryName("soldier76_reload");
	public static SoundEvent hurt = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hurt")).setRegistryName("hurt");
	public static SoundEvent anaHeal = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ana_heal")).setRegistryName("ana_heal");
	public static SoundEvent hanzoSonicArrow = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hanzo_sonic_arrow")).setRegistryName("hanzo_sonic_arrow");
	public static SoundEvent hanzoScatterArrow = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hanzo_scatter_arrow")).setRegistryName("hanzo_scatter_arrow");
	public static SoundEvent soldier76Helix = new SoundEvent(new ResourceLocation(Minewatch.MODID, "soldier76_helix")).setRegistryName("soldier76_helix");
	public static SoundEvent bastionShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "bastion_shoot")).setRegistryName("bastion_shoot");
	public static SoundEvent bastionReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "bastion_reload_0")).setRegistryName("bastion_reload_0");
	public static SoundEvent bastionTurretReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "bastion_reload_1")).setRegistryName("bastion_reload_1");
	public static SoundEvent meiShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_shoot_0")).setRegistryName("mei_shoot_0");
	public static SoundEvent meiIcicleShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_shoot_1")).setRegistryName("mei_shoot_1");
	public static SoundEvent meiFreeze = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_freeze")).setRegistryName("mei_freeze");
	public static SoundEvent meiUnfreeze = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_unfreeze")).setRegistryName("mei_unfreeze");
	public static SoundEvent meiReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_reload")).setRegistryName("mei_reload");

	public static void postInit() {
		EnumHero.ANA.reloadSound = anaReload;
		EnumHero.REAPER.reloadSound = reaperReload;
		EnumHero.GENJI.reloadSound = genjiReload;
		EnumHero.TRACER.reloadSound = tracerReload;
		EnumHero.MCCREE.reloadSound = mccreeReload;
		EnumHero.SOLDIER76.reloadSound = soldier76Reload;
		EnumHero.BASTION.reloadSound = bastionReload;
		EnumHero.MEI.reloadSound = meiReload;
	}

	@Mod.EventBusSubscriber
	public static class RegistrationHandler {

		@SubscribeEvent
		public static void registerSounds(final RegistryEvent.Register<SoundEvent> event) {	
			event.getRegistry().register(anaShoot);
			event.getRegistry().register(anaReload);
			event.getRegistry().register(reaperShoot);
			event.getRegistry().register(reaperReload);
			event.getRegistry().register(hanzoShoot);
			event.getRegistry().register(hanzoDraw);
			event.getRegistry().register(reinhardtWeapon);
			event.getRegistry().register(genjiShoot);
			event.getRegistry().register(genjiReload);
			event.getRegistry().register(tracerShoot);
			event.getRegistry().register(tracerReload);
			event.getRegistry().register(mccreeShoot);
			event.getRegistry().register(mccreeReload);
			event.getRegistry().register(soldier76Shoot);
			event.getRegistry().register(soldier76Reload);

		}
	}
}