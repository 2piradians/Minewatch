package twopiradians.minewatch.common.block;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.tileentity.TileEntityHealthPack;


public class ModBlocks {

	public static ArrayList<Block> allBlocks = new ArrayList<Block>();

	public static Block healthPackSmall;
	public static Block healthPackLarge;// TODO split like 1.12

	public static void preInit() {
		allBlocks = new ArrayList<Block>();

		healthPackSmall = registerBlock(new BlockHealthPack.Small(), "health_pack_small", TileEntityHealthPack.Small.class, true, true);
		healthPackLarge = registerBlock(new BlockHealthPack.Large(), "health_pack_large", TileEntityHealthPack.Large.class, true, true);
	}

	public static Block registerBlock(Block block, String unlocalizedName, @Nullable Class tileEntityClass, boolean isItemBlock, boolean addToTab) {
		block.setUnlocalizedName(unlocalizedName);
		GameRegistry.register(block.setRegistryName(unlocalizedName));
		if (tileEntityClass != null)
			GameRegistry.registerTileEntity(tileEntityClass, unlocalizedName);
		if (isItemBlock) {
			Item item = new ItemBlock(block).setRegistryName(block.getRegistryName());
			GameRegistry.register(item);
			if (addToTab) 
				Minewatch.tabMapMaking.getOrderedStacks().add(new ItemStack(item));
		}
		allBlocks.add(block);
		return block;
	}

}