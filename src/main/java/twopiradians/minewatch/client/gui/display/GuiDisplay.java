package twopiradians.minewatch.client.gui.display;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

@SuppressWarnings({"all"})
@SideOnly(Side.CLIENT)
public class GuiDisplay extends GuiScreen {

	private EntityGuiPlayer guiPlayer;

	/**0 = everything, 1 = no name, tooltip background, or icon, 2 = only name, tooltip background, and icon*/
	private int mode;
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/white.png");

	public GuiDisplay(int mode) {
		this.mode = MathHelper.clamp(mode, 0, 2);
		guiPlayer = new EntityGuiPlayer(Minecraft.getMinecraft().world, Minecraft.getMinecraft().player.getGameProfile(), Minecraft.getMinecraft().player);
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

		for (int i=10; i<EnumHero.values().length; ++i) {
			EnumHero hero = EnumHero.values()[i];
			double x = (i-10)/perColumn * 155 - 20;
			double y = -((i-10)%perColumn)*-80+((i-10)/perColumn)+60;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0);

			// hero name
			ArrayList<String> list = new ArrayList<String>();
			list.add(hero.getFormattedName(false));
			while (Minecraft.getMinecraft().fontRenderer.getStringWidth(list.get(0)) < 140)
				list.set(0, " "+list.get(0)+" ");
			GlStateManager.translate(-(Minecraft.getMinecraft().fontRenderer.getStringWidth(list.get(0))-120)/2, 0, 0);
			list.add("");list.add("");list.add("");list.add("");list.add("");list.add("");
			if (mode == 0 || mode == 2) 
				twopiradians.minewatch.common.util.RenderHelper.drawHoveringText(null, list, 45, -33, 1, 1, -1);

			// equip player
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
				guiPlayer.setItemStackToSlot(slot, hero.getEquipment(slot) == null ? ItemStack.EMPTY : new ItemStack(EnumHero.values()[i].getEquipment(slot)));

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
			if (mode == 0 || mode == 1)
				mc.getRenderManager().doRenderEntity(guiPlayer, 0, 0.05d, 5.0D, 0.0F, 0.01f, true);
			GlStateManager.scale(1/scale, 1/scale, 1/scale);
			GlStateManager.popMatrix();

			// render items
			scale = 16/256f;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x+129, y-38, 0);
			RenderHelper.enableGUIStandardItemLighting();
			if (mode == 0 || mode == 1) {
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
			if (mode == 0 || mode == 2) {
				scale = 0.22f;
				GlStateManager.scale(scale, scale, 1);
				GlStateManager.translate(-180, 80, 0);
				hero.displayPortrait(hero, 0, 0, false, true);
			}

			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
