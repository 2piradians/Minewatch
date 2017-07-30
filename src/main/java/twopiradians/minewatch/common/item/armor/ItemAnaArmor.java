package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.Hero;

public class ItemAnaArmor extends ItemMWArmor
{
	public ItemAnaArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(Hero.ANA, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
