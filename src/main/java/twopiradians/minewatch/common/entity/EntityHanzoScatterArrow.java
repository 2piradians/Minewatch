package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;

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
		if (this.worldObj.isRemote && this.ticksExisted > 1) {
			int numParticles = scatter ? (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*30d) : 20;
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.worldObj, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posY+(this.prevPosY-this.posY)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						0, 0, 0, 0x5EDCE5, 0x007acc, 1, 20);
		}
		
		super.onUpdate();

		if (this.inGround)
			this.inGround = false;
		
		if (!this.worldObj.isRemote && !this.scatter && this.ticksExisted > 100) 
			this.setDead();

	}

	@Override
	protected void onHit(RayTraceResult result) {

		// bounce if not scatter
		if (!this.scatter && !this.worldObj.isRemote && result.typeOfHit == RayTraceResult.Type.BLOCK && this.shootingEntity instanceof EntityPlayer) {
			if (result.sideHit == EnumFacing.DOWN || result.sideHit == EnumFacing.UP) 
				this.motionY *= -1.3d;
			else if (result.sideHit == EnumFacing.NORTH || result.sideHit == EnumFacing.SOUTH) 
				this.motionZ *= -1.3d;
			else 
				this.motionX *= -1.3d;
		}
		else
			super.onHit(result);

		if (result.entityHit instanceof EntityLivingBase)
			((EntityLivingBase)result.entityHit).hurtResistantTime = 0;

		// scatter
		if (this.scatter && !this.worldObj.isRemote && result.typeOfHit == RayTraceResult.Type.BLOCK && this.shootingEntity instanceof EntityPlayer) {
			for (int i=0; i<6; ++i) {
				EntityHanzoScatterArrow entityarrow = new EntityHanzoScatterArrow(worldObj, (EntityLivingBase) this.shootingEntity, false);
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
				this.worldObj.spawnEntityInWorld(entityarrow);
			}
			this.setDead();
		}
	}

}
