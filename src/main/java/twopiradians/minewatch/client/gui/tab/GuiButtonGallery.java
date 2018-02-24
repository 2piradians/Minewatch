package twopiradians.minewatch.client.gui.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import twopiradians.minewatch.client.gui.tab.GuiTab.Screen;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class GuiButtonGallery extends GuiButtonTab {

	private static final ResourceLocation GALLERY_OVERLAY = new ResourceLocation(Minewatch.MODID+":textures/gui/hero_gallery_overlay.png");
	public EnumHero hero;

	public GuiButtonGallery(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Screen screen, EnumHero hero) {
		super(buttonId, x, y, widthIn, heightIn, buttonText, screen);
		this.hero = hero;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {		
		this.visible = GuiTab.currentScreen == screen;
		boolean prev = this.hovered;
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        if (visible && hovered && prev != hovered)
        	ModSoundEvents.GUI_HOVER.playSound(mc.thePlayer, 0.8f, 1.0f, true);
        
		//super.drawButton(mc, mouseX, mouseY);
		//this.hovered = true;
        
		if (this.visible) {
			GlStateManager.pushMatrix();
			if (this.hovered)
				GlStateManager.translate(-5, -3, 0);
			GlStateManager.color(1, 1, 1);
			double iconScale = 0.52d;
			double bothScale = this.hovered ? 0.42d : 0.32d;
			double textScale = this.hovered ? 0.68d : 0.49f;
			GlStateManager.scale(bothScale, bothScale, 1);
			// icon
			GlStateManager.scale(iconScale, iconScale, 1);
			mc.getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+hero.name.toLowerCase()+"_icon_tab.png"));
			GuiUtils.drawTexturedModalRect((int) ((this.xPosition+(this.hovered ? 4.1f : 1))/bothScale/iconScale), (int) (this.yPosition/bothScale/iconScale), 0, 0, 240, 230, 0);
			GlStateManager.scale(1/iconScale, 1/iconScale, 1);
			// icon overlay
			mc.getTextureManager().bindTexture(GALLERY_OVERLAY);
			GuiUtils.drawTexturedModalRect((int) ((this.xPosition-8)/bothScale), (int) (this.yPosition/bothScale), 45, 45, 160, 155, 0);
			GlStateManager.color(hero.color.getRed()/255f, hero.color.getGreen()/255f, hero.color.getBlue()/255f);
			double percent = hero.skinInfo.length/hero.skinInfo.length;
			GuiUtils.drawTexturedModalRect((int) ((this.xPosition-(this.hovered ? 3 : 4))/bothScale), (int) ((this.yPosition+(this.hovered ? 56 : 43))/bothScale), 0, 0, (int) (percent*118), 20, 0);
			// text
			if (this.hovered)
				GlStateManager.translate(18, 26, 0);
			GlStateManager.scale(1/bothScale, 1/bothScale, 1);
			GlStateManager.scale(textScale, textScale, 1);
			String text = TextFormatting.ITALIC+""+TextFormatting.BOLD+hero.getFormattedName(true);
			mc.fontRendererObj.drawString(text, (int) ((this.xPosition+15)/textScale)-mc.fontRendererObj.getStringWidth(text)/2, (int) ((this.yPosition+37)/textScale), 0x293440);
			text = TextFormatting.BOLD+""+hero.skinInfo.length+"/"+hero.skinInfo.length;
			mc.fontRendererObj.drawString(text, (int) ((this.xPosition+14)/textScale)-mc.fontRendererObj.getStringWidth(text)/2, (int) ((this.yPosition+(this.hovered ? 47 : 45))/textScale), 0xFFFFFF, true);
			GlStateManager.popMatrix();
		}

	}

}
