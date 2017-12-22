package twopiradians.minewatch.client.gui.teamStick;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import twopiradians.minewatch.client.gui.teamStick.GuiTeamStick.Screen;
import twopiradians.minewatch.common.util.ColorHelper;

public class GuiButtonTeamColor extends GuiButton {

	private GuiTeamStick gui;
	private int foregroundColor;
	private int backgroundColor;
	private int checkColor;

	public GuiButtonTeamColor(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, GuiTeamStick gui) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.gui = gui;
		foregroundColor = new Color(ColorHelper.getForegroundColor(TextFormatting.fromColorIndex(buttonId))).getRGB();
		backgroundColor = new Color(ColorHelper.getBackgroundColor(TextFormatting.fromColorIndex(buttonId))).getRGB();
		this.checkColor = new Color(this.backgroundColor).brighter().brighter().getRGB();
		switch (TextFormatting.fromColorIndex(buttonId)) {
		case BLACK:
			this.checkColor = new Color(0x555555).getRGB();
			break;
		case DARK_BLUE:
			this.checkColor = new Color(0x5555FF).darker().getRGB();
			break;
		}
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		this.visible = gui.currentScreen == Screen.CREATE_TEAM || 
				(gui.currentScreen == Screen.EDIT_TEAM && gui.getSelectedTeam() != null);

		if (this.visible) {
			GlStateManager.pushMatrix();
			Gui.drawRect(this.x-2, this.y-2, this.x+this.width+2, this.y+this.height+2, this.backgroundColor);
			Gui.drawRect(this.x, this.y, this.x+this.width, this.y+this.height, this.foregroundColor);
			if ((gui.currentScreen == Screen.CREATE_TEAM && gui.selectedColor.getColorIndex() == this.id) || (gui.getSelectedTeam() != null && ((gui.getSelectedTeam().getColor().getColorIndex() == this.id) || 
					(this.id == TextFormatting.WHITE.getColorIndex() && !gui.getSelectedTeam().getColor().isColor())))) {
				double scale = 2d;
				GlStateManager.scale(scale, scale, scale);
				this.drawCenteredString(mc.fontRenderer, TextFormatting.BOLD+String.valueOf('\u2713'), (int) ((this.x+this.width/2+4f)/scale), (int) ((this.y+(this.height-8)/2)/scale)-3, this.checkColor);
			}
			GlStateManager.popMatrix();
		}
	}

}
