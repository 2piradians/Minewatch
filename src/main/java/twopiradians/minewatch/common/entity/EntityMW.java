package twopiradians.minewatch.common.entity;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public abstract class EntityMW extends Entity implements IThrowableEntity {

	public static final DataParameter<Rotations> VELOCITY = EntityDataManager.<Rotations>createKey(EntityMW.class, DataSerializers.ROTATIONS);
	public static final DataParameter<Rotations> POSITION = EntityDataManager.<Rotations>createKey(EntityMW.class, DataSerializers.ROTATIONS);
	public static final DataParameter<Integer> HAND = EntityDataManager.<Integer>createKey(EntityMW.class, DataSerializers.VARINT);
	public boolean notDeflectible;
	public int lifetime;
	private EntityLivingBase thrower;
	public boolean isFriendly;

	public EntityMW(World worldIn) {
		this(worldIn, null, -1);
	}

	/**@param hand -1 no muzzle, 0 main-hand, 1 off-hand, 2 middle*/
	public EntityMW(World worldIn, @Nullable EntityLivingBase throwerIn, int hand) {
		super(worldIn);
		if (throwerIn != null) {
			this.thrower = throwerIn;
			this.setPosition(throwerIn.posX, throwerIn.posY + (double)throwerIn.getEyeHeight() - 0.1D, throwerIn.posZ);
		}
		this.dataManager.set(HAND, hand);
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(VELOCITY, new Rotations(0, 0, 0));
		this.dataManager.register(POSITION, new Rotations(0, 0, 0));
		this.dataManager.register(HAND, -1);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		// velocity
		if (key.getId() == VELOCITY.getId()) {
			this.motionX = this.dataManager.get(VELOCITY).getX();
			this.motionY = this.dataManager.get(VELOCITY).getY();
			this.motionZ = this.dataManager.get(VELOCITY).getZ();
			EntityHelper.setRotations(this);
		}
		// update prev position and spawn trail (for genji's deflect mostly)
		else if (key.getId() == POSITION.getId() && this.world.isRemote && 
				(this.dataManager.get(POSITION).getX() != 0 || this.dataManager.get(POSITION).getY() != 0 || this.dataManager.get(POSITION).getZ() != 0)) {
			this.posX = this.dataManager.get(POSITION).getX();
			this.posY = this.dataManager.get(POSITION).getY();
			this.posZ = this.dataManager.get(POSITION).getZ();
			this.spawnTrailParticles();
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
		}
		// muzzle particle
		else if (key.getId() == HAND.getId() && this.world.isRemote && this.ticksExisted == 0 && !this.isDead && 
				this.dataManager.get(HAND) != -1 && this.getThrower() instanceof EntityLivingBase)
			this.spawnMuzzleParticles(this.dataManager.get(HAND) >= 0 && this.dataManager.get(HAND) < EnumHand.values().length ? 
					EnumHand.values()[this.dataManager.get(HAND)] : null, this.getThrower());

	}

	public void spawnTrailParticles() {}

	/**Spawn muzzle particles when first spawning*/
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {}

	@Override
	public void onUpdate() {	
		// check for impacts
		if (!world.isRemote) { 
			ArrayList<RayTraceResult> results = EntityHelper.checkForImpact(this);
			RayTraceResult nearest = EntityHelper.getNearestImpact(this, results);
			for (RayTraceResult result : results) 
				if (result != null && isValidImpact(result, result == nearest))
					this.onImpact(result);
		}

		// set prev's
		this.prevPosX = this.posX; 
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;

		// move if still alive and has motion
		if ((!world.isRemote || this.ticksExisted > 1 || !this.hasNoGravity()) && 
				!this.isDead && Math.sqrt(motionX*motionX+motionY*motionY+motionZ*motionZ) > 0) {
			if (this.hasNoGravity())
				this.setPosition(this.posX+this.motionX, this.posY+this.motionY, this.posZ+this.motionZ);
			else // needed to set onGround / do block collisions
				this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ); 
		}

		// set dead if needed (why was this only server? - what if on client but not server?)
		if (/*!this.world.isRemote && */((this.ticksExisted > lifetime) || 
				!(this.getThrower() instanceof EntityLivingBase) || posY <= -64))
			this.setDead();

		// spawn trail particles
		if (this.world.isRemote)
			this.spawnTrailParticles();

		this.firstUpdate = false;
	}

	/**Should this result trigger onImpact*/
	protected boolean isValidImpact(RayTraceResult result, boolean nearest) {
		return result != null && result.typeOfHit != RayTraceResult.Type.MISS && 
				(result.typeOfHit != RayTraceResult.Type.ENTITY || 
				(EntityHelper.shouldHit(getThrower(), result.entityHit, isFriendly))) && nearest;
	}

	/**Should this move to the hit position of the RayTraceResult*/
	protected void onImpactMoveToHitPosition(RayTraceResult result) {
		if (result != null) {
			if (world.isRemote)
				this.setDead();
			else
				EntityHelper.moveToHitPosition(this, result);
		}
	}

	public void onImpact(RayTraceResult result) {
		if (!world.isRemote) { 
			this.onImpactMoveToHitPosition(result);
			Minewatch.network.sendToAllAround(new SPacketSimple(41, this, result), 
					new TargetPoint(world.provider.getDimension(), posX, posY, posZ, 64));
		}
		else {
			this.spawnTrailParticles();
			this.onImpactMoveToHitPosition(result);
		}
	}

	@Override
	public float getEyeHeight() {
		return this.height/2f;
	}

	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return distance < 3000;
	}

	@Override
	public EntityLivingBase getThrower() {
		return this.thrower;
	}

	@Override
	public void setThrower(Entity entity) {
		if (entity instanceof EntityLivingBase) 
			this.thrower = (EntityLivingBase) entity;
	}

	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {return false;}
	@Override
	public boolean writeToNBTAtomically(NBTTagCompound compound) {return false;}
	@Override
	public void readFromNBT(NBTTagCompound compound) {}
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {return compound;}
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {}
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {}

}