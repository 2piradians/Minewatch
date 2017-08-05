package twopiradians.minewatch.client.gui.config;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

@SuppressWarnings({"all"})
@SideOnly(Side.CLIENT)
public class GuiDisplay extends GuiScreen
{
	/**Should display be opened on chat event?*/
	public static boolean display_gui = false;
	/**0 = everything, 1 = no name, tooltip background, or icon, 2 = only name, tooltip background, and icon*/
	public static final int GUI_MODE = 0;

	private EntityGuiPlayer guiPlayer;
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/white.png");

	public GuiDisplay() {
		guiPlayer = new EntityGuiPlayer(Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer.getGameProfile(), Minecraft.getMinecraft().thePlayer);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		//background
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(BACKGROUND);
		GlStateManager.pushMatrix();
		this.drawTexturedModalRect(0, 0, 0, 0, this.width, this.height);

		int perColumn = 4;

		for (int i=0; i<EnumHero.values().length; ++i) {
			EnumHero hero = EnumHero.values()[i];
			double x = i/perColumn * 200;
			double y = -(i%perColumn)*-80+(i/perColumn)+60;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0);

			// hero name
			ArrayList<String> list = new ArrayList<String>();
			list.add(hero.name);
			while (Minecraft.getMinecraft().fontRendererObj.getStringWidth(list.get(0)) < 140)
				list.set(0, " "+list.get(0)+" ");
			GlStateManager.translate(-(Minecraft.getMinecraft().fontRendererObj.getStringWidth(list.get(0))-120)/2, 0, 0);
			list.add("");list.add("");list.add("");list.add("");list.add("");list.add("");
			if (GUI_MODE == 0 || GUI_MODE == 2) 
				this.drawHoveringText(list, 45, -33);

			// equip player
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
				guiPlayer.setItemStackToSlot(slot, new ItemStack(EnumHero.values()[i].getEquipment(slot)));

			// render player
			float scale = 30f;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(180F, 0F, 0F, 1F);
			GlStateManager.rotate(135.0F, 0.0F, 1, 0.0f);
			RenderHelper.enableStandardItemLighting();
			GlStateManager.rotate(-165.0F, 0.0F, 1, -0.0f);
			GlStateManager.rotate(-10.0F, -1F, 0F, 0.5f);
			guiPlayer.rotationYawHead = 0.0F;
			guiPlayer.renderYawOffset = 0.0F;
			mc.getRenderManager().setPlayerViewY(-20f);
			if (GUI_MODE == 0 || GUI_MODE == 1)
				mc.getRenderManager().doRenderEntity(guiPlayer, 0, 0.05d, 5.0D, 0.0F, 0.01f, true);
			GlStateManager.scale(1/scale, 1/scale, 1/scale);
			GlStateManager.popMatrix();

			// render items
			scale = 16/256f;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x+129, y-38, 0);
			RenderHelper.enableGUIStandardItemLighting();
			if (GUI_MODE == 0 || GUI_MODE == 1) {
				this.itemRender.renderItemIntoGUI(new ItemStack(hero.token), -20, 5);
				this.itemRender.renderItemIntoGUI(new ItemStack(hero.helmet), 24, 0);
				this.itemRender.renderItemIntoGUI(new ItemStack(hero.chestplate), 24, 16);
				this.itemRender.renderItemIntoGUI(new ItemStack(hero.leggings), 24, 32);
				this.itemRender.renderItemIntoGUI(new ItemStack(hero.boots), 24, 48);
				this.itemRender.renderItemIntoGUI(new ItemStack(hero.weapon), 40, 24);
				if (hero.weapon.hasOffhand)
					this.itemRender.renderItemIntoGUI(new ItemStack(hero.weapon), 8, 24);
			}

			// render icon
			if (GUI_MODE == 0 || GUI_MODE == 2) {
				scale = 0.22f;
				GlStateManager.scale(scale, scale, 1);
				GlStateManager.translate(-180, 80, 0);
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/icon_background.png"));
				GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 240, 230, 0);
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+hero.name+"_icon.png"));
				GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 240, 230, 0);
			}

			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Mod.EventBusSubscriber(Side.CLIENT)
	public static class OpenGuiEvent {

		@SubscribeEvent
		public static void openGui(ClientChatReceivedEvent event) {
			if (GuiDisplay.display_gui) 
				Minecraft.getMinecraft().displayGuiScreen(new GuiDisplay());
		}
	}
}
