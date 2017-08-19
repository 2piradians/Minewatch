package twopiradians.minewatch.creativetab;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.hero.EnumHero;

public class MinewatchTab extends CreativeTabs {
	
	public List<ItemStack> orderedStacks = Lists.<ItemStack>newArrayList();
	
	public MinewatchTab(String label) {
		super(label);
	}
	
	@Override
	public Item getTabIconItem() {
		return new ItemStack(EnumHero.REAPER.token).getItem();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(List<ItemStack> list) {
		list.clear();
		list.addAll(orderedStacks);
	}
	
}
