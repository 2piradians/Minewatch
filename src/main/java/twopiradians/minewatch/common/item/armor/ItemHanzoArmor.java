package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.Hero;

public class ItemHanzoArmor extends ItemMWArmor 
{
	public ItemHanzoArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(Hero.HANZO, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
