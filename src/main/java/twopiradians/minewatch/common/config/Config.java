package twopiradians.minewatch.common.config;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.packet.CPacketSyncSkins;

public class Config {

	public static final String CATEGORY_HERO_TEXTURES = "config.heroTextures";

	private static final String[] DURABILITY_OPTIONS = new String[] {"Normally", "When not wearing full set", "Never"};

	public static Configuration config;
	public static boolean useObjModels;
	public static int tokenDropRate;
	public static boolean allowGunWarnings;
	public static boolean customCrosshairs;
	public static double guiScale;
	public static int durabilityOptionArmors;
	public static int durabilityOptionWeapons;

	public static void preInit(final File file) {
		config = new Configuration(file);
		config.load();
		config.setCategoryComment(Config.CATEGORY_HERO_TEXTURES, "Choose textures for each hero's armor. If you'd like to submit your own skin to be used as a texture, please message us!");
		syncConfig();
		config.save();
	}

	public static void syncConfig() {
		Property use3DModelsprop = config.get(Configuration.CATEGORY_GENERAL, "Use 3D Item Models", true, "Should the Minewatch weapons use 3D models?");
		use3DModelsprop.setRequiresMcRestart(true);
		useObjModels = use3DModelsprop.getBoolean();

		Property allowGunWarningsProp = config.get(Configuration.CATEGORY_GENERAL, "Restrict weapon usage", true, "Should weapons only work like in Overwatch: only in the mainhand (with offhand weapons in the offhand). This also prevents weapons from different heroes from being mixed and matched.");
		allowGunWarnings = allowGunWarningsProp.getBoolean();

		Property customCrosshairsProp = config.get(Configuration.CATEGORY_GENERAL, "Custom Crosshairs", true, "Should weapons change your crosshair.");
		customCrosshairs = customCrosshairsProp.getBoolean();

		Property tokenDropRateProp = config.get(Configuration.CATEGORY_GENERAL, "Token Drop Rate", 100, "Average number of mobs to kill for one token.", 1, 10000);
		tokenDropRate = tokenDropRateProp.getInt();

		Property damageScaleProp = config.get(Configuration.CATEGORY_GENERAL, "Damage Scale", 1d, "1 is the recommended scale for vanilla. A higher scale means weapons do more damage and a lower scale means they do less.", 0, 100);
		ItemMWWeapon.damageScale = (float) (0.1d * damageScaleProp.getDouble());

		Property guiScaleProp = config.get(Configuration.CATEGORY_GENERAL, "Gui Scale", 1d, "Scale for the hero and weapon GUI/overlays.", 0, 2);
		Config.guiScale = guiScaleProp.getDouble();

		Property durabilityArmorsProp = config.get(Configuration.CATEGORY_GENERAL, "Armors use durability", DURABILITY_OPTIONS[0], "Choose when armors should use durability.", DURABILITY_OPTIONS);
		for (int i=0; i<DURABILITY_OPTIONS.length; ++i)
			if (durabilityArmorsProp.getString().equals(DURABILITY_OPTIONS[i]))
				Config.durabilityOptionArmors = i;

		Property durabilityWeaponsProp = config.get(Configuration.CATEGORY_GENERAL, "Weapons use durability", DURABILITY_OPTIONS[1], "Choose when weapons should use durability.", DURABILITY_OPTIONS);
		for (int i=0; i<DURABILITY_OPTIONS.length; ++i)
			if (durabilityWeaponsProp.getString().equals(DURABILITY_OPTIONS[i]))
				Config.durabilityOptionWeapons = i;

		EntityPlayer player = Minewatch.proxy.getClientPlayer();
		if (player != null) {
			for (EnumHero hero : EnumHero.values()) {
				Property heroTextureProp = config.get(Config.CATEGORY_HERO_TEXTURES, hero.name+" Texture", hero.textureCredits[0], "Textures for "+hero.name+"'s armor", hero.textureCredits);
				for (int i=0; i<hero.textureCredits.length; ++i)
					if (hero.textureCredits[i].equalsIgnoreCase(heroTextureProp.getString()))
						hero.setSkin(player, i);
			}
			Minewatch.network.sendToServer(new CPacketSyncSkins());
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void syncSkins(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote && event.getEntity() == Minewatch.proxy.getClientPlayer()) {
			syncConfig();
			config.save();	
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