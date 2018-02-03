package twopiradians.minewatch.client.gui.tab;

import java.awt.Color;
import java.util.ArrayList;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;
import twopiradians.minewatch.client.gui.tab.GuiTab.Screen;

public class GuiButtonTab extends GuiButton {

	protected Screen screen;
	@Nullable
	protected Color color;
	protected Predicate<GuiTab> enabledPredicate;

	public GuiButtonTab(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Screen screen) {
		this(buttonId, x, y, widthIn, heightIn, buttonText, -1, screen, null);
	}
	
	public GuiButtonTab(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Screen screen, Predicate<GuiTab> enabledPredicate) {
		this(buttonId, x, y, widthIn, heightIn, buttonText, -1, screen, enabledPredicate);
	}

	public GuiButtonTab(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, int color, Screen screen, @Nullable Predicate<GuiTab> enabledPredicate) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.screen = screen;
		this.color = color == -1 ? null : new Color(color);
		this.enabledPredicate = enabledPredicate;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		this.visible = GuiTab.currentScreen == screen;
		this.enabled = this.enabledPredicate == null || this.enabledPredicate.apply(GuiTab.activeTab);

		// draw button - copied to modify color
		if (this.visible)
		{
			FontRenderer fontrenderer = mc.fontRenderer;
			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			if (color == null)
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			else
				GlStateManager.color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 1.0F);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int i = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
			this.mouseDragged(mc, mouseX, mouseY);
			int j = 14737632;

			if (packedFGColour != 0)
			{
				j = packedFGColour;
			}
			else
				if (!this.enabled)
				{
					j = 10526880;
				}
				else if (this.hovered)
				{
					j = 16777120;
				}

			this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
		
			// select button hover text
			if (this.hovered && this.displayString.equalsIgnoreCase("SELECT") && GuiTab.galleryHero != null) {
				GuiTab.activeTab.drawHoveringText(new ArrayList<String>() {{add("Equips you with the armor and weapon(s) for "+GuiTab.galleryHero.name);}}, mouseX, mouseY);
			}
		}

		// sync button
		if (this.visible && this.displayString.equals("")) {
			GlStateManager.pushMatrix();
			float scale = 2.2f;
			GlStateManager.scale(scale, scale, 1);
			this.drawCenteredString(mc.fontRenderer, String.valueOf('\u21c6'), (int) ((this.x+this.width/2+2f)/scale), (int) ((this.y+(this.height-8)/2)/scale)-2, 0xFFFFFF);
			GlStateManager.popMatrix();

			if (this.hovered) 
				GuiUtils.drawHoveringText(new ArrayList<String>() {{add("Sync config to server");}}, mouseX, mouseY, mc.displayWidth, mc.displayHeight, -1, mc.fontRenderer);
		}
	}

}
