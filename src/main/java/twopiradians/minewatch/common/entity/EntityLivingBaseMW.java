package twopiradians.minewatch.common.entity;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.util.EntityHelper;

public abstract class EntityLivingBaseMW extends EntityLivingBase implements IThrowableEntity {

	public static final DataParameter<Rotations> VELOCITY_CLIENT = EntityDataManager.<Rotations>createKey(EntityLivingBaseMW.class, DataSerializers.ROTATIONS);
	public static final DataParameter<NBTTagCompound> POSITION_CLIENT = EntityDataManager.<NBTTagCompound>createKey(EntityLivingBaseMW.class, DataSerializers.COMPOUND_TAG);
	public boolean notDeflectible;
	protected int lifetime;
	private EntityLivingBase thrower;
	protected boolean skipImpact;
	public boolean isFriendly;
	public ArrayList<RayTraceResult.Type> ignoreImpacts = new ArrayList<RayTraceResult.Type>() {{add(RayTraceResult.Type.MISS);}};

	public EntityLivingBaseMW(World worldIn) {
		this(worldIn, null);
	}

	public EntityLivingBaseMW(World worldIn, @Nullable EntityLivingBase throwerIn) {
		super(worldIn);
		this.isImmuneToFire = true;
		if (throwerIn != null) {
			this.thrower = throwerIn;
			this.setPosition(throwerIn.posX, throwerIn.posY + (double)throwerIn.getEyeHeight() - 0.1D, throwerIn.posZ);
		}
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		EntityHelper.handleNotifyDataManagerChange(key, this);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(VELOCITY_CLIENT, new Rotations(0, 0, 0));
	}

	@Override
	public void onUpdate() {	
		this.setGlowing(false); 
		
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;

		// check for impacts
		ArrayList<RayTraceResult> results = EntityHelper.checkForImpact(this, this.isFriendly);
		RayTraceResult nearest = EntityHelper.getNearestImpact(this, results);
		for (RayTraceResult result : results) 
			if (result != null && isValidImpact(result, result == nearest))
				this.onImpact(result);
		// move if still alive and has motion
		if (!this.isDead && Math.sqrt(motionX*motionX+motionY*motionY+motionZ*motionZ) > 0) {
			if (this.hasNoGravity())
				this.setPosition(this.posX+this.motionX, this.posY+this.motionY, this.posZ+this.motionZ);
			else // needed to set onGround / do block collisions
				this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ); 
		}

		if (this.hurtTime > 0)
			--this.hurtTime;
		if (this.hurtResistantTime > 0)
			--this.hurtResistantTime;
		if (this.recentlyHit > 0)
			--this.recentlyHit;

		if (!this.world.isRemote && ((this.ticksExisted > lifetime && lifetime > 0) || this.getHealth() <= 0
				|| !(this.getThrower() instanceof EntityLivingBase) || posY <= -64 || !this.getThrower().isEntityAlive()))
			this.setDead();
		
		// spawn trail particles
		if (this.world.isRemote)
			this.spawnTrailParticles();

		this.firstUpdate = false;
	}
	
	public void spawnTrailParticles() {}

	/**Should this result trigger onImpact - moves to hit position if entity*/
	protected boolean isValidImpact(RayTraceResult result, boolean nearest) {
		return result != null && result.typeOfHit != RayTraceResult.Type.MISS && 
				(result.typeOfHit != RayTraceResult.Type.ENTITY || 
				(EntityHelper.shouldHit(getThrower(), result.entityHit, isFriendly) && nearest));
	}

	/**Should this move to the hit position of the RayTraceResult*/
	protected boolean shouldMoveToHitPosition(RayTraceResult result) {
		return result != null;
	}

	protected void onImpact(RayTraceResult result) {
		if (this.shouldMoveToHitPosition(result))
			EntityHelper.moveToHitPosition(this, result);
	}
	
	/**Called when deflected by Genji - only on server*/
	public void onDeflect() { }

	/**Used to check for impacts*/
	public AxisAlignedBB getImpactBoundingBox() {
		return this.getEntityBoundingBox();
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if ((source.getTrueSource() == null || EntityHelper.shouldHit(source.getTrueSource(), this, false, source)) &&
				(source.getTrueSource() == null || EntityHelper.shouldHit(source.getTrueSource(), this, false, source)))
			return super.attackEntityFrom(source, amount);
		else 
			return false;
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
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return distance < 600;
	}

	@Override
	public Iterable<ItemStack> getArmorInventoryList() {
		return new ArrayList<ItemStack>();
	}

	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
		return ItemStack.EMPTY;
	}

	@Override
	protected float getSoundVolume() {
		return 0F;
	}

	@Override
	public EnumHandSide getPrimaryHand() {
		return EnumHandSide.RIGHT;
	}
	
	@Override
    public boolean doesEntityNotTriggerPressurePlate() {return true;}
	@Override
    public boolean canBeCollidedWith() {return true;}
    @Override
    public boolean canBePushed() {return false;}
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {return false;}
	@Override
	public boolean writeToNBTAtomically(NBTTagCompound compound) {return false;}
	@Override
	public void readFromNBT(NBTTagCompound compound) {}
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {return compound;}
	@Override
	public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {}
	@Override
	public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {}
	@Override
	public void fall(float distance, float damageMultiplier) {}

}