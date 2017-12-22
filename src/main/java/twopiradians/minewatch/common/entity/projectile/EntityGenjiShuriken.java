package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityGenjiShuriken extends EntityMW {

	public EntityGenjiShuriken(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityGenjiShuriken(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.15f, 0.15f);
		this.setNoGravity(true);
		this.lifetime = 40;
	}
	
	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0xC8E682, 0x709233, 0.5f, 4, 1);
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		EntityHelper.attemptDamage(this, result.entityHit, 28, false);
		
		if (this.worldObj.isRemote)
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, worldObj, result.entityHit == null ? result.hitVec.xCoord : posX, 
					result.entityHit == null ? result.hitVec.yCoord : posY, 
							result.entityHit == null ? result.hitVec.zCoord : posZ, 
									0, 0, 0, 0xC8E682, 0x709233, 0.7f, 5, 5, 4.5f, worldObj.rand.nextFloat(), 0.01f);
	}
}
