package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityZenyattaOrb extends EntityMW {

	public static final DataParameter<Integer> TYPE = EntityDataManager.<Integer>createKey(EntityZenyattaOrb.class, DataSerializers.VARINT);
	/**@param type -1 none, 0 normal, 1 discord, 2 harmony*/
	public int type = -1;

	public EntityZenyattaOrb(World worldIn) {
		this(worldIn, null, -1, -1);
	}

	/**@param type -1 none, 0 normal, 1 discord, 2 harmony*/
	public EntityZenyattaOrb(World worldIn, EntityLivingBase throwerIn, int hand, int type) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.15f, 0.15f);
		this.setNoGravity(true);
		this.lifetime = 40;
		if (!worldIn.isRemote)
			this.dataManager.set(TYPE, type);
	}

	@Override
	protected void entityInit() {
		super.entityInit();

		this.dataManager.register(TYPE, -1);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);

		if (key.getId() == TYPE.getId()) 
			type = this.dataManager.get(TYPE);
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		if (hand != null)
			EnumHero.ZENYATTA.weapon.reequipAnimation(shooter.getHeldItem(hand));
	}

	@Override
	public void spawnTrailParticles() {
		if (type == 0) {
			EntityHelper.spawnTrailParticles(this, 5, 0, 0x86F3FF, 0x929EC8, 1, 4, 1);
			if (type == 0 && this.ticksExisted % 2 == 0)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, posX, posY+height/2d, posZ, 0, 0, 0, 0x90E3FF, 0x91C3ED, 1f, 7, 2, 2.2f, 0, 0);
		}
		else if (type == 1)
			EntityHelper.spawnTrailParticles(this, 5, 0, 0x8967AF, 0xA3A0A5, 1, 2, 0.1f);
		else if (type == 2)
			EntityHelper.spawnTrailParticles(this, 5, 0, 0xCFC77F, 0xFFF3B7, 1, 2, 0.1f);
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		// normal hit
		if (this.type == 0) {
			EntityHelper.attemptDamage(this, result.entityHit, 46, false);

			if (this.world.isRemote)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, result.entityHit == null ? result.hitVec.x : posX, 
						result.entityHit == null ? result.hitVec.y : posY, 
								result.entityHit == null ? result.hitVec.z : posZ, 
										0, 0, 0, 0x86F3FF, 0xCFDFF9, 0.7f, 10, 5, 4.5f, world.rand.nextFloat(), 0.01f);
		}
	}
}
