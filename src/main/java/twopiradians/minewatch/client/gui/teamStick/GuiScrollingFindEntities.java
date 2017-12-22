package twopiradians.minewatch.client.gui.teamStick;

import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

public class GuiScrollingFindEntities extends GuiScrollingList {

	private GuiTeamStick gui;

	public GuiScrollingFindEntities(GuiTeamStick gui, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
		super(gui.mc, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
		this.gui = gui;
	}

	@Override
	protected int getSize() {
		return gui.entitiesFind.size();
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
		this.selectedIndex = index;
		this.gui.scrollingTeamEntities.setSelectedIndex(-1);
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
		String name = gui.entitiesFind.get(index).getName();
		if (gui.entitiesFind.get(index).getTeam() != null)
			name = gui.entitiesFind.get(index).getTeam().getColor()+name;
		gui.mc.fontRenderer.drawString(name, left+this.listWidth/2-gui.mc.fontRenderer.getStringWidth(name)/2, top+gui.mc.fontRenderer.FONT_HEIGHT/2, 0xFFFFFF);
	}

	public void setSelectedIndex(int index) {
		this.selectedIndex = index;
	}

	public int getSelectedIndex() {
		return this.selectedIndex;
	}

}