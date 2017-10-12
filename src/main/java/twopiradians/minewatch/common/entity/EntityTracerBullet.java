package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityTracerBullet extends EntityMW {

	public EntityTracerBullet(World worldIn) {
		this(worldIn, null);
	}

	public EntityTracerBullet(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 15;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 5, 0, 0x5EDCE5, 0x007acc, 0.5f, 1, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 1.5f, 6, 11, 30)) 
			result.entityHit.hurtResistantTime = 0;
	}
}
