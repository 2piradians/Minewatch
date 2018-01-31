package twopiradians.minewatch.client.gui.tab;

import java.util.ArrayList;
import java.util.Comparator;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero.Skin;
import twopiradians.minewatch.packet.CPacketSyncSkins;

public class GuiScrollingSkins extends GuiScrollingList {

	private GuiTab gui;
	private ArrayList<Skin> skins = new ArrayList<Skin>();

	public GuiScrollingSkins(GuiTab gui, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
		super(gui.mc, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
		this.gui = gui;
	}

	@Override
	protected int getSize() {
		return this.skins.size();
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
		this.selectedIndex = index;
		// get actual skin index
		int skinIndex = getSkinIndex();
		// select skin
		if (GuiTab.galleryHero.getSkin(Minewatch.proxy.getClientUUID()) != skinIndex) {
			GuiTab.galleryHero.setSkin(Minewatch.proxy.getClientUUID(), skinIndex);
			Minewatch.network.sendToServer(new CPacketSyncSkins(Minewatch.proxy.getClientUUID()));
		}
	}

	@Override
	protected boolean isSelected(int index) {
		return this.selectedIndex == index;
	}

	@Override
	protected void drawBackground() {}

	@Override
	protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawSlot(int index, int right, int top, int height, Tessellator tess) {	
		if (index >= 0 && index < this.skins.size()) {
			Skin skin = this.skins.get(index);
			gui.mc.fontRendererObj.drawStringWithShadow(skin.getOWName(), left+10, top+gui.mc.fontRendererObj.FONT_HEIGHT/2, 0xFFFFFF);
		}
	}

	public void setSelectedIndex(int index) {
		this.selectedIndex = index;
	}

	public int getSelectedIndex() {
		return this.selectedIndex;
	}

	public void updateSkins() {
		// update skins
		Skin[] allSkins = GuiTab.galleryHero.skinInfo;
		this.skins = Lists.newArrayList(allSkins);
		// sort skins
		this.skins.sort(new Comparator<Skin>() {
			@Override // sort alphabetically
			public int compare(Skin skin1, Skin skin2) {
				return skin2.owName.compareToIgnoreCase(skin1.owName);
			}
		});
		this.skins.sort(new Comparator<Skin>() {
			@Override // sort by type
			public int compare(Skin skin1, Skin skin2) {
				return skin1.type.ordinal() > skin2.type.ordinal() ? 1 : -1;
			}
		});
		// selected index
		int selected = GuiTab.galleryHero.getSkin(Minewatch.proxy.getClientUUID());
		if (selected >= 0 && selected < allSkins.length && skins.contains(allSkins[selected]))
			this.selectedIndex = skins.indexOf(allSkins[selected]);
		else
			this.selectedIndex = 0;
	}

	@Override
	protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2) {
		/*// override default colors (not used currently)
		if (color1 == 0xC0101010 && color2 == 0xD0101010) {
			color1 = new Color(0x405077).getRGB();
			color2 = new Color(0x405077).getRGB();
		}*/

		super.drawGradientRect(left, top, right, bottom, color1, color2);
	}
	
	public int getSkinIndex() {
		int skinIndex = 0;
		for (int i=0; i<GuiTab.galleryHero.skinInfo.length; ++i)
			if (GuiTab.galleryHero.skinInfo[i] == this.skins.get(this.selectedIndex)) {
				skinIndex = i;
				break;
			}
		return skinIndex;
	}

}