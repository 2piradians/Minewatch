package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemHanzoArmor extends ItemMWArmor 
{
	public ItemHanzoArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(EnumHero.HANZO, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
