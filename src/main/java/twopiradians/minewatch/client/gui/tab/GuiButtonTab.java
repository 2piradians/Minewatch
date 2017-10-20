package twopiradians.minewatch.client.gui.tab;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;
import twopiradians.minewatch.client.gui.tab.GuiTab.Screen;

public class GuiButtonTab extends GuiButton {

	protected Screen screen;

	public GuiButtonTab(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Screen screen) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.screen = screen;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {//String.valueOf('\u21c4')
		this.visible = GuiTab.currentScreen == screen;
		super.drawButton(mc, mouseX, mouseY);
		
		if (this.visible && this.displayString.equals("")) {
			GlStateManager.pushMatrix();
			float scale = 2.2f;
			GlStateManager.scale(scale, scale, 1);
            this.drawCenteredString(mc.fontRendererObj, String.valueOf('\u21c6'), (int) ((this.xPosition+this.width/2+2f)/scale), (int) ((this.yPosition+(this.height-8)/2)/scale)-2, 0xFFFFFF);
            GlStateManager.popMatrix();
            
            if (this.hovered) 
				GuiUtils.drawHoveringText(new ArrayList<String>() {{add("Sync config to server");}}, mouseX, mouseY, mc.displayWidth, mc.displayHeight, -1, mc.fontRendererObj);
		}
    }

}
