package twopiradians.minewatch.common.entity;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;

public class ModEntities 
{
	public static final Block[] ENTITY_PASSES_THROUGH = 
		{Blocks.TALLGRASS, Blocks.VINE, Blocks.RED_FLOWER, Blocks.YELLOW_FLOWER, Blocks.BROWN_MUSHROOM,
		 Blocks.RED_MUSHROOM, Blocks.REEDS, Blocks.DOUBLE_PLANT, Blocks.DEADBUSH, Blocks.WHEAT,
		 Blocks.WATERLILY, Blocks.CARROTS, Blocks.POTATOES, Blocks.SNOW_LAYER};
	
	public static void registerEntities() {
		int id = 0;
		EntityRegistry.registerModEntity(EntityReaperBullet.class, "reaper_pellet", id++, Minewatch.instance, 16, 1, true);
		EntityRegistry.registerModEntity(EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 16, 1, true);
		EntityRegistry.registerModEntity(EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 32, 1, true);
		EntityRegistry.registerModEntity(EntityGenjiShuriken.class, "genji_shuriken", id++, Minewatch.instance, 32, 1, true);
		EntityRegistry.registerModEntity(EntityTracerBullet.class, "tracer_bullet", id++, Minewatch.instance, 16, 1, true);
		EntityRegistry.registerModEntity(EntityMcCreeBullet.class, "mccree_bullet", id++, Minewatch.instance, 16, 1, true);
		EntityRegistry.registerModEntity(EntitySoldier76Bullet.class, "soldier_bullet", id++, Minewatch.instance, 16, 1, true);
	}
}
