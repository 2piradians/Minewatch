package twopiradians.minewatch.common.block.teamBlocks;


import javax.annotation.Nullable;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumGui;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.tileentity.TileEntityTeam;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class BlockTeamSpawn extends BlockTeam {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockTeamSpawn() {
		super();
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HAS_TEAM, false).withProperty(ACTIVATED, false));
	}

	@Override
	@Nullable
	public TileEntity createTileEntity(World worldObj, IBlockState state) {
		return new TileEntityTeamSpawn();
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
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta & 3).
				withProperty(FACING, EnumFacing.getHorizontal(meta >> 2 & 3));
	}

	/**00 (FACING) 0 (HAS_TEAM) 0 (ACTIVATED)*/
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = super.getMetaFromState(state);
		meta <<= 2;
		meta += state.getValue(FACING).getHorizontalIndex();
		return meta;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {FACING, HAS_TEAM, ACTIVATED});
	}

	@Override
	public boolean onBlockActivated(World worldObj, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing facing, float hitX, float hitY, float hitZ) {
		boolean ret = super.onBlockActivated(worldObj, pos, state, player, hand, heldItem, facing, hitX, hitY, hitZ);

		// open gui if in creative mode
		if (!ret && player.worldObj.isRemote && worldObj.getTileEntity(pos) instanceof TileEntityTeam) {
			// warn if not in creative - set cooldown handler to prevent spam
			if (!player.isCreative()) {
				if (!TickHandler.hasHandler(player, Identifier.TEAM_BLOCK_WARNING_COOLDOWN)) {
					player.addChatMessage(new TextComponentString(TextFormatting.RED+"You must be in creative mode to access this."));
					TickHandler.register(true, WARNING_COOLDOWN.setEntity(player).setTicks(50));
				}
			}
			else
				Minewatch.proxy.openGui(EnumGui.TEAM_SPAWN, ((TileEntityTeam)worldObj.getTileEntity(pos)));
		}

		return player.isCreative();
	}

}