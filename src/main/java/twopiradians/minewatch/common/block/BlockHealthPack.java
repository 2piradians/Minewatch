package twopiradians.minewatch.common.block;


import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.tileentity.TileEntityHealthPack;

public abstract class BlockHealthPack extends Block {

	protected static final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);

	public BlockHealthPack() {
		super(Material.STRUCTURE_VOID);
		this.setCreativeTab((CreativeTabs) Minewatch.tabMapMaking);
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (!worldIn.isRemote && worldIn.getTileEntity(pos) instanceof TileEntityHealthPack) {
			TileEntityHealthPack te = (TileEntityHealthPack) worldIn.getTileEntity(pos);
			if (te.cooldown <= 0) 
				te.setCooldown();
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB;
	}

	@Deprecated
	@SideOnly(Side.CLIENT)
	public boolean isTranslucent(IBlockState state) {
		return false; 
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 8;
	}

	@Override
	@Deprecated
	public int getLightValue(IBlockState state) {
		return 8;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return super.getExtendedState(state, world, pos);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return super.getMetaFromState(state);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return super.createBlockState();
	}

	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName();
	}

	public static class Small extends BlockHealthPack {
		@Override
		@Nullable
		public TileEntity createTileEntity(World world, IBlockState state) {
			return new TileEntityHealthPack.Small();
		}
	}
	public static class Large extends BlockHealthPack {
		@Override
		@Nullable
		public TileEntity createTileEntity(World world, IBlockState state) {
			return new TileEntityHealthPack.Large();
		}
	}

}