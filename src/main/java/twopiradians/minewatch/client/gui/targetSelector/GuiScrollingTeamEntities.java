package twopiradians.minewatch.client.gui.targetSelector;

import org.lwjgl.opengl.GL11;

import mezz.jei.gui.GuiHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;

public class GuiScrollingTeamEntities extends GuiScrollingList {

	private String selectedEntity;
	private GuiTeamSelector gui;

	public GuiScrollingTeamEntities(GuiTeamSelector gui, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
		super(gui.mc, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
		this.gui = gui;
	}

	@Override
	protected int getSize() {
		return gui.entitiesTeam.size();
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
		this.selectedIndex = index;
		this.gui.scrollingFindEntities.setSelectedIndex(-1);
	}

	@Override
	protected boolean isSelected(int index) {
		return this.selectedIndex == index;
	}

	@Override
	protected void drawBackground() {

	}

	@Override
	protected void drawHeader(int entryRight, int relativeY, Tessellator tess) { 

	}

	@Override
	protected void drawSlot(int index, int right, int top, int height, Tessellator tess) {	
		String name = gui.entitiesTeam.get(index).getName();
		if (gui.getSelectedTeam() != null)
			name = gui.getSelectedTeam().getChatFormat()+name;
		gui.mc.fontRendererObj.drawString(name, left+this.listWidth/2-gui.mc.fontRendererObj.getStringWidth(name)/2, top+gui.mc.fontRendererObj.FONT_HEIGHT/2, 0xFFFFFF);
	}

	public void setSelectedIndex(int index) {
		this.selectedIndex = index;
	}
	
	public int getSelectedIndex() {
		return this.selectedIndex;
	}

}