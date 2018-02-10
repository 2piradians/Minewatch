package twopiradians.minewatch.common.tileentity;

import java.util.HashSet;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityTeamSpawn extends TileEntity implements ITickable {

	public static HashSet<BlockPos> teamSpawnPositions = new HashSet<BlockPos>();

	public TileEntityTeamSpawn() {
		super();
	}

	@Override
	public void setPos(BlockPos posIn) {
		super.setPos(posIn);

		if (!teamSpawnPositions.contains(getPos())) 
			teamSpawnPositions.add(getPos());
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
		
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);
		
		return nbt;
	}

}