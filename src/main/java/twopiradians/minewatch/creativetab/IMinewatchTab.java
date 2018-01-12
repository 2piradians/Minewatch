package twopiradians.minewatch.creativetab;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IMinewatchTab {

	public abstract List<ItemStack> getOrderedStacks();

}