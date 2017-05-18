package twopiradians.minewatch.common.entity;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.item.weapon.ModWeapon;

public class EntityGenjiShuriken extends EntityThrowable
{
	private static final int LIFETIME = 40;

	public EntityGenjiShuriken(World worldIn) {
		super(worldIn);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
	}

	//Client doesn't read here
	public EntityGenjiShuriken(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
		this.setPosition(throwerIn.posX, throwerIn.posY + (double)throwerIn.getEyeHeight(), throwerIn.posZ);
	}

	/**Copied from EntityArrow*/
	public void setAim(Entity shooter, float pitch, float yaw, float velocity, float inaccuracy) {
		float f = -MathHelper.sin(yaw * (float)Math.PI/180) * MathHelper.cos(pitch * (float)Math.PI/180);
		float f1 = -MathHelper.sin(pitch * (float)Math.PI/180);
		float f2 = MathHelper.cos(yaw * (float)Math.PI/180) * MathHelper.cos(pitch * (float)Math.PI/180);
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
		float f = MathHelper.sqrt_float((float) (this.motionX * this.motionX + this.motionZ * this.motionZ));
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
		if (result.entityHit instanceof EntityLivingBase && this.getThrower() != null && result.entityHit != this.getThrower()) {
			if (this.getThrower() instanceof EntityPlayer)
				((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 28F/ModWeapon.DAMAGE_SCALE);
			else 
				if (this.getThrower() instanceof EntityPlayer)
					((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 28F/ModWeapon.DAMAGE_SCALE);
			if (this.getThrower() != null)
				result.entityHit.worldObj.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
						SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.3f, result.entityHit.worldObj.rand.nextFloat()/2+0.75f);
			((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
			this.setDead();
		}
		else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			Block block = this.worldObj.getBlockState(result.getBlockPos()).getBlock();
			
			if (!Arrays.asList(ModEntities.ENTITY_PASSES_THROUGH).contains(block))
				this.setDead();
		}
	}
}
