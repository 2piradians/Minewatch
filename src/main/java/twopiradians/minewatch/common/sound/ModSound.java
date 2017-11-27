package twopiradians.minewatch.common.sound;

import java.util.ArrayList;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.minewatch.common.Minewatch;

public class ModSound {

	public static ArrayList<ModSound> EVENTS = new ArrayList<ModSound>();
	
	private final SoundEvent event;
	private final ResourceLocation loc;
	
	public ModSound(String soundName) {
		loc = new ResourceLocation(Minewatch.MODID, soundName);
		event = new SoundEvent(loc);
		EVENTS.add(this);
	}

	public void register() {
		GameRegistry.register(event, loc);		
	}

}
