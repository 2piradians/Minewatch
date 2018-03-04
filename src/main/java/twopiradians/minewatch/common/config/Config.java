package twopiradians.minewatch.common.config;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
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
	private static final float CONFIG_VERSION = 3.9F;

	public static final String CATEGORY_TEAM_BLOCKS = "config.server-side.team_blocks";
	public static final String CATEGORY_HERO_MOBS = "config.server-side.hero_mobs";
	public static final String CATEGORY_SERVER_SIDE = "config.server-side";
	public static final String CATEGORY_CLIENT_SIDE = "config.client-side";
	public static final String CATEGORY_HERO_SKINS = "config.client-side.hero_skins";

	private static final String[] DURABILITY_OPTIONS = new String[] {"Normally", "When not wearing full set", "Never"};
	private static final String[] TRACK_KILLS_OPTIONS = new String[] {"For everything", "For players", "Never"};
	private static final String[] SPAWN_OPTIONS = new String[] {"Always", "In darkness", "Never"};
	private static final String[] SPAWN_FREQ_OPTIONS = new String[] {"Never", "Rarely", "Uncommonly", "Commonly"};
	private static final String[] HEAL_CHANGE_HERO_OPTIONS = new String[] {"When active", "When active and with a team selected", "Always", "With a team selected", "Never"};

	public static Configuration config;
	public static boolean useObjModels;
	public static boolean customCrosshairs;
	public static double guiScale;
	public static boolean renderOutlines;
	public static boolean healthBars;

	public static int tokenDropRate;
	public static int wildCardRate;
	public static boolean allowGunWarnings;
	public static boolean projectilesCauseKnockback;
	public static double damageScale;
	public static int durabilityOptionArmors;
	public static int durabilityOptionWeapons;
	public static int trackKillsOption;
	public static boolean preventFallDamage;
	public static boolean healMobs;
	public static double healthPackHealMultiplier;
	public static double healthPackRespawnMultiplier;
	public static double ammoMultiplier;
	public static boolean tokenDropRequiresPlayer;
	public static double abilityCooldownMultiplier;
	public static double aimAssist;
	public static boolean heroMobsDespawn;
	public static boolean deleteItemsOnGround;
	public static boolean lobbyCommand;
	public static double healthScale;
	public static double armor;

	public static boolean customDeathScreen;
	public static int respawnTime; // in ticks
	public static boolean allowHeroRespawn;
	public static boolean allowMobRespawn;
	public static boolean allowPlayerRespawn;
	public static boolean mobRespawnRandomHero;
	public static int healChangeHero;
	public static boolean heroSelectClearMWItems;

	public static boolean mobRandomSkins;
	public static int mobSpawn;
	public static int mobSpawnFreq;
	public static boolean mobTargetPlayers;
	public static boolean mobTargetHostiles;
	public static boolean mobTargetPassives;
	public static boolean mobTargetHeroes;
	public static int mobTokenDropRate;
	public static int mobWildCardDropRate;
	public static double mobEquipmentDropRate;
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
		config.setCategoryComment(Config.CATEGORY_TEAM_BLOCKS, "Choose options for Team Blocks (i.e. Team Spawn).");
		syncConfig();
		config.save();
	}

	public static void syncConfig() {	
		syncConfig(false, false);
	}

	/**@param overriding - should config sync from the config fields
	 * @param clientSideOnly - should only client side options be synced*/
	public static void syncConfig(boolean overriding, boolean clientSideOnly) {		
		// CLIENT-SIDE ======================================================================================

		Property prop = config.get(Config.CATEGORY_CLIENT_SIDE, "Use 3D Item Models", true, "Should the Minewatch weapons use 3D models?");
		useObjModels = prop.getBoolean();

		prop = config.get(Config.CATEGORY_CLIENT_SIDE, "Custom Crosshairs", true, "Should weapons change your crosshair?");
		customCrosshairs = prop.getBoolean();

		prop = config.get(Config.CATEGORY_CLIENT_SIDE, "Gui Scale", 0.75d, "Scale for the hero and weapon GUI/overlays.", 0, 2);
		guiScale = prop.getDouble();

		prop = config.get(Config.CATEGORY_CLIENT_SIDE, "Track Kills and Damage", TRACK_KILLS_OPTIONS[0], "Tracked kills will display a message after killing them and will play kill and multi-kill sounds.", TRACK_KILLS_OPTIONS);
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

		prop = config.get(Config.CATEGORY_CLIENT_SIDE, "Render Outlines", true, "Should enemy heroes have a red outline?");
		renderOutlines = prop.getBoolean();

		prop = config.get(Config.CATEGORY_CLIENT_SIDE, "Health Bars", true, "Should hero's health bars appear above their heads?");
		healthBars = prop.getBoolean();

		// SERVER-SIDE (make sure all new options are synced with command) ======================================================================================

		if (!clientSideOnly) {
			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Prevent Fall Damage", true, "Should fall damage be prevented while wearing a full set of hero armor?");
			if (overriding)
				prop.set(preventFallDamage);
			else
				preventFallDamage = prop.getBoolean();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Restrict Weapon Usage", false, "Should weapons only work like in Overwatch: only in the mainhand (with offhand weapons in the offhand)? This also prevents weapons from different heroes from being mixed and matched.");
			if (overriding)
				prop.set(allowGunWarnings);
			else
				allowGunWarnings = prop.getBoolean();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Projectiles Cause Knockback", false, "Should projectiles (i.e. bullets/weapons) knock back enemies?");
			if (overriding)
				prop.set(projectilesCauseKnockback);
			else
				projectilesCauseKnockback = prop.getBoolean();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Token Drop Percentage", 1, "Percent of time a token drops from a mob upon death.", 0, 100);
			if (overriding)
				prop.set(tokenDropRate);
			else
				tokenDropRate = prop.getInt();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Wild Card Drop Percentage", 10, "Percent of time a dropped token will be a Wild Card token.", 0, 100);
			if (overriding)
				prop.set(wildCardRate);
			else
				wildCardRate = prop.getInt();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Damage Scale", 1d, "1 is the recommended scale for vanilla. A higher scale means weapons do more damage and a lower scale means they do less.", 0, 100);
			if (overriding)
				prop.set(damageScale * 10d);
			else
				damageScale = 0.1d * prop.getDouble();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Armors Use Durability", DURABILITY_OPTIONS[0], "Choose when armors should use durability.", DURABILITY_OPTIONS);
			if (overriding)
				prop.set(DURABILITY_OPTIONS[durabilityOptionArmors]);
			else
				for (int i=0; i<DURABILITY_OPTIONS.length; ++i)
					if (prop.getString().equals(DURABILITY_OPTIONS[i]))
						durabilityOptionArmors = i;

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Weapons Use Durability", DURABILITY_OPTIONS[1], "Choose when weapons should use durability.", DURABILITY_OPTIONS);
			if (overriding)
				prop.set(DURABILITY_OPTIONS[durabilityOptionWeapons]);
			else
				for (int i=0; i<DURABILITY_OPTIONS.length; ++i)
					if (prop.getString().equals(DURABILITY_OPTIONS[i]))
						durabilityOptionWeapons = i;

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Allow Healing Outside Team", true, "Should healing abilities and attacks affect mobs that are not on the same team?"); 
			if (overriding)
				prop.set(healMobs);
			else
				healMobs = prop.getBoolean();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Health Pack Heal Multiplier", 1d, "Multiplied by the healing amount for health packs (which is scaled by the Damage Scale). For example with this set to 2, Health Packs will heal twice as much as normal.", 0, 10);
			if (overriding)
				prop.set(healthPackHealMultiplier);
			else
				healthPackHealMultiplier = prop.getDouble();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Health Pack Respawn Multiplier", 1d, "Multiplied by the respawn timer for health packs. For example with this set to 2, Health Packs will take twice as long to respawn.", 0, 10);
			if (overriding)
				prop.set(healthPackRespawnMultiplier);
			else
				healthPackRespawnMultiplier = prop.getDouble();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Ammo Multiplier", 1d, "Multiplied by the default max ammo for a weapon. For example with this set to 2, weapons will have twice as much ammo. When this is 0, weapons have unlimited ammo.", 0, 10);
			if (overriding)
				prop.set(ammoMultiplier);
			else
				ammoMultiplier = prop.getDouble();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Token Drops Require Player", false, "Should tokens only drop from mobs killed by a player?");
			if (overriding)
				prop.set(tokenDropRequiresPlayer);
			else
				tokenDropRequiresPlayer = prop.getBoolean();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Ability Cooldown Multiplier", 1d, "Multiplied by the default cooldown for abilities. For example with this set to 2, abilities will have twice the normal cooldown.", 0, 10);
			if (overriding)
				prop.set(abilityCooldownMultiplier);
			else
				abilityCooldownMultiplier = prop.getDouble();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Aim Assist", 0.2d, "0 is no aim assist, 1 is heavy aim assist. This will subtly turn the player towards their target while shooting.", 0, 1);
			if (overriding)
				prop.set(aimAssist);
			else
				aimAssist = prop.getDouble();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Delete Minewatch Items on Ground", false, "Should Minewatch armor and weapons be destroyed on the ground?");
			if (overriding)
				prop.set(deleteItemsOnGround);
			else
				deleteItemsOnGround = prop.getBoolean();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Enable /lobby", false, "Should the /lobby command be enabled? It removes your team and respawns you.");
			if (overriding)
				prop.set(lobbyCommand);
			else
				lobbyCommand = prop.getBoolean();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Health Scale", 1d, "1 is the recommended scale for vanilla. A higher scale means heroes have more health and a lower scale means they have less.", 0, 5);
			if (overriding)
				prop.set(healthScale);
			else
				healthScale = prop.getDouble();

			prop = config.get(Config.CATEGORY_SERVER_SIDE, "Armor Damage Reduction", 0d, "Damage reduction value for Minewatch armor.", 0d, 20d);
			if (overriding)
				prop.set(armor);
			else 
				armor = prop.getDouble();

			// Team Block options ======================================================================================

			prop = config.get(Config.CATEGORY_TEAM_BLOCKS, "Custom Death Screen", true, "Should the normal death screen be replaced with the Minewatch death screen?");
			if (overriding)
				prop.set(customDeathScreen);
			else
				customDeathScreen = prop.getBoolean();

			prop = config.get(Config.CATEGORY_TEAM_BLOCKS, "Respawn Time", 10, "Amount of time (in seconds) that entities have to wait to respawn. Only applies when Custom Death Screen is enabled.", 0, 100);
			if (overriding)
				prop.set(respawnTime/20);
			else
				respawnTime = prop.getInt()*20;

			prop = config.get(Config.CATEGORY_TEAM_BLOCKS, "Allow Hero Mobs to Respawn", true, "Should hero mobs respawn at their team's active Team Spawn?");
			if (overriding)
				prop.set(allowHeroRespawn);
			else
				allowHeroRespawn = prop.getBoolean();

			prop = config.get(Config.CATEGORY_TEAM_BLOCKS, "Allow Mobs to Respawn", true, "Should mobs (not hero mobs) respawn at their team's active Team Spawn?");
			if (overriding)
				prop.set(allowMobRespawn);
			else
				allowMobRespawn = prop.getBoolean();

			prop = config.get(Config.CATEGORY_TEAM_BLOCKS, "Allow Players to Respawn", true, "Should players respawn at their team's active Team Spawn?");
			if (overriding)
				prop.set(allowPlayerRespawn);
			else
				allowPlayerRespawn = prop.getBoolean();

			prop = config.get(Config.CATEGORY_TEAM_BLOCKS, "Hero Mobs Respawn with Random Hero", false, "Should Hero Mobs respawn as random heroes?");
			if (overriding)
				prop.set(mobRespawnRandomHero);
			else
				mobRespawnRandomHero = prop.getBoolean();

			prop = config.get(Config.CATEGORY_TEAM_BLOCKS, "Heal and Change Hero at Team Spawns", HEAL_CHANGE_HERO_OPTIONS[0], "Choose when Team Spawns should provide healing and allow players to change heroes within their radius.", HEAL_CHANGE_HERO_OPTIONS);
			if (overriding)
				prop.set(HEAL_CHANGE_HERO_OPTIONS[healChangeHero]);
			else
				for (int i=0; i<HEAL_CHANGE_HERO_OPTIONS.length; ++i)
					if (prop.getString().equals(HEAL_CHANGE_HERO_OPTIONS[i]))
						healChangeHero = i;

			prop = config.get(Config.CATEGORY_TEAM_BLOCKS, "Hero Selection Removes Minewatch Equipment", false, "Should selecting a hero in hero selection remove other Minewatch equipment from the player's inventory?");
			if (overriding)
				prop.set(heroSelectClearMWItems);
			else
				heroSelectClearMWItems = prop.getBoolean();

			// Hero Mob options ======================================================================================

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Hero Mobs on Teams Despawn", false, "Should Hero Mobs on teams be allowed to despawn?");
			if (overriding)
				prop.set(heroMobsDespawn);
			else
				heroMobsDespawn = prop.getBoolean();

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Random Skins", true, "Should Hero Mobs spawn with random skins.");
			if (overriding)
				prop.set(mobRandomSkins);
			else
				mobRandomSkins = prop.getBoolean();

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Spawning", SPAWN_OPTIONS[0], "Choose when Hero Mobs should spawn.", SPAWN_OPTIONS);
			if (overriding)
				prop.set(SPAWN_OPTIONS[mobSpawn]);
			else
				for (int i=0; i<SPAWN_OPTIONS.length; ++i)
					if (prop.getString().equals(SPAWN_OPTIONS[i]))
						mobSpawn = i;

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Spawning Frequency", SPAWN_FREQ_OPTIONS[2], "Choose how frequently Hero Mobs should spawn.", SPAWN_FREQ_OPTIONS);
			if (overriding)
				prop.set(SPAWN_FREQ_OPTIONS[mobSpawnFreq]);
			else
				for (int i=0; i<SPAWN_FREQ_OPTIONS.length; ++i)
					if (prop.getString().equals(SPAWN_FREQ_OPTIONS[i]))
						mobSpawnFreq = i;

			for (EnumHero hero : EnumHero.values()) 
				if (mobSpawnFreq == 0 || mobSpawn == 2)
					EntityRegistry.removeSpawn(hero.heroClass, EnumCreatureType.MONSTER, OVERWORLD_BIOMES);
				else
					EntityRegistry.addSpawn(hero.heroClass, (int) Math.pow(Config.mobSpawnFreq, 3), 1, 1, EnumCreatureType.MONSTER, OVERWORLD_BIOMES);

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Target Players", true, "Should Hero Mobs target players.\nNote: Hero Mobs never target entities on the same team as them.");
			if (overriding)
				prop.set(mobTargetPlayers);
			else
				mobTargetPlayers = prop.getBoolean();

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Target Hostile Mobs", true, "Should Hero Mobs target hostile mobs.\nNote: Hero Mobs never target entities on the same team as them.");
			if (overriding)
				prop.set(mobTargetHostiles);
			else
				mobTargetHostiles = prop.getBoolean();

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Target Passive Mobs", false, "Should Hero Mobs target passive mobs.\nNote: Hero Mobs never target entities on the same team as them.");
			if (overriding)
				prop.set(mobTargetPassives);
			else
				mobTargetPassives = prop.getBoolean();

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Target Hero Mobs", false, "Should Hero Mobs target Hero Mobs.\nNote: Hero Mobs never target entities on the same team as them.");
			if (overriding)
				prop.set(mobTargetHeroes);
			else
				mobTargetHeroes = prop.getBoolean();

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Token Drop Percentage", 25, "Percent of time a token drops from a Hero Mob upon death.\nNote: Hero Mobs will only drop tokens of their respective hero (or Wild Card tokens).", 0, 100);
			if (overriding)
				prop.set(mobTokenDropRate);
			else
				mobTokenDropRate = prop.getInt();

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Wild Card Drop Percentage", 10, "Percent of time a dropped token from a Hero Mob will be a Wild Card token.", 0, 100);
			if (overriding)
				prop.set(mobWildCardDropRate);
			else
				mobWildCardDropRate = prop.getInt();

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Equipment Drop Percentage", 10, "Percent chance that a Hero Mob will drop each piece of its equipment.", 0, 100);
			if (overriding)
				prop.set((int) (mobEquipmentDropRate*100d));
			else
				mobEquipmentDropRate = prop.getInt()/100d;

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Attack Cooldown Multiplier", 2d, "Multiplied by the normal attack cooldown for attacks / abilities. For example with this set to 2, Hero Mob attacks / abilities will have twice the normal cooldown.", 0, 10);
			if (overriding)
				prop.set(mobAttackCooldown);
			else
				mobAttackCooldown = prop.getDouble();

			prop = config.get(Config.CATEGORY_HERO_MOBS, "Inaccuracy Multiplier", 7d, "Multiplied by the normal inaccuracy for attacks / abilities. For example with this set to 2, Hero Mob attacks / abilities will be twice as inaccurate.", 0, 20);
			if (overriding)
				prop.set(mobInaccuracy);
			else
				mobInaccuracy = prop.getDouble();
		}
	}

	public static Property getHeroTextureProp(EnumHero hero) {
		return config.get(Config.CATEGORY_HERO_SKINS, hero.name+" Skin", hero.skinCredits[0], "Skins for "+hero.name+"'s armor", hero.skinCredits);
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
		// if not in mp server (only update server when sync packet sent on mp server)
		if (event.getModID().equals(Minewatch.MODID)) {
			syncConfig(false, FMLCommonHandler.instance().getMinecraftServerInstance() == null);
			config.save();
		}
	}

	/**Send PacketSyncConfig when a player joins a server*/
	@SubscribeEvent
	public void onJoinWorld(PlayerLoggedInEvent event) {
		if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP &&
				event.player.world.getMinecraftServer() != null && 
				!event.player.world.getMinecraftServer().isSinglePlayer()) {
			Minewatch.logger.info("Sending config sync packet to: "+event.player.getName());
			Minewatch.network.sendTo(new PacketSyncConfig(), (EntityPlayerMP) event.player);
		}
	}
}