package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntitySoldier76Bullet extends EntityMW {

	public EntitySoldier76Bullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntitySoldier76Bullet(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 15;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0x5EDCE5, 0x007acc, 1, 1, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptImpact(this, result.entityHit, 19 - (19 - 5.7f) * ((float)this.ticksExisted / lifetime), false)) 
			result.entityHit.hurtResistantTime = 0;
	}
}
