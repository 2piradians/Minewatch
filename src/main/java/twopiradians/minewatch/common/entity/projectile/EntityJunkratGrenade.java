package twopiradians.minewatch.common.entity.projectile;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityJunkratGrenade extends EntityMW {

	public int bounces;
	public int explodeTimer;
	public boolean isDeathGrenade;

	public EntityJunkratGrenade(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityJunkratGrenade(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.12f, 0.12f);
		this.lifetime = 1200;
		this.explodeTimer = -1;
		this.impactOnClient = true;
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, worldObj, (EntityLivingBase) getThrower(), 
				0xFF9D1A, 0x964D21, 0.7f, 5, 5, 4.5f, worldObj.rand.nextFloat(), 0.01f, hand, 10, 0.5f);
	}

	@Override
	public void onUpdate() {		
		// set explode timer
		if (this.explodeTimer == -1 && this.bounces >= 3)
			this.explodeTimer = 10;

		// explode if not moving
		if (!this.worldObj.isRemote && this.explodeTimer == -1 && Math.sqrt(motionX*motionX+motionY*motionY+motionZ*motionZ) < 0.005d &&
				this.posX == this.prevPosX && this.posY == this.prevPosY && this.posZ == this.prevPosZ) {
			this.bounces = 3;
			Minewatch.network.sendToDimension(new SPacketSimple(20, this, false, 3, 0, 0), worldObj.provider.getDimension());
		}

		// spin forward in the direction it's moving
		float f = MathHelper.sqrt_double((float) (this.motionX * this.motionX + this.motionZ * this.motionZ));
		this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
		this.rotationPitch = this.prevRotationPitch-f*1000f;

		// gradual slowdown
		double d1 = this.inWater ? 0.6d : 0.97d;
		this.motionX *= d1;
		this.motionY *= d1;
		this.motionZ *= d1;

		// gravity
		this.motionY -= 0.04D;

		this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);

		// explode
		if (this.explodeTimer != -1 && --this.explodeTimer == 0) 
			this.explode(null);

		// bounce if rolling on ground
		if (this.onGround)
			this.motionY = 0.2d;

		super.onUpdate();
	}

	@Override
	public void spawnTrailParticles() {
		// initial particle spawn / sound start
		if (this.firstUpdate) 
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, worldObj, this, 0xFFDF89, 0xFFDF89, 0.5f, Integer.MAX_VALUE, 2.5f, 2.5f, 0, 1);

		// trail/spark particles
		EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0xFFD387, 0x423D37, 0.4f, 20, 0.3f);

		if (this.worldObj.rand.nextInt(3) == 0)
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, worldObj, 
					this.posX+(worldObj.rand.nextDouble()-0.5d)*0.1d, 
					this.posY+(worldObj.rand.nextDouble()-0.5d)*0.1d, 
					this.posZ+(worldObj.rand.nextDouble()-0.5d)*0.1d, 
					(worldObj.rand.nextDouble()-0.5d)*0.1d, worldObj.rand.nextDouble()*0.1d, (worldObj.rand.nextDouble()-0.5d)*0.1d, 
					0xFFDF89, 0xFFDF89, 1, 10, 0.5f, 0.2f, 0, 1);
	}

	public void explode(@Nullable Entity directHit) {
		if (this.worldObj.isRemote) {
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.EXPLOSION, worldObj, 
					this.posX, this.posY+height/2d, this.posZ, 0, 0, 0, 
					0xFFFFFF, 0xFFFFFF, 1, 35+worldObj.rand.nextInt(10), 10, 10, 0, 0);
			if (this.isDeathGrenade)
				ModSoundEvents.JUNKRAT_GRENADE_EXPLODE.playSound(this, 1, 1);
		}
		else {
			Minewatch.proxy.createExplosion(worldObj, this.getThrower(), posX, posY, posZ, 
					1.6f, 0f, 12.5f, 80f, directHit, 120f, true, 0, 1);
		}
		this.setDead();
	}

	@Override
	public void onImpact(RayTraceResult result) {	
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			EntityHelper.bounce(this, result.sideHit, 0.1d, 0.7d);

			// sync bounces
			if (!this.worldObj.isRemote && bounces < 3) 
				Minewatch.network.sendToDimension(new SPacketSimple(20, this, false, Math.min(3, ++bounces), 0, 0), worldObj.provider.getDimension());
		}
		// direct hit explosion
		else if (result.entityHit != null && !this.isDeathGrenade) {
			super.onImpact(result);

			if (this.worldObj.isRemote) {
				this.motionX = 0;
				this.motionY = 0;
				this.motionZ = 0;
			}
			else {
				this.explode(result.entityHit);
				Minewatch.network.sendToDimension(new SPacketSimple(20, this, true, result.entityHit), worldObj.provider.getDimension());
			}
		}
	}

}
