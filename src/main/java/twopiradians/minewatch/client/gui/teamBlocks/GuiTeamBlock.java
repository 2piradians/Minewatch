package twopiradians.minewatch.client.gui.teamBlocks;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.IGuiScreen;
import twopiradians.minewatch.client.gui.IGuiScreen.Screen;
import twopiradians.minewatch.client.gui.buttons.GuiButtonBase;
import twopiradians.minewatch.client.gui.buttons.GuiButtonBase.Render;
import twopiradians.minewatch.client.gui.teamStick.GuiButtonColored;
import twopiradians.minewatch.client.gui.teamStick.GuiButtonResized;
import twopiradians.minewatch.client.gui.teamStick.GuiButtonURL;
import twopiradians.minewatch.client.gui.teamStick.GuiScrollingTeams;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.tileentity.TileEntityTeam;
import twopiradians.minewatch.packet.CPacketSimple;

@SideOnly(Side.CLIENT)
public class GuiTeamBlock extends GuiTeamSelector implements IGuiScreen {

	/** The X size of the inventory window in pixels. */
	private static final int X_SIZE = 322/2;
	/** The Y size of the inventory window in pixels. */
	private static final int Y_SIZE = 444/2;
	private int guiLeft;
	private int guiTop;
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/team_block.png");

	public Screen currentScreen;
	public GuiScrollingTeams scrollingTeams;
	public GuiTextField nameField;
	public TileEntityTeam te;
	public List<String> hoverText;

	public GuiTeamBlock(TileEntityTeam te) {
		currentScreen = Screen.MAIN;
		this.te = te;
		this.selectedTeam = te.getTeam();
	}

