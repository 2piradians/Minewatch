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

public class EntityBastionBullet extends EntityMW {

	public EntityBastionBullet(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityBastionBullet(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 1;
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, worldObj, shooter, 
				0xFFEF89, 0x5A575A, 0.2f, 1, hand == null ? 2 : 5, hand == null ? 2 : 5, 0, 0, hand, hand == null ? 10 : 9, hand == null ? 0 : 0.41f);
	}
	
	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0xFFFCC7, 0xEAE7B9, 1, 1, 1);
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (this.getThrower() != null && ItemMWWeapon.isAlternate(this.getThrower().getHeldItemMainhand())) 
			EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 4, 15, 35, 55);
		else 
			EntityHelper.attemptFalloffImpact(this, getThrower(), result.entityHit, false, 6, 20, 26, 50);
	}
}
