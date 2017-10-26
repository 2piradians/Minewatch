package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityWidowmakerBullet extends EntityMW {

	private static final DataParameter<Boolean> SCOPED = EntityDataManager.<Boolean>createKey(EntityWidowmakerBullet.class, DataSerializers.BOOLEAN);
	private int damage;

	public EntityWidowmakerBullet(World worldIn) {
		this(worldIn, null, -1, false, 0);
	}

	public EntityWidowmakerBullet(World worldIn, EntityLivingBase throwerIn, int hand, boolean scoped, int damage) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 1;
		this.damage = damage;
		if (!this.world.isRemote)
			this.getDataManager().set(SCOPED, scoped);
	}
	
	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, world, shooter, 
				0xF9394F, 0x5A575A, 0.7f, 1, 2, 1.5f, 0, 0, hand, 4, 0.4f);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(SCOPED, false);
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) 
			if (this.getDataManager().get(SCOPED)) 
				EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0xFF0000, 0xB2B2B2, 0.5f, 15, 0.8f);
			else 
				EntityHelper.spawnTrailParticles(this, 5, 0, 0xFF0000, 0xFF0000, 0.5f, 2, 0.5f);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptImpact(this, result.entityHit, damage, false)) 
			if (!this.dataManager.get(SCOPED))
				result.entityHit.hurtResistantTime = 0;
	}
}
