package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemTracerArmor extends ItemMWArmor
{
	public ItemTracerArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(EnumHero.TRACER, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
