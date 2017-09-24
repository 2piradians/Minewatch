package twopiradians.minewatch.common.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.packet.SPacketSyncSpawningEntity;

public abstract class EntityMWThrowable extends EntityThrowable implements IThrowableEntity {

	protected int lifetime;
	private EntityLivingBase thrower;

	private boolean lockedDirection;
	private float pitch;
	private float yaw;
	private double xMotion;
	private double yMotion;
	private double zMotion;

	public EntityMWThrowable(World worldIn) {
		super(worldIn);
	}

	public EntityMWThrowable(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.thrower = throwerIn;
		this.ignoreEntity = this;
	}

	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	public void updateFromPacket() {
		SPacketSyncSpawningEntity packet = ModEntities.spawningEntityPacket;
		if (packet != null) {
			this.rotationPitch = packet.pitch;
			this.prevRotationPitch = packet.pitch;
			this.rotationYaw = packet.yaw;
			this.prevRotationYaw = packet.yaw;
			this.motionX = packet.motionX;
			this.motionY = packet.motionY;
			this.motionZ = packet.motionZ;
			this.posX = packet.posX;
			this.posY = packet.posY;
			this.posZ = packet.posZ;
			this.prevPosX = packet.posX;
			this.prevPosY = packet.posY;
			this.prevPosZ = packet.posZ;
			ModEntities.spawningEntityUUID = null;
		}
	}

	@Override
	public void onUpdate() {	
		if (this.ticksExisted == 1 && this.getPersistentID().equals(ModEntities.spawningEntityUUID))
			this.updateFromPacket();

		if (!this.lockedDirection) {
			float f = MathHelper.sqrt((float) (this.motionX * this.motionX + this.motionZ * this.motionZ));
			this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
			this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * (180D / Math.PI));
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
			this.pitch = this.rotationPitch;
			this.yaw = this.rotationYaw;
			this.xMotion = this.motionX;
			this.yMotion = this.motionY;
			this.zMotion = this.motionZ;
			this.lockedDirection = true;
		}

		super.onUpdate();

		this.rotationPitch = this.pitch;
		this.prevRotationPitch = this.pitch;
		this.rotationYaw = this.yaw;
		this.prevRotationYaw = this.yaw;
		this.motionX = this.xMotion;
		this.motionY = this.yMotion;
		this.motionZ = this.zMotion;

