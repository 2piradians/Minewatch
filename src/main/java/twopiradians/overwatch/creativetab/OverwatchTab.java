package twopiradians.overwatch.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class OverwatchTab extends CreativeTabs
{
	public OverwatchTab(String label)
	{
		super(label);
	}
	
	@Override
	public ItemStack getTabIconItem() 
	{
		return new ItemStack(Items.CLOCK);
	}
}
