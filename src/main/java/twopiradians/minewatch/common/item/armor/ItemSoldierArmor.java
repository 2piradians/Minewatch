package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemSoldierArmor extends ItemMWArmor
{
	public ItemSoldierArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(EnumHero.SOLDIER76, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
