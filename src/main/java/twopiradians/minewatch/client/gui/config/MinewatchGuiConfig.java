package twopiradians.minewatch.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;

public class MinewatchGuiConfig extends GuiConfig {
	
	public MinewatchGuiConfig(GuiScreen parent) {
		super(parent, getConfigElements(), Minewatch.MODID, false, false, Minewatch.MODNAME+" Configuration");
	}

	private static List<IConfigElement> getConfigElements() {
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.addAll(new ConfigElement(Config.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
		return list;
	}
}