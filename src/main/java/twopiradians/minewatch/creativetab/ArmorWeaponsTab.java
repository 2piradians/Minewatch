package twopiradians.minewatch.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.hero.EnumHero;

public class ArmorWeaponsTab extends CreativeTabs implements IMinewatchTab {
	
	public NonNullList<ItemStack> orderedStacks = NonNullList.create();
	
	public ArmorWeaponsTab(String label) {
		super(label);
	}
	
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(EnumHero.REAPER.token);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(NonNullList<ItemStack> list) {
		list.clear();
		list.addAll(orderedStacks);
	}

	@Override
	public NonNullList<ItemStack> getOrderedStacks() {
		return orderedStacks;
	}
	
}
