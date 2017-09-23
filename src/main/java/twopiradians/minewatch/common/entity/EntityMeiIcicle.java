package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;

public class EntityMeiIcicle extends EntityMWThrowable {

	public EntityMeiIcicle(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityMeiIcicle(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 40;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*10d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.world, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles, 
						this.posY+this.height/2+(this.prevPosY-this.posY)*i/numParticles, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles, 
						0, 0, 0, 0x5EDCE5, 0x007acc, 0.6f, 5, 0.1f);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		this.attemptImpact(result.entityHit, 75 - (75 - 22) * ((float)this.ticksExisted / lifetime), false);
	}
}
