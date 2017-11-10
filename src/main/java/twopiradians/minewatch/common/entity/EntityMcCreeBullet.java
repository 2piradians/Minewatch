package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityMcCreeBullet extends EntityMW {

	private boolean fanTheHammer;

	public EntityMcCreeBullet(World worldIn) {
		this(worldIn, null, -1, false);
	}

	public EntityMcCreeBullet(World worldIn, EntityLivingBase throwerIn, int hand, boolean fanTheHammer) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 1;
		this.fanTheHammer = fanTheHammer;
	}
	
	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, worldObj, (EntityLivingBase) getThrower(), 
				0xFFEF89, 0x5A575A, 0.7f, 1, 5, 4.5f, 0, 0, hand, 10, 0.4f);
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.worldObj.isRemote) 
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
