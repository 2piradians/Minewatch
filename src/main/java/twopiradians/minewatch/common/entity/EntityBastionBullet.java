package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityBastionBullet extends EntityMW {

	public EntityBastionBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityBastionBullet(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 15;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0xFFFCC7, 0xEAE7B9, 1, 1, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptImpact(this, result.entityHit, 20 - (20 - 6) * ((float)this.ticksExisted / lifetime), false)) 
			result.entityHit.hurtResistantTime = 0;
	}
}
