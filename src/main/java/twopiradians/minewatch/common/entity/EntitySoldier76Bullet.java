package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntitySoldier76Bullet extends EntityMW {

	public EntitySoldier76Bullet(World worldIn) {
		this(worldIn, null);
	}

	public EntitySoldier76Bullet(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 3;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0x5EDCE5, 0x007acc, 1, 1, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 5.7f, 19, 30, 55)) 
			result.entityHit.hurtResistantTime = 0;
	}
}
