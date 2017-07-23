package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.Hero;

public class ItemTracerArmor extends ModArmor
{
	public ItemTracerArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(Hero.TRACER, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
