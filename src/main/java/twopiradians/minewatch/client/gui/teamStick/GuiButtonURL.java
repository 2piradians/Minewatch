package twopiradians.minewatch.client.gui.teamStick;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import twopiradians.minewatch.client.gui.teamStick.GuiTeamStick.Screen;

public class GuiButtonURL extends GuiButton {

	public String url;
	private GuiTeamStick gui;

	public GuiButtonURL(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String url, GuiTeamStick gui) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.url = url;
		this.gui = gui;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		this.visible = gui.currentScreen == Screen.QUESTION_MARK;

		if (this.visible)
			mc.fontRendererObj.drawString(displayString, this.xPosition+this.width/2-mc.fontRendererObj.getStringWidth(displayString)/2, yPosition+height/2-mc.fontRendererObj.FONT_HEIGHT/2, 0xFFFFFF);
	}

}
