package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityRoadhogBullet extends EntityMW {

	public EntityRoadhogBullet(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityRoadhogBullet(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 6;
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SMOKE, world, shooter, 
				0x3C3C24, 0x070707, 0.4f/25f, 6, 5, 5, 0, 0, hand, 9, 0.41f);
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, world, shooter, 
				0xFA8C34, 0xF68035, 0.5f/25f, 3, 5, 5, 0, 0, hand, 9, 0.41f);
	}

	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 3, 0.05d, 0xFA8C34, 0xF68035, 0.3f, 1, 1); 
		EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0x3C3C24, 0x070707, 0.5f, 1, 1); 
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		EntityHelper.attemptDamage(getThrower(), result.entityHit, world.rand.nextInt(6)+1, false);
	}
}
