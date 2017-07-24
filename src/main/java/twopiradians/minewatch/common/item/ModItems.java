package twopiradians.minewatch.common.item;

import java.util.ArrayList;

import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.item.armor.ItemAnaArmor;
import twopiradians.minewatch.common.item.armor.ItemGenjiArmor;
import twopiradians.minewatch.common.item.armor.ItemHanzoArmor;
import twopiradians.minewatch.common.item.armor.ItemMcCreeArmor;
import twopiradians.minewatch.common.item.armor.ItemReaperArmor;
import twopiradians.minewatch.common.item.armor.ItemReinhardtArmor;
import twopiradians.minewatch.common.item.armor.ItemSoldierArmor;
import twopiradians.minewatch.common.item.armor.ItemTracerArmor;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemHanzoBow;
import twopiradians.minewatch.common.item.weapon.ItemMcCreeGun;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;
import twopiradians.minewatch.common.item.weapon.ItemReinhardtHammer;
import twopiradians.minewatch.common.item.weapon.ItemSoldierGun;
import twopiradians.minewatch.common.item.weapon.ItemTracerPistol;

public class ModItems {

	public static ArrayList<Item> jsonModelItems  = new ArrayList<Item>();
	public static ArrayList<Item> objModelItems  = new ArrayList<Item>();
	public static ArrayList<Item> tokens = new ArrayList<Item>();

