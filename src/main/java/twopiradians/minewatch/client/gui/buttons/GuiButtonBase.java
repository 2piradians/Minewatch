package twopiradians.minewatch.client.gui.buttons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import twopiradians.minewatch.client.gui.IGuiScreen;
import twopiradians.minewatch.client.gui.heroSelect.GuiHeroSelect;
import twopiradians.minewatch.client.gui.teamBlocks.GuiTeamBlock;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.RenderHelper;

public class GuiButtonBase extends GuiButton {

	public enum Render {
		NORMAL, NONE, COLORED, TEXT, HERO_SELECT, HERO_TYPE
	}

	public static final ResourceLocation HERO_SELECT_ICONS_0 = new ResourceLocation(Minewatch.MODID+":textures/gui/hero_select_icons_0.png");
	public static final ResourceLocation HERO_SELECT_ICONS_1 = new ResourceLocation(Minewatch.MODID+":textures/gui/hero_select_icons_1.png");

	public IGuiScreen gui;
	public Predicate<IGuiScreen> shouldBeVisible;
	public Render render;
	public Color color = new Color(0xFFFFFF);
	public List<String> hoverText;
	public boolean noSound;
	public Function<IGuiScreen, List<String>> hoverTextFuction;
	public EnumHero hero;

	public GuiButtonBase(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, IGuiScreen gui) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.gui = gui;
		this.render = Render.NORMAL;
	}

	public GuiButtonBase setVisiblePredicate(Predicate<IGuiScreen> shouldBeVisible) {
		this.shouldBeVisible = shouldBeVisible;
		return this;
	}

	public GuiButtonBase setHero(EnumHero hero) {
		this.hero = hero;
		this.render = Render.HERO_SELECT;
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

	public GuiButtonBase setHoverTextPredicate(Function<IGuiScreen, List<String>> hoverTextFuction) {
		this.hoverTextFuction = hoverTextFuction;
		return this;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		this.visible = shouldBeVisible == null || shouldBeVisible.apply(gui);
		this.hovered = this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

		if (this.hovered && gui instanceof GuiTeamBlock) {
			if (this.hoverTextFuction != null)
				this.hoverText = this.hoverTextFuction.apply(gui);
			if (this.hoverText != null)
				((GuiTeamBlock)this.gui).hoverText = hoverText;
		}

		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);
		int actualHeight = this.height;
		float yScale = 1;
		float xScale = 1;

		if (render == Render.NORMAL || render == Render.TEXT) {
			yScale = actualHeight/20f;
			xScale = this.height == this.width ? yScale : 1;
			this.height = 20;
			GlStateManager.translate(0, this.yPosition-this.yPosition*(yScale), 0);
			GlStateManager.scale(1, yScale, 1);
		}

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
		case HERO_SELECT:
			if (visible) {
				int num = id >= 20 ? id-20 : id;
				int row = num / 4;
				int col = num % 4;
				this.enabled = hero != null;
				this.hovered = enabled && mouseX >= (this.xPosition+2) && mouseY >= (this.yPosition-2) && mouseX < (this.xPosition + this.width-2) && mouseY < (this.yPosition + this.height+0);
				boolean selected = ((GuiHeroSelect)gui).getSelectedHero() == hero;
				int sizeIncrease = selected ? 6 : hovered ? 4 : 0;

				// resize / move
				this.xPosition -= sizeIncrease/2;
				this.yPosition -= sizeIncrease/2;
				this.width += sizeIncrease;
				this.height += sizeIncrease;

				float scaleX = this.width/56f;
				float scaleY = this.height/56f;
				GlStateManager.scale(scaleX, scaleY, 0);
				GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
				float x = this.xPosition / scaleX;
				float y = this.yPosition / scaleY;

				// draw stencil
				GL11.glEnable(GL11.GL_STENCIL_TEST);
				GL11.glStencilMask(0xFF); // writing on
				GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT); // flush old data
				GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF); // always add to buffer
				GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_REPLACE); // replace on success
				GlStateManager.colorMask(false, false, false, false); // don't draw this
				mc.getTextureManager().bindTexture(HERO_SELECT_ICONS_1);
				this.drawTexturedModalRect(x, y, 1, 196, (int) (this.width/scaleX), (int) (this.height/scaleY));
				GlStateManager.colorMask(true, true, true, true);
				GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // anything written to buffer will be drawn
				GL11.glStencilMask(0x00); // writing off

				// background
				drawRect((int) (x), (int) (y), (int) (x+this.width/scaleX), (int) (y+this.height/scaleY), selected ? 0xF0FFB43D : enabled ? 0xCA000000 : 0x40000000);
				GlStateManager.enableBlend();
				GlStateManager.color(1, 1, 1, enabled ? 1 : 0.3f);
				GlStateManager.enableDepth();

				// icon
				mc.getTextureManager().bindTexture(id >= 20 ? HERO_SELECT_ICONS_1 : HERO_SELECT_ICONS_0);
				this.drawTexturedModalRect(x, y, (int) ((col*56+col+1))+(hero == EnumHero.DOOMFIST ? 1 : hero == EnumHero.MOIRA ? -2 : 0), (int) (row*50+row-3), (int) (this.width/scaleX), (int) (this.height/scaleY));
				GL11.glDisable(GL11.GL_STENCIL_TEST);
				GlStateManager.enableDepth();

				// draw outline on top
				mc.getTextureManager().bindTexture(HERO_SELECT_ICONS_1);
				this.drawTexturedModalRect(x-3, y-2, -2+61*(selected ? 3 : hovered ? 2 : 1), 196-2, (int) (this.width/scaleX)+5, (int) (this.height/scaleY)+5);
				GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

				// text
				if (this.displayString != null && (selected || hovered)) {
					String text = TextFormatting.ITALIC+displayString;
					RenderHelper.drawHoveringText(null, new ArrayList<String>(Lists.newArrayList(text)), (int) (this.xPosition+this.width*0.18f-mc.fontRendererObj.getStringWidth(text)/2*scaleX), (int) (this.yPosition+this.height+mc.fontRendererObj.FONT_HEIGHT*0.7f), scaleX, scaleY, -1);
					GlStateManager.disableLighting();
				}

				// undo resize / move
				this.xPosition += sizeIncrease/2;
				this.yPosition += sizeIncrease/2;
				this.width -= sizeIncrease;
				this.height -= sizeIncrease;
			}
			break;
		case HERO_TYPE:
			if (visible) {
				double scale = this.width/23d;
				GlStateManager.scale(scale, scale, 0);
				mc.getTextureManager().bindTexture(HERO_SELECT_ICONS_1);
				GlStateManager.enableBlend();
				this.drawTexturedModalRect((int)(this.xPosition/scale), (int)(this.yPosition/scale), 234, this.id*19, this.width, actualHeight);

				// text
				this.hovered = mouseX >= (this.xPosition+0) && mouseY >= (this.yPosition+0) && mouseX < (this.xPosition + this.width-3) && mouseY < (this.yPosition + actualHeight-2);
				if (this.hovered && this.displayString != null)
					mc.fontRendererObj.drawString(displayString, (float) ((this.xPosition/scale+this.width/2/scale-mc.fontRendererObj.getStringWidth(displayString)/2)), (float) ((this.yPosition-actualHeight*0.8f)/scale), 14737632, true);
			}
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