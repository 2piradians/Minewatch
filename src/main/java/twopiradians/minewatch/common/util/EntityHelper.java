package twopiradians.minewatch.common.util;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityHanzoArrow;
import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.packet.SPacketSyncSpawningEntity;

public class EntityHelper {

	/**Update entity's position from the spawning packet*/
	public static void updateFromPacket(Entity entity) {
		SPacketSyncSpawningEntity packet = ModEntities.spawningEntityPacket;
		if (packet != null) {
			entity.rotationPitch = packet.pitch;
			entity.prevRotationPitch = packet.pitch;
			entity.rotationYaw = packet.yaw;
			entity.prevRotationYaw = packet.yaw;
			entity.motionX = packet.motionX;
			entity.motionY = packet.motionY;
			entity.motionZ = packet.motionZ;
			entity.posX = packet.posX;
			entity.posY = packet.posY;
			entity.posZ = packet.posZ;
			entity.prevPosX = packet.posX;
			entity.prevPosY = packet.posY;
			entity.prevPosZ = packet.posZ;
			ModEntities.spawningEntityUUID = null;
		}
	}

	/**Copied from EntityThrowable*/
	public static RayTraceResult checkForImpact(Entity entityIn, Entity thrower) {
		Vec3d vec3d = new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ);
		Vec3d vec3d1 = new Vec3d(entityIn.posX + entityIn.motionX, entityIn.posY + entityIn.motionY, entityIn.posZ + entityIn.motionZ);
		RayTraceResult raytraceresult = entityIn.world.rayTraceBlocks(vec3d, vec3d1);
		vec3d = new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ);
		vec3d1 = new Vec3d(entityIn.posX + entityIn.motionX, entityIn.posY + entityIn.motionY, entityIn.posZ + entityIn.motionZ);

		if (raytraceresult != null)
			vec3d1 = new Vec3d(raytraceresult.hitVec.xCoord, raytraceresult.hitVec.yCoord, raytraceresult.hitVec.zCoord);

		Entity entity = null;
		List<Entity> list = entityIn.world.getEntitiesWithinAABBExcludingEntity(entityIn, entityIn.getEntityBoundingBox().addCoord(entityIn.motionX, entityIn.motionY, entityIn.motionZ).expandXyz(1.0D));
		double d0 = 0.0D;

		for (int i = 0; i < list.size(); ++i) {
			Entity entity1 = (Entity)list.get(i);

			if (entity1.canBeCollidedWith() && shouldHit(thrower, entity1)) {
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

		return raytraceresult;
	}

	/**Get the position that an entity should be thrown/shot from*/
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

	/**Aim the entity in the proper direction to be thrown/shot*/
	public static void setAim(Entity entity, EntityLivingBase shooter, float pitch, float yaw, float velocity, float inaccuracy, float adjustment, EnumHand hand, boolean sendPacket) {
		double x = -Math.sin((yaw+Math.copySign(adjustment, hand == EnumHand.MAIN_HAND ? -yaw : yaw)) * Math.PI/180) * Math.cos(pitch * Math.PI/180);
		double y = -Math.sin(pitch * Math.PI/180);
		double z = Math.cos((yaw+Math.copySign(adjustment, hand == EnumHand.MAIN_HAND ? -yaw : yaw)) * Math.PI/180) * Math.cos(pitch * Math.PI/180);
		setThrowableHeading(entity, x, y, z, velocity, inaccuracy);
		entity.motionX += shooter.motionX;
		entity.motionZ += shooter.motionZ;
		Vec3d vec = getShootingPos(shooter, pitch, yaw, hand);
		entity.setPosition(vec.xCoord, vec.yCoord, vec.zCoord);

		// correct trajectory of fast entities (received in render class)
		if (!entity.world.isRemote && entity.ticksExisted == 0 && sendPacket) {
			Minewatch.network.sendToAll(new SPacketSyncSpawningEntity(entity.getPersistentID(), 
					entity.rotationPitch, entity.rotationYaw, entity.motionX, entity.motionY, entity.motionZ, 
					entity.posX, entity.posY, entity.posZ));
		}
	}

	/**Copied from EntityThrowable*/
	public static void setThrowableHeading(Entity entity, double x, double y, double z, float velocity, float inaccuracy) {
		float f = MathHelper.sqrt(x * x + y * y + z * z);
		x = x / (double)f;
		y = y / (double)f;
		z = z / (double)f;
		x = x + entity.world.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		y = y + entity.world.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		z = z + entity.world.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		x = x * (double)velocity;
		y = y * (double)velocity;
		z = z * (double)velocity;
		entity.motionX = x;
		entity.motionY = y;
		entity.motionZ = z;
		float f1 = MathHelper.sqrt(x * x + z * z);
		entity.rotationYaw = -(float)(MathHelper.atan2(x, z) * (180D / Math.PI));
		entity.rotationPitch = -(float)(MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
		entity.prevRotationYaw = entity.rotationYaw;
		entity.prevRotationPitch = entity.rotationPitch;
	}

	/**Should entity entity be hit by entity projectile*/
	public static boolean shouldHit(Entity thrower, Entity entityHit) {
		DamageSource source = getDamageSource(thrower);
		return source != null && shouldHit(thrower, entityHit, source);
	}

	/**Should entity entity be hit by entity projectile*/
	public static boolean shouldHit(Entity thrower, Entity entityHit, DamageSource source) {
		if (entityHit instanceof IThrowableEntity)
			return shouldHit(thrower, ((IThrowableEntity)entityHit).getThrower(), source);
		return ((entityHit instanceof EntityLivingBase && ((EntityLivingBase)entityHit).getHealth() > 0) || 
				entityHit instanceof EntityDragonPart) && thrower != null && entityHit != thrower &&
				!entityHit.isEntityInvulnerable(source);
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful on server
	 * If damage is negative, entity will be healed by that amount
	 * Uses default DamageSources (player/mob damage)*/
	public static <T extends Entity & IThrowableEntity> boolean attemptImpact(T projectile, Entity entityHit, float damage, boolean neverKnockback) {
		DamageSource source = getDamageSource(projectile.getThrower());
		return source != null && attemptImpact(projectile, entityHit, damage, neverKnockback, source);
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful on server
	 * If damage is negative, entity will be healed by that amount*/
	public static <T extends Entity & IThrowableEntity> boolean attemptImpact(T projectile, Entity entityHit, float damage, boolean neverKnockback, DamageSource source) {
		// attempt to damage entity
		if (attemptDamage(projectile.getThrower(), entityHit, damage, neverKnockback, source) && !projectile.world.isRemote) {
			projectile.setDead();
			return true;
		}
		// correct position of projectile - for fixing particles
		else if (shouldHit(projectile.getThrower(), entityHit, source) && projectile.world.isRemote) {
			Vec3d vec3d = new Vec3d(projectile.posX, projectile.posY, projectile.posZ);
			Vec3d vec3d1 = new Vec3d(projectile.posX + projectile.motionX, projectile.posY + projectile.motionY, projectile.posZ + projectile.motionZ);
			AxisAlignedBB aabb = entityHit.getEntityBoundingBox().expandXyz(0.3D);
			RayTraceResult ray =  aabb.calculateIntercept(vec3d, vec3d1);
			if (ray != null) {
				projectile.posX = ray.hitVec.xCoord;
				projectile.posY = ray.hitVec.yCoord;
				projectile.posZ = ray.hitVec.zCoord;
			}

			projectile.setDead();
		}

		return false;
	}

	public static boolean attemptDamage(Entity thrower, Entity entityHit, float damage, boolean neverKnockback) {
		DamageSource source = getDamageSource(thrower);
		return source != null && attemptDamage(thrower, entityHit, damage, neverKnockback, source);
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful
	 * If damage is negative, entity will be healed by that amount*/
	public static boolean attemptDamage(Entity thrower, Entity entityHit, float damage, boolean neverKnockback, DamageSource source) {
		if (shouldHit(thrower, entityHit) && !thrower.world.isRemote) {
			// heal
			if (damage < 0 && entityHit instanceof EntityLivingBase) {
				((EntityLivingBase)entityHit).heal(Math.abs(damage*ItemMWWeapon.damageScale));
				return true;
			}
			// damage
			else if (damage >= 0) {
				boolean damaged = false;
				if (!Config.projectilesCauseKnockback || neverKnockback && entityHit instanceof EntityLivingBase) {
					double prev = ((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue();
					((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
					damaged = entityHit.attackEntityFrom(source, damage*ItemMWWeapon.damageScale);
					((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(prev);
				}
				else
					damaged = entityHit.attackEntityFrom(source, damage*ItemMWWeapon.damageScale);

				return damaged;
			}
		}

		return false;
	}

	/**Get damage source for this entity (player/mob damage)*/
	public static DamageSource getDamageSource(Entity thrower) {
		return thrower instanceof EntityPlayer ? DamageSource.causePlayerDamage((EntityPlayer) thrower) :
			thrower instanceof EntityLivingBase ? DamageSource.causeMobDamage((EntityLivingBase) thrower) : null;
	}

	/**Spawn trail particles behind entity based on entity's prevPos and current motion*/
	public static void spawnTrailParticles(Entity entity, double amount, double random, int color, int colorFade, float scale, int maxAge, float alpha) {
		spawnTrailParticles(entity, amount, random, 0, 0, 0, color, colorFade, scale, maxAge, alpha);
	}
	
	/**Spawn trail particles behind entity based on entity's prevPos and current motion*/
	public static void spawnTrailParticles(Entity entity, double amount, double random, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge, float alpha) {
		int numParticles = (int) ((Math.abs(entity.motionX)+Math.abs(entity.motionY)+Math.abs(entity.motionZ))*amount);
		for (int i=0; i<numParticles; ++i)
			Minewatch.proxy.spawnParticlesTrail(entity.world, 
					entity.posX+(entity.prevPosX-entity.posX)*i/numParticles+(entity.world.rand.nextDouble()-0.5d)*0.05d, 
					entity.posY+(entity instanceof EntityHanzoArrow ? 0 : entity.height/2d)+(entity.prevPosY-entity.posY)*i/numParticles+(entity.world.rand.nextDouble()-0.5d)*0.05d, 
					entity.posZ+(entity.prevPosZ-entity.posZ)*i/numParticles+(entity.world.rand.nextDouble()-0.5d)*0.05d, 
					motionX, motionY, motionZ, color, colorFade, scale, maxAge, alpha);
	}

}