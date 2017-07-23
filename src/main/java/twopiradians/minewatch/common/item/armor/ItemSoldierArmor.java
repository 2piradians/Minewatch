package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.Hero;

public class ItemSoldierArmor extends ModArmor
{
	public ItemSoldierArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(Hero.SOLDIER76, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
