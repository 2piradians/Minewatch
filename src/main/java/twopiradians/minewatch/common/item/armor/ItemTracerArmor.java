package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.Hero;

public class ItemTracerArmor extends ItemMWArmor
{
	public ItemTracerArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(Hero.TRACER, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
