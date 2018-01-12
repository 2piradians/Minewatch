package twopiradians.minewatch.creativetab;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
	public Item getTabIconItem() {
		return new ItemStack(EnumHero.REAPER.token).getItem();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(List<ItemStack> list) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiTab());
		}
	}

	/**Trick tab into switching to inventory when exiting in creative*/
	@SideOnly(Side.CLIENT)
	public int getTabIndex() {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative && 
				((GuiContainerCreative)Minecraft.getMinecraft().currentScreen).getSelectedTabIndex() == CreativeTabs.INVENTORY.getTabIndex()) 
			return ((CreativeTabs)Minewatch.tabArmorWeapons).getTabIndex();
		else
			return CreativeTabs.INVENTORY.getTabIndex();
}
}