package twopiradians.minewatch.common.entity;

import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;

public class ModEntities 
{
	public static void registerEntities() {
		int id = 0;
		EntityRegistry.registerModEntity(EntityReaperBullet.class, "reaper_pellet", id++, Minewatch.instance, 16, 1, true);
		EntityRegistry.registerModEntity(EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 16, 1, true);
		EntityRegistry.registerModEntity(EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 32, 1, true);
	}
}
