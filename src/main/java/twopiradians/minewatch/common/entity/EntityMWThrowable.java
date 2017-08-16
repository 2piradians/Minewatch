package twopiradians.minewatch.common.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.packet.PacketSyncSpawningEntity;

public abstract class EntityMWThrowable extends EntityThrowable implements IThrowableEntity {

	protected int lifetime;
	private EntityLivingBase thrower;

	public EntityMWThrowable(World worldIn) {
		super(worldIn);
	}

	public EntityMWThrowable(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.thrower = throwerIn;
		this.ignoreEntity = this;
	}

	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	@Override
	public void onUpdate() {		
		float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
		this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * (180D / Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;

		super.onUpdate();

		if (!this.world.isRemote && this.ticksExisted > lifetime && lifetime > 0)
			this.setDead();
	}

	public void setAim(EntityPlayer shooter, float pitch, float yaw, float velocity, float inaccuracy, EnumHand hand, boolean sendPacket) {
		double x = -Math.sin(yaw * Math.PI/180) * Math.cos(pitch * Math.PI/180);
		double y = -Math.sin(pitch * Math.PI/180);
		double z = Math.cos(yaw * Math.PI/180) * Math.cos(pitch * Math.PI/180);
		this.setThrowableHeading(x, y, z, velocity, inaccuracy);
		this.motionX += shooter.motionX;
		this.motionZ += shooter.motionZ;
		Vec3d look = shooter.getLookVec().scale(1).rotateYaw(0).rotatePitch(0);

		if (hand == EnumHand.MAIN_HAND) {
			look = look.rotateYaw(-0.5f);
			if (Math.abs(pitch) >= 20 && Math.abs(pitch) < 50) {
				posX = posX - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/8;
				posY = posY + Math.sin(pitch*Math.PI/180)/8;
				posZ = posZ - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/8;
			}
			else if (Math.abs(pitch) >= 50 && Math.abs(pitch) < 70) {
				posX = posX - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/8;
				posY = posY + Math.sin(pitch*Math.PI/180)/20 - (pitch < 0 ? 0.2d : 0);
				posZ = posZ - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/8;
			}
			else if (Math.abs(pitch) >= 70) {
				posX = posX - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/4;
				posY = posY + Math.sin(pitch*Math.PI/180)/30 - (pitch < 0 ? 0.2d : -0.2d);
				posZ = posZ - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/4;
			}
		}
		else if (hand == EnumHand.OFF_HAND) {
			look = look.rotateYaw(0.5f);
			if (Math.abs(pitch) >= 20 && Math.abs(pitch) < 50) {
				posX = posX + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/8;
				posY = posY + Math.sin(pitch*Math.PI/180)/8;
				posZ = posZ + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/8;
			}
			else if (Math.abs(pitch) >= 50 && Math.abs(pitch) < 70) {
				posX = posX + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/8;
				posY = posY + Math.sin(pitch*Math.PI/180)/20 - (pitch < 0 ? 0.2d : 0);
				posZ = posZ + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/8;
			}
			else if (Math.abs(pitch) >= 70) {
				posX = posX + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/4;
				posY = posY + Math.sin(pitch*Math.PI/180)/30 - (pitch < 0 ? 0.2d : -0.2d);
				posZ = posZ + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/4;
			}
		}
		this.setPosition(posX+look.xCoord, posY+look.yCoord, posZ+look.zCoord);

		// correct trajectory of fast entities (received in render class)
		if (!this.world.isRemote && this.ticksExisted == 0 && sendPacket) {
			Minewatch.network.sendToAllAround(
					new PacketSyncSpawningEntity(this.getPersistentID(), this.rotationPitch, this.rotationYaw, this.motionX, this.motionY, this.motionZ), 
					new TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 1024));
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			Block block = this.world.getBlockState(result.getBlockPos()).getBlock();

			if (!block.isPassable(this.world, result.getBlockPos())) 
				this.setDead();
		}
	}

	@Override
	public EntityLivingBase getThrower() {
		return this.thrower;
	}

	@Override
	public void setThrower(Entity entity) {
		if (entity instanceof EntityLivingBase)
			this.thrower = (EntityLivingBase) entity;
	}

}