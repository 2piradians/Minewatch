package twopiradians.minewatch.common.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityJunkratGrenade extends EntityMW {

	public int bounces;
	public int explodeTimer;
	public boolean isDeathGrenade;

	public EntityJunkratGrenade(World worldIn) {
		super(worldIn);
		this.setSize(0.12f, 0.12f);
		this.explodeTimer = -1;
	}

	public EntityJunkratGrenade(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.12f, 0.12f);
		this.lifetime = 1200;
		this.explodeTimer = -1;
	}

	@Override
	public void onUpdate() {
		// set explode timer
		if (this.explodeTimer == -1 && this.bounces >= 3)
			this.explodeTimer = 10;

		// explode if not moving
		if (!this.world.isRemote && this.explodeTimer == -1 && Math.sqrt(motionX*motionX+motionY*motionY+motionZ*motionZ) < 0.005d &&
				this.posX == this.prevPosX && this.posY == this.prevPosY && this.posZ == this.prevPosZ) {
			this.bounces = 3;
			Minewatch.network.sendToAll(new SPacketSimple(20, this, false, 3, 0, 0));
		}

		// gravity
		this.motionY -= 0.04D;

		// spin forward in the direction it's moving
		float f = MathHelper.sqrt((float) (this.motionX * this.motionX + this.motionZ * this.motionZ));
		this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
		this.rotationPitch = this.prevRotationPitch-f*1000f;

		// gradual slowdown
		double d1 = this.inWater ? 0.6d : 0.97d;
		this.motionX *= d1;
		this.motionY *= d1;
		this.motionZ *= d1;

		this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);

		// explode
		if (this.explodeTimer != -1 && --this.explodeTimer == 0) 
			this.explode(null);

		// bounce if rolling on ground
		if (this.onGround)
			this.motionY = 0.2d;

		// initial particle spawn / sound start
		if (this.world.isRemote && this.firstUpdate) {
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, this, 0xFFDF89, 0xFFDF89, 0.5f, Integer.MAX_VALUE, 2.5f, 2.5f, 0, 1);
		}

		// trail/spark particles
		if (this.world.isRemote) {
			EntityHelper.spawnTrailParticles(this, 30, 0.05d, 0xFFD387, 0x423D37, 0.4f, 20, 0.3f);

			if (this.world.rand.nextInt(3) == 0)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, 
						this.posX+(world.rand.nextDouble()-0.5d)*0.1d, 
						this.posY+(world.rand.nextDouble()-0.5d)*0.1d, 
						this.posZ+(world.rand.nextDouble()-0.5d)*0.1d, 
						(world.rand.nextDouble()-0.5d)*0.1d, world.rand.nextDouble()*0.1d, (world.rand.nextDouble()-0.5d)*0.1d, 
						0xFFDF89, 0xFFDF89, 1, 10, 0.5f, 0.2f, 0, 1);
		}

		super.onUpdate();
	}

	public void explode(@Nullable Entity directHit) {
		if (this.world.isRemote) {
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.EXPLOSION, world, 
					this.posX, this.posY+height/2d, this.posZ, 0, 0, 0, 
					0xFFFFFF, 0xFFFFFF, 1, 35+world.rand.nextInt(10), 10, 10, 0, 0);
			if (this.isDeathGrenade)
				this.world.playSound(this.posX, this.posY, this.posZ, ModSoundEvents.junkratGrenadeExplode, 
						SoundCategory.PLAYERS, 1.0f, 1.0f, false);
		}
		else {
			Minewatch.proxy.createExplosion(world, this.getThrower(), posX, posY, posZ, 
					1.6f, 0f, 12.5f, 80f, directHit, 120f, true);
		}
		this.setDead();
	}

	@Override
	protected void onImpact(RayTraceResult result) {	
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			// bounce
			if (result.sideHit == EnumFacing.DOWN || result.sideHit == EnumFacing.UP) 
				this.motionY *= -1.1d;
			else if (result.sideHit == EnumFacing.NORTH || result.sideHit == EnumFacing.SOUTH) 
				this.motionZ *= -0.7d;
			else 
				this.motionX *= -0.7d;

			// sync bounces
			if (!this.world.isRemote && bounces < 3) 
				Minewatch.network.sendToAll(new SPacketSimple(20, this, false, Math.min(3, ++bounces), 0, 0));
		}
		// direct hit explosion
		else if (!this.world.isRemote && result.entityHit != null && !this.isDeathGrenade) {
			this.explode(result.entityHit);
			Minewatch.network.sendToAll(new SPacketSimple(20, this, true));
		}
	}

}
