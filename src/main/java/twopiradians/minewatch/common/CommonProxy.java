package twopiradians.minewatch.common;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.ModTokens;
import twopiradians.minewatch.common.item.weapon.ModWeapon;
import twopiradians.minewatch.common.recipe.ShapelessMatchingDamageRecipe;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.PacketToggleMode;

public class CommonProxy 
{
	public void preInit(FMLPreInitializationEvent event) {
		Minewatch.configFile = event.getSuggestedConfigurationFile();
		Config.preInit(Minewatch.configFile);
		registerPackets();
		ModEntities.registerEntities();
		ModItems.preInit();
		ModSoundEvents.preInit();
	}

	public void init(FMLInitializationEvent event) {
		registerEventListeners();
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
		RecipeSorter.register("Matching Damage Recipe", ShapelessMatchingDamageRecipe.class, Category.SHAPELESS, "");

		//Ana
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.ana_helmet), new ItemStack(ModItems.ana_token), new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.ana_chestplate), new ItemStack(ModItems.ana_token), new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.ana_leggings), new ItemStack(ModItems.ana_token), new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.ana_boots), new ItemStack(ModItems.ana_token), new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.ana_rifle), new ItemStack(ModItems.ana_token));

		//Hanzo
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.hanzo_helmet), new ItemStack(ModItems.hanzo_token), new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.hanzo_chestplate), new ItemStack(ModItems.hanzo_token), new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.hanzo_leggings), new ItemStack(ModItems.hanzo_token), new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.hanzo_boots), new ItemStack(ModItems.hanzo_token), new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.hanzo_bow), new ItemStack(ModItems.hanzo_token));

		//Reaper
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.reaper_helmet), new ItemStack(ModItems.reaper_token), new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.reaper_chestplate), new ItemStack(ModItems.reaper_token), new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.reaper_leggings), new ItemStack(ModItems.reaper_token), new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.reaper_boots), new ItemStack(ModItems.reaper_token), new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.reaper_shotgun, 2), new ItemStack(ModItems.reaper_token));

		//Reinhardt
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.reinhardt_helmet), new ItemStack(ModItems.reinhardt_token), new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.reinhardt_chestplate), new ItemStack(ModItems.reinhardt_token), new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.reinhardt_leggings), new ItemStack(ModItems.reinhardt_token), new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.reinhardt_boots), new ItemStack(ModItems.reinhardt_token), new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.reinhardt_hammer), new ItemStack(ModItems.reinhardt_token));

		//Genji
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.genji_helmet), new ItemStack(ModItems.genji_token), new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.genji_chestplate), new ItemStack(ModItems.genji_token), new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.genji_leggings), new ItemStack(ModItems.genji_token), new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.genji_boots), new ItemStack(ModItems.genji_token), new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.genji_shuriken), new ItemStack(ModItems.genji_token));

		//Tracer
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.tracer_helmet), new ItemStack(ModItems.tracer_token), new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.tracer_chestplate), new ItemStack(ModItems.tracer_token), new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.tracer_leggings), new ItemStack(ModItems.tracer_token), new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(ModItems.tracer_boots), new ItemStack(ModItems.tracer_token), new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.tracer_pistol, 2), new ItemStack(ModItems.tracer_token));
	}
}
