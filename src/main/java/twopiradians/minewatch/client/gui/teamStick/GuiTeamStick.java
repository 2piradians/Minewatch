package twopiradians.minewatch.client.gui.teamStick;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.base.Predicate;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.teamBlocks.GuiTeamSelector;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.item.ItemTeamStick;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.CPacketSimple;

@SideOnly(Side.CLIENT)
public class GuiTeamStick extends GuiTeamSelector {

	public enum Screen {
		MAIN, INFO, EDIT_TEAM, CREATE_TEAM, QUESTION_MARK;
	};

	public enum Filter {
		ENTITIES("Entities", EntityLivingBase.class),
		PLAYERS("Players", EntityPlayer.class),
		HEROES("Heroes", EntityHero.class);

		public String name;
		public Class filter;

		Filter(String name, Class filter) {
			this.name = name;
			this.filter = filter;
		}

		public Filter next() {
			int ordinal = this.ordinal()+1;
			if (ordinal >= Filter.values().length)
				ordinal = 0;
			return Filter.values()[ordinal];
		}
	}

	/** The X size of the inventory window in pixels. */
	private static final int X_SIZE = 322/2;
	/** The Y size of the inventory window in pixels. */
	private static final int Y_SIZE = 444/2;
	private int guiLeft;
	private int guiTop;
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/team_stick.png");

	public Screen currentScreen;
	public GuiScrollingTeams scrollingTeams;
	public GuiScrollingTeamEntities scrollingTeamEntities;
	public GuiScrollingFindEntities scrollingFindEntities;
	public ArrayList<EntityLivingBase> entitiesTeam = new ArrayList<EntityLivingBase>();
	public ArrayList<EntityLivingBase> entitiesFind = new ArrayList<EntityLivingBase>();
	public TextFormatting selectedColor;
	public GuiTextField teamNameField;
	private String teamWaitingFor;
	private Team prevTeam;
	public Filter filter;
	private int stackX;
	private int stackY;

	public GuiTeamStick() {
		currentScreen = Screen.MAIN;
		selectedColor = TextFormatting.WHITE;
		filter = Filter.ENTITIES;
	}
	
