package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemMcCreeArmor extends ItemMWArmor
{
	public ItemMcCreeArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(EnumHero.MCCREE, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
