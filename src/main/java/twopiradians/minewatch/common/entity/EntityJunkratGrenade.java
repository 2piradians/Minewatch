package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityJunkratGrenade extends EntityMWThrowable {

	private int bounces;
	private int explodeTimer;

	public EntityJunkratGrenade(World worldIn) {
		super(worldIn);
		this.setSize(0.12f, 0.12f);
		this.shouldLockDirection = false;
	}

	public EntityJunkratGrenade(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.12f, 0.12f);
		this.lifetime = Integer.MAX_VALUE;
		this.shouldLockDirection = false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		this.motionY -= 0.04D;

		float f = MathHelper.sqrt((float) (this.motionX * this.motionX + this.motionZ * this.motionZ));
		this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
		this.rotationPitch = this.prevRotationPitch-f*1000f;
		double d1 = this.inWater ? 0.6d : 0.95d;
		this.motionX *= d1;
		this.motionY *= d1;
		this.motionZ *= d1;

		this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);

		System.out.println(this.world.isRemote+": "+this.explodeTimer+", "+this.bounces);

		if (!this.world.isRemote && (--this.explodeTimer == 0 || (this.explodeTimer < 0 && this.onGround))) 
			this.explode();

		/*
		if (this.world.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*10d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.world, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles, 
						this.posY+this.height/2+(this.prevPosY-this.posY)*i/numParticles, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles, 
						0, 0, 0, 0x5EDCE5, 0x007acc, 0.6f, 5, 0.1f);
		}*/
	}

	public void explode() {
		// explosion
		Explosion explosion = new Explosion(world, this.getThrower(), posX, posY, posZ, 1.8f, false, true);
		explosion.doExplosionA();
		explosion.clearAffectedBlockPositions();
		explosion.doExplosionB(true);
		if (this.world.isRemote) {
			
		}
		else {
			Minewatch.network.sendToAll(new SPacketSimple(20, this, false));
			this.setDead();
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {		
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			if (result.sideHit == EnumFacing.DOWN || result.sideHit == EnumFacing.UP) 
				this.motionY *= -1.1d;
			else if (result.sideHit == EnumFacing.NORTH || result.sideHit == EnumFacing.SOUTH) 
				this.motionZ *= -1.1d;
			else 
				this.motionX *= -1.1d;
			if (this.bounces++ >= 3)
				this.explodeTimer = 10;
		}
		else if (!this.world.isRemote && this.shouldHit(result.entityHit))
			this.explode();
		//super.onImpact(result);

		//this.attemptImpact(result.entityHit, 75 - (75 - 22) * ((float)this.ticksExisted / lifetime), false);
	}

}
