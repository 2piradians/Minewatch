package twopiradians.minewatch.client.gui.teamSelector;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import javax.annotation.Nullable;

import org.lwjgl.input.Mouse;

import com.google.common.base.Predicate;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.item.ItemTeamSelector;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.packet.CPacketSimple;

@SideOnly(Side.CLIENT)
public class GuiTeamSelector extends GuiScreen {

	public enum Screen {
		MAIN, CREATE_TEAM;
	};

	/** The X size of the inventory window in pixels. */
	private static final int X_SIZE = 322/2;
	/** The Y size of the inventory window in pixels. */
	private static final int Y_SIZE = 444/2;
	private int guiLeft;
	private int guiTop;
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/team_selector.png");

	public Screen currentScreen;
	private Team selectedTeam;
	public GuiScrollingTeams scrollingTeams;
	public GuiScrollingTeamEntities scrollingTeamEntities;
	public GuiScrollingFindEntities scrollingFindEntities;
	public ArrayList<EntityLivingBase> entitiesTeam = new ArrayList<EntityLivingBase>();
	public ArrayList<EntityLivingBase> entitiesFind = new ArrayList<EntityLivingBase>();
	public ArrayList<Team> teams = new ArrayList<Team>();
	public int selectedColor;
	public GuiTextField teamNameField;

	public GuiTeamSelector() {
		currentScreen = Screen.MAIN;
		selectedColor = 15;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.guiLeft = (this.width - GuiTeamSelector.X_SIZE) / 2;
		this.guiTop = (this.height - GuiTeamSelector.Y_SIZE) / 2;

		// set up scrolling lists
		scrollingTeams = new GuiScrollingTeams(this, X_SIZE-6, 0, guiTop+Y_SIZE-3-104, guiTop+Y_SIZE-3, guiLeft+3, 20, width, height);
		scrollingFindEntities = new GuiScrollingFindEntities(this, 100, 0, guiTop+30, guiTop+Y_SIZE-25, guiLeft-103, 20, width, height);
		scrollingTeamEntities = new GuiScrollingTeamEntities(this, 100, 0, guiTop+30, guiTop+Y_SIZE-25, guiLeft+X_SIZE+3, 20, width, height);

		// create buttons
		this.buttonList.add(new GuiButton(0, guiLeft-105, guiTop+Y_SIZE-22, 105, 20, "Add to team >"));
		this.buttonList.add(new GuiButton(1, guiLeft+X_SIZE+1, guiTop+Y_SIZE-22, 105, 20, "< Remove from team"));
		this.buttonList.add(new GuiButton(2, guiLeft+X_SIZE/2-50, guiTop+50, 100, 20, "Create New Team"));
		for (int i=0; i<16; ++i)
			this.buttonList.add(new GuiButtonTeamColor(i, guiLeft+12+i*18-(i/8)*144, guiTop+30+(i/8)*18, 11, 11, "", this));

		// create and sort all teams
		teams = new ArrayList<Team>(mc.world.getScoreboard().getTeams());
		teams.sort(new Comparator<Team>() {
			@Override
			public int compare(Team team1, Team team2) {
				return team1.getRegisteredName().compareToIgnoreCase(team2.getRegisteredName());
			}
		});

		// set up team name text field
		teamNameField = new GuiTextField(0, mc.fontRendererObj, guiLeft+5, guiTop+70, 104, 14);
		teamNameField.setFocused(true);
		teamNameField.setCanLoseFocus(false);
		teamNameField.setMaxStringLength(16);
		
		// find selected team
		for (ItemStack stack : mc.player.getHeldEquipment())
			if (stack != null && stack.getItem() == ModItems.team_selector)
				setSelectedTeam(ItemTeamSelector.getTeam(mc.world, stack));
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		teamNameField.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// update buttons
		for (GuiButton button : this.buttonList) {
			if (!(button instanceof GuiButtonTeamColor)) {
				// add to team
				if (button.id == 0) {
					button.visible = currentScreen == Screen.MAIN && this.getSelectedTeam() != null;
					button.enabled = this.scrollingFindEntities.getSelectedIndex() != -1;
				}
				// remove from team
				else if (button.id == 1) {
					button.visible = currentScreen == Screen.MAIN && this.getSelectedTeam() != null;
					button.enabled = this.scrollingTeamEntities.getSelectedIndex() != -1;
				}
				// create new team
				else if (button.id == 2) {
					button.visible = currentScreen == Screen.MAIN && this.getSelectedTeam() == null;
				}
			}
		}

		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);