	@Override
	public void initGui() {
		super.initGui();

		this.guiLeft = (this.width - GuiTeamBlock.X_SIZE) / 2;
		this.guiTop = (this.height - GuiTeamBlock.Y_SIZE) / 2;

		// buttons
		// Screen.MAIN
		String format = TextFormatting.WHITE+""+TextFormatting.UNDERLINE+""+TextFormatting.BOLD;
		this.buttonList.add(new GuiButtonBase(3, guiLeft+X_SIZE-15, guiTop+5, 10, 10, "?", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN));
		this.buttonList.add(new GuiButtonBase(0, guiLeft+X_SIZE/2-50/2, guiTop+25, 50, 20, format+"Name", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setNoSound().setCustomRender(Render.TEXT).setHoverText(Lists.newArrayList("This name is used by commands"))); // TODO translate names
		this.buttonList.add(new GuiButtonBase(0, guiLeft+X_SIZE/2-110/2, guiTop+58, 110, 20, format+"Activate / Deactivate", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setNoSound().setCustomRender(Render.TEXT).setHoverText(Lists.newArrayList("Players and Hero Mobs will spawn at their team's active Team Spawn"))); 
		this.buttonList.add(new GuiButtonBase(1, guiLeft+X_SIZE/2-60/2, guiTop+77, 60, 20, "Activate", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN && !te.isActivated()).setColor(new Color(50, 255, 50)).setHoverText(Lists.newArrayList("Only one Team Spawn block can be active at a time")));
		this.buttonList.add(new GuiButtonBase(2, guiLeft+X_SIZE/2-60/2, guiTop+77, 60, 20, "Deactivate", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN && te.isActivated()).setColor(new Color(255, 50, 50)).setHoverText(Lists.newArrayList("Only one Team Spawn block can be active at a time")));
		this.buttonList.add(new GuiButtonBase(0, guiLeft+X_SIZE/2-70/2, guiTop+96, 70, 20, format+"Selected Team", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setNoSound().setCustomRender(Render.TEXT).setHoverText(Lists.newArrayList("Choose the team that will respawn here"))); 
		// Screen.QUESTION_MARK
		this.buttonList.add(new GuiButtonBase(4, guiLeft+X_SIZE/2-30/2, guiTop+Y_SIZE-35, 30, 20, "OK", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.QUESTION_MARK));
		this.buttonList.add(new GuiButtonBase(5, guiLeft+X_SIZE/2-80/2, guiTop+Y_SIZE-85, 80, 20, TextFormatting.BLUE+""+TextFormatting.UNDERLINE+""+TextFormatting.BOLD+"Video Demonstration", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.QUESTION_MARK).setNoSound().setCustomRender(Render.TEXT));

		// set up scrolling lists
		scrollingTeams = new GuiScrollingTeams(this, X_SIZE-6, 0, guiTop+Y_SIZE-3-104, guiTop+Y_SIZE-3, guiLeft+3, 20, width, height);

		// create and sort all teams
		teams = new ArrayList<Team>(mc.world.getScoreboard().getTeams());
		teams.sort(new Comparator<Team>() {
			@Override
			public int compare(Team team1, Team team2) {
				return getTeamName(team1).compareToIgnoreCase(getTeamName(team2));
			}
		});

		// set up team name text field
		nameField = new GuiTextField(0, mc.fontRendererObj, guiLeft+X_SIZE/2-104/2, guiTop+44, 104, 14);
		nameField.setFocused(false);
		nameField.setCanLoseFocus(true);
		nameField.setMaxStringLength(16);
		nameField.setText(te.getName());

		// set selected team
		setSelectedTeam(this.selectedTeam, false);
	}

	@Override
	public void updateScreen() {
		nameField.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);

		// refresh tile if it's replaced (from updating blockstate)
		if (te.isInvalid() && mc.world.getTileEntity(te.getPos()) instanceof TileEntityTeam)
			te = (TileEntityTeam) mc.world.getTileEntity(te.getPos());

		// background
		this.drawDefaultBackground();
		mc.getTextureManager().bindTexture(BACKGROUND);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, GuiTeamBlock.X_SIZE, GuiTeamBlock.Y_SIZE);

		if (getSelectedTeam() != null) {
			this.drawCenteredString(mc.fontRendererObj, TextFormatting.BOLD+"Selected Team: "+this.getSelectedTeam().getChatFormat()+getTeamName(this.getSelectedTeam()), guiLeft+X_SIZE/2, guiTop-10, 0xFFFFFF);
		}

		switch (this.currentScreen) {
		case MAIN:
			nameField.drawTextBox();
			scrollingTeams.drawScreen(mouseX, mouseY, partialTicks);
			break;
		case QUESTION_MARK:
			String text = TextFormatting.WHITE.toString()+
			"Players and hero mobs will spawn at their team's active Team Spawn.\n\n"
			+ "Team Spawn blocks can be activated/deactivated with a command, as well:\n\n"
			+ TextFormatting.GREEN+TextFormatting.ITALIC+"/mw teamSpawn <activate/deactivate> <name>";
			int y = guiTop+15;
			for (String s : mc.fontRendererObj.listFormattedStringToWidth(text, X_SIZE-16)) {
				mc.fontRendererObj.drawString(s, guiLeft+8, y, 0xFFFFFF, true);
				y += mc.fontRendererObj.FONT_HEIGHT;
			}
			break;
		}

		// buttons
		super.drawScreen(mouseX, mouseY, partialTicks);

		// draw hoverText
		if (this.hoverText != null)
			this.drawHoveringText(this.hoverText, mouseX, mouseY);
		this.hoverText = null;

		GlStateManager.popMatrix();	
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 1: // activate
			Minewatch.network.sendToServer(new CPacketSimple(15, true, mc.player, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
			break;
		case 2: // deactivate
			Minewatch.network.sendToServer(new CPacketSimple(15, false, mc.player, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
			break;
		case 3: // ?
			this.currentScreen = Screen.QUESTION_MARK;
			break;
		case 4: // OK
			this.currentScreen = Screen.MAIN;
			break;
		case 5: // Video Demonstration
			this.handleComponentClick(new TextComponentString("").setStyle(new Style().setClickEvent(
					new ClickEvent(Action.OPEN_URL, "https://minecraft.curseforge.com/projects/minewatch")))); // TODO
			break;
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		scrollingTeams.handleMouseInput(mouseX, mouseY);
		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		nameField.mouseClicked(x, y, button);
		if (button == 1 && x >= nameField.xPosition && x < nameField.xPosition + nameField.width && y >= nameField.yPosition && y < nameField.yPosition + nameField.height) { 
			nameField.setText("");
			this.updateTextBoxColor();
		}
		super.mouseClicked(x, y, button);
	}

	@Override
	protected void keyTyped(char c, int keyCode) throws IOException {
		super.keyTyped(c, keyCode);
		nameField.textboxKeyTyped(c, keyCode);
		this.updateTextBoxColor();
		// unfocus when enter key hit
		if (keyCode == Keyboard.KEY_RETURN && nameField.isFocused())
			nameField.setFocused(false);
	}

	@Override
	public void setSelectedTeam(@Nullable Team team) {
		setSelectedTeam(team, true);
	}

	public void setSelectedTeam(@Nullable Team team, boolean sendPacket) {
		if ((team != null && !team.isSameTeam(selectedTeam)) || 
				(selectedTeam != null && !selectedTeam.isSameTeam(team))) {
			if (sendPacket)
				Minewatch.network.sendToServer(new CPacketSimple(13, team == null ? null : team.getRegisteredName(), mc.player, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
			selectedTeam = team;
		}
	}

	public void updateTextBoxColor() {
		// set color to red if invalid
		if (!nameField.getText().equals(te.getName()) && !te.isValidName(nameField.getText()))
			nameField.setTextColor(new Color(255, 100, 100).getRGB());
		else
			nameField.setTextColor(-1);
	}

	@Override
	public void onGuiClosed() {
		// update tile's name if it changed
		if (!nameField.getText().equals(te.getName()) && te.isValidName(nameField.getText())) {
			te.setName(nameField.getText());
			Minewatch.network.sendToServer(new CPacketSimple(14, nameField.getText(), mc.player, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
		}
	}

	@Override
	public Screen getCurrentScreen() {
		return this.currentScreen;
	}

}