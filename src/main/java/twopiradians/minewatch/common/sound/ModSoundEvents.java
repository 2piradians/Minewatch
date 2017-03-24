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

	public static void preInit() {
		reaperShotgun = registerSound("reaper_shotguns");
		hanzoBowShoot = registerSound("hanzo_bow_shoot");
		hanzoBowDraw = registerSound("hanzo_bow_draw");
		reinhardtRocketHammer = registerSound("reinhardt_rocket_hammer");
	}
	
	private static SoundEvent registerSound(String soundName) {
		ResourceLocation loc = new ResourceLocation(Minewatch.MODID, soundName);
		SoundEvent sound = new SoundEvent(loc);
		GameRegistry.register(sound, loc);
		return sound;
	}
}