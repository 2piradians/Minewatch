package twopiradians.minewatch.common.entity;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;

public class ModEntities 
{
	public static void registerEntities() {
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "reaper_pellet"), EntityReaperPellet.class, "reaper_pellet", 0, Minewatch.instance, 16, 1, true);
	}
}
