package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.projectile.EntityHanzoArrow;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityHanzoScatterArrow extends EntityHanzoArrow {

	private boolean scatter;

	public EntityHanzoScatterArrow(World worldIn) {
		super(worldIn);
	}

	public EntityHanzoScatterArrow(World worldIn, EntityLivingBase shooter, boolean scatter) {
		super(worldIn, shooter);
		this.scatter = scatter;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.inGround)
			this.inGround = false;

		if (!this.world.isRemote && !this.scatter && this.ticksExisted > 100) 
			this.setDead();
	}
	
	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0x5EDCE5, 0x007acc, 0.8f, 10, 1);
	}

	@Override
	protected void onHit(RayTraceResult result) {
		EntityHelper.moveToHitPosition(this, result, false);
		if (!this.world.isRemote && result.typeOfHit == RayTraceResult.Type.BLOCK && this.shootingEntity instanceof EntityLivingBase) 
			// scatter
			if (scatter) {
				EntityHelper.bounce(this, result.sideHit, 0.1d, 1.3d);
				for (int i=0; i<6; ++i) {
					EntityHanzoScatterArrow entityarrow = new EntityHanzoScatterArrow(world, (EntityLivingBase) this.shootingEntity, false);
					entityarrow.setDamage(this.getDamage());
					entityarrow.copyLocationAndAnglesFrom(this);

					entityarrow.motionX = this.motionX;
					entityarrow.motionY = this.motionY;
					entityarrow.motionZ = this.motionZ;

					entityarrow.setThrowableHeading(entityarrow.motionX, entityarrow.motionY, entityarrow.motionZ, 2.0f, 10.0f);
					entityarrow.getDataManager().set(VELOCITY, new Rotations((float) entityarrow.motionX, (float) entityarrow.motionY, (float) entityarrow.motionZ));
					this.world.spawnEntity(entityarrow);
				}
				EntityHelper.moveToHitPosition(this, result);
			}
		// bounce if not scatter
			else {
				EntityHelper.bounce(this, result.sideHit, 0.1d, 1.3d);
				this.getDataManager().set(VELOCITY, new Rotations((float) this.motionX, (float) this.motionY, (float) this.motionZ));
			}
		else
			super.onHit(result);
	}

}
