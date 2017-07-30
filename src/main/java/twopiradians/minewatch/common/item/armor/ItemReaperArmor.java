package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.Hero;

public class ItemReaperArmor extends ItemMWArmor
{
	public ItemReaperArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(Hero.REAPER, materialIn, renderIndexIn, equipmentSlotIn);
	}
}
