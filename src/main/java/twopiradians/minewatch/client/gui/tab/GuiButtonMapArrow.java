package twopiradians.minewatch.client.gui.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import twopiradians.minewatch.client.gui.tab.GuiTab.Screen;

public class GuiButtonMapArrow extends GuiButtonTab {

	public GuiButtonMapArrow(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Screen screen) {
		super(buttonId, x, y, widthIn, heightIn, buttonText, screen);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		this.visible = GuiTab.currentScreen == screen;
		this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

		if (this.visible) {
			GlStateManager.pushMatrix();
			mc.getTextureManager().bindTexture(GuiTab.BACKGROUND);
			if (this.hovered)
				GlStateManager.color(136/255f, 146/255f, 201/255f);
			else
				GlStateManager.color(1, 1, 1);
			if (this.id == 0)
				this.drawTexturedModalRect(this.xPosition, this.yPosition, 235, 222, 20, 32);
			else if (this.id == 1)
				this.drawTexturedModalRect(this.xPosition, this.yPosition, 215, 222, 20, 32);
			GlStateManager.popMatrix();
		}
	}

}