		// background
		this.drawDefaultBackground();
		mc.getTextureManager().bindTexture(BACKGROUND);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, GuiTeamSelector.X_SIZE, GuiTeamSelector.Y_SIZE);

		// draw screen for scrolling lists
		scrollingTeams.drawScreen(mouseX, mouseY, partialTicks);
		if (getSelectedTeam() != null) {
			this.drawCenteredString(mc.fontRendererObj, getSelectedTeam().getChatFormat()+""+TextFormatting.UNDERLINE+"Team Color", guiLeft+X_SIZE/2, guiTop+11, 0xFFFFFF);

			teamNameField.drawTextBox();

			scrollingFindEntities.drawScreen(mouseX, mouseY, partialTicks);
			scrollingTeamEntities.drawScreen(mouseX, mouseY, partialTicks);

			String title = getSelectedTeam().getChatFormat()+""+TextFormatting.UNDERLINE+getSelectedTeam().getRegisteredName();
			this.drawCenteredString(mc.fontRendererObj, title, guiLeft+X_SIZE+3+50, guiTop+16, 0xFFFFFF);
			title = TextFormatting.UNDERLINE+"Entities in ";
			this.drawCenteredString(mc.fontRendererObj, title, guiLeft+X_SIZE+3+50, guiTop+4, 0xFFFFFF);
			title = TextFormatting.UNDERLINE+"Available Entities";
			this.drawCenteredString(mc.fontRendererObj, title, guiLeft-3-50, guiTop+11, 0xFFFFFF);
		}
		else if (this.teams.isEmpty())
			this.drawCenteredString(mc.fontRendererObj, TextFormatting.GRAY+""+TextFormatting.ITALIC+"No teams created", guiLeft+X_SIZE/2, guiTop+Y_SIZE-3-52-mc.fontRendererObj.FONT_HEIGHT/2, 0xFFFFFF);
		else {
			this.drawCenteredString(mc.fontRendererObj, TextFormatting.GRAY+""+TextFormatting.ITALIC+"or select a team below...", guiLeft+X_SIZE/2, guiTop+100, 0xFFFFFF);
		}

		// buttons
		super.drawScreen(mouseX, mouseY, partialTicks);

		GlStateManager.popMatrix();	
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (currentScreen) {
		case MAIN:
			// team color
			if (button instanceof GuiButtonTeamColor) {
				if (this.getSelectedTeam() != null && this.getSelectedTeam().getChatFormat() != null && 
						this.getSelectedTeam().getChatFormat().getColorIndex() != button.id)
					Minewatch.network.sendToServer(new CPacketSimple(7, this.getSelectedTeam().getRegisteredName(), mc.player, button.id, 0, 0));
			}
			// add to team
			else if (button.id == 0 && this.scrollingFindEntities.getSelectedIndex() >= 0 && 
					this.scrollingFindEntities.getSelectedIndex() <= this.entitiesFind.size()) {
				EntityLivingBase entity = this.entitiesFind.get(this.scrollingFindEntities.getSelectedIndex());
				Minewatch.network.sendToServer(new CPacketSimple(6, false, mc.player, 0, 0, 0, entity, null, this.getSelectedTeam().getRegisteredName()));
				this.entitiesFind.remove(entity);
				this.entitiesTeam.add(0, entity);
				if (this.scrollingFindEntities.getSelectedIndex() == this.entitiesFind.size())
					this.scrollingFindEntities.setSelectedIndex(this.scrollingFindEntities.getSelectedIndex()-1);
			}
			// remove from team
			else if (button.id == 1 && this.scrollingTeamEntities.getSelectedIndex() >= 0 && 
					this.scrollingTeamEntities.getSelectedIndex() <= this.entitiesTeam.size()) {
				EntityLivingBase entity = this.entitiesTeam.get(this.scrollingTeamEntities.getSelectedIndex());
				Minewatch.network.sendToServer(new CPacketSimple(6, false, mc.player, 0, 0, 0, entity, null, null));
				this.entitiesFind.add(0, entity);
				this.entitiesTeam.remove(entity);
				if (this.scrollingTeamEntities.getSelectedIndex() == this.entitiesTeam.size())
					this.scrollingTeamEntities.setSelectedIndex(this.scrollingTeamEntities.getSelectedIndex()-1);
			}
			break;
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		scrollingTeams.handleMouseInput(mouseX, mouseY);
		if (selectedTeam != null) {
			scrollingFindEntities.handleMouseInput(mouseX, mouseY);
			scrollingTeamEntities.handleMouseInput(mouseX, mouseY);
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		teamNameField.mouseClicked(x, y, button);
		if (button == 1 && x >= teamNameField.xPosition && x < teamNameField.xPosition + teamNameField.width && y >= teamNameField.yPosition && y < teamNameField.yPosition + teamNameField.height) 
			teamNameField.setText("");
		super.mouseClicked(x, y, button);
	}

	@Override
	protected void keyTyped(char c, int keyCode) throws IOException {
		super.keyTyped(c, keyCode);
		teamNameField.textboxKeyTyped(c, keyCode);
		teamNameField.setTextColor(-1);
		for (Team team : teams) // TODO switch over to display name and use actual team name check here
			if (teamNameField.getText().isEmpty() || 
					(!team.isSameTeam(getSelectedTeam()) && team.getRegisteredName().equals(teamNameField.getText()))) {
				teamNameField.setTextColor(new Color(0xFF5555).getRGB());
			}
	}

	@Nullable
	public Team getSelectedTeam() {
		return selectedTeam;
	}

	public void setSelectedTeam(@Nullable Team team) {
		if ((team != null && !team.isSameTeam(selectedTeam)) || 
				(selectedTeam != null && !selectedTeam.isSameTeam(team))) {
			selectedTeam = team;
			this.scrollingFindEntities.setSelectedIndex(-1);
			this.scrollingTeamEntities.setSelectedIndex(-1);
			if (team == null) {
				entitiesFind.clear();
				entitiesTeam.clear();
			}
			else {
				teamNameField.setText(team.getRegisteredName());
				teamNameField.setFocused(true);
				entitiesFind = new ArrayList<EntityLivingBase>(mc.world.getEntities(EntityLivingBase.class, new Predicate<EntityLivingBase>() {
					@Override
					public boolean apply(EntityLivingBase entity) {
						return entity.getTeam() == null || !entity.getTeam().isSameTeam(selectedTeam) && !(entity instanceof EntityLivingBaseMW);
					}}));
				entitiesFind.sort(new Comparator<EntityLivingBase>() {
					@Override
					public int compare(EntityLivingBase entity1, EntityLivingBase entity2) {
						return mc.player.getDistanceToEntity(entity1) > mc.player.getDistanceToEntity(entity2) ? 1 : -1;
					}
				});
				entitiesTeam = new ArrayList<EntityLivingBase>(mc.world.getEntities(EntityLivingBase.class, new Predicate<EntityLivingBase>() {
					@Override
					public boolean apply(EntityLivingBase entity) {
						return entity.getTeam() != null && entity.getTeam().isSameTeam(selectedTeam) && !(entity instanceof EntityLivingBaseMW);
					}}));
				entitiesTeam.sort(new Comparator<EntityLivingBase>() {
					@Override
					public int compare(EntityLivingBase entity1, EntityLivingBase entity2) {
						return mc.player.getDistanceToEntity(entity1) > mc.player.getDistanceToEntity(entity2) ? 1 : -1;
					}
				});
			}
		}
	}

}