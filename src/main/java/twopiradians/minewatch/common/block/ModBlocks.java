package twopiradians.minewatch.common.block;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import twopiradians.minewatch.common.block.invis.BlockDeath;
import twopiradians.minewatch.common.block.invis.BlockPush;
import twopiradians.minewatch.common.block.teamBlocks.BlockTeamSpawn;
import twopiradians.minewatch.common.tileentity.TileEntityHealthPack;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;


public class ModBlocks {

	public static ArrayList<Block> allBlocks = new ArrayList<Block>();

	public static Block healthPackSmall;
	public static Block healthPackLarge;
	
	public static Block teamSpawn;
	
	public static Block deathBlock;
	public static Block pushBlock;

	@Mod.EventBusSubscriber
	public static class RegistrationHandler {

		@SubscribeEvent
		public static void registerBlocks(RegistryEvent.Register<Block> event) {
			// health packs
			healthPackSmall = registerBlock(event.getRegistry(), new BlockHealthPack.Small(), "health_pack_small", TileEntityHealthPack.Small.class, true);
			healthPackLarge = registerBlock(event.getRegistry(), new BlockHealthPack.Large(), "health_pack_large", TileEntityHealthPack.Large.class, true);
			// team blocks
			teamSpawn = registerBlock(event.getRegistry(), new BlockTeamSpawn(), "team_spawn", TileEntityTeamSpawn.class, true);
			// misc
			deathBlock = registerBlock(event.getRegistry(), new BlockDeath(), "death_block", null, true);
			pushBlock = registerBlock(event.getRegistry(), new BlockPush(), "push_block", null, true);
		}
	}

	public static Block registerBlock(IForgeRegistry<Block> registry, Block block, String unlocalizedName, @Nullable Class tileEntityClass, boolean addToTab) {
		block.setUnlocalizedName(unlocalizedName);
		registry.register(block.setRegistryName(unlocalizedName));
		if (tileEntityClass != null)
			GameRegistry.registerTileEntity(tileEntityClass, unlocalizedName);
		allBlocks.add(block);
		return block;
	}

}