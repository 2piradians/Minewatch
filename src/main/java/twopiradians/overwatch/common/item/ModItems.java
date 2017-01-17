package twopiradians.overwatch.common.item;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.overwatch.common.Overwatch;
import twopiradians.overwatch.common.item.armor.ItemReaperArmor;
import twopiradians.overwatch.common.item.weapon.ItemReaperShotgun;

public class ModItems 
{
	public static ArrayList<Item> allItems  = new ArrayList<Item>();;
	
	public static ArmorMaterial reaper = EnumHelper.addArmorMaterial("reaper", "overwatch:reaper", 0, new int[] {1,1,1,1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
	public static Item reaper_helmet;
	public static Item reaper_chestplate;
	public static Item reaper_leggings;
	public static Item reaper_boots;
	public static Item reaper_shotgun;
	
	public static void init ()
	{
		reaper_helmet = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.HEAD), "reaper_helmet");
		reaper_chestplate = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.CHEST), "reaper_chestplate");
		reaper_leggings = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.LEGS), "reaper_leggings");
		reaper_boots = registerItem(new ItemReaperArmor(reaper, 0, EntityEquipmentSlot.FEET), "reaper_boots");
		
		reaper_shotgun = registerItem(new ItemReaperShotgun(), "reaper_shotgun");
	}
	
	private static Item registerItem(Item item, String unlocalizedName) 
	{
		allItems.add(item);
		item.setUnlocalizedName(unlocalizedName);
		item.setRegistryName(Overwatch.MODID, unlocalizedName);
        item.setCreativeTab(Overwatch.tab);
        GameRegistry.register(item);
		return item;
	}
	
	public static void registerRenders() 
	{
		for (Item item : allItems)
			registerRender(item);
	}

	private static void registerRender(Item item) {		
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(Overwatch.MODID+":" + item.getUnlocalizedName().substring(5), "inventory"));
	}
}
