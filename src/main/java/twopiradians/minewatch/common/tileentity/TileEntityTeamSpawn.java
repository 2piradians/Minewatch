package twopiradians.minewatch.common.tileentity;

import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityTeamSpawn extends TileEntityTeam {

	public static HashMap<BlockPos, String> teamSpawnPositions = Maps.newHashMap();

	public TileEntityTeamSpawn() {
		super();
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
	public HashMap<BlockPos, String> getPositions(boolean validatePositions) {
		if (validatePositions)
			this.updatePositions = true;
		return teamSpawnPositions;
	}

}