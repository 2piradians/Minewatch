package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityPharahRocket extends EntityMW {

	public static final DataParameter<Rotations> ULT_OSCILLATION = EntityDataManager.<Rotations>createKey(EntityMW.class, DataSerializers.ROTATIONS);
	public enum Type { NORMAL, CONCUSSIVE, ULTIMATE }

	private static final DataParameter<Integer> TYPE = EntityDataManager.<Integer>createKey(EntityPharahRocket.class, DataSerializers.VARINT);
	public Type type;
	public Vec3d velocity = null;
	public Vec3d oscillation = null;

	public EntityPharahRocket(World worldIn) { 
		this(worldIn, null, -1, Type.NORMAL);
	}

	public EntityPharahRocket(World worldIn, EntityLivingBase throwerIn, int hand, Type type) {
		super(worldIn, throwerIn, hand);
		if (!worldIn.isRemote) 
			setOscillation(type);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
		this.lifetime = type == Type.ULTIMATE ? 60 : 200; 
	}

	private void setOscillation(Type type) {
		this.getDataManager().set(TYPE, type.ordinal());
		if (type == Type.ULTIMATE) { 
			Vec3d vec = new Vec3d(world.rand.nextDouble()-0.5d, world.rand.nextDouble()-0.5d, world.rand.nextDouble()-0.5d).normalize();
			this.getDataManager().set(ULT_OSCILLATION, new Rotations((float)vec.x, (float)vec.y, (float)vec.z));
		}		
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		// type
		if (key.getId() == TYPE.getId() && this.dataManager.get(TYPE) >= 0 && this.dataManager.get(TYPE) < Type.values().length)
			type = Type.values()[this.dataManager.get(TYPE)];

		// reset saved values if they are changed
		if (key.getId() == VELOCITY_CLIENT.getId())
			this.velocity = null;
		if (key.getId() == ULT_OSCILLATION.getId())
			this.oscillation = null;
		super.notifyDataManagerChange(key);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(TYPE, -1);
		this.getDataManager().register(ULT_OSCILLATION, new Rotations(0, 0, 0));
	}

	@Override
	public void onUpdate() {
		if (type == Type.ULTIMATE) {
			// save values to not have to read from datamanager constantly
			if (velocity == null) 
				velocity = new Vec3d(dataManager.get(VELOCITY_CLIENT).getX(), dataManager.get(VELOCITY_CLIENT).getY(), dataManager.get(VELOCITY_CLIENT).getZ());
			if (oscillation == null)
				oscillation = new Vec3d(dataManager.get(ULT_OSCILLATION).getX(), dataManager.get(ULT_OSCILLATION).getY(), dataManager.get(ULT_OSCILLATION).getZ());

			float motion = (float) Math.cos(ticksExisted/5f)/4f;
			motionX = velocity.x+motion*oscillation.x;
			motionY = velocity.y+motion*oscillation.y;
			motionZ = velocity.z+motion*oscillation.z;
		}

		super.onUpdate();
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		float verticalAdjust = 12f;
		float horizontalAdjust = 0.15f;
		if (type == Type.CONCUSSIVE) {
			Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.HOLLOW_CIRCLE_2, world, getThrower(), 0xECFFFF, 0x76A5FF, 2f, 5, 0.5f, 2, world.rand.nextFloat(), 0.02f, hand, 15, 0.6f);
		}
		else if (type == Type.NORMAL) {
			Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.CIRCLE, world, getThrower(), 0xFFBA5F, 0xFFF569, 0.4f, 5, 0.5f, 2, world.rand.nextFloat(), 0.02f, hand, verticalAdjust, horizontalAdjust);
			Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.HOLLOW_CIRCLE_2, world, getThrower(), 0xFFE439, 0xFFC830, 2f, 5, 0.5f, 2, world.rand.nextFloat(), 0.02f, hand, verticalAdjust, horizontalAdjust);
			Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, world, getThrower(), 0xFFE439, 0xFFC830, 0.5f, 5, 0.5f, 2, world.rand.nextFloat(), 0.02f, hand, verticalAdjust, horizontalAdjust);
		}
	}

	@Override
	public void spawnTrailParticles() {
		if (type == Type.CONCUSSIVE) {
			// following particles
			if (this.firstUpdate) {
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, this, 0x76A5FF, 0x76A5FF, 0.8f, Integer.MAX_VALUE, 2.5f, 2.5f, 0, 1);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, this, 0xECFFFF, 0xECFFFF, 0.85f, Integer.MAX_VALUE, 1.6f, 1.6f, 0, 0);
			}
			else {			
				Vec3d motion = new Vec3d((world.rand.nextFloat()-0.5f)*0.05f, (world.rand.nextFloat()-0.5f)*0.05f, (world.rand.nextFloat()-0.5f)*0.05f);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_2, world, prevPosX, prevPosY, prevPosZ, 
						motion.x, motion.y, motion.z, 
						0x76A5FF, 0x76A5FF, 1, 10, 0.6f, 0.2f, world.rand.nextFloat(), 0.02f);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, prevPosX, prevPosY, prevPosZ, 
						motion.x, motion.y, motion.z, 
						0xECFFFF, 0xECFFFF, 1, 10, 0.6f, 0.2f, world.rand.nextFloat(), 0.02f);
			}
		}
		else if (type == Type.NORMAL) {
			EntityHelper.spawnTrailParticles(this, 5, 0.09d, 0xCDCDC4, 0xCDCDC4, 0.9f, 14, 1); 
			if (!this.firstUpdate) 
				EntityHelper.spawnTrailParticles(this, 3, 0.05d, 0xFDF36A, 0xDC9D6A, 1.5f, 2, 1);
		}
		else if (type == Type.ULTIMATE && (this.ticksExisted > 1 || getThrower() != Minewatch.proxy.getClientPlayer())) {
			EntityHelper.spawnTrailParticles(this, 2, 0.04d, 0xCDCDC4, 0xCDCDC4, 0.6f, 9, 1); 
			EntityHelper.spawnTrailParticles(this, 2, 0.05d, 0xFDF36A, 0xDC9D6A, 0.7f, 2, 1);
		}
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (type == Type.CONCUSSIVE) {
			if (!world.isRemote) {
				ModSoundEvents.PHARAH_CONCUSSION_HIT.playSound(this, 1.5f, 1);
				Minewatch.proxy.createExplosion(world, this, posX, posY, posZ, 8f, 0, 0, 0, result.entityHit, 0, false, 0.9f, 2.8f, 0.9f, 2.8f);
			}
			else {
				for (int i=0; i<5; ++i)
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.SMOKE, world, posX, posY, posZ, 
							(world.rand.nextFloat()-0.5f)*0.5f, (world.rand.nextFloat()-0.5f)*0.5f, (world.rand.nextFloat()-0.5f)*0.5f,
							0xF0FDFF, 0x5E6984, 0.4f, 20, 10, 20, world.rand.nextFloat(), 0);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_2, world, posX, posY, posZ, 0, 0, 0, 0xD2F1FF, 0xA2B8FE, 5f, 5, 1, 40, world.rand.nextFloat(), 0);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, posX, posY, posZ, 0, 0, 0, 0xFFFFFF, 0xA2B8FE, 5f, 5, 1, 40, world.rand.nextFloat(), 0);
			}
		}
		else if (type == Type.NORMAL) {
			if (!world.isRemote) {
				ModSoundEvents.PHARAH_ROCKET_HIT.playSound(this, 4, 1);
				Minewatch.proxy.createExplosion(world, this, posX, posY, posZ, 2.5f, 40, 20, 80, result.entityHit, 120, false, 1, 1);
			}
			else {
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.EXPLOSION, world, posX, posY, posZ, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 0.8f, 20, 20, 80, world.rand.nextFloat(), 0);
			}
		}
		else if (type == Type.ULTIMATE) {
			if (!world.isRemote) {
				if (world.rand.nextInt(3) == 0)
					ModSoundEvents.PHARAH_ROCKET_HIT.playSound(this, 1, 1);
				Minewatch.proxy.createExplosion(world, this, posX, posY, posZ, 1.0f, 40, 40, 40, result.entityHit, 40, false, 0.15f, 0.15f, false);
			}
			else {
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.EXPLOSION, world, posX, posY, posZ, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 0.8f, 15, 10, 30, world.rand.nextFloat(), 0);
			}
		}
	}

}