package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.Hero;

public class ItemGenjiArmor extends ItemMWArmor
{
	public ItemGenjiArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(Hero.GENJI, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
