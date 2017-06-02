package twopiradians.minewatch.common.item;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.armor.ItemAnaArmor;
import twopiradians.minewatch.common.item.armor.ItemGenjiArmor;
import twopiradians.minewatch.common.item.armor.ItemHanzoArmor;
import twopiradians.minewatch.common.item.armor.ItemReaperArmor;
import twopiradians.minewatch.common.item.armor.ItemReinhardtArmor;
import twopiradians.minewatch.common.item.armor.ItemTracerArmor;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemHanzoBow;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;
import twopiradians.minewatch.common.item.weapon.ItemReinhardtHammer;
import twopiradians.minewatch.common.item.weapon.ItemTracerPistol;

public class ModItems 
{
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
	public static Item genji_token;
	
	public static ArmorMaterial tracer = EnumHelper.addArmorMaterial("tracer", "minewatch:tracer", 20, new int[] {2,3,3,2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item tracer_helmet;
	public static Item tracer_chestplate;
	public static Item tracer_leggings;
	public static Item tracer_boots;
	public static Item tracer_pistol;
	public static Item tracer_token;
	
	public static void preInit () {
		reaper_helmet = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.HEAD), "reaper_helmet", false);
		reaper_chestplate = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.CHEST), "reaper_chestplate", false);
		reaper_leggings = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.LEGS), "reaper_leggings", false);
		reaper_boots = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.FEET), "reaper_boots", false);
		reaper_shotgun = registerItem(new ItemReaperShotgun(), "reaper_shotgun", true);
		reaper_token = registerItem(new ModTokens.ItemReaperToken(), "reaper_token", false);
		
		hanzo_helmet = registerItem(new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.HEAD), "hanzo_helmet", false);
		hanzo_chestplate = registerItem(new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.CHEST), "hanzo_chestplate", false);
		hanzo_leggings = registerItem(new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.LEGS), "hanzo_leggings", false);
		hanzo_boots = registerItem(new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.FEET), "hanzo_boots", false);
		hanzo_bow = registerItem(new ItemHanzoBow(), "hanzo_bow", false);
		hanzo_token = registerItem(new ModTokens.ItemHanzoToken(), "hanzo_token", false);

		reinhardt_helmet = registerItem(new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.HEAD), "reinhardt_helmet", false);
		reinhardt_chestplate = registerItem(new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.CHEST), "reinhardt_chestplate", false);
		reinhardt_leggings = registerItem(new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.LEGS), "reinhardt_leggings", false);
		reinhardt_boots = registerItem(new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.FEET), "reinhardt_boots", false);
		reinhardt_hammer = registerItem(new ItemReinhardtHammer(), "reinhardt_hammer", false);
		reinhardt_token = registerItem(new ModTokens.ItemReinhardtToken(), "reinhardt_token", false);

		ana_helmet = registerItem(new ItemAnaArmor(ana, 0, EntityEquipmentSlot.HEAD), "ana_helmet", false);
		ana_chestplate = registerItem(new ItemAnaArmor(ana, 0, EntityEquipmentSlot.CHEST), "ana_chestplate", false);
		ana_leggings = registerItem(new ItemAnaArmor(ana, 0, EntityEquipmentSlot.LEGS), "ana_leggings", false);
		ana_boots = registerItem(new ItemAnaArmor(ana, 0, EntityEquipmentSlot.FEET), "ana_boots", false);
		ana_rifle = registerItem(new ItemAnaRifle(), "ana_rifle", false);
		ana_token = registerItem(new ModTokens.ItemAnaToken(), "ana_token", false);
		
		genji_helmet = registerItem(new ItemGenjiArmor(genji, 0, EntityEquipmentSlot.HEAD), "genji_helmet", false);
		genji_chestplate = registerItem(new ItemGenjiArmor(genji, 0, EntityEquipmentSlot.CHEST), "genji_chestplate", false);
		genji_leggings = registerItem(new ItemGenjiArmor(genji, 0, EntityEquipmentSlot.LEGS), "genji_leggings", false);
		genji_boots = registerItem(new ItemGenjiArmor(genji, 0, EntityEquipmentSlot.FEET), "genji_boots", false);
		genji_shuriken = registerItem(new ItemGenjiShuriken(), "genji_shuriken", false);
		genji_token = registerItem(new ModTokens.ItemGenjiToken(), "genji_token", false);
		
		tracer_helmet = registerItem(new ItemTracerArmor(tracer, 0, EntityEquipmentSlot.HEAD), "tracer_helmet", false);
		tracer_chestplate = registerItem(new ItemTracerArmor(tracer, 0, EntityEquipmentSlot.CHEST), "tracer_chestplate", false);
		tracer_leggings = registerItem(new ItemTracerArmor(tracer, 0, EntityEquipmentSlot.LEGS), "tracer_leggings", false);
		tracer_boots = registerItem(new ItemTracerArmor(tracer, 0, EntityEquipmentSlot.FEET), "tracer_boots", false);
		tracer_pistol = registerItem(new ItemTracerPistol(), "tracer_pistol", false);
		tracer_token = registerItem(new ModTokens.ItemTracerToken(), "tracer_token", false);
	}
	
	private static Item registerItem(Item item, String unlocalizedName, boolean usesObjModel) {
		if (usesObjModel)
			objModelItems.add(item);
		else
			jsonModelItems.add(item);
		if (item instanceof ModTokens)
			tokens.add(item);
		item.setUnlocalizedName(unlocalizedName);
		item.setRegistryName(Minewatch.MODID, unlocalizedName);
        item.setCreativeTab(Minewatch.tab);
        GameRegistry.register(item);
		return item;
	}
	
	public static void registerRenders() {
		for (Item item : jsonModelItems)
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5), "inventory"));
	}
	
	public static void registerObjRenders() {
		for (Item item : objModelItems)
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5), "inventory"));	}

}
