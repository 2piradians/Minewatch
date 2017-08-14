package twopiradians.minewatch.common.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

public class ModSoundEvents {

	public static SoundEvent anaShoot;
	public static SoundEvent reaperShoot;
	public static SoundEvent hanzoShoot;
	public static SoundEvent hanzoDraw;
	public static SoundEvent hanzoSonicArrow;
	public static SoundEvent hanzoScatterArrow;
	public static SoundEvent reinhardtRocketHammer;
	public static SoundEvent genjiShoot;
	public static SoundEvent tracerShoot;
	public static SoundEvent mccreeShoot;
	public static SoundEvent soldier76Shoot;
	public static SoundEvent soldier76Helix;

	public static void preInit() {
		anaShoot = registerSound("ana_shoot");
		EnumHero.ANA.reloadSound = registerSound("ana_reload");
		reaperShoot = registerSound("reaper_shoot");
		EnumHero.REAPER.reloadSound = registerSound("reaper_reload");
		hanzoShoot = registerSound("hanzo_shoot");
		hanzoDraw = registerSound("hanzo_draw");
		hanzoSonicArrow = registerSound("hanzo_sonic_arrow");
		hanzoScatterArrow = registerSound("hanzo_scatter_arrow");
		reinhardtRocketHammer = registerSound("reinhardt_rocket_hammer");
		genjiShoot = registerSound("genji_shoot");
		EnumHero.GENJI.reloadSound = registerSound("genji_reload");
		tracerShoot = registerSound("tracer_shoot");
		EnumHero.TRACER.reloadSound = registerSound("tracer_reload");
		mccreeShoot = registerSound("mccree_shoot");
		EnumHero.MCCREE.reloadSound = registerSound("mccree_reload");
		soldier76Shoot = registerSound("soldier76_shoot");
		EnumHero.SOLDIER76.reloadSound = registerSound("soldier76_reload");
		soldier76Helix = registerSound("soldier76_helix");
	}
	
	private static SoundEvent registerSound(String soundName) {
		ResourceLocation loc = new ResourceLocation(Minewatch.MODID, soundName);
		SoundEvent sound = new SoundEvent(loc);
		GameRegistry.register(sound, loc);
		return sound;
	}
}