package twopiradians.minewatch.client.gui.heroSelect;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.IGuiScreen;
import twopiradians.minewatch.client.gui.buttons.GuiButtonBase;
import twopiradians.minewatch.client.gui.buttons.GuiButtonBase.Render;
import twopiradians.minewatch.client.gui.display.EntityGuiPlayer;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.EnumHero.Type;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.packet.CPacketSimple;

@SideOnly(Side.CLIENT)
public class GuiHeroSelect extends GuiScreen implements IGuiScreen {

	private static final ResourceLocation BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/hero_select.png");

	public Screen currentScreen;
	public List<String> hoverText;
	private EnumHero selectedHero;
	private EntityGuiPlayer guiPlayer;

	public GuiHeroSelect() {
		currentScreen = Screen.MAIN;
		this.setSelectedHero(null);
		guiPlayer = new EntityGuiPlayer(Minecraft.getMinecraft().world, Minecraft.getMinecraft().player.getGameProfile(), Minecraft.getMinecraft().player);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		super.initGui();

		// hero buttons
		Type prevType = null;
		int betweenX = -4;
		int bigBetweenX = 20;
		int fullWidth = this.width-bigBetweenX*5-betweenX*(EnumHero.ORDERED_HEROES.size()+1);
		int width = fullWidth / (EnumHero.ORDERED_HEROES.size()+1);
		int height = width;
		int x = width/2;
		int y = (int) (this.height * 0.8f);
		for (int i=0; i<EnumHero.ORDERED_HEROES.size(); ++i) {
			EnumHero hero = EnumHero.ORDERED_HEROES.get(i);
			if (i > 0) 
				x += width+betweenX;
			// moving to new type
			if ((hero != null && hero.type != prevType) || i == 14) {
				x += bigBetweenX;
				prevType = i == 14 ? Type.TANK : hero.type;
				this.buttonList.add(new GuiButtonBase(prevType.ordinal(), (int) (x-bigBetweenX*0.7f), y+2, 19, 19, prevType.name(), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setNoSound().setCustomRender(Render.HERO_TYPE));
			}
			this.buttonList.add(new GuiButtonBase(i, x, y, width, height, hero == null ? null : hero.getFormattedName(true), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setHero(hero).setNoSound());
		}

		// other buttons
		this.buttonList.add(new GuiButtonBase(100, this.width/2-50, y+(this.height-y+height)/2-10, 80, 20, Minewatch.translate("gui.hero_select.select"), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setColor(new Color(0xFFB43D)));
		this.buttonList.add(new GuiButtonBase(101, this.width-30, 10, 20, 20, "!", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setColor(new Color(0x9AE6FD)));
		this.buttonList.add(new GuiButtonBase(102, this.width/2-50, y+(this.height-y+height)/2-10, 80, 20, Minewatch.translate("gui.team_block.button.ok"), this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.HERO_INFO).setColor(new Color(0x9AE6FD)));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.guiPlayer.ticksExisted = Minecraft.getMinecraft().player.ticksExisted;

		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);

		// background
		GlStateManager.pushMatrix();
		this.drawGradientRect(0, 0, this.width, this.height, 0x50FFFFFF, 0x10FFFFFF);
		GlStateManager.popMatrix();

		// main
		GlStateManager.pushMatrix();
		switch (this.currentScreen) {
		case MAIN:
			// select a hero
			GlStateManager.pushMatrix();
			double scale = 2.7d;
			GlStateManager.scale(scale, scale, 1);
			String text = TextFormatting.AQUA+""+TextFormatting.ITALIC+Minewatch.translate("gui.hero_select.select_hero").toUpperCase();
			this.drawString(mc.fontRendererObj, text, 5, 5, 0xFFFFFF);
			GlStateManager.popMatrix();

			// hero name
			GlStateManager.pushMatrix();
			scale = 1.7d;
			GlStateManager.scale(scale, scale, 1);
			text = TextFormatting.WHITE+""+TextFormatting.ITALIC+this.getSelectedHero().getFormattedName(true);
			mc.fontRendererObj.drawString(text, (int) (this.width/scale-mc.fontRendererObj.getStringWidth(text)-25), 8, 0xFFFFFF, true);
			GlStateManager.popMatrix();

			// draw guiPlayer
			GlStateManager.enableDepth();
			GlStateManager.translate(0, 0, -500);
			scale = Math.min(width, height) / 2;
			int x = this.width/2;
			int y = (int) (guiPlayer.height*scale+this.height*0.2f);
			GuiInventory.drawEntityOnScreen(x, y, (int) scale, -mouseX+x, (float) (-mouseY-this.guiPlayer.eyeHeight*scale)+y, this.guiPlayer);
			GlStateManager.translate(0, 0, 500);

			// background 
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			mc.getTextureManager().bindTexture(BACKGROUND);
			GlStateManager.scale(width/256d, height/256d, 0);
			this.drawTexturedModalRect(0, 0, 0, 0, this.width, this.height);
			GlStateManager.popMatrix();

			// portrait
			scale = Math.min(width, height) / 1500d;
			GlStateManager.scale(scale, scale, 1);
			EnumHero.displayPortrait(this.getSelectedHero(), this.width/2/scale-128, this.height/2/scale, this.getSelectedHero() != SetManager.getWornSet(mc.player));
			GlStateManager.scale(1/scale, 1/scale, 1);
			this.drawCenteredString(mc.fontRendererObj, TextFormatting.AQUA+""+TextFormatting.ITALIC+mc.player.getName().toUpperCase(), (int) (this.width/2), (int) (this.height*0.67f), 0xFFFFFF);

			break;
		case HERO_INFO:
			this.getSelectedHero().displayInfoScreen(this.width, this.height);
			break;
		}
		GlStateManager.popMatrix();

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
		if (button.id >= 0 && button.id < EnumHero.ORDERED_HEROES.size() && EnumHero.ORDERED_HEROES.get(button.id) != null &&
				button.isMouseOver()) 
			this.setSelectedHero(EnumHero.ORDERED_HEROES.get(button.id));
		switch (button.id) {
		case 100: 
			Minewatch.network.sendToServer(new CPacketSimple(11, "/mw hero "+this.getSelectedHero().name, mc.player)); // TODO do without perms and clear other items, sounds?
			Minecraft.getMinecraft().displayGuiScreen(null);
			break;
		case 101:
			this.currentScreen = Screen.HERO_INFO;
			break;
		case 102:
			this.currentScreen = Screen.MAIN;
			break;
		}
	}

	@Override
	protected void keyTyped(char c, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE)
			switch (currentScreen) {
			case HERO_INFO:
				currentScreen = Screen.MAIN;
				return;
			}

		super.keyTyped(c, keyCode);
	}

	public EnumHero getSelectedHero() {
		if (this.selectedHero == null) {
			if (SetManager.getWornSet(mc.player) != null) // worn hero
				this.setSelectedHero(SetManager.getWornSet(mc.player));
			else // random hero
				this.setSelectedHero(EnumHero.values()[mc.world.rand.nextInt(EnumHero.values().length)]);
		}

		return this.selectedHero;
	}

	public void setSelectedHero(@Nullable EnumHero hero) {
		if (this.selectedHero != hero) {
			// equip guiPlayer
			if (hero != null) {
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
					guiPlayer.setItemStackToSlot(slot, hero.getEquipment(slot) == null ? ItemStack.EMPTY : new ItemStack(hero.getEquipment(slot)));
				guiPlayer.skin = hero.getSkin(Minecraft.getMinecraft().player.getPersistentID());
			}

			this.selectedHero = hero;
		}
	}

	@Override
	public Screen getCurrentScreen() {
		return currentScreen;
	}

}