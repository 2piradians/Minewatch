package twopiradians.minewatch.common.config;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.packet.CPacketSyncSkins;
import twopiradians.minewatch.packet.PacketSyncConfig;

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
	public static int tokenDropRate;
	public static int wildCardRate;
	public static boolean allowGunWarnings;
	public static boolean customCrosshairs;
	public static boolean projectilesCauseKnockback;
	public static double guiScale;
	public static int durabilityOptionArmors;
	public static int durabilityOptionWeapons;
	public static int trackKillsOption;
	public static boolean preventFallDamage;
	public static boolean healMobs;
	public static double healthPackHealMultiplier;
	public static double healthPackRespawnMultiplier;

	public static boolean mobRandomSkins;
	public static int mobSpawn;
	public static int mobSpawnFreq;
	public static boolean mobTargetPlayers;
	public static boolean mobTargetHostiles;
	public static boolean mobTargetPassives;
	public static boolean mobTargetHeroes;
	public static int mobTokenDropRate;
	public static int mobWildCardDropRate;
	public static float mobEquipmentDropRate;
	public static double mobAttackCooldown;
	public static double mobInaccuracy;

	public static void preInit(final File file) {
		config = new Configuration(file, CONFIG_VERSION+Configuration.NEW_LINE+Configuration.NEW_LINE+
				"##########################################################################################################"+Configuration.NEW_LINE+
				"# I would recommend editing this config using the in-game menus. The config can be accessed using the Mods button in the main screen, "+Configuration.NEW_LINE+
				"# the Mod Options button in the escape menu, or the Options button in the Minewatch Tab."+Configuration.NEW_LINE+
				"##########################################################################################################");
		config.load();
		
		//If loaded version < CONFIG_VERSION, delete it
		String version = Config.config.getLoadedConfigVersion();
		try {
			if (version == null || Float.parseFloat(version.split("\n")[0]) < CONFIG_VERSION) 
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
		Property prop = config.get(Config.CATEGORY_CLIENT_SIDE, "Use 3D item models", true, "Should the Minewatch weapons use 3D models?");
		useObjModels = prop.getBoolean();

		prop = config.get(Config.CATEGORY_CLIENT_SIDE, "Custom crosshairs", true, "Should weapons change your crosshair?");
		customCrosshairs = prop.getBoolean();

		prop = config.get(Config.CATEGORY_CLIENT_SIDE, "Gui scale", 1d, "Scale for the hero and weapon GUI/overlays.", 0, 2);
		guiScale = prop.getDouble();

		prop = config.get(Config.CATEGORY_CLIENT_SIDE, "Track kills and damage", TRACK_KILLS_OPTIONS[0], "Tracked kills will display a message after killing them and will play kill and multi-kill sounds.", TRACK_KILLS_OPTIONS);
		for (int i=0; i<TRACK_KILLS_OPTIONS.length; ++i)
			if (prop.getString().equals(TRACK_KILLS_OPTIONS[i]))
				trackKillsOption = i;

		UUID uuid = Minewatch.proxy.getClientUUID();
		if (uuid != null) {
			for (EnumHero hero : EnumHero.values()) {
				prop = getHeroTextureProp(hero);
				for (int i=0; i<hero.skinCredits.length; ++i)
					if (hero.skinCredits[i].equalsIgnoreCase(prop.getString()))
						hero.setSkin(uuid, i);
			}
			Minewatch.network.sendToServer(new CPacketSyncSkins(uuid));
		}

		// SERVER-SIDE (make sure all new options are synced with command)

		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Prevent fall damage", false, "Should fall damage be prevented while wearing a full set of hero armor?");
		preventFallDamage = prop.getBoolean();

		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Restrict weapon usage", false, "Should weapons only work like in Overwatch: only in the mainhand (with offhand weapons in the offhand)? This also prevents weapons from different heroes from being mixed and matched.");
		allowGunWarnings = prop.getBoolean();

		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Projectiles cause knockback", true, "Should projectiles (i.e. bullets/weapons) knock back enemies?");
		projectilesCauseKnockback = prop.getBoolean();

		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Token drop percentage", 1, "Percent of time a token drops from a mob upon death.", 0, 100);
		tokenDropRate = prop.getInt();

		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Wild Card drop percentage", 10, "Percent of time a dropped token will be a Wild Card token.", 0, 100);
		wildCardRate = prop.getInt();

		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Damage scale", 1d, "1 is the recommended scale for vanilla. A higher scale means weapons do more damage and a lower scale means they do less.", 0, 100);
		damageScale = (float) (0.1d * prop.getDouble());

		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Armors use durability", DURABILITY_OPTIONS[0], "Choose when armors should use durability.", DURABILITY_OPTIONS);
		for (int i=0; i<DURABILITY_OPTIONS.length; ++i)
			if (prop.getString().equals(DURABILITY_OPTIONS[i]))
				durabilityOptionArmors = i;

		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Weapons use durability", DURABILITY_OPTIONS[1], "Choose when weapons should use durability.", DURABILITY_OPTIONS);
		for (int i=0; i<DURABILITY_OPTIONS.length; ++i)
			if (prop.getString().equals(DURABILITY_OPTIONS[i]))
				durabilityOptionWeapons = i;
		
		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Allow healing mobs", true, "Should healing abilities and attacks affect other mobs? This does not apply to Hero Mobs.");
		healMobs = prop.getBoolean();
		
		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Health Pack Heal Multiplier", 1d, "Multiplied by the healing amount for health packs (which is scaled by the Damage Scale). For example with this set to 2, Health Packs will heal twice as much as normal.", 0, 10);
		healthPackHealMultiplier = prop.getDouble();
		
		prop = config.get(Config.CATEGORY_SERVER_SIDE, "Health Pack Respawn Multiplier", 1d, "Multiplied by the respawn timer for health packs. For example with this set to 2, Health Packs will take twice as long to respawn.", 0, 10);
		healthPackRespawnMultiplier = prop.getDouble();

		// Hero Mob options

		prop = config.get(Config.CATEGORY_HERO_MOBS, "Random skins", true, "Should Hero Mobs spawn with random skins.");
		mobRandomSkins = prop.getBoolean();
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Spawning", SPAWN_OPTIONS[0], "Choose when Hero Mobs should spawn.", SPAWN_OPTIONS);
		for (int i=0; i<SPAWN_OPTIONS.length; ++i)
			if (prop.getString().equals(SPAWN_OPTIONS[i]))
				mobSpawn = i;
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Spawning frequency", SPAWN_FREQ_OPTIONS[2], "Choose how frequently Hero Mobs should spawn.", SPAWN_FREQ_OPTIONS);
		for (int i=0; i<SPAWN_FREQ_OPTIONS.length; ++i)
			if (prop.getString().equals(SPAWN_FREQ_OPTIONS[i]))
				mobSpawnFreq = i;

		for (EnumHero hero : EnumHero.values()) 
			if (mobSpawnFreq == 0 || mobSpawn == 2)
				EntityRegistry.removeSpawn(hero.heroClass, EnumCreatureType.CREATURE, OVERWORLD_BIOMES);
			else
				EntityRegistry.addSpawn(hero.heroClass, (int) Math.pow(Config.mobSpawnFreq, 3), 1, 1, EnumCreatureType.CREATURE, OVERWORLD_BIOMES);
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Target players", true, "Should Hero Mobs target players.\nNote: Hero Mobs never target entities on the same team as them.");
		mobTargetPlayers = prop.getBoolean();
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Target hostile mobs", true, "Should Hero Mobs target hostile mobs.\nNote: Hero Mobs never target entities on the same team as them.");
		mobTargetHostiles = prop.getBoolean();
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Target passive mobs", false, "Should Hero Mobs target passive mobs.\nNote: Hero Mobs never target entities on the same team as them.");
		mobTargetPassives = prop.getBoolean();
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Target other hero mobs", false, "Should Hero Mobs target other Hero Mobs.\nNote: Hero Mobs never target entities on the same team as them.");
		mobTargetHeroes = prop.getBoolean();
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Token drop percentage", 25, "Percent of time a token drops from a Hero Mob upon death.\nNote: Hero Mobs will only drop tokens of their respective hero (or Wild Card tokens).", 0, 100);
		mobTokenDropRate = prop.getInt();
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Wild Card drop percentage", 10, "Percent of time a dropped token from a Hero Mob will be a Wild Card token.", 0, 100);
		mobWildCardDropRate = prop.getInt();
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Equipment drop percentage", 10, "Percent chance that a Hero Mob will drop each piece of its equipment.", 0, 100);
		mobEquipmentDropRate = prop.getInt()/100f;
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Attack Cooldown Multiplier", 2d, "Multiplied by the normal attack cooldown for attacks / abilities. For example with this set to 2, Hero Mob attacks / abilities will have twice the normal cooldown.", 0, 10);
		mobAttackCooldown = prop.getDouble();
		
		prop = config.get(Config.CATEGORY_HERO_MOBS, "Inaccuracy Multiplier", 7d, "Multiplied by the normal inaccuracy for attacks / abilities. For example with this set to 2, Hero Mob attacks / abilities will be twice as inaccurate.", 0, 20);
		mobInaccuracy = prop.getDouble();
	}

	public static Property getHeroTextureProp(EnumHero hero) {
		return config.get(Config.CATEGORY_HERO_SKINS, hero.name+" skin", hero.skinCredits[0], "Skins for "+hero.name+"'s armor", hero.skinCredits);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void syncSkins(EntityJoinWorldEvent event) {
		// sync skins client -> server
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
	
	/**Send PacketSyncConfig when a player joins a server*/
	@SubscribeEvent
	public void onJoinWorld(PlayerLoggedInEvent event) {
		if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP && 
				!(event.player.world.getMinecraftServer() instanceof IntegratedServer)) {
			Minewatch.logger.info("Sending config sync packet to: "+event.player.getName());
			Minewatch.network.sendTo(new PacketSyncConfig(), (EntityPlayerMP) event.player);
		}
	}
}