package twopiradians.minewatch.client.gui.tab;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.config.GuiFactory;
import twopiradians.minewatch.client.gui.display.EntityGuiPlayer;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.packet.CPacketSyncSkins;

@SideOnly(Side.CLIENT)
public class GuiTab extends GuiScreen {

	public enum Screen {
		MAIN, GALLERY, GALLERY_HERO, GALLERY_HERO_INFO, GALLERY_HERO_SKINS, GALLERY_HERO_SKINS_INFO;
	};

	/** The X size of the inventory window in pixels. */
	private static final int X_SIZE = 512/2;
	/** The Y size of the inventory window in pixels. */
	private static final int Y_SIZE = 379/2;
	private EntityGuiPlayer guiPlayer;
	private int guiLeft;
	private int guiTop;
	private EnumHero mainScreenHero;
	private GuiButtonTab buttonOKGHSI;
	private GuiButtonTab buttonOKGHI;
	public static EnumHero galleryHero;
	public static Screen currentScreen;
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/inventory_tab.png");

	public GuiTab() {
		guiPlayer = new EntityGuiPlayer(Minecraft.getMinecraft().world, Minecraft.getMinecraft().player.getGameProfile(), Minecraft.getMinecraft().player);
		mainScreenHero = EnumHero.values()[guiPlayer.world.rand.nextInt(EnumHero.values().length)];
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
			guiPlayer.setItemStackToSlot(slot, mainScreenHero.getEquipment(slot) == null ? ItemStack.EMPTY : new ItemStack(mainScreenHero.getEquipment(slot)));
		GuiTab.currentScreen = Screen.MAIN;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.guiLeft = (this.width - GuiTab.X_SIZE) / 2;
		this.guiTop = (this.height - GuiTab.Y_SIZE) / 2;

		TabRegistry.updateTabValues(this.guiLeft, this.guiTop, InventoryTab.class);
		TabRegistry.addTabsToList(this.buttonList);

		// Screen.MAIN
		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+10, this.guiTop+GuiTab.Y_SIZE/2-20-15, 80, 20, "Hero Gallery", Screen.MAIN));
		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+10, this.guiTop+GuiTab.Y_SIZE/2-20+15, 80, 20, "Options", Screen.MAIN));
		// Screen.GALLERY
		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+198, this.guiTop+Y_SIZE-29, 50, 20, "Back", Screen.GALLERY));
		int spaceBetweenX = 40;
		int spaceBetweenY = 55;
		int perRow = 6;
		for (int i=0; i<EnumHero.values().length; ++i) 
			this.buttonList.add(new GuiButtonGallery(0, (int) ((this.guiLeft+8)+(int)(i%perRow)*spaceBetweenX), (int) ((this.guiTop+37+spaceBetweenY*(int)(i/perRow))), spaceBetweenX, spaceBetweenY, "", Screen.GALLERY, EnumHero.values()[i]));
		// Screen.GALLERY_HERO
		this.buttonList.add(new GuiButtonGalleryHero(1, this.guiLeft+12, this.guiTop+40, 100, 20, "", Screen.GALLERY_HERO)); //Skins
		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+198, this.guiTop+Y_SIZE-29, 50, 20, "Back", Screen.GALLERY_HERO));
		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+X_SIZE/2-58/2, this.guiTop+Y_SIZE-29, 58, 20, "HERO INFO", Screen.GALLERY_HERO)); 
		// Screen.GALLERY_HERO_INFO
		this.buttonOKGHI = new GuiButtonTab(0, 0, 0, 40, 20, "OK", Screen.GALLERY_HERO_INFO); 
		this.buttonList.add(this.buttonOKGHI);
		// Screen.GALLERY_HERO_SKINS
		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+198, this.guiTop+Y_SIZE-29, 50, 20, "Back", Screen.GALLERY_HERO_SKINS));
		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+X_SIZE/2-58/2, this.guiTop+Y_SIZE-29, 58, 20, "HERO INFO", Screen.GALLERY_HERO_SKINS)); 
		for (int i=0; i<6; ++i) {
			this.buttonList.add(new GuiButtonSkin(i, this.guiLeft+5, this.guiTop+28+i*22, 100, 20, "", Screen.GALLERY_HERO_SKINS)); 
			this.buttonList.add(new GuiButtonSkin(i, this.guiLeft+5+101, this.guiTop+28+i*22, 20, 20, "?", Screen.GALLERY_HERO_SKINS)); 
		}
		// Screen.GALLERY_HERO_SKINS_INFO
		this.buttonOKGHSI = new GuiButtonTab(0, 0, 0, 40, 20, "OK", Screen.GALLERY_HERO_SKINS_INFO); 
		this.buttonList.add(this.buttonOKGHSI);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.guiPlayer.ticksExisted = Minecraft.getMinecraft().player.ticksExisted;

		GlStateManager.pushMatrix();
		// background
		this.drawDefaultBackground();
		mc.getTextureManager().bindTexture(BACKGROUND);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, GuiTab.X_SIZE, GuiTab.Y_SIZE);

		switch (GuiTab.currentScreen) {
		case MAIN:
			this.drawTexturedModalRect(this.guiLeft, this.guiTop+2, 0, 230, 130, 24);
			this.drawHero(mainScreenHero, mainScreenHero.getSkin(Minecraft.getMinecraft().player.getPersistentID()), mouseX, mouseY);
			this.drawCenteredString(fontRenderer, mainScreenHero.name, this.guiLeft + 190, this.guiTop + 165, 0x7F7F7F);
			break;
		case GALLERY:
			double textScale = 1.5d;
			GlStateManager.scale(textScale, textScale, 1);
			this.fontRenderer.drawString(TextFormatting.ITALIC+"HERO GALLERY", (int) ((this.guiLeft+14)/textScale), (int) ((this.guiTop+16)/textScale), 0, false);
			GlStateManager.scale(1.004d, 1.004d, 1);
			GlStateManager.translate(-1.3F, 0, 0);
			this.fontRenderer.drawString(TextFormatting.ITALIC+"HERO GALLERY", (int) ((this.guiLeft+14)/textScale), (int) ((this.guiTop+16)/textScale), 0x7F7F7F, false);
			break;
		case GALLERY_HERO:
			this.drawHero(galleryHero, galleryHero.getSkin(Minecraft.getMinecraft().player.getPersistentID()), mouseX, mouseY);
			// hero name
			textScale = 1.5d;
			GlStateManager.scale(textScale, textScale, 1);
			this.fontRenderer.drawString(TextFormatting.ITALIC+(galleryHero == EnumHero.SOLDIER76 ? "Soldier: 76" : galleryHero.name).toUpperCase(), (int) ((this.guiLeft+14)/textScale), (int) ((this.guiTop+16)/textScale), 0, false);
			GlStateManager.scale(1.004d, 1.004d, 1);
			GlStateManager.translate(-1.3F, 0, 0);
			this.fontRenderer.drawString(TextFormatting.ITALIC+(galleryHero == EnumHero.SOLDIER76 ? "Soldier: 76" : galleryHero.name).toUpperCase(), (int) ((this.guiLeft+14)/textScale), (int) ((this.guiTop+16)/textScale), 0x7F7F7F, false);
			break;
		case GALLERY_HERO_INFO:
			ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
			galleryHero.displayInfoScreen(res);
			// draw and position OK button manually
			this.buttonOKGHI.x = (int) (res.getScaledWidth_double()/2 - this.buttonOKGHI.width/2);
			this.buttonOKGHI.y = res.getScaledHeight() - this.buttonOKGHI.height - 10;
			this.buttonOKGHI.drawButton(mc, mouseX, mouseY, partialTicks);
			break;
		case GALLERY_HERO_SKINS:
			this.drawHero(galleryHero, galleryHero.getSkin(Minecraft.getMinecraft().player.getPersistentID()), mouseX, mouseY);
			// hero name
			textScale = 1.5d;
			GlStateManager.scale(textScale, textScale, 1);
			this.fontRenderer.drawString(TextFormatting.ITALIC+(galleryHero == EnumHero.SOLDIER76 ? "Soldier: 76" : galleryHero.name).toUpperCase(), (int) ((this.guiLeft+14)/textScale), (int) ((this.guiTop+16)/textScale), 0, false);
			GlStateManager.scale(1.004d, 1.004d, 1);
			GlStateManager.translate(-1.3F, 0, 0);
			this.fontRenderer.drawString(TextFormatting.ITALIC+(galleryHero == EnumHero.SOLDIER76 ? "Soldier: 76" : galleryHero.name).toUpperCase(), (int) ((this.guiLeft+14)/textScale), (int) ((this.guiTop+16)/textScale), 0x7F7F7F, false);
			break;
		case GALLERY_HERO_SKINS_INFO:
			res = new ScaledResolution(Minecraft.getMinecraft());
			galleryHero.displayInfoScreen(res);
			// draw and position OK button manually
			this.buttonOKGHSI.x = (int) (res.getScaledWidth_double()/2 - this.buttonOKGHSI.width/2);
			this.buttonOKGHSI.y = res.getScaledHeight() - this.buttonOKGHSI.height - 10;
			this.buttonOKGHSI.drawButton(mc, mouseX, mouseY, partialTicks);
			break;
		}
		GlStateManager.popMatrix();

		if (currentScreen != Screen.GALLERY_HERO_INFO && currentScreen != Screen.GALLERY_HERO_SKINS_INFO)
			super.drawScreen(mouseX, mouseY, partialTicks);

		// draw hovered button above others
		if (GuiTab.currentScreen == Screen.GALLERY)
			for (GuiButton button : this.buttonList)
				if (button instanceof GuiButtonGallery && button.isMouseOver())
					button.drawButton(mc, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (GuiTab.currentScreen) {
		case MAIN:
			if (button.displayString.equals("Hero Gallery"))
				GuiTab.currentScreen = Screen.GALLERY;
			else if (button.displayString.equals("Options"))
				Minecraft.getMinecraft().displayGuiScreen(new GuiFactory().createConfigGui(this));
			break;
		case GALLERY:
			if (button.displayString.equals("Back"))
				GuiTab.currentScreen = Screen.MAIN;
			else if (button instanceof GuiButtonGallery) {
				GuiTab.galleryHero = ((GuiButtonGallery)button).hero;
				GuiTab.currentScreen = Screen.GALLERY_HERO;
			}
			break;
		case GALLERY_HERO:
			if (button.displayString.equals("Back"))
				GuiTab.currentScreen = Screen.GALLERY;
			else if (button.displayString.equals("HERO INFO"))
				GuiTab.currentScreen = Screen.GALLERY_HERO_INFO;
			else if (button.id == 1) // skins
				GuiTab.currentScreen = Screen.GALLERY_HERO_SKINS;
			break;
		case GALLERY_HERO_INFO:
			if (button.displayString.equals("OK"))
				GuiTab.currentScreen = Screen.GALLERY_HERO;
			break;
		case GALLERY_HERO_SKINS:
			if (button.displayString.equals("Back"))
				GuiTab.currentScreen = Screen.GALLERY_HERO;
			else if (button.displayString.equals("HERO INFO"))
				GuiTab.currentScreen = Screen.GALLERY_HERO_SKINS_INFO;
			else if (button.displayString.equals("?")) {
				EnumHero.Skin skin = galleryHero.skinInfo[button.id];
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString(
						"This skin is ")
						.appendSibling(new TextComponentString(TextFormatting.BLUE+""+TextFormatting.UNDERLINE+skin.skinName).setStyle(
								new Style().setClickEvent(new ClickEvent(Action.OPEN_URL, skin.address))))
						.appendSibling(new TextComponentString(" by "+skin.author)));
			}
			else if (button instanceof GuiButtonSkin) 
				if (galleryHero.getSkin(Minecraft.getMinecraft().player.getPersistentID()) != button.id) {
					galleryHero.setSkin(Minecraft.getMinecraft().player.getPersistentID(), button.id);
					Minewatch.network.sendToServer(new CPacketSyncSkins(Minecraft.getMinecraft().player.getPersistentID()));
				}
			break;
		case GALLERY_HERO_SKINS_INFO:
			if (button.displayString.equals("OK"))
				GuiTab.currentScreen = Screen.GALLERY_HERO_SKINS;
			break;
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE)
			switch (GuiTab.currentScreen) {
			case GALLERY:
				GuiTab.currentScreen = Screen.MAIN;
				return;
			case GALLERY_HERO:
				GuiTab.currentScreen = Screen.GALLERY;
				return;
			case GALLERY_HERO_INFO:
				GuiTab.currentScreen = Screen.GALLERY_HERO;
				return;
			case GALLERY_HERO_SKINS:
				GuiTab.currentScreen = Screen.GALLERY_HERO;
				return;
			case GALLERY_HERO_SKINS_INFO:
				GuiTab.currentScreen = Screen.GALLERY_HERO_SKINS;
				return;
			}
		else if (keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())
			this.mc.displayGuiScreen(new GuiInventory(this.mc.player));
		super.keyTyped(typedChar, keyCode);
	}

	public void drawHero(EnumHero hero, int skin, int mouseX, int mouseY) {
		// equip hero 
		if (this.guiPlayer.getHeldItemMainhand() == null || 
				(this.guiPlayer.getHeldItemMainhand().getItem() instanceof ItemMWWeapon &&
						((ItemMWWeapon)this.guiPlayer.getHeldItemMainhand().getItem()).hero != hero))
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
				guiPlayer.setItemStackToSlot(slot, hero.getEquipment(slot) == null ? ItemStack.EMPTY : new ItemStack(hero.getEquipment(slot)));
		this.guiPlayer.skin = skin;
		// draw hero
		int x = this.guiLeft + 190;
		int y = this.guiTop + 150;
		int scale = 60;
		GuiInventory.drawEntityOnScreen(x, y, scale, -mouseX+x, -mouseY+y-this.guiPlayer.eyeHeight*scale, this.guiPlayer);
	}

}
