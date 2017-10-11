package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityMercyBullet extends EntityMW {

	public EntityMercyBullet(World worldIn) {
		this(worldIn, null);
	}

	public EntityMercyBullet(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 40;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) {
			EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0xE39684, 0xE26E53, 1.5f, 2, 1);
			EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0xF7F489, 0xF4EF5A, 0.8f, 2, 1);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptImpact(this, result.entityHit, 20, false)) 
			result.entityHit.hurtResistantTime = 0;

		if (this.world.isRemote)
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, result.entityHit == null ? result.hitVec.xCoord : posX, 
					result.entityHit == null ? result.hitVec.yCoord : posY, 
							result.entityHit == null ? result.hitVec.zCoord : posZ,
									0, 0, 0, 0xE39684, 0xE26E53, 1, 5, 5, 4, world.rand.nextFloat(), 0.01f);
	}
}
