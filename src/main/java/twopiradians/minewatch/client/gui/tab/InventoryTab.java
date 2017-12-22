package twopiradians.minewatch.client.gui.tab;

import java.util.ArrayList;

import micdoodle8.mods.galacticraft.api.client.tabs.AbstractTab;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;
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
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		
		this.hovered = this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

		if (this.hovered)
			GuiUtils.drawHoveringText(new ArrayList<String>() {{add("Minewatch Tab");}}, mouseX, mouseY, mc.displayWidth, mc.displayHeight, -1, mc.fontRenderer);
	}
	
}