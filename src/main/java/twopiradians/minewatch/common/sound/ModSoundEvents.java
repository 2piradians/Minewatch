package twopiradians.minewatch.common.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.minewatch.common.Minewatch;

public class ModSoundEvents {

	public static final SoundEvent REAPER_SHOTGUN = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_shotguns")).setRegistryName("reaper_shotguns");
	public static final SoundEvent HANZO_BOW_SHOOT = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hanzo_bow_shoot")).setRegistryName("hanzo_bow_shoot");
	public static final SoundEvent HANZO_BOW_DRAW = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hanzo_bow_draw")).setRegistryName("hanzo_bow_draw");
	public static final SoundEvent REINHARDT_ROCKET_HAMMER = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reinhardt_rocket_hammer")).setRegistryName("reinhardt_rocket_hammer");
	public static final SoundEvent GENJI_SHURIKEN = new SoundEvent(new ResourceLocation(Minewatch.MODID, "genji_shuriken")).setRegistryName("genji_shuriken");
	public static final SoundEvent TRACER_PISTOL = new SoundEvent(new ResourceLocation(Minewatch.MODID, "tracer_pistol")).setRegistryName("tracer_pistol");
	public static final SoundEvent MCCREE_GUN = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mccree_gun")).setRegistryName("mccree_gun");
	public static final SoundEvent SOLDIER_GUN = new SoundEvent(new ResourceLocation(Minewatch.MODID, "soldier_gun")).setRegistryName("soldier_gun");

	
	@Mod.EventBusSubscriber
	public static class RegistrationHandler {

		@SubscribeEvent
		public static void registerSounds(final RegistryEvent.Register<SoundEvent> event) {	
			event.getRegistry().register(REAPER_SHOTGUN);
			event.getRegistry().register(HANZO_BOW_SHOOT);
			event.getRegistry().register(HANZO_BOW_DRAW);
			event.getRegistry().register(REINHARDT_ROCKET_HAMMER);
			event.getRegistry().register(GENJI_SHURIKEN);
			event.getRegistry().register(TRACER_PISTOL);
			event.getRegistry().register(MCCREE_GUN);
			event.getRegistry().register(SOLDIER_GUN);

		}
	}
}