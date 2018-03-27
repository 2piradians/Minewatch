package twopiradians.minewatch.common.block.invis;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBarrier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;

public class BlockInvis extends BlockBarrier {
	
	public static final AxisAlignedBB EMPTY_AABB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	
	@Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true; 
    }
	
	@Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}
	
	@Override
	@SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return (Minewatch.proxy.getClientPlayer() != null && !Minewatch.proxy.getClientPlayer().isCreative()) ? EMPTY_AABB : super.getBoundingBox(state, source, pos);
    }

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	public static class ItemBlockInvis extends ItemBlock {


		public ItemBlockInvis(Block block) {
			super(block);
		}

		public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
			// particle
			if (isSelected && entity.ticksExisted % 1 == 0 && world.isRemote && 
					entity == Minewatch.proxy.getClientPlayer() && ((EntityPlayer)entity).isCreative()) {
				this.doVoidFogParticles(world, (int) entity.posX, (int) entity.posY, (int) entity.posZ); 
			}
		}

		// modified from WorldClient#doVoidFogParticles
		public void doVoidFogParticles(World world, int posX, int posY, int posZ) {
			//int i = 32;
			Random random = new Random();
			//ItemStack itemstack = this.mc.player.getHeldItemMainhand();
			boolean flag = true;//this.mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER);
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

			for (int j = 0; j < 667; ++j)
			{
				this.showBarrierParticles(world, posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
				this.showBarrierParticles(world, posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
			}
		}

		// modified from WorldClient#showBarrierParticles
		public void showBarrierParticles(World world, int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos) {
			int i = x + random.nextInt(offset) - random.nextInt(offset);
			int j = y + random.nextInt(offset) - random.nextInt(offset);
			int k = z + random.nextInt(offset) - random.nextInt(offset);
			pos.setPos(i, j, k);
			IBlockState iblockstate = world.getBlockState(pos);
			//iblockstate.getBlock().randomDisplayTick(iblockstate, this, pos, random);

			if (holdingBarrier && iblockstate.getBlock() == this.block)
			{
				spawnParticle(world, iblockstate, i+0.5f, j+0.5f, k+0.5f);
				//this.spawnParticle(EnumParticleTypes.BARRIER, (double)((float)i + 0.5F), (double)((float)j + 0.5F), (double)((float)k + 0.5F), 0.0D, 0.0D, 0.0D, new int[0]);
			}
		}
		
		public void spawnParticle(World world, IBlockState state, float x, float y, float z) {}

	}

}
