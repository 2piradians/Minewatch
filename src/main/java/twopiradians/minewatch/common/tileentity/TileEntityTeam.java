package twopiradians.minewatch.common.tileentity;

import java.util.HashSet;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import twopiradians.minewatch.common.block.teamBlocks.TeamBlock;

public abstract class TileEntityTeam extends TileEntity implements ITickable {

	private Team team;

	public TileEntityTeam() {
		super();
	}

	/**Marks the block to be updated - only call on server*/
	public void updateBlock() {
		//if (!world.isRemote)
		//	this.world.markAndNotifyBlock(pos, this.world.getChunkFromBlockCoords(pos), this.getBlockType().getDefaultState(), this.getBlockType().getDefaultState(), 2);
	}

	public abstract HashSet<BlockPos> getPositions();

	@Nullable
	public Team getTeam() {
		return team;
	}

	public void setTeam(@Nullable Team team) {
		if (!world.isRemote) {
			this.team = team;
			IBlockState oldState = world.getBlockState(pos);
			IBlockState newState = oldState
					.withProperty(TeamBlock.HAS_TEAM, team != null)
					.withProperty(TeamBlock.ACTIVATED, team != null && oldState.getValue(TeamBlock.ACTIVATED));
			this.world.setBlockState(pos, newState);
			TileEntity te = this.world.getTileEntity(pos);
			if (te instanceof TileEntityTeam)
				((TileEntityTeam)te).team = team;
			this.world.markAndNotifyBlock(pos, this.world.getChunkFromBlockCoords(pos), oldState, newState, 2);
			this.updateBlock();
		}
	}

	@Override
	public void setPos(BlockPos posIn) {
		super.setPos(posIn);

		if (!getPositions().contains(getPos())) 
			getPositions().add(getPos());
	}

	@Override
	public void update() {

	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 6, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbt = pkt.getNbtCompound();
		this.readExtraNBT(nbt);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);
		if (this.team != null)
			nbt.setString("team", team.getRegisteredName());
		return nbt;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.readExtraNBT(compound);
	}

	/**Read extra info from nbt - like team and activated status*/
	protected void readExtraNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("team"))
			this.team = world.getScoreboard().getTeam(TextFormatting.getTextWithoutFormattingCodes(nbt.getString("team")));
	}
	
	/**Set the world when creating - because readExtraNBT is called when world is null for some stupid reason*/
	@Override
    protected void setWorldCreate(World world) {
		super.setWorldCreate(world);
		if (this.world == null)
			this.world = world;
    }

}