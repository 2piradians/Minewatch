package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemGenjiArmor extends ItemMWArmor
{
	public ItemGenjiArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(EnumHero.GENJI, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