		if (!this.world.isRemote && this.ticksExisted > lifetime && lifetime > 0)
			this.setDead();
	}

	public static Vec3d getShootingPos(EntityLivingBase entity, float pitch, float yaw, EnumHand hand) {
		Vec3d look = entity.getLookVec();
		double x = entity.posX;
		double y = entity.posY + (double)entity.getEyeHeight() - 0.10000000149011612D;
		double z = entity.posZ;

		if (hand == EnumHand.MAIN_HAND) {
			look = look.rotateYaw(-0.5f);
			if (Math.abs(pitch) >= 20 && Math.abs(pitch) < 50) {
				x = x - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/8;
				y = y + Math.sin(pitch*Math.PI/180)/8;
				z = z - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/8;
			}
			else if (Math.abs(pitch) >= 50 && Math.abs(pitch) < 70) {
				x = x - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/8;
				y = y + Math.sin(pitch*Math.PI/180)/20 - (pitch < 0 ? 0.2d : 0);
				z = z - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/8;
			}
			else if (Math.abs(pitch) >= 70) {
				x = x - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/4;
				y = y + Math.sin(pitch*Math.PI/180)/30 - (pitch < 0 ? 0.2d : -0.2d);
				z = z - Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/4;
			}
		}
		else if (hand == EnumHand.OFF_HAND) {
			look = look.rotateYaw(0.5f);
			if (Math.abs(pitch) >= 20 && Math.abs(pitch) < 50) {
				x = x + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/8;
				y = y + Math.sin(pitch*Math.PI/180)/8;
				z = z + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/8;
			}
			else if (Math.abs(pitch) >= 50 && Math.abs(pitch) < 70) {
				x = x + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/8;
				y = y + Math.sin(pitch*Math.PI/180)/20 - (pitch < 0 ? 0.2d : 0);
				z = z + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/8;
			}
			else if (Math.abs(pitch) >= 70) {
				x = x + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.cos(yaw*Math.PI/180)/4;
				y = y + Math.sin(pitch*Math.PI/180)/30 - (pitch < 0 ? 0.2d : -0.2d);
				z = z + Math.sin(Math.abs(pitch)*Math.PI/180)*Math.sin(yaw*Math.PI/180)/4;
			}
		}

		return new Vec3d(x+look.xCoord, y+look.yCoord, z+look.zCoord);
	}

	public void setAim(EntityPlayer shooter, float pitch, float yaw, float velocity, float inaccuracy, float adjustment, EnumHand hand, boolean sendPacket) {
		double x = -Math.sin((yaw+Math.copySign(adjustment, hand == EnumHand.MAIN_HAND ? -yaw : yaw)) * Math.PI/180) * Math.cos(pitch * Math.PI/180);
		double y = -Math.sin(pitch * Math.PI/180);
		double z = Math.cos((yaw+Math.copySign(adjustment, hand == EnumHand.MAIN_HAND ? -yaw : yaw)) * Math.PI/180) * Math.cos(pitch * Math.PI/180);
		this.setThrowableHeading(x, y, z, velocity, inaccuracy);
		this.motionX += shooter.motionX;
		this.motionZ += shooter.motionZ;
		Vec3d vec = EntityMWThrowable.getShootingPos(shooter, pitch, yaw, hand);
		this.setPosition(vec.xCoord, vec.yCoord, vec.zCoord);

		// correct trajectory of fast entities (received in render class)
		if (!this.world.isRemote && this.ticksExisted == 0 && sendPacket) {
			Minewatch.network.sendToAll(
					new SPacketSyncSpawningEntity(this.getPersistentID(), this.rotationPitch, this.rotationYaw, this.motionX, this.motionY, this.motionZ, this.posX, this.posY, this.posZ));
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			Block block = this.world.getBlockState(result.getBlockPos()).getBlock();

			if (!block.isPassable(this.world, result.getBlockPos())) 
				this.setDead();
		}
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

	/**Should this entity be hit by this projectile*/
	public boolean shouldHit(Entity entityHit) {
		return this.getThrower() instanceof EntityPlayer && 
				this.shouldHit(entityHit, DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()));
	}

	/**Should this entity be hit by this projectile*/
	public boolean shouldHit(Entity entityHit, DamageSource source) {
		return entityHit instanceof EntityLivingBase && this.getThrower() instanceof EntityPlayer && 
				entityHit != this.getThrower() && ((EntityLivingBase)entityHit).getHealth() > 0 &&
				!entityHit.isEntityInvulnerable(source);
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful on server
	 *  If damage is negative, entity will be healed by that amount*/
	public boolean attemptImpact(Entity entityHit, float damage, boolean neverKnockback) {
		return this.getThrower() instanceof EntityPlayer && 
				this.attemptImpact(entityHit, damage, neverKnockback, DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()));
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful on server
	 * If damage is negative, entity will be healed by that amount*/
	public boolean attemptImpact(Entity entityHit, float damage, boolean neverKnockback, DamageSource source) {
		// should entity be hit
		if (this.shouldHit(entityHit, source)) {
			if (!this.world.isRemote && this.getThrower() instanceof EntityPlayerMP) {
				// heal
				if (damage < 0)
					((EntityLivingBase)entityHit).heal(Math.abs(damage*ItemMWWeapon.damageScale));
				else {
					boolean damaged = false;
					if (!Config.projectilesCauseKnockback || neverKnockback) {
						double prev = ((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue();
						((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
						damaged = ((EntityLivingBase)entityHit).attackEntityFrom(source, damage*ItemMWWeapon.damageScale);
						((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(prev);
					}
					else
						damaged = ((EntityLivingBase)entityHit).attackEntityFrom(source, damage*ItemMWWeapon.damageScale);

					if (!damaged)
						return false;
				}
				this.setDead();
				return true;
			}

			// correct position of projectile - for fixing particles
			Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			AxisAlignedBB aabb = entityHit.getEntityBoundingBox().expandXyz(0.3D);
			RayTraceResult ray =  aabb.calculateIntercept(vec3d, vec3d1);
			if (ray != null) {
				this.posX = ray.hitVec.xCoord;
				this.posY = ray.hitVec.yCoord;
				this.posZ = ray.hitVec.zCoord;
				this.motionX = this.motionY = this.motionZ = 0;
			}

			this.setDead();
		}


		return false;
	}

}