package twopiradians.overwatch.common.entity;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.overwatch.common.Overwatch;

public class ModEntities 
{
	public static void registerEntities() 
	{
		EntityRegistry.registerModEntity(new ResourceLocation(Overwatch.MODID, "reaper_pellet"), EntityReaperPellet.class, "reaper_pellet", 0, Overwatch.instance, 16, 1, true);
	}
}
