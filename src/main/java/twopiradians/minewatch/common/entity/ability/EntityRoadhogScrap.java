package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.entity.projectile.EntityRoadhogBullet;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityRoadhogScrap extends EntityMW {

	public EntityRoadhogScrap(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityRoadhogScrap(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.23f, 0.23f);
		this.lifetime = 2;
		this.setNoGravity(true);
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SMOKE, world, shooter, 
				0x3C3C24, 0x070707, 0.4f/25f, 6, 5, 5, 0, 0, hand, 9, 0.41f);
	}

	@Override
	public void onUpdate() {
		// split
		if (this.ticksExisted == this.lifetime+1) {
			if (world.isRemote) {
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SMOKE, world, posX, posY, posZ, 0, 0, 0, 0x3C3C24, 0x070707, 1f, 9, 4, 4, 0, 0);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, posX, posY, posZ, 0, 0, 0, 0xFA8C34, 0xF68035, 1f, 6, 8, 8, 0, 0);
			}
			else {
				for (int i=0; i<25; ++i) {
					EntityRoadhogBullet projectile = new EntityRoadhogBullet(world, getThrower(), -1);
					EntityHelper.setAim(projectile, getThrower(), this.rotationPitch, this.rotationYaw, 60, 19F, null, 10, 0, true);
					projectile.setLocationAndAngles(this.posX, this.posY, this.posZ, projectile.rotationYaw, projectile.rotationPitch);
					world.spawnEntity(projectile);
				}
				ModSoundEvents.ROADHOG_SHOOT_EXPLODE.playSound(this, 3.0f, 1.0f);
				this.setDead();
			}
		}

		super.onUpdate();
	}

	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 10, 0, 0xFA3F0F, 0x9D270F, 0.5f, 5, 1);
	}

	@Override
	public void onImpact(RayTraceResult result) {
		// only damage if it hasn't exploded yet (has to be before super.onImpact bc that kills it too)
		if (!this.isDead)
			EntityHelper.attemptDamage(getThrower(), result.entityHit, 50, false);

		super.onImpact(result);

	}

}