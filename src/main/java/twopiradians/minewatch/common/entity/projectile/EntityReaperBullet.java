package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityReaperBullet extends EntityMW {

	public EntityReaperBullet(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityReaperBullet(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 1;
	}
	
	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SMOKE, world, shooter, 
				0xD93B1A, 0x510D30, 0.3f, 8, 5, 4, 0, 0, hand, 14, 0.5f);
	}
	
	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 3, 0.1d, 0xAF371E, 0xFFC26E, 0.3f, 2, 1);
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 2, 7, 11, 20);
	}
}
