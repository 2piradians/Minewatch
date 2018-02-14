package twopiradians.minewatch.client.gui.teamBlocks;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.scoreboard.Team;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.IGuiScreen.Screen;
import twopiradians.minewatch.client.gui.buttons.GuiButtonBase;
import twopiradians.minewatch.client.gui.buttons.GuiButtonBase.Render;
import twopiradians.minewatch.common.tileentity.TileEntityTeam;

@SideOnly(Side.CLIENT)
public class GuiTeamSpawn extends GuiTeamBlock {

	public GuiTeamSpawn(@Nullable TileEntityTeam objs) {
		super(objs);
		//		this.buttonList.add(new GuiButtonBase(0, guiLeft+X_SIZE/2-50/2, guiTop+5, 50, 20, format+"Name", this).setVisiblePredicate(gui->gui.getCurrentScreen() == Screen.MAIN).setNoSound().setCustomRender(Render.TEXT).setHoverText(Lists.newArrayList("This name is used by commands")));

	}

	

}