package twopiradians.minewatch.common.entity;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.util.EntityHelper;

public abstract class EntityMW extends Entity implements IThrowableEntity {

	public static final DataParameter<Rotations> VELOCITY = EntityDataManager.<Rotations>createKey(EntityMW.class, DataSerializers.ROTATIONS);
	public boolean notDeflectible;
	public int lifetime;
	private EntityLivingBase thrower;
	protected boolean skipImpact;
	public boolean isFriendly;

	public EntityMW(World worldIn) {
		this(worldIn, null);
	}

	public EntityMW(World worldIn, @Nullable EntityLivingBase throwerIn) {
		super(worldIn);
		if (throwerIn != null) {
			this.thrower = throwerIn;
			this.setPosition(throwerIn.posX, throwerIn.posY + (double)throwerIn.getEyeHeight() - 0.1D, throwerIn.posZ);
		}
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(VELOCITY, new Rotations(0, 0, 0));
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (key == VELOCITY) {
			this.motionX = this.dataManager.get(VELOCITY).getX();
			this.motionY = this.dataManager.get(VELOCITY).getY();
			this.motionZ = this.dataManager.get(VELOCITY).getZ();
			EntityHelper.setRotations(this);
		}
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX; 
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;

		// move if not collided
		RayTraceResult result = this.skipImpact ? null : EntityHelper.checkForImpact(this, this.getThrower(), this.isFriendly);
		if (result != null)
			this.onImpact(result);
		else {
			if (this.hasNoGravity())
				this.setPosition(this.posX+this.motionX, this.posY+this.motionY, this.posZ+this.motionZ);
			else
				this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		}

		if (!this.world.isRemote && this.ticksExisted > lifetime && lifetime > 0)
			this.setDead();

		this.firstUpdate = false;
	}

	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			IBlockState state = this.world.getBlockState(result.getBlockPos());
			if (!state.getBlock().isPassable(this.world, result.getBlockPos()) && state.getMaterial() != Material.AIR) {
				this.setPosition(result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord);
				this.setDead();
			}
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