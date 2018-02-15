package twopiradians.minewatch.common.tileentity;

import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class TileEntityTeamSpawn extends TileEntityTeam {

	public static HashMap<BlockPos, String> teamSpawnPositions = Maps.newHashMap();
	public static final int MAX_SPAWN_RADIUS = 20;

	private int spawnRadius;

	public TileEntityTeamSpawn() {
		super();
	}
	
	@Override
	public void update() {
		super.update();
		
		// copied from MobSpawnerBaseLogic particles
		if (this.world.isRemote && this.world.rand.nextBoolean() && this.isActivated()) {
            double d3 = (double)((float)pos.getX() + 0.5f + (this.world.rand.nextFloat()-0.5f)*0.8f);
            double d4 = (double)((float)pos.getY() + 0.5f + (this.world.rand.nextFloat()-0.5f)*0.8f);
            double d5 = (double)((float)pos.getZ() + 0.5f + (this.world.rand.nextFloat()-0.5f)*0.8f);
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d3, d4, d5, 0.0D, 0.0D, 0.0D, new int[0]);
            this.world.spawnParticle(EnumParticleTypes.FLAME, d3, d4, d5, 0.0D, 0.0D, 0.0D, new int[0]);
        }
	}

	public int getSpawnRadius() {
		return spawnRadius;
	}

	public void setSpawnRadius(int spawnRadius) {
		if (!world.isRemote) {
			spawnRadius = MathHelper.clamp(spawnRadius, 0, MAX_SPAWN_RADIUS);
			this.spawnRadius = spawnRadius;
			this.needsToBeUpdated = true;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		nbt.setInteger("spawnRadius", spawnRadius);
		
		return nbt;
	}
	
	/**Read extra info from nbt - like team and activated status*/
	protected void readExtraNBT(NBTTagCompound nbt) {
		super.readExtraNBT(nbt);
		
		if (nbt.hasKey("spawnRadius"))
			this.spawnRadius = MathHelper.clamp(nbt.getInteger("spawnRadius"), 0, MAX_SPAWN_RADIUS);
	}

	@Override
	public HashMap<BlockPos, String> getPositions() {
		return teamSpawnPositions;
	}

	@Override
	public void copyToNewTile(TileEntityTeam te) {
		super.copyToNewTile(te);

		if (te instanceof TileEntityTeamSpawn)
			((TileEntityTeamSpawn)te).spawnRadius = spawnRadius;
	}

}