package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.Hero;

public class ItemMcCreeArmor extends ModArmor
{
	public ItemMcCreeArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(Hero.MCCREE, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
