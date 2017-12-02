package twopiradians.minewatch.client.gui.wildCard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.tab.GuiTab.Screen;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;

@SideOnly(Side.CLIENT)
public class GuiButtonWildCard extends GuiButton {
	
	protected Screen screen;
	public EnumHero hero;

	public GuiButtonWildCard(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, EnumHero hero) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.hero = hero;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {	
		boolean prev = this.hovered;
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

		if (hovered && prev != hovered)
			ModSoundEvents.GUI_HOVER.playSound(mc.player, 0.8f, 1.0f, true);

		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);
		if (this.hovered) 
			GlStateManager.translate(-4, -3, 0);
		double scale = this.hovered ? 0.15d : 0.12d;
		GlStateManager.scale(scale, scale, 1);
		mc.getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/items/"+hero.name.toLowerCase()+"_token.png"));
		GuiUtils.drawTexturedModalRect((int) (this.x/scale), (int) (this.y/scale), 0, 0, 256, 256, 0);
		
		GlStateManager.popMatrix();
	}
	
}