package twopiradians.minewatch.common.tileentity;

import java.util.HashSet;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public abstract class TileEntityHealthPack extends TileEntity implements ITickable {

	/**Set of health pack positions for use by EntityHero AI*/
	public static HashSet<BlockPos> healthPackPositions = new HashSet<BlockPos>();

	/**full ticks until health pack would respawn*/
	private final double resetCooldown;
	/**current ticks until health pack respawns*/
	private double cooldown;
	private final double healAmount;

	public TileEntityHealthPack(int resetCooldown, int healAmount) {
		super();
		this.resetCooldown = resetCooldown;
		this.healAmount = healAmount;
	}

	@Override
	public void setPos(BlockPos posIn) {
		super.setPos(posIn);
		if (!this.world.isRemote) 
			healthPackPositions.add(getPos());
	}

	@Override
	public void update() {		
		if (this.cooldown > 0) {
			// make sure cooldown is <= resetCooldown (incase it's changed in config)
			if (this.cooldown > this.getResetCooldown())
				this.cooldown = this.getResetCooldown();
			// sync to client every once in a while
			if (--this.cooldown % 100 == 0 && this.cooldown < this.getResetCooldown() && !world.isRemote) {
				this.world.markAndNotifyBlock(pos, this.world.getChunkFromBlockCoords(pos), this.getBlockType().getDefaultState(), this.getBlockType().getDefaultState(), 2);
				if (this.cooldown == 0)
					ModSoundEvents.HEALTH_PACK_RESPAWN.playSound(world, pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
			}

		}
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 6, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		if (pkt.getNbtCompound().hasKey("cooldown")) 
			this.cooldown = pkt.getNbtCompound().getDouble("cooldown");
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);
		nbt.setDouble("cooldown", cooldown);
		return nbt;
	}

	/**Get current cooldown*/ 
	public double getCooldown() {
		if (this.cooldown > this.getResetCooldown())
			this.cooldown = this.getResetCooldown();
		return this.cooldown;
	}

	/**Get reset cooldown*/
	public double getResetCooldown() {
		return this.resetCooldown * Config.healthPackRespawnMultiplier;
	}

	/**Sets cooldown to resetCooldown*/
	public void setResetCooldown() {
		this.cooldown = this.getResetCooldown();
		if (!world.isRemote) {
			this.world.markAndNotifyBlock(pos, this.world.getChunkFromBlockCoords(pos), this.getBlockType().getDefaultState(), this.getBlockType().getDefaultState(), 2);
			ModSoundEvents.HEALTH_PACK_USE.playSound(world, pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
		}
	}

	/**Get unscaled heal amount*/
	public float getHealAmount() {
		return (float) (this.healAmount * Config.healthPackHealMultiplier);
	}

	public static class Large extends TileEntityHealthPack {
		public Large() {
			super(300, 250);
		}
	}
	public static class Small extends TileEntityHealthPack {
		public Small() {
			super(200, 75);
		}
	}

}