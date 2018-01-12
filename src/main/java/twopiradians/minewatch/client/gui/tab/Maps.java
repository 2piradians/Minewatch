package twopiradians.minewatch.client.gui.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;

public class Maps {

	public static enum MinecraftMap {

		EICHENWALDE_TRIXY_BLOX(OverwatchMap.EICHENWALDE, "Trixy Blox", "https://www.planetminecraft.com/project/overwatch---eichenwalde-castle/"),
		ILIOS_RAMZES(OverwatchMap.ILIOS, "ramzes222", "https://www.planetminecraft.com/project/overwatch-minecraft-edition/"),
		HANAMURA_REHCTELF(OverwatchMap.HANAMURA, "RehctElf68", "https://www.planetminecraft.com/project/overwatch-map---hanamura/"),
		NECROPOLIS_CAGRIMANOKA(OverwatchMap.NECROPOLIS, "Cagrimanoka_TR", "https://www.planetminecraft.com/project/necropolis-overwatch-world-download/"),
		BLACK_FOREST_CAGRIMANOKA(OverwatchMap.BLACK_FOREST, "Cagrimanoka_TR", "https://www.planetminecraft.com/project/world-save-black-forest-overwatch/"),
		DR_JUNKENSTEINS_DANL(OverwatchMap.DR_JUNKENSTEINS, "danl16boon", "https://www.planetminecraft.com/project/dr-junkensteins-revenge/"),
		ECOPOINT_MARSHMELLOCAT(OverwatchMap.ECOPOINT, "MarshmelloCAT", "https://www.planetminecraft.com/project/ecopoint-antarctica/"),
		CASTILLO_ISAK20(OverwatchMap.CASTILLO, "Isak20", "https://www.planetminecraft.com/project/castillo-overwatch/"),
		KINGSROW_GALIL(OverwatchMap.KINGS_ROW, "GalilWaifu", "https://www.planetminecraft.com/project/overwatch-king-s-row-wip/"),
		CHATEAU_ISAK20(OverwatchMap.CHATEAU, "Isak20", "https://www.planetminecraft.com/project/ch-teau-guillard-overwatch/");

		public OverwatchMap map;
		public String creator;
		public String url;

		private MinecraftMap(OverwatchMap map, String creator, String url) {
			this.map = map;
			this.creator = creator;
			this.url = url;
		}

