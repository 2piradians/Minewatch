package twopiradians.minewatch.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.recipe.ShapelessMatchingDamageRecipe;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.PacketPotionEffect;
import twopiradians.minewatch.packet.PacketSpawnParticle;
import twopiradians.minewatch.packet.PacketSyncAmmo;
import twopiradians.minewatch.packet.PacketSyncCooldown;
import twopiradians.minewatch.packet.PacketSyncKeys;
import twopiradians.minewatch.packet.PacketSyncSpawningEntity;

public class CommonProxy 
{
	public void preInit(FMLPreInitializationEvent event) {
		Minewatch.configFile = event.getSuggestedConfigurationFile();
		Config.preInit(Minewatch.configFile);
		registerPackets();
		ModEntities.registerEntities();
		ModSoundEvents.postInit();
	}

	public void init(FMLInitializationEvent event) {
		ModPotions.init();
		registerEventListeners();
		registerCraftingRecipes();
	}

	public void postInit(FMLPostInitializationEvent event) {}

	private void registerPackets() { // Side is where the packets goes TO
		int id = 0;
		Minewatch.network.registerMessage(PacketSyncKeys.Handler.class, PacketSyncKeys.class, id++, Side.SERVER);
		Minewatch.network.registerMessage(PacketSyncAmmo.Handler.class, PacketSyncAmmo.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(PacketSyncSpawningEntity.Handler.class, PacketSyncSpawningEntity.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(PacketSyncCooldown.Handler.class, PacketSyncCooldown.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(PacketSpawnParticle.Handler.class, PacketSpawnParticle.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(PacketPotionEffect.Handler.class, PacketPotionEffect.class, id++, Side.CLIENT);
	}

	public void spawnParticlesAnaHealth(EntityLivingBase entity) { }
	public void spawnParticlesHanzoSonic(World world, double x, double y, double z, boolean isBig, boolean isFast) { }
	public void spawnParticlesHanzoSonic(World world, Entity trackEntity, boolean isBig) { }
	public void spawnParticlesTrail(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge) { }
	public void spawnParticlesSmoke(World world, double x, double y, double z, int color, int colorFade, float scale, int maxAge) {}
	public void spawnParticlesSpark(World world, double x, double y, double z, int color, int colorFade, float scale, int maxAge) {}
	public void spawnParticlesMeiBlaster(World world, double x, double y, double z, double motionX, double motionY, double motionZ, float alpha, int maxAge, float initialScale, float finalScale) { }
	
	protected void registerEventListeners() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new Config());
		MinecraftForge.EVENT_BUS.register(new ItemMWToken());
	}

private void registerCraftingRecipes() {
		
		for (EnumHero hero : EnumHero.values()) {
			ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(hero.helmet), 
					new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(hero.token))); 
					add(Ingredient.fromStacks(new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, hero.name.toLowerCase()+"_helmet"));
			ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(hero.chestplate), 
					new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(hero.token))); 
					add(Ingredient.fromStacks(new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, hero.name.toLowerCase()+"_chestplate"));
			ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(hero.leggings), 
					new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(hero.token))); 
					add(Ingredient.fromStacks(new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, hero.name.toLowerCase()+"_leggings"));
			ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(hero.boots), 
					new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(hero.token))); 
					add(Ingredient.fromStacks(new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, hero.name.toLowerCase()+"_boots"));
			ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(hero.weapon, hero.weapon.hasOffhand ? 2 : 1), 
					new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(hero.token)));}}).setRegistryName(Minewatch.MODID, hero.name.toLowerCase()+"_weapon"));

		}
	}

	@SubscribeEvent(receiveCanceled=true)
	public void commandDev(CommandEvent event) {
		try {
		if (event.getCommand().getName().equalsIgnoreCase("dev") && 
				event.getCommand().checkPermission(event.getSender().getServer(), event.getSender()) &&
				CommandDev.runCommand(event.getSender().getServer(), event.getSender(), event.getParameters())) 
			event.setCanceled(true);
		}
		catch (Exception e) {}
	}
	public void mouseClick() { }
	
}
