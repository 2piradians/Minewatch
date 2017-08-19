package twopiradians.minewatch.common.item;

import java.util.ArrayList;

import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class ModItems {

	public static ArrayList<Item> jsonModelItems  = new ArrayList<Item>();
	public static ArrayList<Item> objModelItems  = new ArrayList<Item>();
	
	public static Item genji_shuriken_single; // used for projectile

	@Mod.EventBusSubscriber
	public static class RegistrationHandler {

		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event) {
			for (EnumHero hero : EnumHero.values()) {
				hero.material = EnumHelper.addArmorMaterial(hero.name.toLowerCase(), Minewatch.MODNAME+":"+hero.name.toLowerCase(), 20, hero.armorReductionAmounts, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
				hero.helmet = (ItemMWArmor) registerItem(event.getRegistry(), new ItemMWArmor(hero, hero.material, 0, EntityEquipmentSlot.HEAD),	hero.name.toLowerCase()+"_helmet", true, false);
				hero.chestplate = (ItemMWArmor) registerItem(event.getRegistry(), new ItemMWArmor(hero, hero.material, 0, EntityEquipmentSlot.CHEST), hero.name.toLowerCase()+"_chestplate", true, false);
				hero.leggings = (ItemMWArmor) registerItem(event.getRegistry(), new ItemMWArmor(hero, hero.material, 0, EntityEquipmentSlot.LEGS), hero.name.toLowerCase()+"_leggings", true, false);
				hero.boots = (ItemMWArmor) registerItem(event.getRegistry(), new ItemMWArmor(hero, hero.material, 0, EntityEquipmentSlot.FEET), hero.name.toLowerCase()+"_boots", true, false);
				hero.weapon = (ItemMWWeapon) registerItem(event.getRegistry(), hero.weapon, hero.name.toLowerCase()+"_weapon", true, true);
				hero.token = (ItemMWToken) registerItem(event.getRegistry(), new ItemMWToken(), hero.name.toLowerCase()+"_token", true, false);
			}

			genji_shuriken_single = registerItem(event.getRegistry(), new ItemGenjiShuriken(), "genji_shuriken_single", false, true);

		}

		private static Item registerItem(IForgeRegistry<Item> registry, Item item, String unlocalizedName, boolean addToTab, boolean usesObjModel) {
			if (usesObjModel && Config.useObjModels)
				objModelItems.add(item);
			else
				jsonModelItems.add(item);
			item.setUnlocalizedName(unlocalizedName);
			item.setRegistryName(Minewatch.MODID, unlocalizedName);
			if (addToTab) {
				item.setCreativeTab(Minewatch.tab);
				Minewatch.tab.orderedStacks.add(new ItemStack(item));
			}
			registry.register(item);
			return item;
		}
	}
}
