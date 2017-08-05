package twopiradians.minewatch.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import twopiradians.minewatch.common.hero.EnumHero;

public class MinewatchTab extends CreativeTabs
{
	public MinewatchTab(String label) {
		super(label);
	}
	
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(EnumHero.REAPER.token);
	}
}
