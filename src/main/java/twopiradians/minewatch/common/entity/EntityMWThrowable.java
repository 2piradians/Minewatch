package twopiradians.minewatch.common.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.packet.PacketSyncSpawningEntity;

public abstract class EntityMWThrowable extends EntityThrowable {

	protected int lifetime;

	public EntityMWThrowable(World worldIn) {
		super(worldIn);
	}

	public EntityMWThrowable(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.ignoreEntity = this;
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
		/*double velX = Math.cos(pitch*Math.PI/180) * Math.cos(yaw*Math.PI/180 + Math.PI/2);;
        double velY = - Math.sin(pitch*Math.PI/180);
        double velZ = Math.cos(pitch*Math.PI/180) * Math.sin(yaw*Math.PI/180 + Math.PI/2);;

        // copied from EntityArrow
        double x = shooter.posX + Math.cos(pitch*Math.PI/180)*Math.cos(yaw*Math.PI/180 + Math.PI/2);
        double y = shooter.posY + shooter.getEyeHeight() - Math.sin(pitch*Math.PI/180);
        double z = shooter.posZ + Math.cos(pitch*Math.PI/180)*Math.sin(yaw*Math.PI/180 + Math.PI/2);

        double crosshairAdjust = 0.1d;
        if (hand == EnumHand.MAIN_HAND) {
            x -= Math.cos(yaw*Math.PI/180)/2;
            y -= 0.15d - Math.sin(pitch*Math.PI/180)/2;
            z -= Math.sin(yaw*Math.PI/180)/2;
            //velX = Math.cos(pitch*Math.PI/180) * Math.cos(yaw*Math.PI/180 + Math.PI/2 - Math.copySign(crosshairAdjust, yaw));
            //velZ = Math.cos(pitch*Math.PI/180) * Math.sin(yaw*Math.PI/180 + Math.PI/2 - Math.copySign(crosshairAdjust, yaw));
        }
        else if (hand == EnumHand.OFF_HAND) {
            x += Math.cos(yaw*Math.PI/180)/2;
            y -= 0.15d - Math.sin(pitch*Math.PI/180)/2;
            z += Math.sin(yaw*Math.PI/180)/2;
            //velX = Math.cos(pitch*Math.PI/180) * Math.cos(yaw*Math.PI/180 + Math.PI/2 + Math.copySign(crosshairAdjust, yaw));
            //velZ = Math.cos(pitch*Math.PI/180) * Math.sin(yaw*Math.PI/180 + Math.PI/2 + Math.copySign(crosshairAdjust, yaw));
        }
        this.setPosition(x, y, z);
		this.setThrowableHeading((double)velX, (double)velY, (double)velZ, velocity, inaccuracy);*/
		double x = -Math.sin(yaw * Math.PI/180) * Math.cos(pitch * Math.PI/180);
        double y = -Math.sin(pitch * Math.PI/180);
        double z = Math.cos(yaw * Math.PI/180) * Math.cos(pitch * Math.PI/180);
        this.setThrowableHeading(x, y, z, velocity, inaccuracy);
		this.motionX += shooter.motionX;
		this.motionZ += shooter.motionZ;
		Vec3d look = shooter.getLookVec().scale(1).rotateYaw(0).rotatePitch(0);
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

}
