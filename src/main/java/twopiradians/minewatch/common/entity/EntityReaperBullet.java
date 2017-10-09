package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityReaperBullet extends EntityMW {

	public EntityReaperBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityReaperBullet(World worldIn, EntityLivingBase throwerIn, EnumHand hand) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 5;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0xAF371E, 0xFFC26E, 0.3f, 1, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptImpact(this, result.entityHit, 7 - (7 - 2) * ((float)this.ticksExisted / lifetime), false)) 
			result.entityHit.hurtResistantTime = 0;
	}
}
