package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

		if (this.world.isRemote) {
			int numParticles = 10;
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesHanzoScatter(this.world, 
						this.posX+(this.lastTickPosX-this.posX)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posY+(this.lastTickPosY-this.posY)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posZ+(this.lastTickPosZ-this.posZ)*i/numParticles+world.rand.nextDouble()*0.05d);
		}

		if (this.inGround)
			this.setDead();

	}

	@Override
	protected void onHit(RayTraceResult result) {
		super.onHit(result);

		if (this.scatter && !this.world.isRemote && result.typeOfHit == RayTraceResult.Type.BLOCK && this.shootingEntity instanceof EntityPlayer) {
			System.out.println(this.rotationYaw);
			System.out.println(this.rotationPitch);
			// TODO deflect based on arrow pitch/yaw vs. result.sideHit
			for (int i=0; i<1; ++i) {
				EntityHanzoScatterArrow entityarrow = new EntityHanzoScatterArrow(world, (EntityLivingBase) this.shootingEntity, false);
				entityarrow.setDamage(this.getDamage());
				entityarrow.copyLocationAndAnglesFrom(this);
				//entityarrow.setThrowableHeading(x, y, z, 2.0f, 0.0f);
				//entityarrow.rotationPitch = ;
				//entityarrow.rotationYaw = ;
				this.world.spawnEntity(entityarrow);
			}
			this.setDead();
		}
	}

}
