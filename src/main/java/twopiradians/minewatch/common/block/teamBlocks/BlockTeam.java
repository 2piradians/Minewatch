package twopiradians.minewatch.common.block.teamBlocks;

import java.awt.Color;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.ItemTeamStick;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.tileentity.TileEntityTeam;
import twopiradians.minewatch.common.util.ColorHelper;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public abstract class BlockTeam extends Block{

    public static final PropertyBool HAS_TEAM = PropertyBool.create("has_team");
    public static final PropertyBool ACTIVATED = PropertyBool.create("activated");
    
	public static final Handler WARNING_COOLDOWN = new Handler(Identifier.TEAM_BLOCK_WARNING_COOLDOWN, false) {};
	
	public BlockTeam() {
		super(Material.BARRIER);
		this.setBlockUnbreakable();
		this.setResistance(6000001.0F);
		this.setCreativeTab((CreativeTabs) Minewatch.tabMapMaking);
		this.setDefaultState(this.blockState.getBaseState().withProperty(HAS_TEAM, false).withProperty(ACTIVATED, false));
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
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
	public int getLightValue(IBlockState state) {
		return 8;
	}
	
	@Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
        		.withProperty(ACTIVATED, (meta & 1) == 1)
        		.withProperty(HAS_TEAM, (meta >> 1 & 1) == 1);
    }

    /**0 (HAS_TEAM) 0 (ACTIVATED)*/
    @Override
    public int getMetaFromState(IBlockState state) {
    	int meta = 0;
        meta += state.getValue(HAS_TEAM).booleanValue() ? 1 : 0;
        meta <<= 1;
        meta += state.getValue(ACTIVATED).booleanValue() ? 1 : 0;
        return meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {HAS_TEAM, ACTIVATED});
    }

	public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
		if (world.getTileEntity(pos) instanceof TileEntityTeam) {
			TileEntityTeam te = (TileEntityTeam) world.getTileEntity(pos);
			Team team = te.getTeam();
			if (team != null && team.getChatFormat() != null && team.getChatFormat().isColor()) {
				Color color = new Color(ColorHelper.getForegroundColor(team.getChatFormat()));
				return color.getRGB();
			}
		}
		return -1;
	}
	
	@Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!player.world.isRemote && !player.isSneaking() && world.getTileEntity(pos) instanceof TileEntityTeam &&
				player.getHeldItem(hand) != null && player.getHeldItem(hand).getItem() == ModItems.team_stick) {
			TileEntityTeam te = (TileEntityTeam) world.getTileEntity(pos);
			String name = te.getBlockType().getLocalizedName();
			// remove from team
			if (te.getTeam() != null) {
				te.setTeam(null);
				ItemTeamStick.sendChatMessage(player, "Removed "+name+"'s team");
				player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.PLAYERS, 0.8f, 1);
			}
			// remove from team when not on team
			else if (te.getTeam() == null)
				ItemTeamStick.sendChatMessage(player, name+" is not assigned a team");
		}
		return player.getHeldItem(hand) != null && player.getHeldItem(hand).getItem() == ModItems.team_stick;
    }
    
}