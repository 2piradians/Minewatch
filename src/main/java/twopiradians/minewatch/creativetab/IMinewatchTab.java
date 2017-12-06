package twopiradians.minewatch.creativetab;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IMinewatchTab {

	public abstract NonNullList<ItemStack> getOrderedStacks();

}
