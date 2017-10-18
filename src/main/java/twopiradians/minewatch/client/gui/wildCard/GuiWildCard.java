package twopiradians.minewatch.client.gui.wildCard;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.packet.CPacketSimple;

@SideOnly(Side.CLIENT)
public class GuiWildCard extends GuiScreen {

	/** The X size of the inventory window in pixels. */
	private static final int X_SIZE = 512/2;
	/** The Y size of the inventory window in pixels. */
	private static final int Y_SIZE = 444/2;
	private int guiLeft;
	private int guiTop;
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/inventory_tab.png");

	public GuiWildCard() {}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.guiLeft = (this.width - GuiWildCard.X_SIZE) / 2;
		this.guiTop = (this.height - GuiWildCard.Y_SIZE) / 2;

		int spaceBetweenX = 40;
		int spaceBetweenY = 40;
		int perRow = 6;
		for (int i=0; i<EnumHero.values().length; ++i) 
			this.buttonList.add(new GuiButtonWildCard(0, (int) ((this.guiLeft+11)+(int)(i%perRow)*spaceBetweenX), (int) ((this.guiTop+50+spaceBetweenY*(int)(i/perRow))), spaceBetweenX-10, spaceBetweenY-10, "", EnumHero.values()[i]));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);

		// background
		this.drawDefaultBackground();
		mc.getTextureManager().bindTexture(BACKGROUND);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, GuiWildCard.X_SIZE, GuiWildCard.Y_SIZE);

		// buttons
		super.drawScreen(mouseX, mouseY, partialTicks);

		// text
		double textScale = 1.2d;
		GlStateManager.scale(textScale, textScale, 1);
		this.fontRendererObj.drawString(TextFormatting.ITALIC+"Select a token to receive in exchange", (int) ((this.guiLeft+14)/textScale), (int) ((this.guiTop+18)/textScale), 0, false);
		this.fontRendererObj.drawString(TextFormatting.ITALIC+" for the Wild Card Token.", (int) ((this.guiLeft+48)/textScale), (int) ((this.guiTop+30)/textScale), 0, false);
		GlStateManager.translate(-0.7F, -0.2f, 0);
		this.fontRendererObj.drawString(TextFormatting.ITALIC+"Select a token to receive in exchange", (int) ((this.guiLeft+14)/textScale), (int) ((this.guiTop+18)/textScale), 0x7F7F7F, false);
		this.fontRendererObj.drawString(TextFormatting.ITALIC+" for the Wild Card Token.", (int) ((this.guiLeft+48)/textScale), (int) ((this.guiTop+30)/textScale), 0x7F7F7F, false);
		GlStateManager.scale(1/textScale, 1/textScale, 1);

		// hover text
		for (GuiButton button : this.buttonList) 
			if (button.isMouseOver() && button instanceof GuiButtonWildCard) {
				String name = ((GuiButtonWildCard)button).hero.equals(EnumHero.SOLDIER76) ? "Soldier: 76" :
					((GuiButtonWildCard)button).hero.name;
				this.drawHoveringText(new ArrayList<String>() {{add(name);}}, 
						button.xPosition-mc.fontRendererObj.getStringWidth(name)/2+4, button.yPosition+51);
				break;
			}

		GlStateManager.popMatrix();	
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (mc.player != null && ((GuiButtonWildCard)button).hero != null)
			Minewatch.network.sendToServer(new CPacketSimple(2, mc.player, ((GuiButtonWildCard)button).hero.ordinal(), 0, 0));
	}

}