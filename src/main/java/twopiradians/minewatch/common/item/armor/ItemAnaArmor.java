package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemAnaArmor extends ItemMWArmor
{
	public ItemAnaArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(EnumHero.ANA, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
