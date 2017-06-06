package twopiradians.minewatch.common.entity;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.item.weapon.ModWeapon;

public class EntityTracerBullet extends EntityThrowable
{
	private static final int LIFETIME = 5;

	public EntityTracerBullet(World worldIn) {
		super(worldIn);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
	}

	//Client doesn't read here
	public EntityTracerBullet(World worldIn, EntityLivingBase throwerIn, EnumHand hand) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
		double velX = Math.cos(throwerIn.rotationPitch*Math.PI/180) * Math.cos(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.1d;
		double velY = - Math.sin(throwerIn.rotationPitch*Math.PI/180) + (Math.random() - 0.5d)*0.1d;
		double velZ = Math.cos(throwerIn.rotationPitch*Math.PI/180) * Math.sin(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.1d;
		double x = throwerIn.posX + Math.cos(throwerIn.rotationPitch*Math.PI/180)*Math.cos(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2);
		double y = throwerIn.posY + throwerIn.getEyeHeight() - Math.sin(throwerIn.rotationPitch*Math.PI/180);
		double z = throwerIn.posZ + Math.cos(throwerIn.rotationPitch*Math.PI/180)*Math.sin(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2);
		if (hand == EnumHand.MAIN_HAND) {
			x -= Math.cos(throwerIn.rotationYawHead*Math.PI/180)/2;
			y -= 0.15d - Math.sin(throwerIn.rotationPitch*Math.PI/180)/2;
			z -= Math.sin(throwerIn.rotationYawHead*Math.PI/180)/3;
		}
		else {
			x += Math.cos(throwerIn.rotationYawHead*Math.PI/180)/2;
			y -= 0.15d - Math.sin(throwerIn.rotationPitch*Math.PI/180)/2;
			z += Math.sin(throwerIn.rotationYawHead*Math.PI/180)/3;
		}
		this.setPosition(x, y, z);
		this.setRotation(0, 0);
		double speed = 5.0d;
		double speedNormalize = Math.sqrt(velX*velX + velY*velY + velZ*velZ);
		velX *= speed/speedNormalize;
		velY *= speed/speedNormalize;
		velZ *= speed/speedNormalize;
		this.motionX = velX;
		this.motionY = velY;
		this.motionZ = velZ;
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
		if (result.entityHit != null && result.entityHit == this.getThrower())
			return;
		else if (result.entityHit instanceof EntityLivingBase && this.getThrower() != null) {
			float damage = 6 - (6 - 1.5f) * (this.ticksExisted / LIFETIME);
			if (this.getThrower() instanceof EntityPlayer)
				((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), damage/ModWeapon.DAMAGE_SCALE);
			else
				((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), damage/ModWeapon.DAMAGE_SCALE);
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