	@Override
	public void initGui() {
		super.initGui();

		this.guiLeft = (this.width - GuiTeamStick.X_SIZE) / 2;
		this.guiTop = (this.height - GuiTeamStick.Y_SIZE) / 2;

		this.stackX = guiLeft+X_SIZE/2-8;
		this.stackY = guiTop+70;

		// set up scrolling lists
		scrollingTeams = new GuiScrollingTeams(this, X_SIZE-6, 0, guiTop+Y_SIZE-3-104, guiTop+Y_SIZE-3, guiLeft+3, 20, width, height);
		scrollingFindEntities = new GuiScrollingFindEntities(this, 100, 0, guiTop+33, guiTop+Y_SIZE-25, guiLeft-103, 20, width, height);
		scrollingTeamEntities = new GuiScrollingTeamEntities(this, 100, 0, guiTop+33, guiTop+Y_SIZE-25, guiLeft+X_SIZE+3, 20, width, height);

		// create buttons
		this.buttonList.add(new GuiButton(0, guiLeft-105, guiTop+Y_SIZE-22, 105, 20, "Add to team >"));
		this.buttonList.add(new GuiButton(1, guiLeft+X_SIZE+1, guiTop+Y_SIZE-22, 105, 20, "< Remove from team"));
		this.buttonList.add(new GuiButton(2, guiLeft+X_SIZE/2-50, guiTop+50, 100, 20, "Create New Team"));
		this.buttonList.add(new GuiButton(3, guiLeft+X_SIZE/2+35, guiTop+70, 40, 20, "Set"));
		this.buttonList.add(new GuiButton(4, guiLeft+X_SIZE/2+2, guiTop+93, 65, 20, "Edit Team"));
		this.buttonList.add(new GuiButton(5, guiLeft+X_SIZE/2-2-65, guiTop+93, 65, 20, "Done"));
		this.buttonList.add(new GuiButtonResized(6, guiLeft+X_SIZE-15, guiTop+5, 10, 10, "?"));
		this.buttonList.add(new GuiButton(7, guiLeft+X_SIZE/2-15, guiTop+93, 30, 20, "OK"));
		this.buttonList.add(new GuiButtonURL(8, guiLeft+X_SIZE/2-70, guiTop+30, 140, 20, TextFormatting.BLUE+""+TextFormatting.UNDERLINE+"How to use the Team Stick", "https://youtu.be/6LMpimAFEDs", this)); 
		this.buttonList.add(new GuiButtonURL(9, guiLeft+X_SIZE/2-40, guiTop+65, 80, 20, TextFormatting.BLUE+""+TextFormatting.UNDERLINE+"Minecraft Wiki", "https://minecraft.gamepedia.com/Scoreboard#Teams", this)); 
		for (int i=0; i<16; ++i)
			this.buttonList.add(new GuiButtonTeamColor(i, guiLeft+12+i*18-(i/8)*144, guiTop+23+(i/8)*18, 11, 11, "", this));
		this.buttonList.add(new GuiButtonColored(0, guiLeft+X_SIZE/2+2, guiTop+93, 65, 20, "Delete Team", new Color(255, 50, 50), Screen.EDIT_TEAM, this));
		this.buttonList.add(new GuiButtonColored(0, guiLeft+X_SIZE/2-50, guiTop+93, 100, 20, "Create Team", new Color(50, 255, 50), Screen.CREATE_TEAM, this));
		this.buttonList.add(new GuiButtonResized(50, guiLeft+X_SIZE+3, guiTop, 100, 30, ""));
		this.buttonList.add(new GuiButtonResized(50, guiLeft-3-100, guiTop, 100, 30, ""));

		// create and sort all teams
		teams = new ArrayList<Team>(mc.theWorld.getScoreboard().getTeams());
		teams.sort(new Comparator<Team>() {
			@Override
			public int compare(Team team1, Team team2) {
				return getTeamName(team1).compareToIgnoreCase(getTeamName(team2));
			}
		});

		// set up team name text field
		teamNameField = new GuiTextField(0, mc.fontRendererObj, guiLeft+7, guiTop+73, 104, 14);
		teamNameField.setFocused(true);
		teamNameField.setCanLoseFocus(true);
		teamNameField.setMaxStringLength(16);

		// find selected team
		setSelectedTeam(ItemTeamStick.getTeam(mc.theWorld, getStack()), false);
	}

	@Override
	public void updateScreen() {
		teamNameField.updateCursorCounter();

		// set focused if selected team changes - because field detects mouse clicks while drawing for some reason
		if (this.getSelectedTeam() != prevTeam) 
			this.teamNameField.setFocused(true);
		prevTeam = this.getSelectedTeam();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// waiting for new team to be synced
		if (this.teamWaitingFor != null) {
			Team team = mc.theWorld.getScoreboard().getTeam(this.teamWaitingFor);
			if (team != null) {
				teams.add(team);
				teams.sort(new Comparator<Team>() {
					@Override
					public int compare(Team team1, Team team2) {
						return getTeamName(team1).compareToIgnoreCase(getTeamName(team2));
					}
				});
				this.currentScreen = Screen.MAIN;
				this.setSelectedTeam(team);
				this.teamWaitingFor = null;
			}
		}

		// update buttons
		for (GuiButton button : this.buttonList) {
			if (!(button instanceof GuiButtonTeamColor)) {
				// add to team
				if (button.displayString.contains("Add to team")) {
					button.visible = currentScreen == Screen.INFO && this.getSelectedTeam() != null;
					button.enabled = this.scrollingFindEntities.getSelectedIndex() != -1;
				}
				// remove from team
				else if (button.displayString.contains("Remove from team")) {
					button.visible = currentScreen == Screen.INFO && this.getSelectedTeam() != null;
					button.enabled = this.scrollingTeamEntities.getSelectedIndex() != -1;
				}
				// create new team
				else if (button.displayString.contains("Create New Team")) {
					button.visible = currentScreen == Screen.MAIN && this.getSelectedTeam() == null;
				}
				// set team display name
				else if (button.displayString.equals("Set")) {
					button.visible = currentScreen == Screen.MAIN && this.getSelectedTeam() != null;
					button.enabled = button.visible && !this.teamNameField.getText().equals(getTeamName(this.getSelectedTeam()))
							&& !this.teamNameField.getText().isEmpty();
				}
				// filter
				else if (button.id == 50)
					button.visible = currentScreen == Screen.INFO && this.getSelectedTeam() != null;
				// edit team
				else if (button.displayString.equals("Edit Team"))
					button.visible = currentScreen == Screen.INFO && this.getSelectedTeam() != null;
				// done editing button
				else if (button.displayString.equals("Done"))
					button.visible = (currentScreen == Screen.EDIT_TEAM || currentScreen == Screen.INFO) && 
					this.getSelectedTeam() != null;
				// QUESTION_MARK button
				else if (button.displayString.equals("?"))
					button.visible = currentScreen != Screen.QUESTION_MARK;
				// OK QUESTION_MARK button
				else if (button.displayString.equals("OK"))
					button.visible = currentScreen == Screen.QUESTION_MARK;
			}
		}

		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);

