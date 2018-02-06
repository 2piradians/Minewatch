package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityDoomfistBullet extends EntityMW {

	public EntityDoomfistBullet(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityDoomfistBullet(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 9;
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, world, shooter, 
				0xE9FCF6, 0x7E89EB, 0.2f, 1, 2, 2, 0, 0, hand, 9, 0.41f);
	}

	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 20, 0.05d, 0xE9FCF6, 0x7E89EB, 0.3f, 1, 1); 
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		EntityHelper.attemptDamage(getThrower(), result.entityHit, 11, false);
	}
}
