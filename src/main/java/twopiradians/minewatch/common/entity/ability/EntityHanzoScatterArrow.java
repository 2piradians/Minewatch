package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.projectile.EntityHanzoArrow;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityHanzoScatterArrow extends EntityHanzoArrow {

	private boolean scatter;
	public Entity ignoreEntity;

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
		if (result.getBlockPos() == null || !EntityHelper.shouldIgnoreBlock(world.getBlockState(result.getBlockPos()).getBlock())) {
			EntityHelper.moveToHitPosition(this, result, false);
			if (!this.world.isRemote && this.shootingEntity instanceof EntityLivingBase) {
				// bounce off block / entity
				EnumFacing side = result.sideHit;
				if (side == null && result.entityHit != null && result.hitVec != null) {
					Vec3d vec = this.getPositionVector().subtract(EntityHelper.getCenter(result.entityHit.getEntityBoundingBox()));
					side = EnumFacing.getFacingFromVector((float)vec.x, (float)vec.y, (float)vec.z);
				}
				EntityHelper.bounce(this, side, 0.1d, 1.3d);
				this.getDataManager().set(VELOCITY_CLIENT, new Rotations((float) this.motionX, (float) this.motionY, (float) this.motionZ));

				// scatter
				if (scatter) {
					for (int i=0; i<6; ++i) {
						EntityHanzoScatterArrow entityarrow = new EntityHanzoScatterArrow(world, (EntityLivingBase) this.shootingEntity, false);
						entityarrow.setDamage(this.getDamage());
						entityarrow.copyLocationAndAnglesFrom(this);

						entityarrow.motionX = this.motionX;
						entityarrow.motionY = this.motionY;
						entityarrow.motionZ = this.motionZ;

						entityarrow.setThrowableHeading(entityarrow.motionX, entityarrow.motionY, entityarrow.motionZ, 2.0f, 10.0f);
						entityarrow.getDataManager().set(VELOCITY_CLIENT, new Rotations((float) entityarrow.motionX, (float) entityarrow.motionY, (float) entityarrow.motionZ));
						if (result.entityHit != null)
							entityarrow.ignoreEntity = result.entityHit;
						this.world.spawnEntity(entityarrow);
					}
					EntityHelper.moveToHitPosition(this, result);
				}

				// hit entity (ignore entity scattered on for first tick)
				if (result.typeOfHit == RayTraceResult.Type.ENTITY && !(!this.scatter && this.ignoreEntity == result.entityHit && this.ticksExisted <= 2))
					super.onHit(result);
			}
			else
				super.onHit(result);
		}
	}

}
