package twopiradians.minewatch.client.gui.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import twopiradians.minewatch.client.gui.tab.GuiTab.Screen;

public class GuiButtonGalleryHero extends GuiButtonTab {

	private static final float Y_SCALE = 1.5f;

	public GuiButtonGalleryHero(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Screen screen) {
		super(buttonId, x, y, widthIn, heightIn, buttonText, screen);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {		
		this.visible = GuiTab.currentScreen == screen;

		if (this.visible) {
			// manually drawn to account for different height
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, this.yPosition-this.yPosition*Y_SCALE, 0);
			GlStateManager.scale(1, Y_SCALE, 1);
			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + (20 * Y_SCALE);
			int i = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
			this.mouseDragged(mc, mouseX, mouseY);
			GlStateManager.popMatrix();
			
			// Skins
			if (this.id == 1) {
				this.drawString(mc.fontRendererObj, 
						TextFormatting.BOLD+"SKINS "+TextFormatting.RESET+
						GuiTab.galleryHero.skinInfo.length+TextFormatting.GRAY+"/"+GuiTab.galleryHero.skinInfo.length, 
						this.xPosition+5, this.yPosition+6, 0xFFFFFF);
				this.drawString(mc.fontRendererObj, 
						TextFormatting.DARK_AQUA+TextFormatting.getTextWithoutFormattingCodes(GuiTab.galleryHero.skinInfo[GuiTab.galleryHero.getSkin(mc.player.getPersistentID())].owName).toUpperCase(),
						this.xPosition+5, this.yPosition+16, 0xFFFFFF);
			}		
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + (20 * Y_SCALE);
	}

}
