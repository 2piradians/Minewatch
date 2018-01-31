package twopiradians.minewatch.client.gui.tab;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;

public class GuiScrollingServers extends GuiScrollingList {

	private GuiTab gui;
	public static String hoveringText;

	public GuiScrollingServers(GuiTab gui, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
		super(gui.mc, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
		this.gui = gui;
	}

	@Override
	protected int getSize() {
		return gui.serverList.size();
	}

	public void connectToServer(int index) {
		if (index >= 0 && index < gui.serverList.size())
			gui.mc.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback() {
				@Override
				public void confirmClicked(boolean result, int id) {
					if (result)
						net.minecraftforge.fml.client.FMLClientHandler.instance().connectToServer(new GuiMultiplayer(new GuiMainMenu()), gui.serverList.get(index).getServerData());
					else
						gui.mc.displayGuiScreen(gui);
				}
			}, "", "Do you want to connect to "+gui.serverList.get(index).getServerData().serverName+TextFormatting.RESET+"?", 0));
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
		if (doubleClick || (this.isSelected(index) && mouseX-this.left > 16 && mouseX-this.left < 32)) 
			connectToServer(index);
		this.selectedIndex = index;
	}

	@Override
	protected boolean isSelected(int index) {
		return this.selectedIndex == index;
	}

	@Override
	protected void drawBackground() {}

	@Override
	protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GuiScrollingServers.hoveringText = null;
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (GuiScrollingServers.hoveringText != null) {
			gui.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(GuiScrollingServers.hoveringText)), mouseX, mouseY);
			GlStateManager.disableLighting();
		}
	}

	@Override
	protected void drawSlot(int index, int right, int top, int height, Tessellator tess) {	
		boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth &&
                mouseY >= this.top && mouseY <= this.bottom;
		gui.serverList.get(index).drawEntry(index, left+4, top, listWidth-10, 100, mouseX, mouseY, this.selectedIndex == index && isHovering);
	}

	public void setSelectedIndex(int index) {
		this.selectedIndex = index;
	}

	public int getSelectedIndex() {
		return this.selectedIndex;
	}

}