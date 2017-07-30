package twopiradians.minewatch.common;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.recipe.ShapelessMatchingDamageRecipe;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.PacketSyncKeys;

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
		Minewatch.network.registerMessage(PacketSyncKeys.Handler.class, PacketSyncKeys.class, id++, Side.SERVER);
	}

	public void spawnParticlesHealthPlus(EntityLivingBase entity) { }

	protected void registerEventListeners() {
		MinecraftForge.EVENT_BUS.register(new Config());
		MinecraftForge.EVENT_BUS.register(new ItemMWToken());
	}

	private void registerCraftingRecipes() {
		RecipeSorter.register("Matching Damage Recipe", ShapelessMatchingDamageRecipe.class, Category.SHAPELESS, "");
		
		for (EnumHero hero : EnumHero.values()) {
			GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(hero.helmet), new ItemStack(hero.token), new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));
			GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(hero.chestplate), new ItemStack(hero.token), new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));
			GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(hero.leggings), new ItemStack(hero.token), new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));
			GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(hero.boots), new ItemStack(hero.token), new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));
			GameRegistry.addShapelessRecipe(new ItemStack(hero.weapon), new ItemStack(hero.token));
		}
	}

}
