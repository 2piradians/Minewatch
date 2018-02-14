package twopiradians.minewatch.client.gui.buttons;

import java.awt.Color;
import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import twopiradians.minewatch.client.gui.IGuiScreen;
import twopiradians.minewatch.client.gui.teamBlocks.GuiTeamBlock;

public class GuiButtonBase extends GuiButton {

	public enum Render {
		NORMAL, NONE, COLORED, TEXT
	}

	public IGuiScreen gui;
	public Predicate<IGuiScreen> shouldBeVisible;
	public Render render;
	public Color color = new Color(0xFFFFFF);
	public List<String> hoverText;
	public boolean noSound;

	public GuiButtonBase(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, IGuiScreen gui) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.gui = gui;
		this.render = Render.NORMAL;
	}

	public GuiButtonBase setVisiblePredicate(Predicate<IGuiScreen> shouldBeVisible) {
		this.shouldBeVisible = shouldBeVisible;
		return this;
	}

	public GuiButtonBase setCustomRender(Render render) {
		this.render = render;
		return this;
	}

	public GuiButtonBase setColor(Color color) {
		this.color = color;
		return this;
	}

	public GuiButtonBase setHoverText(List<String> hoverText) {
		this.hoverText = hoverText;
		return this;
	}

	public GuiButtonBase setNoSound() {
		this.noSound = true;
		return this;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		this.visible = shouldBeVisible == null || shouldBeVisible.apply(gui);
		this.hovered = this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

		if (this.hovered && gui instanceof GuiTeamBlock && this.hoverText != null)
			((GuiTeamBlock)this.gui).hoverText = hoverText;

		GlStateManager.pushMatrix();
		int actualHeight = this.height;
		float yScale = actualHeight/20f;
		float xScale = this.height == this.width ? yScale : 1;
		this.height = 20;
		GlStateManager.translate(0, this.yPosition-this.yPosition*(yScale), 0);
		GlStateManager.scale(1, yScale, 1);
		
		switch (render) {
		case NORMAL:
			// copied - added color
			if (this.visible)
			{
				FontRenderer fontrenderer = mc.fontRendererObj;
				mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
				GlStateManager.color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
	            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + actualHeight;
				int i = this.getHoverState(this.hovered);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
				this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
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

				GlStateManager.translate(this.xPosition-this.xPosition*(xScale), 0, 0);
				GlStateManager.scale(xScale, 1, 1);
	            this.drawCenteredString(fontrenderer, this.displayString, (int) (this.xPosition + this.width/xScale / 2), this.yPosition + (this.height - 8) / 2, j);
			}
			break;
		case NONE:
			break;
		case TEXT:
			if (visible)
				mc.fontRendererObj.drawString(displayString, this.xPosition + this.width / 2 - mc.fontRendererObj.getStringWidth(displayString)/2, this.yPosition + (this.height - 8) / 2, 14737632, true);
			break;
		}
		this.height = actualHeight;
		GlStateManager.popMatrix();
	}

	@Override
	public void playPressSound(SoundHandler soundHandler) {
		if (!this.noSound)
			super.playPressSound(soundHandler);
	}

}