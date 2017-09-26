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
		Config.config.getCategory(Config.CATEGORY_HERO_SKINS).setLanguageKey(Config.CATEGORY_HERO_SKINS);
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new ConfigElement(Config.config.getCategory(Config.CATEGORY_CLIENT_SIDE).setLanguageKey(Config.CATEGORY_CLIENT_SIDE)));
		list.add(new ConfigElement(Config.config.getCategory(Config.CATEGORY_SERVER_SIDE).setLanguageKey(Config.CATEGORY_SERVER_SIDE)));
		list.addAll(new ConfigElement(Config.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
		return list;
	}
}