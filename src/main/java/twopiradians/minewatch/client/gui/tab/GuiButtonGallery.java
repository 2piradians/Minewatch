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
	private static final double OVERALL_SCALE = 1d;
	public EnumHero hero;

	public GuiButtonGallery(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Screen screen, EnumHero hero) {
		super(buttonId, x, y, widthIn, heightIn, buttonText, screen);
		this.hero = hero;
	}

	@Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.enabled && this.visible && mouseX >= this.x*OVERALL_SCALE && mouseY >= this.y*OVERALL_SCALE && mouseX < this.x*OVERALL_SCALE + this.width*OVERALL_SCALE && mouseY < this.y*OVERALL_SCALE + this.height*OVERALL_SCALE;
    }
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {		
		this.visible = GuiTab.currentScreen == screen;
		boolean prev = this.hovered;
        this.hovered = mouseX >= this.x*OVERALL_SCALE && mouseY >= this.y*OVERALL_SCALE && mouseX < this.x*OVERALL_SCALE + this.width*OVERALL_SCALE && mouseY < this.y*OVERALL_SCALE + this.height*OVERALL_SCALE;

        if (visible && hovered && prev != hovered)
        	ModSoundEvents.GUI_HOVER.playSound(mc.player, 0.8f, 1.0f, true);
        
		//super.drawButton(mc, mouseX, mouseY, partialTicks);
		//this.hovered = true;
        
		if (this.visible) {
			GlStateManager.pushMatrix();
			if (this.hovered)
				GlStateManager.translate(-5, -3, 0);
			GlStateManager.color(1, 1, 1);
			//GlStateManager.scale(OVERALL_SCALE, OVERALL_SCALE, 1);
			double iconScale = 0.52d;
			double bothScale = this.hovered ? 0.42d : 0.32d;
			double textScale = this.hovered ? 0.68d : 0.49f;
			GlStateManager.scale(bothScale, bothScale, 1);
			// icon
			GlStateManager.scale(iconScale, iconScale, 1);
			mc.getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+hero.name.toLowerCase()+"_icon_tab.png"));
			GuiUtils.drawTexturedModalRect((int) ((this.x+(this.hovered ? 4.1f : 1))/bothScale/iconScale), (int) (this.y/bothScale/iconScale), 0, 0, 240, 230, 0);
			GlStateManager.scale(1/iconScale, 1/iconScale, 1);
			// icon overlay
			mc.getTextureManager().bindTexture(GALLERY_OVERLAY);
			GuiUtils.drawTexturedModalRect((int) ((this.x-8)/bothScale), (int) (this.y/bothScale), 45, 45, 160, 155, 0);
			GlStateManager.color(hero.color.getRed()/255f, hero.color.getGreen()/255f, hero.color.getBlue()/255f);
			double percent = hero.skinInfo.length/hero.skinInfo.length;
			GuiUtils.drawTexturedModalRect((int) ((this.x-(this.hovered ? 3 : 4))/bothScale), (int) ((this.y+(this.hovered ? 56 : 43))/bothScale), 0, 0, (int) (percent*118), 20, 0);
			// text
			if (this.hovered)
				GlStateManager.translate(18, 26, 0);
			GlStateManager.scale(1/bothScale, 1/bothScale, 1);
			GlStateManager.scale(textScale, textScale, 1);
			String text = TextFormatting.ITALIC+""+TextFormatting.BOLD+hero.getFormattedName(true);
			mc.fontRenderer.drawString(text, (int) ((this.x+15)/textScale)-mc.fontRenderer.getStringWidth(text)/2, (int) ((this.y+37)/textScale), 0x293440);
			text = TextFormatting.BOLD+""+hero.skinInfo.length+"/"+hero.skinInfo.length;
			mc.fontRenderer.drawString(text, (int) ((this.x+14)/textScale)-mc.fontRenderer.getStringWidth(text)/2, (int) ((this.y+(this.hovered ? 47 : 45))/textScale), 0xFFFFFF, true);
			GlStateManager.popMatrix();
		}

	}

}
