package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityPharahRocket extends EntityMW {

	private static final DataParameter<Boolean> CONCUSSIVE = EntityDataManager.<Boolean>createKey(EntityPharahRocket.class, DataSerializers.BOOLEAN);

	public EntityPharahRocket(World worldIn) {
		this(worldIn, null, -1, false);

	}

	public EntityPharahRocket(World worldIn, EntityLivingBase throwerIn, int hand, boolean concussive) {
		super(worldIn, throwerIn, hand);
		if (!worldIn.isRemote)
			this.getDataManager().set(CONCUSSIVE, concussive);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
		this.lifetime = 200; 
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(CONCUSSIVE, false);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 18, 0.05d, 0x9361D4, 0xEBBCFF, 0.5f, 8, 1); 
	}

	@Override
	public void onImpact(RayTraceResult result) {
		boolean concussive = this.getDataManager().get(CONCUSSIVE);

		super.onImpact(result);

		if (concussive) {
			ModSoundEvents.PHARAH_CONCUSSION_HIT.playSound(this, 1.5f, 1);
			Minewatch.proxy.createExplosion(world, this, posX, posY, posZ, 8f, 0, 0, 0, result.entityHit, 0, false, 0.9f, 2.8f, 0.9f, 2.8f);
		}
		else {
			ModSoundEvents.PHARAH_ROCKET_HIT.playSound(this, 2, 1);
			Minewatch.proxy.createExplosion(world, this, posX, posY, posZ, 2.5f, 40, 20, 80, result.entityHit, 120, false, 1, 1);
		}
	}
}
