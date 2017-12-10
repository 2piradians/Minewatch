package twopiradians.minewatch.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.item.ModItems;

public class MapMakingTab extends CreativeTabs implements IMinewatchTab {
	
	public NonNullList<ItemStack> orderedStacks = NonNullList.create();
	
	public MapMakingTab(String label) {
		super(label);
	}
	
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(ModItems.team_stick);
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