		@SideOnly(Side.CLIENT)
		public void drawBackground(Gui gui) {
			Minecraft mc = Minecraft.getMinecraft();
			ScaledResolution resolution = new ScaledResolution(mc);
			GlStateManager.pushMatrix();
			GlStateManager.scale(resolution.getScaledWidth_double()/256d, resolution.getScaledHeight_double()/256d, 1);
			mc.getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID+":textures/gui/map_"+this.ordinal()+".png"));
			gui.drawTexturedModalRect(0, 0, 0, 0, mc.displayWidth, mc.displayHeight);
			GlStateManager.popMatrix();
		}

	}

	public static enum OverwatchMap {
		EICHENWALDE("Eichenwalde is an Assault/Escort hybrid map set in an abandoned village on the outskirts of Stuttgart, Germany.\n" + 
				"\n" + 
				"The site of one of the most famous battles during the Omnic Crisis, it was here that the leader of the Crusaders, Balderich von Alder, and a handful of his best soldiers made a last stand against an advancing automaton army. Outnumbered and outgunned, they were ultimately slain during the resulting combat. However, thanks to their valiant efforts, the German military was able to push back the omnic offensive and win the fight.\n" + 
				"\n" + 
				"On this map, one team must escort a battering ram to Eichenwalde Castle to reclaim Balderich’s remains from their current resting place, while the opposing team does everything in its power to thwart the keep from being breached."),
		ILIOS("Ilios is a small Greek island in the Aegean Sea that is very popular with tourists. Atop the island, there is an archaeology site where ancient Greek statues and mosaics have been discovered. Talon activity has been seen in the area, and Athena believes they are interested in the artifacts."),
		HANAMURA("Hanamura was home to the Shimada Clan, and of Hanzo and Genji by extension. While Hanzo was trained in martial arts, archery, and sword fighting in the dojo, Genji had the freedom to enjoy the ramen shops and arcades of the modern districts surrounding the Shimada compound.\n" + 
				"After Hanzo was named heir, he fought and nearly killed Genji. Though Genji was saved by Overwatch, Hanzo was unaware, and left his sword in Hanamura out of shame. The Shimada Clan was later dismantled by Overwatch, and the town became the site of an annual cherry blossom festival. Hanzo returned to the dojo every year to honor Genji.\n" + 
				"\n" + 
				"At some point, McCree visited Hanamura and stopped a robbery at the local Rikimaru.\n" + 
				"\n" + 
				"In Dragons, Genji returned to Hanamura to meet Hanzo for the first time since he was nearly killed. Hanzo thought Genji was just another assassin, but after Genji came out on top of a fight, he revealed himself and forgave Hanzo for what he had done."), 
		NECROPOLIS("The Necropolis is a small Egyptian tomb in the desert near Giza. After coming out of retirement, Ana Amari set up camp here, turning one building into a small workshop. After the events of Old Soldiers, Soldier: 76 has set up his own room at the Necropolis. The two have since been trying to track down Reaper."), 
		BLACK_FOREST("On the outskirts of Eichenwalde, the misty Black Forest map wraps around a long-overlooked battlefield dotted with the wreckage of Bastion units.\n" + 
				"\n" + 
				"Flank your opponents on pathways between the castle and woods, and use the wooden balconies to move between the second stories of houses throughout the forest."), 
		DR_JUNKENSTEINS("The event uses a modified version of the Eichenwalde castle gate, known as Adlersbrunn. Unlike Eichenwalde, Adlersbrunn does not have any zone that players can fall off of. the two sides of the bridge are replaced by pools of water, and the drop on the right side is blocked by rocks. The entrance to the bridge has been blocked and is used as a spawn location for Zomnics.\n" + 
				"The area is lit by moonlight and torches. There are occasional flashes of lightning and crashes of thunder."), 
		ECOPOINT("Ecopoint: Antarctica was an Overwatch-run research facility near the coast of Antarctica. The scientists stationed here - Adams, MacReady, Opara, Torres, Arrhenius, and Mei-Ling Zhou - were investigating climate anomalies and cryostasis technology when a massive storm hit the ecopoint, and they lost connection to the outside world. As their food and energy supplies dwindled, the scientists were forced to freeze themselves in cryostasis until help arrived.\n" + 
				"A decade later, the scientists were found, but all had died in the storm except for Mei."), 
		CASTILLO("Castillo is a colonial-era fort overlooking Dorado and its bay. The fort is home to a Los Muertos hideout, as well as Sombra's home and Calaveras. Junkrat and Roadhog went to Calaveras to plan their heist on the Bank of Dorado.\n" + 
				"In Reflections, Sombra and McCree are both at Calaveras, sitting at opposite sides of the bar."), 
		CHATEAU("Château Guillard was owned for hundreds of years by the Guillard family. After the French Revolution, the family's influence waned and the château was abandoned. Recently Widowmaker began to use the château as her base of operations."),
		KINGS_ROW("King's Row is an upscale, cosmopolitan neighborhood of London, but just beneath its peaceful surface, tensions between omnics and humans are running high. While much of modern England was built on the backs of omnic laborers, they have been denied the basic rights that humans have, with most omnics forced to live in the dense, claustrophobic city-beneath-the-city known by some as \"the Underworld.\" Of greater concern is that recent demonstrations by pro-omnic-rights protestors have resulted in violent clashes with the police, and a solution is nowhere in sight.");
		
		public String info;

		OverwatchMap(String info) {
			this.info = info;
		}
	}

}