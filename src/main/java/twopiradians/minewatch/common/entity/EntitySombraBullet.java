package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntitySombraBullet extends EntityMW {

	public EntitySombraBullet(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntitySombraBullet(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 1; 
	}
	
	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, worldObj, shooter, 
				0xF37BFF, 0xF37BFF, 0.7f, 1, 2, 1, 0, 0, hand, 9, 0.41f);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.worldObj.isRemote) 
			EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0xFFF1F1, 0xF37BFF, 0.5f, 1, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 2.4f, 8f, 15, 25)) 
			result.entityHit.hurtResistantTime = 0;
	}
}
