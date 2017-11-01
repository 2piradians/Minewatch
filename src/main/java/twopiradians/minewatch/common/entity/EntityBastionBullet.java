package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityBastionBullet extends EntityMW {

	public EntityBastionBullet(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityBastionBullet(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 1;
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, world, shooter, 
				0xFFEF89, 0x5A575A, 0.2f, 1, hand == null ? 2 : 5, hand == null ? 2 : 5, 0, 0, hand, hand == null ? 10 : 9, hand == null ? 0 : 0.41f);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0xFFFCC7, 0xEAE7B9, 1, 1, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (this.getThrower() != null && EnumHero.BASTION.playersUsingAlt.contains(this.getThrower().getPersistentID())) {
			if (EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 4, 15, 35, 55)) 
				result.entityHit.hurtResistantTime = 0;
		}
		else if (EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 6, 20, 26, 50)) 
			result.entityHit.hurtResistantTime = 0;
	}
}
