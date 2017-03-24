package twopiradians.minewatch.common.item.armor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ModArmor extends ItemArmor 
{
	public static final EntityEquipmentSlot[] SLOTS = new EntityEquipmentSlot[] 
			{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

	public ModArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
	}

	public static boolean isSet(EntityPlayer player, ArmorMaterial material) {
		if (player == null) 
			return false;
		for (EntityEquipmentSlot slot : SLOTS) {
			ItemStack stack = player.getItemStackFromSlot(slot);
			if (stack == null || !(stack.getItem() instanceof ModArmor)
					|| ((ModArmor)(stack.getItem())).getArmorMaterial() != material) 
				return false;
		}
		return true;
	}
}
