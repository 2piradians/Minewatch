package twopiradians.minewatch.common.config;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.packet.CPacketSyncSkins;

public class Config {

	private static final Biome[] OVERWORLD_BIOMES;
	static {
		ArrayList<Biome> biomes = new ArrayList<Biome>();
		for (ResourceLocation loc : Biome.REGISTRY.getKeys()) {
			Biome biome = Biome.REGISTRY.getObject(loc);
			if (biome != null && biome != Biomes.HELL && biome != Biomes.SKY)
				biomes.add(biome);
		}
		OVERWORLD_BIOMES = biomes.toArray(new Biome[biomes.size()]);
	}
	
	/**Version of this config - if loaded version is less than this, delete the config*/
	private static final float CONFIG_VERSION = 3.4F;

	public static final String CATEGORY_HERO_MOBS = "config.server-side.hero_mobs";
	public static final String CATEGORY_SERVER_SIDE = "config.server-side";
	public static final String CATEGORY_CLIENT_SIDE = "config.client-side";
	public static final String CATEGORY_HERO_SKINS = "config.client-side.hero_skins";

	private static final String[] DURABILITY_OPTIONS = new String[] {"Normally", "When not wearing full set", "Never"};
	private static final String[] TRACK_KILLS_OPTIONS = new String[] {"For everything", "For players", "Never"};
	private static final String[] SPAWN_OPTIONS = new String[] {"Always", "In darkness", "Never"};
	private static final String[] SPAWN_FREQ_OPTIONS = new String[] {"Never", "Rarely", "Uncommonly", "Commonly"};

	public static Configuration config;
	public static boolean useObjModels;
	public static float damageScale;
	public static double tokenDropRate;
	public static double wildCardRate;
	public static boolean allowGunWarnings;
	public static boolean customCrosshairs;
	public static boolean projectilesCauseKnockback;
	public static double guiScale;
	public static int durabilityOptionArmors;
	public static int durabilityOptionWeapons;
	public static int trackKillsOption;
	public static boolean preventFallDamage;

	public static boolean mobRandomSkins;
	public static int mobSpawn;
	public static int mobSpawnFreq;

	public static void preInit(final File file) {
		config = new Configuration(file, String.valueOf(CONFIG_VERSION));
		config.load();

		//If loaded version < CONFIG_VERSION, delete it
		String version = Config.config.getLoadedConfigVersion();
		try {
			if (version == null || Float.parseFloat(version) < CONFIG_VERSION) 
				for (String category : Config.config.getCategoryNames())
					Config.config.removeCategory(Config.config.getCategory(category));
		}
		catch (Exception e) {
			for (String category : Config.config.getCategoryNames())
				Config.config.removeCategory(Config.config.getCategory(category));
		}

		config.setCategoryComment(Config.CATEGORY_CLIENT_SIDE, "Options that affect the client");
		config.setCategoryComment(Config.CATEGORY_SERVER_SIDE, "Options that only take effect if changed in the server's config (or in Single-Player)");
		config.setCategoryComment(Config.CATEGORY_HERO_SKINS, "Choose skins for each hero's armor. If you'd like to submit your own skin to be used in the mod, please message us!");
		config.setCategoryComment(Config.CATEGORY_HERO_MOBS, "Choose options for Hero Mobs.");
		syncConfig();
		config.save();
	}

