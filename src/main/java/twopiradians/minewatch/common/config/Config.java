package twopiradians.minewatch.common.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.minewatch.common.Minewatch;

public class Config {
	
	public static Configuration config;
	public static boolean useObjModels;
	public static int tokenDropRate;

	public static void preInit(final File file) {
		config = new Configuration(file);
		config.load();
		syncConfig();
		config.save();
	}
	
	public static void syncConfig() {
		Property prop = config.get(Configuration.CATEGORY_GENERAL, "Use 3D Item Models", true, "Should the Minewatch weapons use 3D models");
		prop.setRequiresMcRestart(true);
		useObjModels = prop.getBoolean();
		
		Property tokenDropRateProp = config.get(Configuration.CATEGORY_GENERAL, "Token Drop Rate", 100, "Average number of mobs to kill for one token.", 1, 10000);
		tokenDropRate = tokenDropRateProp.getInt();
	}

	@SubscribeEvent(receiveCanceled=true)
	public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(Minewatch.MODID)) {
			syncConfig();
			config.save();
		}
	}
}