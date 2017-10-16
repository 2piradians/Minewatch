package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityReaperBullet extends EntityMW {

	public EntityReaperBullet(World worldIn) {
		this(worldIn, null);
	}

	public EntityReaperBullet(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 2;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0xAF371E, 0xFFC26E, 0.3f, 2, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 2, 7, 11, 20))
			result.entityHit.hurtResistantTime = 0;
	}
}
