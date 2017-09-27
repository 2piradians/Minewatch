package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;

public class EntityMercyBullet extends EntityMWThrowable {

	public EntityMercyBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityMercyBullet(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 40;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.worldObj.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*10d);
			for (int i=0; i<numParticles; ++i) {
				Minewatch.proxy.spawnParticlesTrail(this.worldObj, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posY+(this.prevPosY-this.posY)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						0, 0, 0, 0xE39684, 0xE26E53, 1.5f, 2, 1);
				Minewatch.proxy.spawnParticlesTrail(this.worldObj, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posY+(this.prevPosY-this.posY)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						0, 0, 0, 0xF7F489, 0xF4EF5A, 0.8f, 2, 1);
			}
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (this.attemptImpact(result.entityHit, 20, false)) 
			((EntityLivingBase)result.entityHit).hurtResistantTime = 0;

		if (this.worldObj.isRemote && (result.entityHit == null || this.shouldHit(result.entityHit)))
			Minewatch.proxy.spawnParticlesSpark(worldObj, 
					result.entityHit == null ? result.hitVec.xCoord : posX, 
							result.entityHit == null ? result.hitVec.yCoord : posY, 
									result.entityHit == null ? result.hitVec.zCoord : posZ, 
											0xE39684, 0xE26E53, 5, 5);
	}
}
