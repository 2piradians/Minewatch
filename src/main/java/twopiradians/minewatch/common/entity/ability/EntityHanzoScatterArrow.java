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

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0x5EDCE5, 0x007acc, 1, 20, 1);

		if (this.inGround)
			this.inGround = false;

		if (!this.world.isRemote && !this.scatter && this.ticksExisted > 100) 
			this.setDead();
	}

	@Override
	protected void onHit(RayTraceResult result) {

		if (!this.world.isRemote && result.typeOfHit == RayTraceResult.Type.BLOCK && this.shootingEntity instanceof EntityLivingBase) 
			// scatter
			if (scatter) {
				for (int i=0; i<6; ++i) {
					EntityHanzoScatterArrow entityarrow = new EntityHanzoScatterArrow(world, (EntityLivingBase) this.shootingEntity, false);
					entityarrow.setDamage(this.getDamage());
					entityarrow.copyLocationAndAnglesFrom(this);

					entityarrow.motionX = this.motionX;
					entityarrow.motionY = this.motionY;
					entityarrow.motionZ = this.motionZ;

					if (result.sideHit == EnumFacing.DOWN || result.sideHit == EnumFacing.UP) 
						entityarrow.motionY *= -1.3d;
					else if (result.sideHit == EnumFacing.NORTH || result.sideHit == EnumFacing.SOUTH) 
						entityarrow.motionZ *= -1.3d;
					else 
						entityarrow.motionX *= -1.3d;

					entityarrow.setThrowableHeading(entityarrow.motionX, entityarrow.motionY, entityarrow.motionZ, 2.0f, 10.0f);
					entityarrow.getDataManager().set(VELOCITY, new Rotations((float) entityarrow.motionX, (float) entityarrow.motionY, (float) entityarrow.motionZ));
					this.world.spawnEntity(entityarrow);
				}
				EntityHelper.moveToHitPosition(this, result);
			}
		// bounce if not scatter
			else {
				if (result.sideHit == EnumFacing.DOWN || result.sideHit == EnumFacing.UP) 
					this.motionY *= -1.3d;
				else if (result.sideHit == EnumFacing.NORTH || result.sideHit == EnumFacing.SOUTH) 
					this.motionZ *= -1.3d;
				else 
					this.motionX *= -1.3d;
				this.getDataManager().set(VELOCITY, new Rotations((float) this.motionX, (float) this.motionY, (float) this.motionZ));
			}
		else
			super.onHit(result);
	}

}
