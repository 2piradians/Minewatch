package twopiradians.minewatch.common.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
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

	/* This is what Reaper used to use, for reference
	 * double velX = Math.cos(throwerIn.rotationPitch*Math.PI/180) * Math.cos(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.2d;
		double velY = - Math.sin(throwerIn.rotationPitch*Math.PI/180) + (Math.random() - 0.5d)*0.2d;
		double velZ = Math.cos(throwerIn.rotationPitch*Math.PI/180) * Math.sin(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.2d;
		double x = throwerIn.posX + Math.cos(throwerIn.rotationPitch*Math.PI/180)*Math.cos(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2);
		double y = throwerIn.posY + throwerIn.getEyeHeight() - Math.sin(throwerIn.rotationPitch*Math.PI/180);
		double z = throwerIn.posZ + Math.cos(throwerIn.rotationPitch*Math.PI/180)*Math.sin(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2);
		if (hand == EnumHand.MAIN_HAND) {
			x -= Math.cos(throwerIn.rotationYawHead*Math.PI/180)/3;
			y -= 0.15d - Math.sin(throwerIn.rotationPitch*Math.PI/180)/2;
			z -= Math.sin(throwerIn.rotationYawHead*Math.PI/180)/3;
		}
		else {
			x += Math.cos(throwerIn.rotationYawHead*Math.PI/180)/3;
			y -= 0.15d - Math.sin(throwerIn.rotationPitch*Math.PI/180)/2;
			z += Math.sin(throwerIn.rotationYawHead*Math.PI/180)/3;
		}
		this.setPosition(x, y, z);
		this.setRotation(0, 0);
		double speed = 3.0d;
		double speedNormalize = Math.sqrt(velX*velX + velY*velY + velZ*velZ);
		velX *= speed/speedNormalize;
		velY *= speed/speedNormalize;
		velZ *= speed/speedNormalize;
		this.motionX = velX;
		this.motionY = velY;
		this.motionZ = velZ;
	 */
	public void setAim(EntityPlayer shooter, float pitch, float yaw, float velocity, float inaccuracy, EnumHand hand, boolean sendPacket) {
		double velX = Math.cos(shooter.rotationPitch*Math.PI/180) * Math.cos(shooter.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.2d;
		double velY = - Math.sin(shooter.rotationPitch*Math.PI/180) + (Math.random() - 0.5d)*0.2d;
		double velZ = Math.cos(shooter.rotationPitch*Math.PI/180) * Math.sin(shooter.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.2d;
		// copied from EntityArrow
		double x = shooter.posX + Math.cos(shooter.rotationPitch*Math.PI/180)*Math.cos(shooter.rotationYawHead*Math.PI/180 + Math.PI/2);
		double y = shooter.posY + shooter.getEyeHeight() - Math.sin(shooter.rotationPitch*Math.PI/180);
		double z = shooter.posZ + Math.cos(shooter.rotationPitch*Math.PI/180)*Math.sin(shooter.rotationYawHead*Math.PI/180 + Math.PI/2);
		if (hand == EnumHand.MAIN_HAND) {
			x -= Math.cos(shooter.rotationYawHead*Math.PI/180)/3;
			y -= 0.15d - Math.sin(shooter.rotationPitch*Math.PI/180)/2;
			z -= Math.sin(shooter.rotationYawHead*Math.PI/180)/3;
		}
		else {
			x += Math.cos(shooter.rotationYawHead*Math.PI/180)/3;
			y -= 0.15d - Math.sin(shooter.rotationPitch*Math.PI/180)/2;
			z += Math.sin(shooter.rotationYawHead*Math.PI/180)/3;
		}
		this.setPosition(x, y, z);
		this.setThrowableHeading((double)velX, (double)velY, (double)velZ, velocity, inaccuracy);
		this.motionX += shooter.motionX;
		this.motionZ += shooter.motionZ;
		this.prevRotationPitch = pitch;
		this.prevRotationYaw = yaw;
		this.setRotation(yaw, pitch);
		if (!shooter.onGround) 
			this.motionY += shooter.motionY;

		// correct trajectory of fast entities (received in render class)
		if (!this.world.isRemote && this.ticksExisted == 0 && sendPacket) {
			System.out.println("sent, pitch: "+this.rotationPitch+", yaw: "+this.rotationYaw);
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
