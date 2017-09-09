package twopiradians.minewatch.client.gui.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import twopiradians.minewatch.client.gui.tab.GuiTab.Screen;
import twopiradians.minewatch.common.hero.EnumHero;

public class GuiButtonSkin extends GuiButton {

	protected Screen screen;

	public GuiButtonSkin(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Screen screen) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.screen = screen;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		this.visible = GuiTab.currentScreen == screen && id < GuiTab.galleryHero.skinInfo.length;
		
		if (this.visible && !this.displayString.equals("?")) {
			this.enabled = GuiTab.galleryHero.getSkin(mc.player.getPersistentID()) != this.id;
			EnumHero.Skin skin = GuiTab.galleryHero.skinInfo[id];
			this.displayString = skin.owName.toUpperCase();
		}
		
		super.drawButton(mc, mouseX, mouseY);
    }

}
