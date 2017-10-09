package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityTracerBullet extends EntityMW {

	public EntityTracerBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityTracerBullet(World worldIn, EntityLivingBase throwerIn, EnumHand hand) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 15;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 10, 0, 0x5EDCE5, 0x007acc, 0.5f, 1, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptImpact(this, result.entityHit, 6 - (6 - 1.5f) * ((float)this.ticksExisted / lifetime), false)) 
			result.entityHit.hurtResistantTime = 0;
	}
}
