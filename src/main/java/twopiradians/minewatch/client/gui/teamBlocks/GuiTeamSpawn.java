package twopiradians.minewatch.client.gui.teamBlocks;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.buttons.GuiButtonBase;
import twopiradians.minewatch.client.gui.buttons.GuiButtonBase.Render;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;
import twopiradians.minewatch.packet.CPacketSimple;

@SideOnly(Side.CLIENT)
public class GuiTeamSpawn extends GuiTeamBlock {

	private int offsetY;
	private String questionMarkDescription;

	public GuiTeamSpawn(TileEntityTeamSpawn te) {
		super(te, 12);
		this.offsetY = 2;
	}

	@Override
	public void initGui() {
		super.initGui();

		// buttons
		this.buttonList.add(new GuiButtonBase(0, guiLeft+X_SIZE/2-80/2, offsetY+guiTop+0, 80, 20, format+Minewatch.translate("gui.team_spawn.button.spawn_radius"), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setNoSound().setCustomRender(Render.TEXT).setHoverText(Lists.newArrayList(Minewatch.translate("gui.team_spawn.button.spawn_radius.desc")))); 
		this.buttonList.add(new GuiButtonBase(6, guiLeft+X_SIZE/2-18/2+25, offsetY+guiTop+19, 18, 18, "+", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN)); 
		this.buttonList.add(new GuiButtonBase(7, guiLeft+X_SIZE/2-18/2-25, offsetY+guiTop+19, 18, 18, "-", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN)); 

		// add button descriptions
		for (GuiButton button : this.buttonList) {
			if (button instanceof GuiButtonBase) {
				switch (button.id) {
				case -2:
					((GuiButtonBase)button).setHoverText(Lists.newArrayList(Minewatch.translate("gui.team_spawn.button.activate_deactivate.desc")));
					break;
				case 1:
					((GuiButtonBase)button).setHoverText(Lists.newArrayList(Minewatch.translate("gui.team_spawn.button.activate.desc")));
					break;
				case 2:
					((GuiButtonBase)button).setHoverText(Lists.newArrayList(Minewatch.translate("gui.team_spawn.button.deactivate.desc")));
					break;
				case -3:
					((GuiButtonBase)button).setHoverText(Lists.newArrayList(Minewatch.translate("gui.team_spawn.button.selected_team.desc")));
					break;
				}
			}
		}

		this.questionMarkDescription = TextFormatting.WHITE.toString()+
				Minewatch.translate("gui.team_spawn.question_mark_description_0")+"\n\n"+
				Minewatch.translate("gui.team_spawn.question_mark_description_1")+"\n\n"+
				TextFormatting.GREEN+TextFormatting.ITALIC+"/mw teamSpawn <" +
				Minewatch.translate("gui.team_block.button.name").toLowerCase() + "> <"+
				Minewatch.translate("gui.team_block.button.activate_deactivate").replace(" ", "").toLowerCase() + ">";
	}

	@Override
	public void drawMainScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawMainScreen(mouseX, mouseY, partialTicks);

		switch (this.currentScreen) {
		case MAIN:
			this.drawCenteredString(mc.fontRenderer, String.valueOf(((TileEntityTeamSpawn) te).getSpawnRadius()), guiLeft+X_SIZE/2, offsetY+guiTop+24, 0xFFFFFF);
			break;
		case QUESTION_MARK:
			this.drawWrappedString(this.questionMarkDescription, guiLeft+8, guiTop+15, true, X_SIZE-16);
			break;
		}
	}

	@Override
	protected List<String> getStatusText() {
		String team = null;
		if (this.getSelectedTeam() != null)
			team = this.getSelectedTeam().getColor()+getTeamName(this.getSelectedTeam());
		String entities = te.isActivated() ? team == null ? Minewatch.translate("gui.team_spawn.status.players_heroes_all") : Minewatch.translate("gui.team_spawn.status.players_heroes_team")+" "+TextFormatting.RESET+team : Minewatch.translate("gui.team_spawn.status.players_heroes_none");
		String str = Minewatch.translate("gui.team_spawn.status.will_spawn");
		return Lists.newArrayList(TextFormatting.GOLD+""+TextFormatting.BOLD+entities+" "+TextFormatting.RESET+str);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		switch (button.id) {
		case 6: // +
			if (((TileEntityTeamSpawn) te).getSpawnRadius() < TileEntityTeamSpawn.MAX_SPAWN_RADIUS)
				Minewatch.network.sendToServer(new CPacketSimple(16, true, mc.player, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
			break;
		case 7: // -
			if (((TileEntityTeamSpawn) te).getSpawnRadius() > 0)
				Minewatch.network.sendToServer(new CPacketSimple(16, false, mc.player, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
			break;
		}
	}	

}