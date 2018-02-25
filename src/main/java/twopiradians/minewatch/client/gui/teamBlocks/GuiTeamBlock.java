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
import twopiradians.minewatch.client.gui.buttons.GuiButtonBase;
import twopiradians.minewatch.client.gui.buttons.GuiButtonBase.Render;
import twopiradians.minewatch.client.gui.teamStick.GuiScrollingTeams;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.tileentity.TileEntityTeam;
import twopiradians.minewatch.packet.CPacketSimple;

@SideOnly(Side.CLIENT)
public class GuiTeamBlock extends GuiTeamSelector implements IGuiScreen {

	/** The X size of the inventory window in pixels. */
	protected static final int X_SIZE = 322/2;
	/** The Y size of the inventory window in pixels. */
	protected static final int Y_SIZE = 444/2;
	protected int guiLeft;
	protected int guiTop;
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/team_block.png");

	public Screen currentScreen;
	public GuiScrollingTeams scrollingTeams;
	public GuiTextField nameField;
	public TileEntityTeam te;
	public List<String> hoverText;
	protected String format = TextFormatting.WHITE+""+TextFormatting.UNDERLINE+""+TextFormatting.BOLD;
	protected int offsetY;

	public GuiTeamBlock(TileEntityTeam te, int offsetY) {
		currentScreen = Screen.MAIN;
		this.offsetY = offsetY;
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
		this.buttonList.add(new GuiButtonBase(3, guiLeft+X_SIZE-15, guiTop+5, 10, 10, "?", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setHoverTextPredicate(gui->((GuiTeamBlock)gui).getStatusText()));
		this.buttonList.add(new GuiButtonBase(-1, guiLeft+X_SIZE/2-50/2, offsetY+guiTop+25, 50, 20, format+Minewatch.translate("gui.team_block.button.name"), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setNoSound().setCustomRender(Render.TEXT).setHoverText(Lists.newArrayList(Minewatch.translate("gui.team_block.button.name.desc")))); 
		this.buttonList.add(new GuiButtonBase(-2, guiLeft+X_SIZE/2-120/2, offsetY+guiTop+58, 120, 20, format+Minewatch.translate("gui.team_block.button.activate_deactivate"), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setNoSound().setCustomRender(Render.TEXT)); 
		this.buttonList.add(new GuiButtonBase(1, guiLeft+X_SIZE/2-60/2, offsetY+guiTop+77, 60, 20, Minewatch.translate("gui.team_block.button.activate"), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN && !te.isActivated()).setColor(new Color(50, 255, 50)));
		this.buttonList.add(new GuiButtonBase(2, guiLeft+X_SIZE/2-60/2, offsetY+guiTop+77, 60, 20, Minewatch.translate("gui.team_block.button.deactivate"), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN && te.isActivated()).setColor(new Color(255, 50, 50)));
		this.buttonList.add(new GuiButtonBase(-3, guiLeft+X_SIZE/2-80/2, offsetY+guiTop+96, 80, 20, format+Minewatch.translate("gui.team_block.button.selected_team"), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setNoSound().setCustomRender(Render.TEXT)); 
		// Screen.QUESTION_MARK
		this.buttonList.add(new GuiButtonBase(4, guiLeft+X_SIZE/2-30/2, guiTop+Y_SIZE-35, 30, 20, Minewatch.translate("gui.team_block.button.ok"), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.QUESTION_MARK));
		this.buttonList.add(new GuiButtonBase(5, guiLeft+X_SIZE/2-80/2, guiTop+Y_SIZE-65, 80, 20, TextFormatting.BLUE+""+TextFormatting.UNDERLINE+""+TextFormatting.BOLD+Minewatch.translate("gui.team_block.button.video_demonstration"), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.QUESTION_MARK).setNoSound().setCustomRender(Render.TEXT));

		// set up scrolling lists
		scrollingTeams = new GuiScrollingTeams(this, X_SIZE-6, 0, offsetY+guiTop+Y_SIZE-3-104, guiTop+Y_SIZE-3, guiLeft+3, 20, width, height);

		// create and sort all teams
		teams = new ArrayList<Team>(mc.world.getScoreboard().getTeams());
		teams.sort(new Comparator<Team>() {
			@Override
			public int compare(Team team1, Team team2) {
				return getTeamName(team1).compareToIgnoreCase(getTeamName(team2));
			}
		});

		// set up team name text field
		nameField = new GuiTextField(0, mc.fontRenderer, guiLeft+X_SIZE/2-104/2, offsetY+guiTop+44, 104, 14);
		nameField.setFocused(false);
		nameField.setCanLoseFocus(true);
		nameField.setMaxStringLength(16);
		nameField.setText(te.getName());

		// set selected team
		setSelectedTeam(this.selectedTeam, false);
	}

	protected List<String> getStatusText() {
		return null;
	}

	@Override
	public void updateScreen() {
		nameField.updateCursorCounter();
	}
	
	public void drawMainScreen(int mouseX, int mouseY, float partialTicks) {
		switch (this.currentScreen) {
		case MAIN:
			nameField.drawTextBox();
			scrollingTeams.drawScreen(mouseX, mouseY, partialTicks);
			break;
		case QUESTION_MARK:
			break;
		}
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
			this.drawCenteredString(mc.fontRenderer, TextFormatting.BOLD+Minewatch.translate("gui.team_block.button.selected_team")+": "+this.getSelectedTeam().getColor()+getTeamName(this.getSelectedTeam()), guiLeft+X_SIZE/2, guiTop-10, 0xFFFFFF);
		}
		
		this.drawMainScreen(mouseX, mouseY, partialTicks);

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
					new ClickEvent(Action.OPEN_URL, Minewatch.MAP_TOOLS_VIDEO_URL)))); 
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
		if (button == 1 && x >= nameField.x && x < nameField.x + nameField.width && y >= nameField.y && y < nameField.y + nameField.height) { 
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
				Minewatch.network.sendToServer(new CPacketSimple(13, team == null ? null : team.getName(), mc.player, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
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
	
	public void drawWrappedString(String text, int x, int y, boolean dropShadow, int wrapWidth) {
		for (String s : mc.fontRenderer.listFormattedStringToWidth(text, wrapWidth)) {
			mc.fontRenderer.drawString(s, x, y, 0xFFFFFF, dropShadow);
			y += mc.fontRenderer.FONT_HEIGHT;
		}
	}

}