	public static ArmorMaterial reaper = EnumHelper.addArmorMaterial("reaper", "minewatch:reaper", 20, new int[] {2,3,3,2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item reaper_helmet;
	public static Item reaper_chestplate;
	public static Item reaper_leggings;
	public static Item reaper_boots;
	public static Item reaper_shotgun;
	public static Item reaper_token;

	public static ArmorMaterial hanzo = EnumHelper.addArmorMaterial("hanzo", "minewatch:hanzo", 20, new int[] {2,3,3,2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item hanzo_helmet;
	public static Item hanzo_chestplate;
	public static Item hanzo_leggings;
	public static Item hanzo_boots;
	public static Item hanzo_bow;
	public static Item hanzo_token;

	public static ArmorMaterial reinhardt = EnumHelper.addArmorMaterial("reinhardt", "minewatch:reinhardt", 20, new int[] {4,6,6,4}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item reinhardt_helmet;
	public static Item reinhardt_chestplate;
	public static Item reinhardt_leggings;
	public static Item reinhardt_boots;
	public static Item reinhardt_hammer;
	public static Item reinhardt_token;

	public static ArmorMaterial ana = EnumHelper.addArmorMaterial("ana", "minewatch:ana", 20, new int[] {2,3,3,2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item ana_helmet;
	public static Item ana_chestplate;
	public static Item ana_leggings;
	public static Item ana_boots;
	public static Item ana_rifle;
	public static Item ana_token;

	public static ArmorMaterial genji = EnumHelper.addArmorMaterial("genji", "minewatch:genji", 20, new int[] {2,3,3,2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item genji_helmet;
	public static Item genji_chestplate;
	public static Item genji_leggings;
	public static Item genji_boots;
	public static Item genji_shuriken;
	public static Item genji_shuriken_single; // used for projectile
	public static Item genji_token;

	public static ArmorMaterial tracer = EnumHelper.addArmorMaterial("tracer", "minewatch:tracer", 20, new int[] {2,2,2,2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item tracer_helmet;
	public static Item tracer_chestplate;
	public static Item tracer_leggings;
	public static Item tracer_boots;
	public static Item tracer_pistol;
	public static Item tracer_token;

	public static ArmorMaterial mccree = EnumHelper.addArmorMaterial("mccree", "minewatch:mccree", 20, new int[] {2,3,3,2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item mccree_helmet;
	public static Item mccree_chestplate;
	public static Item mccree_leggings;
	public static Item mccree_boots;
	public static Item mccree_gun;
	public static Item mccree_token;

	public static ArmorMaterial soldier = EnumHelper.addArmorMaterial("soldier", "minewatch:soldier", 20, new int[] {2,3,3,2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item soldier_helmet;
	public static Item soldier_chestplate;
	public static Item soldier_leggings;
	public static Item soldier_boots;
	public static Item soldier_gun;
	public static Item soldier_token;

	@Mod.EventBusSubscriber
	public static class RegistrationHandler {

		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event) {
			reaper_helmet = registerItem(event.getRegistry(), new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.HEAD), "reaper_helmet", true, false);
			reaper_chestplate = registerItem(event.getRegistry(), new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.CHEST), "reaper_chestplate", true, false);
			reaper_leggings = registerItem(event.getRegistry(), new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.LEGS), "reaper_leggings", true, false);
			reaper_boots = registerItem(event.getRegistry(), new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.FEET), "reaper_boots", true, false);
			reaper_shotgun = registerItem(event.getRegistry(), new ItemReaperShotgun(), "reaper_shotgun", true, true);
			reaper_token = registerItem(event.getRegistry(), new ModTokens.ItemReaperToken(), "reaper_token", true, false);

			hanzo_helmet = registerItem(event.getRegistry(), new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.HEAD), "hanzo_helmet", true, false);
			hanzo_chestplate = registerItem(event.getRegistry(), new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.CHEST), "hanzo_chestplate", true, false);
			hanzo_leggings = registerItem(event.getRegistry(), new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.LEGS), "hanzo_leggings", true, false);
			hanzo_boots = registerItem(event.getRegistry(), new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.FEET), "hanzo_boots", true, false);
			hanzo_bow = registerItem(event.getRegistry(), new ItemHanzoBow(), "hanzo_bow", true, true);
			hanzo_token = registerItem(event.getRegistry(), new ModTokens.ItemHanzoToken(), "hanzo_token", true, false);

			reinhardt_helmet = registerItem(event.getRegistry(), new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.HEAD), "reinhardt_helmet", true, false);
			reinhardt_chestplate = registerItem(event.getRegistry(), new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.CHEST), "reinhardt_chestplate", true, false);
			reinhardt_leggings = registerItem(event.getRegistry(), new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.LEGS), "reinhardt_leggings", true, false);
			reinhardt_boots = registerItem(event.getRegistry(), new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.FEET), "reinhardt_boots", true, false);
			reinhardt_hammer = registerItem(event.getRegistry(), new ItemReinhardtHammer(), "reinhardt_hammer", true, true);
			reinhardt_token = registerItem(event.getRegistry(), new ModTokens.ItemReinhardtToken(), "reinhardt_token", true, false);

			ana_helmet = registerItem(event.getRegistry(), new ItemAnaArmor(ana, 0, EntityEquipmentSlot.HEAD), "ana_helmet", true, false);
			ana_chestplate = registerItem(event.getRegistry(), new ItemAnaArmor(ana, 0, EntityEquipmentSlot.CHEST), "ana_chestplate", true, false);
			ana_leggings = registerItem(event.getRegistry(), new ItemAnaArmor(ana, 0, EntityEquipmentSlot.LEGS), "ana_leggings", true, false);
			ana_boots = registerItem(event.getRegistry(), new ItemAnaArmor(ana, 0, EntityEquipmentSlot.FEET), "ana_boots", true, false);
			ana_rifle = registerItem(event.getRegistry(), new ItemAnaRifle(), "ana_rifle", true, true);
			ana_token = registerItem(event.getRegistry(), new ModTokens.ItemAnaToken(), "ana_token", true, false);

			genji_helmet = registerItem(event.getRegistry(), new ItemGenjiArmor(genji, 0, EntityEquipmentSlot.HEAD), "genji_helmet", true, false);
			genji_chestplate = registerItem(event.getRegistry(), new ItemGenjiArmor(genji, 0, EntityEquipmentSlot.CHEST), "genji_chestplate", true, false);
			genji_leggings = registerItem(event.getRegistry(), new ItemGenjiArmor(genji, 0, EntityEquipmentSlot.LEGS), "genji_leggings", true, false);
			genji_boots = registerItem(event.getRegistry(), new ItemGenjiArmor(genji, 0, EntityEquipmentSlot.FEET), "genji_boots", true, false);
			genji_shuriken = registerItem(event.getRegistry(), new ItemGenjiShuriken(), "genji_shuriken", true, true);
			genji_shuriken_single = registerItem(event.getRegistry(), new ItemGenjiShuriken(), "genji_shuriken_single", false, true); 
			genji_token = registerItem(event.getRegistry(), new ModTokens.ItemGenjiToken(), "genji_token", true, false);

			tracer_helmet = registerItem(event.getRegistry(), new ItemTracerArmor(tracer, 0, EntityEquipmentSlot.HEAD), "tracer_helmet", true, false);
			tracer_chestplate = registerItem(event.getRegistry(), new ItemTracerArmor(tracer, 0, EntityEquipmentSlot.CHEST), "tracer_chestplate", true, false);
			tracer_leggings = registerItem(event.getRegistry(), new ItemTracerArmor(tracer, 0, EntityEquipmentSlot.LEGS), "tracer_leggings", true, false);
			tracer_boots = registerItem(event.getRegistry(), new ItemTracerArmor(tracer, 0, EntityEquipmentSlot.FEET), "tracer_boots", true, false);
			tracer_pistol = registerItem(event.getRegistry(), new ItemTracerPistol(), "tracer_pistol", true, true);
			tracer_token = registerItem(event.getRegistry(), new ModTokens.ItemTracerToken(), "tracer_token", true, false);

			mccree_helmet = registerItem(event.getRegistry(), new ItemMcCreeArmor(mccree, 0, EntityEquipmentSlot.HEAD), "mccree_helmet", true, false);
			mccree_chestplate = registerItem(event.getRegistry(), new ItemMcCreeArmor(mccree, 0, EntityEquipmentSlot.CHEST), "mccree_chestplate", true, false);
			mccree_leggings = registerItem(event.getRegistry(), new ItemMcCreeArmor(mccree, 0, EntityEquipmentSlot.LEGS), "mccree_leggings", true, false);
			mccree_boots = registerItem(event.getRegistry(), new ItemMcCreeArmor(mccree, 0, EntityEquipmentSlot.FEET), "mccree_boots", true, false);
			mccree_gun = registerItem(event.getRegistry(), new ItemMcCreeGun(), "mccree_gun", true, true);
			mccree_token = registerItem(event.getRegistry(), new ModTokens.ItemMcCreeToken(), "mccree_token", true, false);

			soldier_helmet = registerItem(event.getRegistry(), new ItemSoldierArmor(soldier, 0, EntityEquipmentSlot.HEAD), "soldier_helmet", true, false);
			soldier_chestplate = registerItem(event.getRegistry(), new ItemSoldierArmor(soldier, 0, EntityEquipmentSlot.CHEST), "soldier_chestplate", true, false);
			soldier_leggings = registerItem(event.getRegistry(), new ItemSoldierArmor(soldier, 0, EntityEquipmentSlot.LEGS), "soldier_leggings", true, false);
			soldier_boots = registerItem(event.getRegistry(), new ItemSoldierArmor(soldier, 0, EntityEquipmentSlot.FEET), "soldier_boots", true, false);
			soldier_gun = registerItem(event.getRegistry(), new ItemSoldierGun(), "soldier_gun", true, true);
			soldier_token = registerItem(event.getRegistry(), new ModTokens.ItemMcCreeToken(), "soldier_token", true, false);
		}

		private static Item registerItem(IForgeRegistry<Item> registry, Item item, String unlocalizedName, boolean addToTab, boolean usesObjModel) {
			if (usesObjModel && Config.useObjModels)
				objModelItems.add(item);
			else
				jsonModelItems.add(item);
			if (item instanceof ModTokens)
				tokens.add(item);
			item.setUnlocalizedName(unlocalizedName);
			item.setRegistryName(Minewatch.MODID, unlocalizedName);
			if (addToTab)
				item.setCreativeTab(Minewatch.tab);
			registry.register(item);
			return item;
		}
	}
}
