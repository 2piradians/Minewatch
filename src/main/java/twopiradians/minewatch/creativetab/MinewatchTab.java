package twopiradians.minewatch.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class MinewatchTab extends CreativeTabs
{
	public MinewatchTab(String label) {
		super(label);
	}
	
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(Items.CLOCK);
	}
}
