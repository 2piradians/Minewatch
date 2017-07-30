package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemReaperArmor extends ItemMWArmor
{
	public ItemReaperArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(EnumHero.REAPER, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
