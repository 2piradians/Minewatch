package twopiradians.minewatch.common.tileentity;

import java.util.HashSet;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public abstract class TileEntityHealthPack extends TileEntity implements ITickable {

	/**Set of health pack positions for use by EntityHero AI*/
	public static HashSet<BlockPos> healthPackPositions = new HashSet<BlockPos>();

	/**full ticks until health pack would respawn*/
	private final double resetCooldown;
	/**full ticks until health pack would respawn when hacked*/
	private final double hackedResetCooldown;
	/**current ticks until health pack respawns*/
	private double cooldown;
	private final double healAmount;

	public static final int HACK_TIME = 1200;
	@Nullable
	public Team hackedTeam;	
	public int hackedTime;

	public TileEntityHealthPack(int resetCooldown, int hackedResetCooldown, int healAmount) {
		super();
		this.resetCooldown = resetCooldown;
		this.hackedResetCooldown = hackedResetCooldown;
		this.healAmount = healAmount;
	}

	@Override
	public void setPos(BlockPos posIn) {
		super.setPos(posIn);

		if (!healthPackPositions.contains(getPos())) 
			healthPackPositions.add(getPos());
	}

	@Override
	public void update() {
		// hacked
		if (this.isHacked()) {
			// particles
			if (world.isRemote && this.hackedTime % 40 == 0 && this.hackedTime > 80)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SOMBRA_HACK_MESH, world, 
						pos.getX()+0.5d+(world.rand.nextFloat()-0.5f)*1f, 
						pos.getY()+0.5d+(world.rand.nextFloat()-0.5f)*1f, 
						pos.getZ()+0.5d+(world.rand.nextFloat())*1f, 
						(world.rand.nextFloat()-0.5f)*0.05f, 
						(world.rand.nextFloat()-0.5f)*0.05f, 
						(world.rand.nextFloat()-0.5f)*0.05f, 
						0x8F40F7, 0x8F40F7, 1, 100, 12, 8, 1f+(world.rand.nextFloat()-0.5f)*0.1f, (world.rand.nextFloat()-0.5f)*0.03f);
			
			if (--this.hackedTime % 100 == 0 && !world.isRemote)
				this.world.markAndNotifyBlock(pos, this.world.getChunkFromBlockCoords(pos), this.getBlockType().getDefaultState(), this.getBlockType().getDefaultState(), 2);
			if (this.hackedTime <= 0) {
				this.hackedTeam = null;
				this.hackedTime = 0;
			}
		}
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
		NBTTagCompound nbt = pkt.getNbtCompound();
		if (nbt.hasKey("cooldown")) 
			this.cooldown = nbt.getDouble("cooldown");
		else
			this.cooldown = 0;
		if (nbt.hasKey("hackedTeam")) 
			this.hackedTeam = world.getScoreboard().getTeam(nbt.getString("hackedTeam"));
		else
			this.hackedTeam = null;
		if (nbt.hasKey("hackedTime"))
			this.hackedTime = nbt.getInteger("hackedTime");
		else
			this.hackedTime = 0;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);
		nbt.setDouble("cooldown", cooldown);
		if (this.hackedTeam != null) 
			nbt.setString("hackedTeam", this.hackedTeam.getRegisteredName());
		if (this.hackedTime > 0)
			nbt.setInteger("hackedTime", this.hackedTime);
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
		return (this.isHacked() ? this.hackedResetCooldown : this.resetCooldown) * Config.healthPackRespawnMultiplier;
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

	/**Hacks for team*/
	public void hack(@Nullable Team team) {
		if (!world.isRemote) {
			this.hackedTeam = team;
			this.hackedTime = HACK_TIME;
			this.world.markAndNotifyBlock(pos, this.world.getChunkFromBlockCoords(pos), this.getBlockType().getDefaultState(), this.getBlockType().getDefaultState(), 2);
		}
	}
	
	/**Not on cooldown, not hacked by opposing team, health below max*/
	public boolean canHeal(EntityLivingBase entity) {
		return this.getCooldown() <= 0 && entity.getHealth() < entity.getMaxHealth() && entity.isEntityAlive() &&
				(!this.isHacked() || this.hackedTeam == null || this.hackedTeam == entity.getTeam());
	}

	public boolean isHacked() {
		return this.hackedTime > 0;
	}

	public static class Large extends TileEntityHealthPack {
		public Large() {
			super(300, 75, 250);
		}
	}
	public static class Small extends TileEntityHealthPack {
		public Small() {
			super(200, 50, 75);
		}
	}

}