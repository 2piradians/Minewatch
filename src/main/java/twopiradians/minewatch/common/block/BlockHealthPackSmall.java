package twopiradians.minewatch.common.block;


import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import twopiradians.minewatch.common.tileentity.TileEntityHealthPack;

public class BlockHealthPackSmall extends Block {

	public BlockHealthPackSmall() {
		super(Material.STRUCTURE_VOID);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	 @Override
     public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		 return super.getExtendedState(state, world, pos);
	 }

     @Override
     public BlockStateContainer createBlockState() {
    	 return super.createBlockState();
     }

	@Nullable
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityHealthPack();
	}

}