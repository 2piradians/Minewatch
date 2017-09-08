package twopiradians.minewatch.client.gui.tab;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.config.GuiFactory;
import twopiradians.minewatch.client.gui.display.EntityGuiPlayer;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

@SideOnly(Side.CLIENT)
public class GuiTab extends GuiScreen {

	public enum Screen {
		MAIN, GALLERY, GALLERY_HERO, GALLERY_HERO_SKINS;
	};

	/** The X size of the inventory window in pixels. */
	private static final int X_SIZE = 512/2;
	/** The Y size of the inventory window in pixels. */
	private static final int Y_SIZE = 379/2;
	private EntityGuiPlayer guiPlayer;
	private int guiLeft;
	private int guiTop;
	private EnumHero mainScreenHero;
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

		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+10, this.guiTop+GuiTab.Y_SIZE/2-20-15, 80, 20, "Hero Gallery", Screen.MAIN));
		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+10, this.guiTop+GuiTab.Y_SIZE/2-20+15, 80, 20, "Options", Screen.MAIN));
		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+198, this.guiTop+160, 50, 20, "Back", Screen.GALLERY));
		this.buttonList.add(new GuiButtonTab(0, this.guiLeft+198, this.guiTop+160, 50, 20, "Back", Screen.GALLERY_HERO));
		this.buttonList.add(new GuiButtonGalleryHero(0, this.guiLeft+12, this.guiTop+40, 80, 20, "", Screen.GALLERY_HERO)); //Skins

		int spaceBetweenX = 40;
		int spaceBetweenY = 55;
		int perRow = 6;
		for (int i=0; i<EnumHero.values().length; ++i) {
			this.buttonList.add(new GuiButtonGallery(0, (int) ((this.guiLeft+8)+(int)(i%perRow)*spaceBetweenX), (int) ((this.guiTop+30+spaceBetweenY*(int)(i/perRow))), spaceBetweenX, spaceBetweenY, "", Screen.GALLERY, EnumHero.values()[i]));
		}
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
			// equip hero 
			if (this.guiPlayer.getHeldItemMainhand() == null || 
			(this.guiPlayer.getHeldItemMainhand().getItem() instanceof ItemMWWeapon &&
					((ItemMWWeapon)this.guiPlayer.getHeldItemMainhand().getItem()).hero != this.mainScreenHero))
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
					guiPlayer.setItemStackToSlot(slot, this.mainScreenHero.getEquipment(slot) == null ? ItemStack.EMPTY : new ItemStack(this.mainScreenHero.getEquipment(slot)));
			this.guiPlayer.skin = 0;
			// logo
			this.drawTexturedModalRect(this.guiLeft, this.guiTop+2, 0, 230, 130, 24);
			// draw random hero
			int x = this.guiLeft + 180;
			int y = this.guiTop + 150;
			int scale = 60;
			GuiInventory.drawEntityOnScreen(x, y, scale, -mouseX+x, -mouseY+y-this.guiPlayer.eyeHeight*scale, this.guiPlayer);
			this.drawCenteredString(fontRendererObj, mainScreenHero.name, x, y+15, 0x7F7F7F);
			break;
		case GALLERY:
			double textScale = 1.5d;
			GlStateManager.scale(textScale, textScale, 1);
			this.fontRendererObj.drawString(TextFormatting.ITALIC+"HERO GALLERY", (int) ((this.guiLeft+10)/textScale), (int) ((this.guiTop+13)/textScale), 0, false);
			GlStateManager.scale(1.004d, 1.004d, 1);
			GlStateManager.translate(-1.3F, 0, 0);
			this.fontRendererObj.drawString(TextFormatting.ITALIC+"HERO GALLERY", (int) ((this.guiLeft+10)/textScale), (int) ((this.guiTop+13)/textScale), 0x7F7F7F, false);
			break;
		case GALLERY_HERO:
			// equip hero 
			if (this.guiPlayer.getHeldItemMainhand() == null || 
			(this.guiPlayer.getHeldItemMainhand().getItem() instanceof ItemMWWeapon &&
					((ItemMWWeapon)this.guiPlayer.getHeldItemMainhand().getItem()).hero != GuiTab.galleryHero))
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
					guiPlayer.setItemStackToSlot(slot, galleryHero.getEquipment(slot) == null ? ItemStack.EMPTY : new ItemStack(galleryHero.getEquipment(slot)));
			this.guiPlayer.skin = galleryHero.getSkin(Minecraft.getMinecraft().player.getPersistentID());
			// draw hero
			x = this.guiLeft + 180;
			y = this.guiTop + 150;
			scale = 60;
			GuiInventory.drawEntityOnScreen(x, y, scale, -mouseX+x, -mouseY+y-this.guiPlayer.eyeHeight*scale, this.guiPlayer);
			// hero name
			textScale = 1.5d;
			GlStateManager.scale(textScale, textScale, 1);
			this.fontRendererObj.drawString(TextFormatting.ITALIC+GuiTab.galleryHero.name.toUpperCase(), (int) ((this.guiLeft+14)/textScale), (int) ((this.guiTop+16)/textScale), 0, false);
			GlStateManager.scale(1.004d, 1.004d, 1);
			GlStateManager.translate(-1.3F, 0, 0);
			this.fontRendererObj.drawString(TextFormatting.ITALIC+GuiTab.galleryHero.name.toUpperCase(), (int) ((this.guiLeft+14)/textScale), (int) ((this.guiTop+16)/textScale), 0x7F7F7F, false);
			break;
		}
		GlStateManager.popMatrix();

		super.drawScreen(mouseX, mouseY, partialTicks);

		// draw hovered button above others
		if (GuiTab.currentScreen == Screen.GALLERY)
			for (GuiButton button : this.buttonList)
				if (button instanceof GuiButtonGallery && button.isMouseOver())
					button.drawButton(mc, mouseX, mouseY);
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
			}
		super.keyTyped(typedChar, keyCode);
	}

}
