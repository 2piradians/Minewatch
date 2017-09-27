package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;

public class EntityGenjiShuriken extends EntityMWThrowable {

	public EntityGenjiShuriken(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityGenjiShuriken(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 40;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.worldObj.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*30d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.worldObj, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posY+this.height/2+(this.prevPosY-this.posY)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						0, 0, 0, 0xC8E682, 0x709233, 0.5f, 4, 1);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (this.attemptImpact(result.entityHit, 28, false)) 
			((EntityLivingBase)result.entityHit).hurtResistantTime = 0;

		if (this.worldObj.isRemote && (result.entityHit == null || this.shouldHit(result.entityHit)))
			Minewatch.proxy.spawnParticlesSpark(worldObj, 
					result.entityHit == null ? result.hitVec.xCoord : posX, 
							result.entityHit == null ? result.hitVec.yCoord : posY, 
									result.entityHit == null ? result.hitVec.zCoord : posZ, 
											0xC8E682, 0x709233, 5, 5);
	}
}
