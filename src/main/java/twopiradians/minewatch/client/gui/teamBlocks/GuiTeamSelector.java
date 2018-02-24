package twopiradians.minewatch.client.gui.teamBlocks;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;

public abstract class GuiTeamSelector extends GuiScreen {
	
	public ArrayList<Team> teams = new ArrayList<Team>();
	protected Team selectedTeam;
	
	@Nullable
	public Team getSelectedTeam() {
		return selectedTeam;
	}
	
	public void setSelectedTeam(@Nullable Team team) {
		this.selectedTeam = team;
	}
	
	public String getTeamName(Team team) {
		if (team instanceof ScorePlayerTeam)
			return ((ScorePlayerTeam)team).getDisplayName();
		else if (team != null)
			return team.getName();
		else
			return "";
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
}
