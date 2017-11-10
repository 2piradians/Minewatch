package twopiradians.minewatch.client.gui.tab;

import micdoodle8.mods.galacticraft.api.client.tabs.AbstractTab;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import twopiradians.minewatch.common.hero.EnumHero;

public class InventoryTab extends AbstractTab
{
	public static final InventoryTab INSTANCE = new InventoryTab();

	public InventoryTab() {
		super(0, 0, 0, new ItemStack(EnumHero.REAPER.token));
	}

	@Override
	public void onTabClicked() {
		Minecraft.getMinecraft().displayGuiScreen(new GuiTab());
	}

	@Override
	public boolean shouldAddToList() {
		return true;
	}
}