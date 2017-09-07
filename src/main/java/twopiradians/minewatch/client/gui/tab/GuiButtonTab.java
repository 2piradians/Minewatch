package twopiradians.minewatch.client.gui.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import twopiradians.minewatch.client.gui.tab.GuiTab.Screen;

public class GuiButtonTab extends GuiButton {

	protected Screen screen;

	public GuiButtonTab(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Screen screen) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.screen = screen;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		this.visible = GuiTab.currentScreen == screen;
		
		super.drawButton(mc, mouseX, mouseY);
    }

}
