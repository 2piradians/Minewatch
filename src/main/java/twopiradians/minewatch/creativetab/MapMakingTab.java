package twopiradians.minewatch.creativetab;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.item.ModItems;

public class MapMakingTab extends CreativeTabs implements IMinewatchTab {
	
	public List<ItemStack> orderedStacks = Lists.<ItemStack>newArrayList();
	
	public MapMakingTab(String label) {
		super(label);
	}
	
	@Override
	public Item getTabIconItem() {
		return new ItemStack(ModItems.team_stick).getItem();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(List<ItemStack> list) {
		list.clear();
		list.addAll(orderedStacks);
	}
	
	@Override
	public List<ItemStack> getOrderedStacks() {
		return orderedStacks;
	}
	
}
