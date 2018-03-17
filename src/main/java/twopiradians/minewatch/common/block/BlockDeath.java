package twopiradians.minewatch.common.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBarrier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;

public class BlockDeath extends BlockBarrier {

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add(TextFormatting.GOLD+""+TextFormatting.ITALIC+Minewatch.translate("tile.death_block.desc1"));
		tooltip.add(TextFormatting.GOLD+""+TextFormatting.ITALIC+Minewatch.translate("tile.death_block.desc2"));
	}
	
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
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		// kill on collide
		if (!worldIn.isRemote && entityIn instanceof EntityLivingBase && ((EntityLivingBase)entityIn).isEntityAlive() && 
				(!(entityIn instanceof EntityPlayer) || (!((EntityPlayer)entityIn).isSpectator() && !((EntityPlayer)entityIn).isCreative())))
			entityIn.onKillCommand();
	}

	public static class ItemBlockDeath extends ItemBlock {

		public ItemBlockDeath(Block block) {
			super(block);
		}

		public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
			// death particle
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

			if (holdingBarrier && iblockstate.getBlock() == ModBlocks.deathBlock)
			{
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.DEATH_BLOCK, world, i+0.5f, j+0.5f, k+0.5f, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1, 80, 6, 6, 0, 0);
				//this.spawnParticle(EnumParticleTypes.BARRIER, (double)((float)i + 0.5F), (double)((float)j + 0.5F), (double)((float)k + 0.5F), 0.0D, 0.0D, 0.0D, new int[0]);
			}
		}

	}

}
