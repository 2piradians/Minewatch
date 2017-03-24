package twopiradians.minewatch.common.item;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.armor.ItemAnaArmor;
import twopiradians.minewatch.common.item.armor.ItemHanzoArmor;
import twopiradians.minewatch.common.item.armor.ItemReaperArmor;
import twopiradians.minewatch.common.item.armor.ItemReinhardtArmor;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.item.weapon.ItemHanzoBow;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;
import twopiradians.minewatch.common.item.weapon.ItemReinhardtRocketHammer;

public class ModItems 
{
	public static ArrayList<Item> allItems  = new ArrayList<Item>();;
	
	public static ArmorMaterial reaper = EnumHelper.addArmorMaterial("reaper", "minewatch:reaper", 0, new int[] {1,1,1,1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item reaper_helmet;
	public static Item reaper_chestplate;
	public static Item reaper_leggings;
	public static Item reaper_boots;
	public static Item reaper_shotgun;
	
	public static ArmorMaterial hanzo = EnumHelper.addArmorMaterial("hanzo", "minewatch:hanzo", 0, new int[] {1,1,1,1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item hanzo_helmet;
	public static Item hanzo_chestplate;
	public static Item hanzo_leggings;
	public static Item hanzo_boots;
	public static Item hanzo_bow;
	
	public static ArmorMaterial reinhardt = EnumHelper.addArmorMaterial("reinhardt", "minewatch:reinhardt", 0, new int[] {1,1,1,1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item reinhardt_helmet;
	public static Item reinhardt_chestplate;
	public static Item reinhardt_leggings;
	public static Item reinhardt_boots;
	public static Item reinhardt_rocket_hammer;
	
	public static ArmorMaterial ana = EnumHelper.addArmorMaterial("ana", "minewatch:ana", 0, new int[] {1,1,1,1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item ana_helmet;
	public static Item ana_chestplate;
	public static Item ana_leggings;
	public static Item ana_boots;
	public static Item ana_bow;
	
	public static void preInit () {
		reaper_helmet = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.HEAD), "reaper_helmet");
		reaper_chestplate = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.CHEST), "reaper_chestplate");
		reaper_leggings = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.LEGS), "reaper_leggings");
		reaper_boots = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.FEET), "reaper_boots");
		reaper_shotgun = registerItem(new ItemReaperShotgun(), "reaper_shotgun");
		
		hanzo_helmet = registerItem(new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.HEAD), "hanzo_helmet");
		hanzo_chestplate = registerItem(new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.CHEST), "hanzo_chestplate");
		hanzo_leggings = registerItem(new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.LEGS), "hanzo_leggings");
		hanzo_boots = registerItem(new ItemHanzoArmor(hanzo, 0, EntityEquipmentSlot.FEET), "hanzo_boots");
		hanzo_bow = registerItem(new ItemHanzoBow(), "hanzo_bow");
		
		reinhardt_helmet = registerItem(new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.HEAD), "reinhardt_helmet");
		reinhardt_chestplate = registerItem(new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.CHEST), "reinhardt_chestplate");
		reinhardt_leggings = registerItem(new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.LEGS), "reinhardt_leggings");
		reinhardt_boots = registerItem(new ItemReinhardtArmor(reinhardt, 0, EntityEquipmentSlot.FEET), "reinhardt_boots");
		reinhardt_rocket_hammer = registerItem(new ItemReinhardtRocketHammer(), "reinhardt_rocket_hammer");
		
		ana_helmet = registerItem(new ItemAnaArmor(ana, 0, EntityEquipmentSlot.HEAD), "ana_helmet");
		ana_chestplate = registerItem(new ItemAnaArmor(ana, 0, EntityEquipmentSlot.CHEST), "ana_chestplate");
		ana_leggings = registerItem(new ItemAnaArmor(ana, 0, EntityEquipmentSlot.LEGS), "ana_leggings");
		ana_boots = registerItem(new ItemAnaArmor(ana, 0, EntityEquipmentSlot.FEET), "ana_boots");
		ana_bow = registerItem(new ItemAnaRifle(), "ana_rifle");
	}
	
	private static Item registerItem(Item item, String unlocalizedName) {
		allItems.add(item);
		item.setUnlocalizedName(unlocalizedName);
		item.setRegistryName(Minewatch.MODID, unlocalizedName);
        item.setCreativeTab(Minewatch.tab);
        GameRegistry.register(item);
		return item;
	}
	
	public static void registerRenders() {
		for (Item item : allItems)
			registerRender(item);
	}

	private static void registerRender(Item item) {		
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5), "inventory"));
	}
}
