package twopiradians.minewatch.creativetab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.tab.GuiTab;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

public class MinewatchTab extends CreativeTabs {
		
	public MinewatchTab(String label) {
		super(label);
	}
	
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(EnumHero.REAPER.token);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(NonNullList<ItemStack> list) {
		System.out.println(Minecraft.getMinecraft().currentScreen);
		if (Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative) {
			
			Minecraft.getMinecraft().displayGuiScreen(new GuiTab());
		}
	}
	
}