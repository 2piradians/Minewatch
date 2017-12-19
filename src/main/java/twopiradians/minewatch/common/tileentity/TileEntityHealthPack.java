package twopiradians.minewatch.common.tileentity;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.sound.ModSoundEvents;

@SideOnly(Side.CLIENT)
public abstract class TileEntityHealthPack extends TileEntity implements ITickable {

	/**full ticks until health pack would respawn*/
	public final int resetCooldown;
	/**current ticks until health pack respawns*/
	public int cooldown;

	public TileEntityHealthPack(int resetCooldown) {
		super();
		this.resetCooldown = resetCooldown;
	}

	@Override
	public void update() {		
		if (this.cooldown > 0) {
			// sync to client every once in a while
			if (--this.cooldown % 100 == 0 && this.cooldown < this.resetCooldown && !world.isRemote) {
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
			this.cooldown = pkt.getNbtCompound().getInteger("cooldown");
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);
		nbt.setInteger("cooldown", cooldown);
		return nbt;
	}

	/**Sets cooldown to resetCooldown*/
	public void setCooldown() {
		this.cooldown = this.resetCooldown;
		if (!world.isRemote) {
			this.world.markAndNotifyBlock(pos, this.world.getChunkFromBlockCoords(pos), this.getBlockType().getDefaultState(), this.getBlockType().getDefaultState(), 2);
			ModSoundEvents.HEALTH_PACK_USE.playSound(world, pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
		}
	}

	public static class Large extends TileEntityHealthPack {
		public Large() {
			super(300);
		}
	}
	public static class Small extends TileEntityHealthPack {
		public Small() {
			super(200);
		}
	}

}