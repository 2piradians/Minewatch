package twopiradians.minewatch.common.tileentity;

import java.util.HashMap;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import twopiradians.minewatch.common.block.teamBlocks.BlockTeam;

public abstract class TileEntityTeam extends TileEntity implements ITickable {

	private Team team;
	private String name;
	private boolean activated;
	
	// have to delay a tick to prevent infinite loops while first adding tile
	protected boolean needsToBeUpdated;

	public TileEntityTeam() {
		super();
	}
	
	@Override
	public void update() {
		// sync server data to client and update render
		if (this.needsToBeUpdated) {
			if (!worldObj.isRemote) {
				IBlockState oldState = worldObj.getBlockState(pos);
				IBlockState newState = oldState
						.withProperty(BlockTeam.HAS_TEAM, team != null)
						.withProperty(BlockTeam.ACTIVATED, activated);
				this.worldObj.setBlockState(pos, newState);
				// set values for new tile, because setBlockState will recreate tile
				TileEntity te = this.worldObj.getTileEntity(pos);
				if (te instanceof TileEntityTeam) {
					this.copyToNewTile((TileEntityTeam) te);
				}
				this.worldObj.markAndNotifyBlock(pos, this.worldObj.getChunkFromBlockCoords(pos), oldState, newState, 3);
			}
			else	
				worldObj.markBlockRangeForRenderUpdate(pos, pos);
			this.markDirty();
			this.needsToBeUpdated = false;
		}
	}
	
	@Override
    public void invalidate() {
        super.invalidate();
        
        this.getPositions().remove(pos);
    }

	public abstract HashMap<BlockPos, String> getPositions();
	
	/**Checks that this name is unique and valid*/
	public boolean isValidName(String name) {
		return name != null && !name.contains(" ") && (!this.getPositions().values().contains(name) || name.equals(this.getPositions().get(this.pos)));
	}

	/**Gets the tile's name or generates one if there isn't one yet*/
	public String getName() {
		if (this.name == null || this.name.isEmpty()) {
			int num = 0;
			while (!isValidName(this.getBlockType().getLocalizedName().replace(" ", "_")+"_"+String.valueOf(num)))
				num++;
			this.setName(this.getBlockType().getLocalizedName().replace(" ", "_")+"_"+String.valueOf(num));
		}
		return name;
	}

	public void setName(String name) {
		if (name != null && !name.equals(this.name)) {
			this.name = name;
			this.getPositions().put(pos, name);
			this.needsToBeUpdated = true;
		}
	}

	@Nullable
	public Team getTeam() {
		return team;
	}

	public void setTeam(@Nullable Team team) {
		if (!worldObj.isRemote) {
			this.team = team;
			if (activated)
				this.deactivateOtherActive();
			this.needsToBeUpdated = true;
		}
	}
	
	public boolean isActivated() {
		return this.activated;
	}
	
	public void setActivated(boolean activated) {
		if (!worldObj.isRemote) {
			this.activated = activated;
			if (activated) 
				this.deactivateOtherActive();
			this.needsToBeUpdated = true;
		}
	}
	
	/**Deactivate other active tiles with same team*/
	public void deactivateOtherActive() {
		for (BlockPos pos : this.getPositions().keySet())
			if (!pos.equals(this.pos) && worldObj.getTileEntity(pos) instanceof TileEntityTeam) {
				TileEntityTeam te = (TileEntityTeam) worldObj.getTileEntity(pos);
				if (te.activated && te.getTeam() == this.getTeam()) {
					te.activated = false;
					te.needsToBeUpdated = true;
				}
			}
	}

	@Override
	public void onLoad() {
		// put position / name in map
		if (!getPositions().values().contains(getPos())) {
			getPositions().put(getPos(), getName());
		}
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
		if (this.name != null)
			nbt.setString("name", getName());
		nbt.setBoolean("activated", activated);
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
			this.team = worldObj.getScoreboard().getTeam(TextFormatting.getTextWithoutFormattingCodes(nbt.getString("team")));
		if (nbt.hasKey("name"))
			this.setName(nbt.getString("name"));
		if (nbt.hasKey("activated"))
			this.activated = nbt.getBoolean("activated");
		if (worldObj.isRemote)
			this.needsToBeUpdated = true;
	}

	/**Set the worldObj when creating - because readExtraNBT is called when worldObj is null for some stupid reason*/
	@Override
	protected void setWorldCreate(World worldObj) {
		super.setWorldCreate(worldObj);

		if (this.worldObj == null) 
			this.worldObj = worldObj;
	}
	
	/**Copy this tile's variables to the new one when this is invalidated*/
	public void copyToNewTile(TileEntityTeam te) {
		te.team = team;
		te.activated = activated;
		te.name = name;
	}

}