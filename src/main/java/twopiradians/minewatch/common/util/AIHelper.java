package twopiradians.minewatch.common.util;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class AIHelper {

	/**Returns first held ItemStack of the specified item*/
	@Nullable
	public static ItemStack getHeldItems(EntityLivingBase entity, Item item, EnumHand... hands) {
		if (entity != null && item != null)
			for (EnumHand hand : hands) {
				ItemStack stack = entity.getHeldItem(hand);
				if (stack != null && stack.getItem() == item)
					return stack;
			}
		return null;
	}
	
}