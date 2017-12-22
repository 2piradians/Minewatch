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
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		this.visible = gui.currentScreen == Screen.QUESTION_MARK;

		if (this.visible)
			mc.fontRenderer.drawString(displayString, this.x+this.width/2-mc.fontRenderer.getStringWidth(displayString)/2, y+height/2-mc.fontRenderer.FONT_HEIGHT/2, 0xFFFFFF);
	}

}
