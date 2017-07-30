package twopiradians.minewatch.common.item.armor;

import net.minecraft.inventory.EntityEquipmentSlot;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemReinhardtArmor extends ItemMWArmor {

	public ItemReinhardtArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(EnumHero.REINHARDT, materialIn, renderIndexIn, equipmentSlotIn);
	}

}
