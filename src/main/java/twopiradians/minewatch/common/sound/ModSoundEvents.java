package twopiradians.minewatch.common.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.minewatch.common.Minewatch;

public class ModSoundEvents {

	public static SoundEvent reaperShotgun;
	public static SoundEvent hanzoBowShoot;
	public static SoundEvent hanzoBowDraw;
	public static SoundEvent reinhardtRocketHammer;
	public static SoundEvent genjiShuriken;
	public static SoundEvent tracerPistol;
	public static SoundEvent mccreeGun;
	public static SoundEvent soldierGun;

	public static void preInit() {
		reaperShotgun = registerSound("reaper_shotguns");
		hanzoBowShoot = registerSound("hanzo_bow_shoot");
		hanzoBowDraw = registerSound("hanzo_bow_draw");
		reinhardtRocketHammer = registerSound("reinhardt_rocket_hammer");
		genjiShuriken = registerSound("genji_shuriken");
		tracerPistol = registerSound("tracer_pistol");
		mccreeGun = registerSound("mccree_gun");
		soldierGun = registerSound("soldier_gun");
	}
	
	private static SoundEvent registerSound(String soundName) {
		ResourceLocation loc = new ResourceLocation(Minewatch.MODID, soundName);
		SoundEvent sound = new SoundEvent(loc);
		GameRegistry.register(sound, loc);
		return sound;
	}
}