package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntitySoldier76Bullet extends EntityMW {

	public EntitySoldier76Bullet(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntitySoldier76Bullet(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 1;
	}
	
	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, worldObj, shooter, 
				0x4AFDFD, 0x4AFDFD, 0.7f, 1, 5, 4.5f, 0, 0, hand, 12, 0.45f);
	}
	
	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0x5EDCE5, 0x007acc, 1, 1, 1);
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 5.7f, 19, 30, 55);
	}
}