		// background
		this.drawDefaultBackground();
		mc.getTextureManager().bindTexture(BACKGROUND);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, GuiTeamStick.X_SIZE, GuiTeamStick.Y_SIZE);

		// set text box pos
		if (this.currentScreen == Screen.MAIN)
			teamNameField.xPosition = guiLeft+7;
		else
			teamNameField.xPosition = guiLeft+X_SIZE/2-teamNameField.width/2;

		// QUESTION_MARK info screen
		if (this.currentScreen == Screen.QUESTION_MARK) {
			mc.fontRendererObj.drawString(TextFormatting.DARK_GRAY.toString()+TextFormatting.ITALIC+
					"Video demonstration:", guiLeft+8, guiTop+24, 0xFFFFFF);
			mc.fontRendererObj.drawString(TextFormatting.DARK_GRAY.toString()+TextFormatting.ITALIC+
					"More info on teams:", guiLeft+8, guiTop+60, 0xFFFFFF);
		}
		
		// draw screen for scrolling lists
		scrollingTeams.drawScreen(mouseX, mouseY, partialTicks);
		if (getSelectedTeam() != null) {
			this.drawCenteredString(mc.fontRendererObj, TextFormatting.BOLD+"Selected Team: "+this.getSelectedTeam().getChatFormat()+getTeamName(this.getSelectedTeam()), guiLeft+X_SIZE/2, guiTop-10, 0xFFFFFF);

			if (this.currentScreen == Screen.INFO) {
				scrollingFindEntities.drawScreen(mouseX, mouseY, partialTicks);
				scrollingTeamEntities.drawScreen(mouseX, mouseY, partialTicks);

				String text = TextFormatting.DARK_GRAY.toString()+TextFormatting.ITALIC+
						"Add or remove nearby "+filter.name.toLowerCase()+" using the left and right sidebars, respectively.\n\n"
						+ "Or click on entities/Team blocks with the Team Stick to assign or remove teams.";
				int y = guiTop+8;
				for (String s : mc.fontRendererObj.listFormattedStringToWidth(text, X_SIZE-16)) {
					mc.fontRendererObj.drawString(s, guiLeft+8, y, 0xFFFFFF);
					y += mc.fontRendererObj.FONT_HEIGHT;
				}

				RenderHelper.enableGUIStandardItemLighting();
				this.itemRender.renderItemIntoGUI(getStack(), stackX, stackY+3);
				GlStateManager.disableLighting();

			}
		}
		else if (this.teams.isEmpty())
			this.drawCenteredString(mc.fontRendererObj, TextFormatting.GRAY+""+TextFormatting.ITALIC+"No teams created", guiLeft+X_SIZE/2, guiTop+Y_SIZE-3-52-mc.fontRendererObj.FONT_HEIGHT/2, 0xFFFFFF);
		else if (this.currentScreen == Screen.MAIN) {
			String text = TextFormatting.DARK_GRAY+""+TextFormatting.ITALIC+"or select a team below...";
			mc.fontRendererObj.drawString(text, guiLeft+X_SIZE/2-mc.fontRendererObj.getStringWidth(text)/2, guiTop+100, 0xFFFFFF);
		}

		if ((this.getSelectedTeam() != null && this.currentScreen == Screen.EDIT_TEAM) || this.currentScreen == Screen.CREATE_TEAM) {
			TextFormatting format = this.getSelectedTeam() == null ? this.selectedColor : this.getSelectedTeam().getChatFormat();
			this.drawCenteredString(mc.fontRendererObj, format+""+TextFormatting.UNDERLINE+"Team Color", guiLeft+X_SIZE/2, guiTop+8, 0xFFFFFF);
			this.drawCenteredString(mc.fontRendererObj, format+""+TextFormatting.UNDERLINE+"Team Name", guiLeft+X_SIZE/2, guiTop+58, 0xFFFFFF);

			teamNameField.drawTextBox();
		}

		// buttons
		super.drawScreen(mouseX, mouseY, partialTicks);

		// draw on top of buttons
		if (this.getSelectedTeam() != null && this.currentScreen == Screen.INFO) {
			String title = getSelectedTeam().getChatFormat()+""+TextFormatting.UNDERLINE+getTeamName(getSelectedTeam());
			this.drawCenteredString(mc.fontRendererObj, title, guiLeft+X_SIZE+3+50, guiTop+16, 0xFFFFFF);
			title = TextFormatting.UNDERLINE+filter.name+" in ";
			this.drawCenteredString(mc.fontRendererObj, title, guiLeft+X_SIZE+3+50, guiTop+4, 0xFFFFFF);
			title = TextFormatting.UNDERLINE+"Available "+filter.name;
			this.drawCenteredString(mc.fontRendererObj, title, guiLeft-3-50, guiTop+11, 0xFFFFFF);
		}

		if (getSelectedTeam() != null && currentScreen == Screen.INFO && 
				mouseX <= stackX+16 && mouseX >= stackX && mouseY <= stackY+16 && mouseY >= stackY)
			this.drawHoveringText(getStack().getTooltip(mc.thePlayer, false), mouseX, mouseY);

		GlStateManager.popMatrix();	
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		// team color
		if (button instanceof GuiButtonTeamColor) {
			if (this.currentScreen == Screen.CREATE_TEAM)
				this.selectedColor = TextFormatting.fromColorIndex(button.id);
			else if (this.getSelectedTeam() != null && this.getSelectedTeam().getChatFormat() != null && 
					this.getSelectedTeam().getChatFormat().getColorIndex() != button.id) {
				Minewatch.network.sendToServer(new CPacketSimple(7, this.getSelectedTeam().getRegisteredName(), mc.thePlayer, button.id, 0, 0));
			}
		}
		// add to team
		else if (button.displayString.contains("Add to team") && this.scrollingFindEntities.getSelectedIndex() >= 0 && 
				this.scrollingFindEntities.getSelectedIndex() <= this.entitiesFind.size()) {
			EntityLivingBase entity = this.entitiesFind.get(this.scrollingFindEntities.getSelectedIndex());
			Minewatch.network.sendToServer(new CPacketSimple(6, false, mc.thePlayer, 0, 0, 0, entity, null, this.getSelectedTeam().getRegisteredName()));
			this.entitiesFind.remove(entity);
			this.entitiesTeam.add(0, entity);
			if (this.scrollingFindEntities.getSelectedIndex() == this.entitiesFind.size())
				this.scrollingFindEntities.setSelectedIndex(this.scrollingFindEntities.getSelectedIndex()-1);
		}
		// remove from team
		else if (button.displayString.contains("Remove from team") && this.scrollingTeamEntities.getSelectedIndex() >= 0 && 
				this.scrollingTeamEntities.getSelectedIndex() <= this.entitiesTeam.size()) {
			EntityLivingBase entity = this.entitiesTeam.get(this.scrollingTeamEntities.getSelectedIndex());
			Minewatch.network.sendToServer(new CPacketSimple(6, false, mc.thePlayer, 0, 0, 0, entity, null, null));
			this.entitiesFind.add(0, entity);
			this.entitiesTeam.remove(entity);
			if (this.scrollingTeamEntities.getSelectedIndex() == this.entitiesTeam.size())
				this.scrollingTeamEntities.setSelectedIndex(this.scrollingTeamEntities.getSelectedIndex()-1);
		}
		// delete team
		else if (button.displayString.contains("Delete Team") && this.getSelectedTeam() != null) {
			mc.displayGuiScreen(new GuiYesNo(this, "Are you sure you want to", 
					"delete team "+this.getSelectedTeam().getChatFormat()+getTeamName(this.getSelectedTeam())+TextFormatting.RESET+"?", 906));
		}
		// create new team
		else if (button.displayString.contains("Create New Team") && this.getSelectedTeam() == null) {
			this.currentScreen = Screen.CREATE_TEAM;
			this.teamNameField.setText("");
			this.teamNameField.setFocused(true);
			this.updateTextBoxColor();
			this.selectedColor = TextFormatting.WHITE;
		}
		// set team display name
		else if (button.displayString.equals("Set") && this.getSelectedTeam() != null) {
			Minewatch.network.sendToServer(new CPacketSimple(9, this.getSelectedTeam().getRegisteredName(), mc.thePlayer, this.teamNameField.getText()));
		}
		// filter
		else if (button.id == 50) {
			this.filter = this.filter.next();
			this.updateEntityLists();
		}
		// create team
		else if (button.displayString.equals("Create Team")) {
			boolean valid = !teamNameField.getText().isEmpty() && 
					!teamNameField.getText().contains(" ");
			for (Team team : this.teams)
				if (team.getRegisteredName().equals(teamNameField.getText()))
					valid = false;
			if (valid) {
				Minewatch.network.sendToServer(new CPacketSimple(10, teamNameField.getText(), mc.thePlayer, this.selectedColor.getColorIndex(), 0, 0));
				this.teamWaitingFor = teamNameField.getText();
			}
		}
		// edit team
		else if (button.displayString.equals("Edit Team"))
			this.currentScreen = Screen.EDIT_TEAM;
		// done editing
		else if (button.displayString.equals("Done")) {
			if (this.currentScreen == Screen.EDIT_TEAM)
				this.currentScreen = Screen.INFO;
			else if (this.currentScreen == Screen.INFO) {
				this.setSelectedTeam(null);
				this.currentScreen = Screen.MAIN;
			}
		}
		// QUESTION_MARK
		else if (button.displayString.equals("?"))
			this.currentScreen = Screen.QUESTION_MARK;
		// OK 
		else if (button.displayString.equals("OK"))
			this.currentScreen = this.getSelectedTeam() != null ? Screen.INFO : Screen.MAIN;
		// GuiButtonURL
		else if (button instanceof GuiButtonURL) 
			this.handleComponentClick(new TextComponentString("").setStyle(new Style().setClickEvent(
					new ClickEvent(Action.OPEN_URL, ((GuiButtonURL)button).url))));
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		// delete team
		if (id == 906) {
			this.mc.displayGuiScreen(this);

			if (result && this.getSelectedTeam() != null) {
				Minewatch.network.sendToServer(new CPacketSimple(8, this.getSelectedTeam().getRegisteredName(), mc.thePlayer));
				this.teams.remove(this.getSelectedTeam());
				this.setSelectedTeam(null);
			}
			else if (!result && this.getSelectedTeam() != null)
				teamNameField.setText(getTeamName(this.getSelectedTeam()));
		}
		
		super.confirmClicked(result, id);
	}

	@Override
	public void handleMouseInput() throws IOException {
		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		scrollingTeams.handleMouseInput(mouseX, mouseY);
		if (selectedTeam != null) {
			scrollingFindEntities.handleMouseInput(mouseX, mouseY);
			scrollingTeamEntities.handleMouseInput(mouseX, mouseY);
		}
		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		teamNameField.mouseClicked(x, y, button);
		if (button == 1 && x >= teamNameField.xPosition && x < teamNameField.xPosition + teamNameField.width && y >= teamNameField.yPosition && y < teamNameField.yPosition + teamNameField.height) { 
			teamNameField.setText("");
			this.updateTextBoxColor();
		}
		super.mouseClicked(x, y, button);
	}

	@Override
	protected void keyTyped(char c, int keyCode) throws IOException {
		super.keyTyped(c, keyCode);
		teamNameField.textboxKeyTyped(c, keyCode);
		this.updateTextBoxColor();
		// update team display name when enter is hit
		if (keyCode == Keyboard.KEY_RETURN && teamNameField.isFocused() && 
				!teamNameField.getText().isEmpty() && !teamNameField.getText().equals(getTeamName(this.getSelectedTeam())))
			Minewatch.network.sendToServer(new CPacketSimple(9, this.getSelectedTeam().getRegisteredName(), mc.thePlayer, this.teamNameField.getText()));
	}

	@Override
	public void setSelectedTeam(@Nullable Team team) {
		setSelectedTeam(team, true);
	}

	public void setSelectedTeam(@Nullable Team team, boolean sendPacket) {
		if ((team != null && !team.isSameTeam(selectedTeam)) || 
				(selectedTeam != null && !selectedTeam.isSameTeam(team))) {
			if (sendPacket)
				Minewatch.network.sendToServer(new CPacketSimple(5, team == null ? null : team.getRegisteredName(), mc.thePlayer));
			selectedTeam = team;
			this.scrollingFindEntities.setSelectedIndex(-1);
			this.scrollingTeamEntities.setSelectedIndex(-1);
			if (team == null) {
				this.currentScreen = Screen.MAIN;
				entitiesFind.clear();
				entitiesTeam.clear();
			}
			else {
				this.currentScreen = Screen.INFO;
				teamNameField.setText(getTeamName(team));
				teamNameField.setFocused(true);
				this.updateTextBoxColor();
				this.updateEntityLists();
			}
		}
	}

	public void updateEntityLists() {
		entitiesFind = new ArrayList<EntityLivingBase>(mc.theWorld.getEntities(filter.filter, new Predicate<EntityLivingBase>() {
			@Override
			public boolean apply(EntityLivingBase entity) {
				return (entity.getTeam() == null || !entity.getTeam().isSameTeam(selectedTeam)) && !EntityHelper.shouldIgnoreEntity(entity);
			}}));
		entitiesFind.sort(new Comparator<EntityLivingBase>() {
			@Override
			public int compare(EntityLivingBase entity1, EntityLivingBase entity2) {
				return mc.thePlayer.getDistanceToEntity(entity1) > mc.thePlayer.getDistanceToEntity(entity2) ? 1 : -1;
			}
		});
		entitiesTeam = new ArrayList<EntityLivingBase>(mc.theWorld.getEntities(filter.filter, new Predicate<EntityLivingBase>() {
			@Override
			public boolean apply(EntityLivingBase entity) {
				return entity.getTeam() != null && entity.getTeam().isSameTeam(selectedTeam) && !EntityHelper.shouldIgnoreEntity(entity);
			}}));
		entitiesTeam.sort(new Comparator<EntityLivingBase>() {
			@Override
			public int compare(EntityLivingBase entity1, EntityLivingBase entity2) {
				return mc.thePlayer.getDistanceToEntity(entity1) > mc.thePlayer.getDistanceToEntity(entity2) ? 1 : -1;
			}
		});
	}

	public void updateTextBoxColor() {
		teamNameField.setTextColor(-1);
		// set color to red if invalid
		if (this.currentScreen == Screen.CREATE_TEAM) {
			if (teamNameField.getText().isEmpty() || teamNameField.getText().contains(" "))
				teamNameField.setTextColor(new Color(255, 100, 100).getRGB());
			else
				for (Team team : this.teams)
					if (team.getRegisteredName().equals(teamNameField.getText()))
						teamNameField.setTextColor(new Color(255, 100, 100).getRGB());
		}
	}

	/**Get first team stick held - or use fake one*/
	public ItemStack getStack() {
		for (ItemStack stack : mc.thePlayer.getHeldEquipment())
			if (stack != null && stack.getItem() == ModItems.team_stick) 
				return stack;
		return new ItemStack(ModItems.team_stick);
	}

}