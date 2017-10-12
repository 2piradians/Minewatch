package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityMcCreeBullet extends EntityMW {

	private boolean fanTheHammer;

	public EntityMcCreeBullet(World worldIn) {
		this(worldIn, null, false);
	}

	public EntityMcCreeBullet(World worldIn, EntityLivingBase throwerIn, boolean fanTheHammer) {
		super(worldIn, throwerIn);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 3;
		this.fanTheHammer = fanTheHammer;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0x5AD8E8, 0x5A575A, 0.8f, 7, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (this.fanTheHammer && EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 13.5f, 45, 18, 30))
				result.entityHit.hurtResistantTime = 0;
		else if (!this.fanTheHammer && EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 21, 70, 22, 45))
			result.entityHit.hurtResistantTime = 0;
	}
}
