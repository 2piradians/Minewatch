package twopiradians.minewatch.common;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.ModTokens;
import twopiradians.minewatch.common.item.weapon.ModWeapon;
import twopiradians.minewatch.common.recipe.ShapelessMatchingDamageRecipe;
import twopiradians.minewatch.packet.PacketToggleMode;

public class CommonProxy 
{
	public void preInit(FMLPreInitializationEvent event) {
		Minewatch.configFile = event.getSuggestedConfigurationFile();
		Config.preInit(Minewatch.configFile);
		registerPackets();
		ModEntities.registerEntities();
		registerEventListeners();
	}

	public void init(FMLInitializationEvent event) {
		registerCraftingRecipes();
	}

	public void postInit(FMLPostInitializationEvent event) {}

	private void registerPackets() { // Side is where the packets goes TO
		int id = 0;
		Minewatch.network.registerMessage(PacketToggleMode.Handler.class, PacketToggleMode.class, id++, Side.SERVER);
	}

	public void spawnParticlesHealthPlus(World worldIn, double x, double y, double z, double motionX, double motionY, double motionZ, float scale) {}

	protected void registerEventListeners() {
		MinecraftForge.EVENT_BUS.register(new Config());
		MinecraftForge.EVENT_BUS.register(new ModTokens());
		MinecraftForge.EVENT_BUS.register(new ModWeapon());
	}

	private void registerCraftingRecipes() {

		//Ana
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.ana_helmet), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.ana_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "ana_helmet"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.ana_chestplate), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.ana_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "ana_chestplate"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.ana_leggings), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.ana_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "ana_leggings"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.ana_boots), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.ana_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "ana_boots"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.ana_rifle), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.ana_token)));}}).setRegistryName(Minewatch.MODID, "ana_rifle"));
		
		//Hanzo
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.hanzo_helmet), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.hanzo_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "hanzo_helmet"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.hanzo_chestplate), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.hanzo_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "hanzo_chestplate"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.hanzo_leggings), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.hanzo_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "hanzo_leggings"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.hanzo_boots), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.hanzo_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "hanzo_boots"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.hanzo_bow), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.hanzo_token)));}}).setRegistryName(Minewatch.MODID, "hanzo_bow"));
		
		//Reaper
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.reaper_helmet), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.reaper_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "reaper_helmet"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.reaper_chestplate), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.reaper_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "reaper_chestplate"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.reaper_leggings), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.reaper_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "reaper_leggings"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.reaper_boots), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.reaper_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "reaper_boots"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.reaper_shotgun), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.reaper_token)));}}).setRegistryName(Minewatch.MODID, "reaper_shotgun"));
		
		//Reinhardt
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.reinhardt_helmet), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.reinhardt_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "reinhardt_helmet"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.reinhardt_chestplate), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.reinhardt_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "reinhardt_chestplate"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.reinhardt_leggings), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.reinhardt_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "reinhardt_leggings"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.reinhardt_boots), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.reinhardt_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "reinhardt_boots"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.reinhardt_hammer), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.reinhardt_token)));}}).setRegistryName(Minewatch.MODID, "reinhardt_hammer"));
		
		//Genji
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.genji_helmet), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.genji_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "genji_helmet"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.genji_chestplate), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.genji_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "genji_chestplate"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.genji_leggings), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.genji_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "genji_leggings"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.genji_boots), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.genji_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "genji_boots"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.genji_shuriken), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.genji_token)));}}).setRegistryName(Minewatch.MODID, "genji_shuriken"));
		
		//Tracer
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.tracer_helmet), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.tracer_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "tracer_helmet"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.tracer_chestplate), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.tracer_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "tracer_chestplate"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.tracer_leggings), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.tracer_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "tracer_leggings"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.tracer_boots), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.tracer_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "tracer_boots"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.tracer_pistol), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.tracer_token)));}}).setRegistryName(Minewatch.MODID, "tracer_pistol"));
		
		//McCree
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.mccree_helmet), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.mccree_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "mccree_helmet"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.mccree_chestplate), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.mccree_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "mccree_chestplate"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.mccree_leggings), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.mccree_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "mccree_leggings"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.mccree_boots), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.mccree_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "mccree_boots"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.mccree_gun), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.mccree_token)));}}).setRegistryName(Minewatch.MODID, "mccree_gun"));
		
		//Soldier
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.soldier_helmet), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.soldier_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "soldier_helmet"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.soldier_chestplate), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.soldier_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "soldier_chestplate"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.soldier_leggings), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.soldier_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "soldier_leggings"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.soldier_boots), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.soldier_token))); 
				add(Ingredient.fromStacks(new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, "soldier_boots"));
		ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(ModItems.soldier_gun), 
				new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(ModItems.soldier_token)));}}).setRegistryName(Minewatch.MODID, "soldier_gun"));
		
	}
}
