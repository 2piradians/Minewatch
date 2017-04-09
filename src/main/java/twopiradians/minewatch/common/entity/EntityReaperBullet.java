package twopiradians.minewatch.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.item.weapon.ModWeapon;

public class EntityReaperBullet extends EntityThrowable
{
	private static final int LIFETIME = 5;
    private EntityLivingBase thrower;
	
	public EntityReaperBullet(World worldIn) {
		super(worldIn);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
	}
	
	//Client doesn't read here
	public EntityReaperBullet(World worldIn, EntityLivingBase throwerIn) {
		this(worldIn);
		//TODO adjust for which gun fires
		double velX = Math.cos(throwerIn.rotationPitch*Math.PI/180) * Math.cos(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.2d;
		double velY = - Math.sin(throwerIn.rotationPitch*Math.PI/180) + (Math.random() - 0.5d)*0.2d;
		double velZ = Math.cos(throwerIn.rotationPitch*Math.PI/180) * Math.sin(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.2d;
		double x = throwerIn.posX + Math.cos(throwerIn.rotationPitch*Math.PI/180)*Math.cos(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2);
		double y = throwerIn.posY + 1.5d - Math.sin(throwerIn.rotationPitch*Math.PI/180);
		double z = throwerIn.posZ + Math.cos(throwerIn.rotationPitch*Math.PI/180)*Math.sin(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2);
		this.setPositionAndRotationDirect(x, y, z, 0, 0, 0, false);
		double speed = 3.0d;
		double speedNormalize = Math.sqrt(velX*velX + velY*velY + velZ*velZ);
		velX *= speed/speedNormalize;
		velY *= speed/speedNormalize;
		velZ *= speed/speedNormalize;
		setVelocity(velX, velY, velZ);
	}

	/**Copied from EntityArrow*/
	public void setAim(Entity shooter, float pitch, float yaw, float velocity, float inaccuracy) {
		float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
		float f1 = -MathHelper.sin(pitch * 0.017453292F);
		float f2 = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
		this.setThrowableHeading((double)f, (double)f1, (double)f2, velocity, inaccuracy);
		this.motionX += shooter.motionX;
		this.motionZ += shooter.motionZ;
		this.prevRotationPitch = pitch;
		this.prevRotationYaw = yaw;
		this.setRotation(yaw, pitch);

		if (!shooter.onGround) {
			this.motionY += shooter.motionY;
		}
	}

	@Override
	public void onUpdate() {		
		float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
		this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * (180D / Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;

		super.onUpdate();
				
		if (this.ticksExisted > LIFETIME)
			this.setDead();
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.entityHit != null && result.entityHit == this.thrower)
			return;
		else if (result.entityHit instanceof EntityLiving) {
			float damage = 7 - (7 - 2) * (this.ticksExisted / LIFETIME);
			((EntityLiving)result.entityHit).attackEntityFrom(DamageSource.MAGIC, damage/ModWeapon.DAMAGE_SCALE);
			((EntityLiving)result.entityHit).hurtResistantTime = 0;
			this.setDead();
		}
		else if (result.typeOfHit == RayTraceResult.Type.BLOCK)
			this.setDead();
	}
}
