package twopiradians.minewatch.client.gui.teamStick;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.scoreboard.Team;
import net.minecraftforge.fml.client.GuiScrollingList;

public class GuiScrollingTeams extends GuiScrollingList {

	private GuiTeamStick gui;

	public GuiScrollingTeams(GuiTeamStick gui, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
		super(gui.mc, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
		this.gui = gui;
	}

	@Override
	protected int getSize() {
		return gui.teams.size();
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
		Team team = gui.teams.toArray(new Team[0])[index];
		if (!team.isSameTeam(gui.getSelectedTeam())) 
			gui.setSelectedTeam(team);
		else 
			gui.setSelectedTeam(null);
	}

	@Override
	protected boolean isSelected(int index) {
		return gui.teams.toArray(new Team[0])[index].isSameTeam(gui.getSelectedTeam());
	}

	@Override
	protected void drawBackground() {

	}

	@Override
	protected void drawSlot(int index, int right, int top, int height, Tessellator tess) {		
		Team team = gui.teams.toArray(new Team[0])[index];
		String name = team.getColor()+gui.getTeamName(team);
		gui.mc.fontRenderer.drawStringWithShadow(name, left+this.listWidth/2-gui.mc.fontRenderer.getStringWidth(name)/2, top+gui.mc.fontRenderer.FONT_HEIGHT/2, 0xFFFFFF);
	}

}