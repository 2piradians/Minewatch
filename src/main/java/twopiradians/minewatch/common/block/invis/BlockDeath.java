package twopiradians.minewatch.common.block.invis;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;

public class BlockDeath extends BlockInvis {

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add(TextFormatting.GOLD+""+TextFormatting.ITALIC+Minewatch.translate("tile.death_block.desc1"));
		tooltip.add(TextFormatting.GOLD+""+TextFormatting.ITALIC+Minewatch.translate("tile.death_block.desc2"));
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		// kill on collide
		if (!worldIn.isRemote && entityIn instanceof EntityLivingBase && ((EntityLivingBase)entityIn).isEntityAlive() && 
				(!(entityIn instanceof EntityPlayer) || (!((EntityPlayer)entityIn).isSpectator() && !((EntityPlayer)entityIn).isCreative())))
			entityIn.onKillCommand();
	}

	public static class ItemBlockDeath extends ItemBlockInvis {
		public ItemBlockDeath(Block block) {
			super(block);
		}
		
		@Override
		public void spawnParticle(World world, IBlockState state, float x, float y, float z) {
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.DEATH_BLOCK, world, x, y, z, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1, 80, 6, 6, 0, 0);
		}
	}

}