	public static void syncConfig() {		
		// CLIENT-SIDE
		Property use3DModelsprop = config.get(Config.CATEGORY_CLIENT_SIDE, "Use 3D item models", true, "Should the Minewatch weapons use 3D models?");
		useObjModels = use3DModelsprop.getBoolean();

		Property customCrosshairsProp = config.get(Config.CATEGORY_CLIENT_SIDE, "Custom crosshairs", true, "Should weapons change your crosshair?");
		customCrosshairs = customCrosshairsProp.getBoolean();

		Property guiScaleProp = config.get(Config.CATEGORY_CLIENT_SIDE, "Gui scale", 1d, "Scale for the hero and weapon GUI/overlays.", 0, 2);
		Config.guiScale = guiScaleProp.getDouble();

		Property trackKillsProp = config.get(Config.CATEGORY_CLIENT_SIDE, "Track kills and damage", TRACK_KILLS_OPTIONS[0], "Tracked kills will display a message after killing them and will play kill and multi-kill sounds.", TRACK_KILLS_OPTIONS);
		for (int i=0; i<TRACK_KILLS_OPTIONS.length; ++i)
			if (trackKillsProp.getString().equals(TRACK_KILLS_OPTIONS[i]))
				Config.trackKillsOption = i;

		UUID uuid = Minewatch.proxy.getClientUUID();
		if (uuid != null) {
			for (EnumHero hero : EnumHero.values()) {
				Property heroTextureProp = getHeroTextureProp(hero);
				for (int i=0; i<hero.skinCredits.length; ++i)
					if (hero.skinCredits[i].equalsIgnoreCase(heroTextureProp.getString()))
						hero.setSkin(uuid, i);
			}
			Minewatch.network.sendToServer(new CPacketSyncSkins(uuid));
		}

		// SERVER-SIDE (make sure all new options are synced with command)

		Property preventFallDamageProp = config.get(Config.CATEGORY_SERVER_SIDE, "Prevent fall damage", false, "Should fall damage be prevented while wearing a full set of hero armor?");
		preventFallDamage = preventFallDamageProp.getBoolean();

		Property allowGunWarningsProp = config.get(Config.CATEGORY_SERVER_SIDE, "Restrict weapon usage", false, "Should weapons only work like in Overwatch: only in the mainhand (with offhand weapons in the offhand)? This also prevents weapons from different heroes from being mixed and matched.");
		allowGunWarnings = allowGunWarningsProp.getBoolean();

		Property projectilesCauseKnockbackProp = config.get(Config.CATEGORY_SERVER_SIDE, "Projectiles cause knockback", true, "Should projectiles (i.e. bullets/weapons) knock back enemies?");
		projectilesCauseKnockback = projectilesCauseKnockbackProp.getBoolean();

		Property tokenDropRateProp = config.get(Config.CATEGORY_SERVER_SIDE, "Token drop percentage", 1, "Percent of time a token drops from a mob upon death.", 0, 100);
		tokenDropRate = tokenDropRateProp.getInt();

		Property wildCardRateProp = config.get(Config.CATEGORY_SERVER_SIDE, "Wild Card drop percentage", 10, "Percent of time a dropped token will be a wild card token.", 0, 100);
		wildCardRate = wildCardRateProp.getInt();

		Property damageScaleProp = config.get(Config.CATEGORY_SERVER_SIDE, "Damage scale", 1d, "1 is the recommended scale for vanilla. A higher scale means weapons do more damage and a lower scale means they do less.", 0, 100);
		Config.damageScale = (float) (0.1d * damageScaleProp.getDouble());

		Property durabilityArmorsProp = config.get(Config.CATEGORY_SERVER_SIDE, "Armors use durability", DURABILITY_OPTIONS[0], "Choose when armors should use durability.", DURABILITY_OPTIONS);
		for (int i=0; i<DURABILITY_OPTIONS.length; ++i)
			if (durabilityArmorsProp.getString().equals(DURABILITY_OPTIONS[i]))
				Config.durabilityOptionArmors = i;

		Property durabilityWeaponsProp = config.get(Config.CATEGORY_SERVER_SIDE, "Weapons use durability", DURABILITY_OPTIONS[1], "Choose when weapons should use durability.", DURABILITY_OPTIONS);
		for (int i=0; i<DURABILITY_OPTIONS.length; ++i)
			if (durabilityWeaponsProp.getString().equals(DURABILITY_OPTIONS[i]))
				Config.durabilityOptionWeapons = i;

		// Hero Mob options

		// TODO natural spawn, token drop multiplier, drop items, accuracy, attack cooldown, targets, can despawn
		Property mobRandomSkinsProp = config.get(Config.CATEGORY_HERO_MOBS, "Random Skins", true, "Should Hero Mobs spawn with random skins.");
		mobRandomSkins = mobRandomSkinsProp.getBoolean();
		
		Property mobSpawnProp = config.get(Config.CATEGORY_HERO_MOBS, "Spawning", SPAWN_OPTIONS[0], "Choose when Hero Mobs should spawn.", SPAWN_OPTIONS);
		for (int i=0; i<SPAWN_OPTIONS.length; ++i)
			if (mobSpawnProp.getString().equals(SPAWN_OPTIONS[i]))
				Config.mobSpawn = i;
		
		Property mobSpawnFreqProp = config.get(Config.CATEGORY_HERO_MOBS, "Spawning Frequency", SPAWN_FREQ_OPTIONS[1], "Choose how frequently Hero Mobs should spawn.", SPAWN_FREQ_OPTIONS);
		for (int i=0; i<SPAWN_FREQ_OPTIONS.length; ++i)
			if (mobSpawnFreqProp.getString().equals(SPAWN_FREQ_OPTIONS[i]))
				Config.mobSpawnFreq = i;

		for (EnumHero hero : EnumHero.values()) 
			if (Config.mobSpawnFreq == 0 || Config.mobSpawn == 2)
				EntityRegistry.removeSpawn(hero.heroClass, EnumCreatureType.CREATURE, OVERWORLD_BIOMES);
			else
				EntityRegistry.addSpawn(hero.heroClass, (int) Math.pow(Config.mobSpawnFreq, 2), 1, 1, EnumCreatureType.MONSTER, OVERWORLD_BIOMES);
	System.out.println((int) Math.pow(Config.mobSpawnFreq, 2)); // TODO
	}

	public static Property getHeroTextureProp(EnumHero hero) {
		return config.get(Config.CATEGORY_HERO_SKINS, hero.name+" skin", hero.skinCredits[0], "Skins for "+hero.name+"'s armor", hero.skinCredits);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void syncSkins(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote && Minewatch.proxy.getClientUUID() != null && 
				event.getEntity().getPersistentID().toString().equals(Minewatch.proxy.getClientUUID().toString())) {
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