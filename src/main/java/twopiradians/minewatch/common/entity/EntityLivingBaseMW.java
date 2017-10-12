package twopiradians.minewatch.common.entity;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.util.EntityHelper;

public abstract class EntityLivingBaseMW extends EntityLivingBase implements IThrowableEntity {

    public static final DataParameter<Rotations> VELOCITY = EntityDataManager.<Rotations>createKey(EntityLivingBaseMW.class, DataSerializers.ROTATIONS);
	public boolean notDeflectible;
	protected int lifetime;
	private EntityLivingBase thrower;
	protected boolean skipImpact;
	public boolean isFriendly;

	public EntityLivingBaseMW(World worldIn) {
		this(worldIn, null);
	}

	public EntityLivingBaseMW(World worldIn, @Nullable EntityLivingBase throwerIn) {
		super(worldIn);
		if (throwerIn != null) {
			this.thrower = throwerIn;
			this.setPosition(throwerIn.posX, throwerIn.posY + (double)throwerIn.getEyeHeight() - 0.1D, throwerIn.posZ);
		}
	}
	
	@Override
    public void notifyDataManagerChange(DataParameter<?> key) {
		if (key == VELOCITY) {
			this.motionX = this.dataManager.get(VELOCITY).getX();
			this.motionY = this.dataManager.get(VELOCITY).getY();
			this.motionZ = this.dataManager.get(VELOCITY).getZ();
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
		}
    }
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(VELOCITY, new Rotations(0, 0, 0));
	}

	@Override
	public void onUpdate() {	
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;

		// move
		RayTraceResult result = this.skipImpact ? null : EntityHelper.checkForImpact(this, this.getThrower(), this.isFriendly);
		if (result != null)
			this.onImpact(result);
		else {
			if (this.hasNoGravity())
				this.setPosition(this.posX+this.motionX, this.posY+this.motionY, this.posZ+this.motionZ);
			else
				this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		}

		if (this.hurtTime > 0)
			--this.hurtTime;
		if (this.hurtResistantTime > 0)
			--this.hurtResistantTime;
		if (this.recentlyHit > 0)
			--this.recentlyHit;

		if (!this.world.isRemote && ((this.ticksExisted > lifetime && lifetime > 0) || this.getHealth() <= 0))
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
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.getSourceOfDamage() == null || EntityHelper.shouldHit(source.getSourceOfDamage(), this, false, source))
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
	public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {}
	
	@Override
	public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {}

	@Override
	public void fall(float distance, float damageMultiplier) {}


}