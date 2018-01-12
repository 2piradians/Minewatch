package twopiradians.minewatch.common.entity.ability;

import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityMoiraOrb extends EntityMW {

	private static final DataParameter<Boolean> HEAL = EntityDataManager.<Boolean>createKey(EntityMoiraOrb.class, DataSerializers.BOOLEAN);

	public CopyOnWriteArrayList<EntityLivingBase> tethered = new CopyOnWriteArrayList<EntityLivingBase>();
	public int chargeClient = 80;
	public int chargeServer = 80;
	private double prevMotionX;
	private double prevMotionY;
	private double prevMotionZ;

	private int bounceSoundCooldown;

	public EntityMoiraOrb(World worldIn) {
		this(worldIn, null, -1, false);
	}

	public EntityMoiraOrb(World worldIn, EntityLivingBase throwerIn, int hand, boolean heal) {
		super(worldIn, throwerIn, hand); 
		this.setSize(1f, 1f);
		this.lifetime = 200;
		this.impactOnClient = true;
		if (!worldIn.isRemote)
			this.getDataManager().set(HEAL, heal);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(HEAL, false);
	}

	@Override
	public void onUpdate() {
		this.isFriendly = this.getDataManager().get(HEAL);
		
		if (!worldObj.isRemote)
			--bounceSoundCooldown;

		// check for entities to tether to
		if (this.ticksExisted % 2 == 0) {
			this.tethered.clear();
			for (Entity entity : worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expandXyz(5)))
				if (entity instanceof EntityLivingBase && EntityHelper.shouldHit(this, entity, this.isFriendly) &&
						((EntityLivingBase)entity).canEntityBeSeen(this) && 
						(!this.isFriendly || ((EntityLivingBase) entity).getHealth() < ((EntityLivingBase) entity).getMaxHealth())) 
					this.tethered.add((EntityLivingBase) entity);
		}

		// effects
		for (EntityLivingBase entity : this.tethered) {
			if (this.chargeServer > 0 && !worldObj.isRemote)
				EntityHelper.attemptDamage(getThrower(), entity, this.isFriendly ? -3.75f : 2.5f, true);

			if (!worldObj.isRemote && --this.chargeServer <= 0)
				this.setDead();
			else if (worldObj.isRemote && --this.chargeClient <= 0)
				this.setDead();
		}

		// adjust motion when tethered vs. not tethered
		Vec3d motion = new Vec3d(motionX, motionY, motionZ).normalize().scale(this.tethered.isEmpty() ? 1 : 0.275d);
		this.motionX = motion.xCoord;
		this.motionY = motion.yCoord;
		this.motionZ = motion.zCoord;

		// save motion from before moving - because collision can reduce it
		prevMotionX = motionX;
		prevMotionY = motionY;
		prevMotionZ = motionZ;
		super.onUpdate();
		this.motionX = prevMotionX;
		this.motionY = prevMotionY;
		this.motionZ = prevMotionZ;
	}

	@Override
	public void spawnTrailParticles() {
		// initial particle spawn
		if (this.firstUpdate) {
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.MOIRA_ORB, worldObj, this, this.isFriendly ? 0xFBF235 : 0xFF50FF, this.isFriendly ? 0xFBF235 : 0xFF50FF, 1, Integer.MAX_VALUE, 10, 10, 0, 0.05f);
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.MOIRA_ORB, worldObj, this, this.isFriendly ? 0xFBF235 : 0x251A60, this.isFriendly ? 0xFBF235 : 0x251A60, 1, Integer.MAX_VALUE, 10, 10, 0, 0.05f);
		}
		
		//if (this.ticksExisted % 2 == 0)
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, worldObj, 
					this.posX+(worldObj.rand.nextFloat()-0.5f)*0.5f, 
					this.posY+(worldObj.rand.nextFloat()-0.5f)*0.5f+this.height/2f, 
					this.posZ+(worldObj.rand.nextFloat()-0.5f)*0.5f, 
					(worldObj.rand.nextFloat()-0.5f)*0.1f, 
					(worldObj.rand.nextFloat()-0.5f)*0.1f, 
					(worldObj.rand.nextFloat()-0.5f)*0.1f, 
					this.isFriendly ? 0xFCFCF3 : 0x251A60, this.isFriendly ? 0xFBF235 : 0x251A60, 1, 20, 0.5f, 0.5f, worldObj.rand.nextFloat(), worldObj.rand.nextFloat()*0.2f);
	}

	@Override
	public void onImpact(RayTraceResult result) {	
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {			
			if (!worldObj.isRemote && bounceSoundCooldown <= 0) {
				if (this.isFriendly)
					ModSoundEvents.MOIRA_ORB_HEAL_BOUNCE.playFollowingSound(this, 2.0f, (worldObj.rand.nextFloat()-0.5f)*0.2f+0.9f, false);
				else
					ModSoundEvents.MOIRA_ORB_DAMAGE_BOUNCE.playFollowingSound(this, 2.0f, (worldObj.rand.nextFloat()-0.5f)*0.2f+0.9f, false);
				this.bounceSoundCooldown = 4;
			}

			EntityHelper.bounce(this, result.sideHit, 0.1d, 1);

			// update saved momentum
			prevMotionX = motionX;
			prevMotionY = motionY;
			prevMotionZ = motionZ;
		}
	}

}
