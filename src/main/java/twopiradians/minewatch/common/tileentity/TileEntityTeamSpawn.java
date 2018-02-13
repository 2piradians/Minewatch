package twopiradians.minewatch.common.tileentity;

import java.util.HashSet;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityTeamSpawn extends TileEntityTeam implements ITickable {

	public static HashSet<BlockPos> teamSpawnPositions = new HashSet<BlockPos>();

	public TileEntityTeamSpawn() {
		super();
	}
	
	@Override
	public void update() {
		
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		NBTTagCompound nbt = pkt.getNbtCompound();
		
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		
		return compound;
	}

	@Override
	public HashSet<BlockPos> getPositions() {
		return teamSpawnPositions;
	}

}