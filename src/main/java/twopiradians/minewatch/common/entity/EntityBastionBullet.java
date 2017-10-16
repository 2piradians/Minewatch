package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityBastionBullet extends EntityMW {

	public EntityBastionBullet(World worldIn) {
		this(worldIn, null);
	}

	public EntityBastionBullet(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 3;
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

		if (EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 6, 20, 26, 50)) 
			result.entityHit.hurtResistantTime = 0;
	}
}
