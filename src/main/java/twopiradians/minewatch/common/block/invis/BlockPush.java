package twopiradians.minewatch.common.block.invis;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;

public class BlockPush extends BlockInvis {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockPush() {
		super();
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add(TextFormatting.GOLD+""+TextFormatting.ITALIC+Minewatch.translate("tile.push_block.desc1"));
		tooltip.add(TextFormatting.GOLD+""+TextFormatting.ITALIC+Minewatch.translate("tile.push_block.desc2"));
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		// push on collide
		if (entityIn instanceof EntityLivingBase) {
			entityIn.onGround = false;
			EnumFacing facing = state.getValue(FACING);
			Vec3d vec = new Vec3d(facing.getDirectionVec()).scale(0.025d);
			entityIn.motionX += vec.x;
			entityIn.motionZ += vec.z;
		}
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y)
			enumfacing = EnumFacing.NORTH;

		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing)state.getValue(FACING)).getIndex();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {FACING});
	}

	public static class ItemBlockPush extends ItemBlockInvis {
		public ItemBlockPush(Block block) {
			super(block);
		}
		
		@Override
		public void spawnParticle(World world, IBlockState state, float x, float y, float z) {
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.PUSH_BLOCK, world, x, y, z, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1, 80, 6, 6, state.getValue(FACING).getHorizontalIndex()/2f+0.5f, 0, EnumFacing.UP, false);
		}
	}

}