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
		super.onUpdate();

		if (!this.scatter && !this.world.isRemote)
			System.out.println("prev: "+this.prevPosX+", x: "+this.posX);

		if (this.world.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*30d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesHanzoScatter(this.world, 
						this.posX+(this.lastTickPosX-this.posX)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posY+(this.lastTickPosY-this.posY)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posZ+(this.lastTickPosZ-this.posZ)*i/numParticles+world.rand.nextDouble()*0.05d);
		}

		if (this.inGround || (!this.scatter && this.ticksExisted > 100))
			this.setDead();

	}

	@Override
	protected void onHit(RayTraceResult result) {

		if (!this.scatter && result.typeOfHit == RayTraceResult.Type.BLOCK && this.shootingEntity instanceof EntityPlayer) {
			double velX = 0;
			double velZ = 0;
			double velY = 0;

			if (result.sideHit == EnumFacing.DOWN || result.sideHit == EnumFacing.UP) {
				velX = this.prevPosX - this.posX;
				velZ = this.prevPosZ - this.posZ;
				velY = this.posY - this.prevPosY;
			}
			else if (result.sideHit == EnumFacing.NORTH || result.sideHit == EnumFacing.SOUTH) {
				velX = this.prevPosX - this.posX;
				velZ = this.posZ - this.prevPosZ;
				velY = this.prevPosY - this.posY;
			}
			else {
				velX = this.posX - this.prevPosX;
				velZ = this.prevPosZ - this.posZ;
				velY = this.prevPosY - this.posY;
			} 

			this.setThrowableHeading(velX, velY, velZ, 3.0f, 0.0f);
			System.out.println("bounce: x: "+velX+", y: "+velY+", z: "+velZ);
		}
		else
			super.onHit(result);

		if (result.entityHit instanceof EntityLivingBase)
			((EntityLivingBase)result.entityHit).hurtResistantTime = 0;

		if (this.scatter && !this.world.isRemote && result.typeOfHit == RayTraceResult.Type.BLOCK && this.shootingEntity instanceof EntityPlayer) {
			for (int i=0; i<1; ++i) {
				EntityHanzoScatterArrow entityarrow = new EntityHanzoScatterArrow(world, (EntityLivingBase) this.shootingEntity, false);
				entityarrow.setDamage(this.getDamage());
				entityarrow.copyLocationAndAnglesFrom(this);

				double velX = 0;
				double velZ = 0;
				double velY = 0;

				if (result.sideHit == EnumFacing.DOWN || result.sideHit == EnumFacing.UP) {
					velX = this.prevPosX - this.posX;
					velZ = this.prevPosZ - this.posZ;
					velY = this.posY - this.prevPosY;
				}
				else if (result.sideHit == EnumFacing.NORTH || result.sideHit == EnumFacing.SOUTH) {
					velX = this.prevPosX - this.posX;
					velZ = this.posZ - this.prevPosZ;
					velY = this.prevPosY - this.posY;
				}
				else {
					velX = this.posX - this.prevPosX;
					velZ = this.prevPosZ - this.posZ;
					velY = this.prevPosY - this.posY;
				} 

				entityarrow.setThrowableHeading(velX, velY, velZ, 3.0f, 10.0f);
				System.out.println("spawn: x: "+velX+", y: "+velY+", z: "+velZ);
				this.world.spawnEntity(entityarrow);
			}
			this.setDead();
		}
	}

}
