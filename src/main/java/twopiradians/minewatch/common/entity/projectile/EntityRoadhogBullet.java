package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityRoadhogBullet extends EntityMW {

	private static final DataParameter<Boolean> ULTIMATE = EntityDataManager.<Boolean>createKey(EntityRoadhogBullet.class, DataSerializers.BOOLEAN);
	public boolean ultimate;

	public EntityRoadhogBullet(World worldIn) {
		this(worldIn, null, -1, false);
	}

	public EntityRoadhogBullet(World worldIn, EntityLivingBase throwerIn, int hand, boolean ultimate) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 6;
		if (!world.isRemote)
			this.getDataManager().set(ULTIMATE, ultimate);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(ULTIMATE, false);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		// ultimate
		if (key.getId() == ULTIMATE.getId())
			ultimate = this.dataManager.get(ULTIMATE);

		super.notifyDataManagerChange(key);
	}


	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		if (ultimate) { // ult
			Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SMOKE, world, shooter, 
					0x3C3C24, 0x070707, 0.4f/25f, 6, 5, 5, 0, 0, null, 30, 0);
			Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, world, shooter, 
					0xFA8C34, 0xF68035, 0.5f/25f, 3, 5, 5, 0, 0, null, 30, 0);
		}
		else {
			if (hand == null) { // scrap 
				
			}
			else { // primary
				Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SMOKE, world, shooter, 
						0x3C3C24, 0x070707, 0.4f/25f, 6, 5, 5, 0, 0, hand, 10, 0.31f);
				Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, world, shooter, 
						0xFA8C34, 0xF68035, 0.5f/25f, 3, 5, 5, 0, 0, hand, 10, 0.31f);
			}
		}
	}

	@Override
	public void spawnTrailParticles() {
		if (ultimate) {
			boolean friendly = !EntityHelper.shouldHit(this, Minewatch.proxy.getClientPlayer(), false);
			if (friendly)
				EntityHelper.spawnTrailParticles(this, 1, 0.05d, 0xF4C869, 0xAD8F4A, 0.5f, 1, 1); 
			else
				EntityHelper.spawnTrailParticles(this, 2, 0.05d, 0xD7513E, 0x070707, 0.5f, 1, 1);
		}
		else {
			EntityHelper.spawnTrailParticles(this, 3, 0.05d, 0xFA8C34, 0xF68035, 0.3f, 1, 1); 
			EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0x3C3C24, 0x070707, 0.5f, 1, 1); 
		}
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);
		
		EntityHelper.attemptDamage(this, result.entityHit, world.rand.nextInt(6)+1, false, true, false, !ultimate);

		if (ultimate && result.entityHit instanceof EntityLivingBase) {
			((EntityLivingBase)result.entityHit).knockBack(getThrower(), 1.5f, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
		}
	}
}
