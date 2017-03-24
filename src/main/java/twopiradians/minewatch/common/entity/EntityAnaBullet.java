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

public class EntityAnaBullet extends EntityThrowable
{
	private static final int LIFETIME = 40;

	public EntityAnaBullet(World worldIn) {
		super(worldIn);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
	}

	//Client doesn't read here
	public EntityAnaBullet(World worldIn, EntityLivingBase throwerIn) {
		this(worldIn);
		this.setPosition(throwerIn.posX, throwerIn.posY + (double)throwerIn.getEyeHeight() - 0.1D, throwerIn.posZ);
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
		if (result.entityHit instanceof EntityLiving) {
			((EntityLiving)result.entityHit).attackEntityFrom(DamageSource.MAGIC, 60F/ModWeapon.DAMAGE_SCALE);
			this.setDead();
		}
		else if (result.typeOfHit == RayTraceResult.Type.BLOCK)
			this.setDead();
	}
}
