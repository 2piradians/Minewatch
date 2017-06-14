package twopiradians.minewatch.common.entity;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
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
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "reaper_pellet"), EntityReaperBullet.class, "reaper_pellet", id++, Minewatch.instance, 16, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 16, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "ana_bullet"), EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 32, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "genji_shuriken"), EntityGenjiShuriken.class, "genji_shuriken", id++, Minewatch.instance, 32, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "tracer_bullet"), EntityTracerBullet.class, "tracer_bullet", id++, Minewatch.instance, 16, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mccree_bullet"), EntityMcCreeBullet.class, "mccree_bullet", id++, Minewatch.instance, 16, 1, true);
	}
}
