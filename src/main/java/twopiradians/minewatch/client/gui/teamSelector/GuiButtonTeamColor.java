package twopiradians.minewatch.client.gui.teamSelector;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import twopiradians.minewatch.client.gui.teamSelector.GuiTeamSelector.Screen;

public class GuiButtonTeamColor extends GuiButton {

	private GuiTeamSelector gui;
	private int foregroundColor;
	private int backgroundColor;
	private int checkColor;

	public GuiButtonTeamColor(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, GuiTeamSelector gui) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.gui = gui;
		switch (TextFormatting.fromColorIndex(buttonId)) {
		case AQUA:
			foregroundColor = new Color(0x55FFFF).getRGB();
			backgroundColor = new Color(0x153F3F).getRGB();
			break;
		case BLACK:
			foregroundColor = new Color(0x000000).getRGB();
			backgroundColor = new Color(0x000000).getRGB();
			break;
		case BLUE:
			foregroundColor = new Color(0x5555FF).getRGB();
			backgroundColor = new Color(0x15153F).getRGB();
			break;
		case DARK_AQUA:
			foregroundColor = new Color(0x00AAAA).getRGB();
			backgroundColor = new Color(0x002A2A).getRGB();
			break;
		case DARK_BLUE:
			foregroundColor = new Color(0x0000AA).getRGB();
			backgroundColor = new Color(0x00002A).getRGB();
			break;
		case DARK_GRAY:
			foregroundColor = new Color(0x555555).getRGB();
			backgroundColor = new Color(0x151515).getRGB();
			break;
		case DARK_GREEN:
			foregroundColor = new Color(0x00AA00).getRGB();
			backgroundColor = new Color(0x002A00).getRGB();
			break;
		case DARK_PURPLE:
			foregroundColor = new Color(0xAA00AA).getRGB();
			backgroundColor = new Color(0x2A002A).getRGB();
			break;
		case DARK_RED:
			foregroundColor = new Color(0xAA0000).getRGB();
			backgroundColor = new Color(0x2A0000).getRGB();
			break;
		case GOLD:
			foregroundColor = new Color(0xFFAA00).getRGB();
			backgroundColor = new Color(0x2A2A00).getRGB();
			break;
		case GRAY:
			foregroundColor = new Color(0xAAAAAA).getRGB();
			backgroundColor = new Color(0x2A2A2A).getRGB();
			break;
		case GREEN:
			foregroundColor = new Color(0x55FF55).getRGB();
			backgroundColor = new Color(0x153F15).getRGB();
			break;
		case LIGHT_PURPLE:
			foregroundColor = new Color(0xFF55FF).getRGB();
			backgroundColor = new Color(0x3F153F).getRGB();
			break;
		case RED:
			foregroundColor = new Color(0xFF5555).getRGB();
			backgroundColor = new Color(0x3F1515).getRGB();
			break;
		case WHITE:
			foregroundColor = new Color(0xFFFFFF).getRGB();
			backgroundColor = new Color(0x3F3F3F).getRGB();
			break;
		case YELLOW:
			foregroundColor = new Color(0xFFFF55).getRGB();
			backgroundColor = new Color(0x3F3F15).getRGB();
			break;
		}
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
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		this.visible = gui.currentScreen == Screen.CREATE_TEAM || 
				(gui.currentScreen == Screen.MAIN && gui.getSelectedTeam() != null);

		if (this.visible) {
			GlStateManager.pushMatrix();
			Gui.drawRect(this.xPosition-2, this.yPosition-2, this.xPosition+this.width+2, this.yPosition+this.height+2, this.backgroundColor);
			Gui.drawRect(this.xPosition, this.yPosition, this.xPosition+this.width, this.yPosition+this.height, this.foregroundColor);
			if (gui.getSelectedTeam() != null && ((gui.getSelectedTeam().getChatFormat().getColorIndex() == this.id) || 
					(this.id == TextFormatting.WHITE.getColorIndex() && !gui.getSelectedTeam().getChatFormat().isColor()))) {
				double scale = 2d;
				GlStateManager.scale(scale, scale, scale);
				this.drawCenteredString(mc.fontRendererObj, TextFormatting.BOLD+String.valueOf('\u2713'), (int) ((this.xPosition+this.width/2+4f)/scale), (int) ((this.yPosition+(this.height-8)/2)/scale)-3, this.checkColor);
			}
			GlStateManager.popMatrix();
		}
	}

}
