package twopiradians.minewatch.common.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.packet.SPacketSyncSpawningEntity;

public abstract class EntityMWThrowable extends Entity implements IThrowableEntity {

	public boolean notDeflectible;
	protected int lifetime;
	private EntityLivingBase thrower;

	public EntityMWThrowable(World worldIn) {
		this(worldIn, null);
	}

	public EntityMWThrowable(World worldIn, @Nullable EntityLivingBase throwerIn) {
		super(worldIn);
		if (throwerIn != null) {
			this.thrower = throwerIn;
			this.setPosition(throwerIn.posX, throwerIn.posY + (double)throwerIn.getEyeHeight() - 0.1D, throwerIn.posZ);
		}
	}

	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return distance < 600;
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
		if (this.firstUpdate && this.world.isRemote && this.getPersistentID().equals(ModEntities.spawningEntityUUID))
			this.updateFromPacket();

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;

		// move if not collided
		if (!this.checkForImpact()) {
			if (this.hasNoGravity())
				this.setPosition(this.posX+this.motionX, this.posY+this.motionY, this.posZ+this.motionZ);
			else
				this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		}

		if (!this.world.isRemote && this.ticksExisted > lifetime && lifetime > 0)
			this.setDead();

		this.firstUpdate = false;
	}

	/**Copied from EntityThrowable*/
	private boolean checkForImpact() {
		Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d, vec3d1);
		vec3d = new Vec3d(this.posX, this.posY, this.posZ);
		vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

		if (raytraceresult != null)
			vec3d1 = new Vec3d(raytraceresult.hitVec.xCoord, raytraceresult.hitVec.yCoord, raytraceresult.hitVec.zCoord);

		Entity entity = null;
		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expandXyz(1.0D));
		double d0 = 0.0D;

		for (int i = 0; i < list.size(); ++i) {
			Entity entity1 = (Entity)list.get(i);

			if (entity1.canBeCollidedWith() && this.shouldHit(entity1)) {
				AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expandXyz(0.30000001192092896D);
				RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);

				if (raytraceresult1 != null) {
					double d1 = vec3d.squareDistanceTo(raytraceresult1.hitVec);

					if (d1 < d0 || d0 == 0.0D) {
						entity = entity1;
						d0 = d1;
					}
				}
			}
		}

		if (entity != null)
			raytraceresult = new RayTraceResult(entity);

		if (raytraceresult != null) 
			this.onImpact(raytraceresult);

		return raytraceresult != null;
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
			Minewatch.network.sendToAll(new SPacketSyncSpawningEntity(this.getPersistentID(), 
					this.rotationPitch, this.rotationYaw, this.motionX, this.motionY, this.motionZ, 
					this.posX, this.posY, this.posZ));
		}
	}

	/**Copied from EntityThrowable*/
	public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
		float f = MathHelper.sqrt(x * x + y * y + z * z);
		x = x / (double)f;
		y = y / (double)f;
		z = z / (double)f;
		x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		x = x * (double)velocity;
		y = y * (double)velocity;
		z = z * (double)velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f1 = MathHelper.sqrt(x * x + z * z);
		this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
		this.rotationPitch = (float)(MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
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
		return ((entityHit instanceof EntityLivingBase && ((EntityLivingBase)entityHit).getHealth() > 0) || 
				entityHit instanceof EntityDragonPart) && this.getThrower() instanceof EntityPlayer && 
				entityHit != this.getThrower() &&
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
				if (damage < 0 && entityHit instanceof EntityLivingBase)
					((EntityLivingBase)entityHit).heal(Math.abs(damage*ItemMWWeapon.damageScale));
				else if (damage > 0) {
					boolean damaged = false;
					if (!Config.projectilesCauseKnockback || neverKnockback && entityHit instanceof EntityLivingBase) {
						double prev = ((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue();
						((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
						damaged = entityHit.attackEntityFrom(source, damage*ItemMWWeapon.damageScale);
						((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(prev);
					}
					else
						damaged = entityHit.attackEntityFrom(source, damage*ItemMWWeapon.damageScale);

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
			}

			this.setDead();
		}

		return false;
	}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {}

}