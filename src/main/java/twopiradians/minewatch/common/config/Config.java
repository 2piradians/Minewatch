package twopiradians.minewatch.common.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

public class Config {

	public static final String CATEGORY_HERO_TEXTURES = "config.heroTextures";

	public static Configuration config;
	public static boolean useObjModels;
	public static int tokenDropRate;

	public static void preInit(final File file) {
		config = new Configuration(file);
		config.load();
		config.setCategoryComment(Config.CATEGORY_HERO_TEXTURES, "Choose textures for each hero's armor. If you'd like to submit your own skin to be used as a texture, please message us!");
		syncConfig();
		config.save();
	}

	public static void syncConfig() {
		Property prop = config.get(Configuration.CATEGORY_GENERAL, "Use 3D Item Models", true, "Should the Minewatch weapons use 3D models");
		prop.setRequiresMcRestart(true);
		useObjModels = prop.getBoolean();

		Property tokenDropRateProp = config.get(Configuration.CATEGORY_GENERAL, "Token Drop Rate", 100, "Average number of mobs to kill for one token.", 1, 10000);
		tokenDropRate = tokenDropRateProp.getInt();

		for (EnumHero hero : EnumHero.values()) {
			Property heroTextureProp = config.get(Config.CATEGORY_HERO_TEXTURES, hero.name+" Texture", hero.textureCredits[0], "Textures for "+hero.name+"'s armor", hero.textureCredits);
			for (int i=0; i<hero.textureCredits.length; ++i)
				if (hero.textureCredits[i].equalsIgnoreCase(heroTextureProp.getString()))
					hero.textureVariation = i;
			if (hero.textureVariation < 0 || hero.textureVariation > hero.textureCredits.length)
				hero.textureVariation = 0;
		}
	}

	@SubscribeEvent
	public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(Minewatch.MODID)) {
			syncConfig();
			config.save();
		}
	}
}