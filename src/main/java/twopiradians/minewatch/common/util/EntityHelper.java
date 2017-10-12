package twopiradians.minewatch.common.util;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityHanzoArrow;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityHelper {

	/**Copied from EntityThrowable*/
	public static RayTraceResult checkForImpact(Entity entityIn, Entity thrower, boolean friendly) {
		Vec3d vec3d = new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ);
		Vec3d vec3d1 = new Vec3d(entityIn.posX + entityIn.motionX, entityIn.posY + entityIn.motionY, entityIn.posZ + entityIn.motionZ);
		RayTraceResult raytraceresult = entityIn.world.rayTraceBlocks(vec3d, vec3d1, false, true, true);
		vec3d = new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ);
		vec3d1 = new Vec3d(entityIn.posX + entityIn.motionX, entityIn.posY + entityIn.motionY, entityIn.posZ + entityIn.motionZ);

		if (raytraceresult != null)
			vec3d1 = new Vec3d(raytraceresult.hitVec.xCoord, raytraceresult.hitVec.yCoord, raytraceresult.hitVec.zCoord);

		Entity entity = null;
		List<Entity> list = entityIn.world.getEntitiesWithinAABBExcludingEntity(entityIn, entityIn.getEntityBoundingBox().addCoord(entityIn.motionX, entityIn.motionY, entityIn.motionZ).expandXyz(1.0D));
		double d0 = 0.0D;

		for (int i = 0; i < list.size(); ++i) {
			Entity entity1 = (Entity)list.get(i);

			if (entity1.canBeCollidedWith() && shouldHit(thrower, entity1, friendly)) {
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
	public static Vec3d getShootingPos(EntityLivingBase entity, float pitch, float yaw, EnumHand hand) {//TODO improve
		Vec3d look = getLook(pitch, yaw);
		double x = entity.posX;
		double y = entity.posY + (double)entity.getEyeHeight() - 0.13D;
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
		else
			look = look.scale(0.25d);

		return new Vec3d(x+look.xCoord, y+look.yCoord, z+look.zCoord);
	}

	/**Aim the entity in the proper direction to be thrown/shot. Hitscan if metersPerSecond == -1*/
	public static void setAim(Entity entity, EntityLivingBase shooter, float pitch, float yaw, float metersPerSecond, float inaccuracy, EnumHand hand) {
		boolean friendly = isFriendly(entity);
		Vec3d vec = getShootingPos(shooter, pitch, yaw, hand);

		pitch += (entity.world.rand.nextFloat()-0.5f)*inaccuracy;
		yaw += (entity.world.rand.nextFloat()-0.5f)*inaccuracy;

		// get block that shooter is looking at
		double blockDistance = Double.MAX_VALUE;
		RayTraceResult blockTrace = EntityHelper.getMouseOverBlock(shooter, 512, pitch, yaw);
		if (blockTrace != null && blockTrace.typeOfHit == RayTraceResult.Type.BLOCK)
			blockDistance = Math.sqrt(vec.squareDistanceTo(blockTrace.hitVec.xCoord, blockTrace.hitVec.yCoord, blockTrace.hitVec.zCoord));
		// get entity that shooter is looking at
		double entityDistance = Double.MAX_VALUE;
		RayTraceResult entityTrace = EntityHelper.getMouseOverEntity(shooter, 512, friendly, pitch, yaw);
		if (entityTrace != null && entityTrace.typeOfHit == RayTraceResult.Type.ENTITY)
			entityDistance = Math.sqrt(vec.squareDistanceTo(entityTrace.hitVec.xCoord, entityTrace.hitVec.yCoord, entityTrace.hitVec.zCoord));

		double x, y, z;
		// block is closest
		if (blockDistance < entityDistance && blockDistance < Double.MAX_VALUE) {
			x = blockTrace.hitVec.xCoord - vec.xCoord;
			y = blockTrace.hitVec.yCoord - vec.yCoord - entity.height/2d;
			z = blockTrace.hitVec.zCoord - vec.zCoord;
		}
		// entity is closest
		else if (entityDistance < blockDistance && entityDistance < Double.MAX_VALUE) {
			x = entityTrace.hitVec.xCoord - vec.xCoord;
			y = entityTrace.hitVec.yCoord - vec.yCoord - entity.height/2d;
			z = entityTrace.hitVec.zCoord - vec.zCoord;
		}
		// not looking at block/entity
		else {
			Vec3d look = getLook(pitch, yaw).scale(metersPerSecond == -1 ? 100 : 1);
			x = look.xCoord;
			y = look.yCoord;
			z = look.zCoord;
		}

		entity.setPositionAndUpdate(vec.xCoord, vec.yCoord, vec.zCoord);

		// send velocity to server/client
		Vec3d scaledVelocity = new Vec3d(x, y, z);
		if (metersPerSecond != -1) // hitscan if -1
			scaledVelocity = scaledVelocity.normalize().scale(metersPerSecond/20d);
		DataParameter<Rotations> data = entity instanceof EntityMW ? EntityMW.VELOCITY : 
			entity instanceof EntityLivingBaseMW ? EntityLivingBaseMW.VELOCITY : 
				entity instanceof EntityHanzoArrow ? EntityHanzoArrow.VELOCITY : null;
		if (data != null)
			entity.getDataManager().set(data, new Rotations((float)scaledVelocity.xCoord, (float)scaledVelocity.yCoord, (float)scaledVelocity.zCoord));
		else
			System.out.println("Missing velocity parameter for: "+entity);

		// set rotations
		if (entity instanceof EntityHanzoArrow) {
			float f = MathHelper.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
			entity.rotationYaw = (float)(MathHelper.atan2(entity.motionX, entity.motionZ) * (180D / Math.PI));
			entity.rotationPitch = (float)(MathHelper.atan2(entity.motionY, (double)f) * (180D / Math.PI));
			entity.prevRotationYaw = entity.rotationYaw;
			entity.prevRotationPitch = entity.rotationPitch;
		}
		else {
			float f = MathHelper.sqrt(x * x + z * z);
			entity.rotationYaw = -(float)(MathHelper.atan2(x, z) * (180D / Math.PI));
			entity.rotationPitch = -(float)(MathHelper.atan2(y, (double)f) * (180D / Math.PI));
			entity.prevRotationYaw = entity.rotationYaw;
			entity.prevRotationPitch = entity.rotationPitch;
		}
	}

	/**Is an entity friendly - i.e. will it heal or damage*/
	public static boolean isFriendly(Entity entity) {
		return (entity instanceof EntityMW && ((EntityMW)entity).isFriendly) ||
				(entity instanceof EntityLivingBaseMW && ((EntityLivingBaseMW)entity).isFriendly);
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

	/**Should entity entity be hit by entity projectile.
	 * @param friendly - should this hit teammates or enemies?*/
	public static boolean shouldHit(Entity thrower, Entity entityHit, boolean friendly) {
		DamageSource source = getDamageSource(thrower);
		return source != null && shouldHit(thrower, entityHit, friendly, source);
	}

	/**Should entity entity be hit by entity projectile.
	 * @param friendly - should this hit teammates or enemies?*/
	public static boolean shouldHit(Entity thrower, Entity entityHit, boolean friendly, DamageSource source) {
		if (entityHit instanceof IThrowableEntity)
			return shouldHit(thrower, ((IThrowableEntity)entityHit).getThrower(), friendly, source);
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
		else if (shouldHit(projectile.getThrower(), entityHit, damage <= 0, source) && projectile.world.isRemote) {
			moveToEntityHit(projectile, entityHit);

			projectile.setDead();
		}

		return false;
	}
	
	/**Attempts do damage with falloff returns true if successful on server*/
	public static <T extends Entity & IThrowableEntity> boolean attemptFalloffImpact(T projectile, Entity shooter, Entity entityHit, boolean friendly, float minDamage, float maxDamage, float minFalloff, float maxFalloff) {
		if (EntityHelper.shouldHit(shooter, entityHit, friendly)) {
			EntityHelper.moveToEntityHit(projectile, entityHit);
			double distance = projectile.getPositionVector().distanceTo(new Vec3d(projectile.prevPosX, projectile.prevPosY, projectile.prevPosZ));
			if (distance <= maxFalloff && attemptImpact(projectile, entityHit, (float) (maxDamage-(maxDamage-minDamage) * MathHelper.clamp((distance-minFalloff) / (maxFalloff-minFalloff), 0, 1)), friendly)) 
				return true;
			projectile.setDead();
		}
		return false;
	}

	/**Move projectile to where it would collide with the entityHit - for fixing particles on impact*/
	public static void moveToEntityHit(Entity projectile, Entity entityHit) {
		Vec3d vec3d = new Vec3d(projectile.posX, projectile.posY, projectile.posZ);
		Vec3d vec3d1 = new Vec3d(projectile.posX + projectile.motionX, projectile.posY + projectile.motionY, projectile.posZ + projectile.motionZ);
		AxisAlignedBB aabb = entityHit.getEntityBoundingBox().expandXyz(0.3D);
		RayTraceResult ray =  aabb.calculateIntercept(vec3d, vec3d1);
		if (ray != null) {
			projectile.posX = ray.hitVec.xCoord;
			projectile.posY = ray.hitVec.yCoord;
			projectile.posZ = ray.hitVec.zCoord;
		}
	}

	public static boolean attemptDamage(Entity thrower, Entity entityHit, float damage, boolean neverKnockback) {
		DamageSource source = getDamageSource(thrower);
		return source != null && attemptDamage(thrower, entityHit, damage, neverKnockback, source);
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful
	 * If damage is negative, entity will be healed by that amount*/
	public static boolean attemptDamage(Entity thrower, Entity entityHit, float damage, boolean neverKnockback, DamageSource source) {
		if (shouldHit(thrower, entityHit, damage <= 0) && !thrower.world.isRemote) {
			// heal
			if (damage < 0 && entityHit instanceof EntityLivingBase) {
				((EntityLivingBase)entityHit).heal(Math.abs(damage*ItemMWWeapon.damageScale));
				return true;
			}
			// damage
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
	public static void spawnTrailParticles(Entity entity, double amountPerBlock, double random, int color, int colorFade, float scale, int maxAge, float alpha) {
		spawnTrailParticles(entity, amountPerBlock, random, 0, 0, 0, color, colorFade, scale, maxAge, alpha);
	}

	/**Spawn trail particles behind entity based on entity's prevPos and current motion*/
	public static void spawnTrailParticles(Entity entity, double amountPerBlock, double random, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge, float alpha) {
		int numParticles = MathHelper.ceil(amountPerBlock * Math.sqrt(entity.getDistanceSq(entity.prevPosX, entity.prevPosY, entity.prevPosZ)));//(int) ((Math.abs(entity.motionX)+Math.abs(entity.motionY)+Math.abs(entity.motionZ))*amountPerBlock);
		for (float i=0; i<numParticles; ++i) 
			Minewatch.proxy.spawnParticlesTrail(entity.world, 
					entity.posX+(entity.prevPosX-entity.posX)*i/numParticles+(entity.world.rand.nextDouble()-0.5d)*random, 
					entity.posY+(entity instanceof EntityHanzoArrow ? 0 : entity.height/2d)+(entity.prevPosY-entity.posY)*i/numParticles+(entity.world.rand.nextDouble()-0.5d)*random, 
					entity.posZ+(entity.prevPosZ-entity.posZ)*i/numParticles+(entity.world.rand.nextDouble()-0.5d)*random, 
					motionX, motionY, motionZ, color, colorFade, scale, maxAge, (i/numParticles), alpha);
	}

	/**Get block that shooter is looking at within distance blocks - modified from Entity#rayTrace*/
	@Nullable
	public static RayTraceResult getMouseOverBlock(EntityLivingBase shooter, double distance) {
		return getMouseOverBlock(shooter, distance, shooter.rotationPitch, shooter.rotationYawHead);
	}

	/**Get block that shooter is looking at within distance blocks - modified from Entity#rayTrace*/
	@Nullable
	public static RayTraceResult getMouseOverBlock(EntityLivingBase shooter, double distance, float pitch, float yawHead) {
		Vec3d vec3d = shooter.getPositionEyes(1);
		Vec3d vec3d1 = getLook(pitch, yawHead);
		Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * distance, vec3d1.yCoord * distance, vec3d1.zCoord * distance);
		return shooter.world.rayTraceBlocks(vec3d, vec3d2, false, true, true);
	}

	/**Get entity that shooter is looking at within distance blocks - modified from EntityRenderer#getMouseOver*/
	public static RayTraceResult getMouseOverEntity(EntityLivingBase shooter, int distance, boolean friendly) {
		return getMouseOverEntity(shooter, distance, friendly, shooter.rotationPitch, shooter.rotationYawHead);
	}

	/**Get entity that shooter is looking at within distance blocks - modified from EntityRenderer#getMouseOver*/
	public static RayTraceResult getMouseOverEntity(EntityLivingBase shooter, int distance, boolean friendly, float pitch, float yawHead) {
		RayTraceResult result = null;
		if (shooter != null) {
			double d0 = distance - 1;
			Vec3d vec3d = shooter.getPositionEyes(1);
			double d1 = d0;
			Vec3d vec3d1 = getLook(pitch, yawHead);
			Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0);
			List<Entity> list = shooter.world.getEntitiesInAABBexcluding(shooter, shooter.getEntityBoundingBox().addCoord(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0).expand(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
				public boolean apply(@Nullable Entity entity) {
					return entity != null && entity.canBeCollidedWith() && shouldHit(shooter, entity, friendly);
				}
			}));
			double d2 = d1;

			for (int j = 0; j < list.size(); ++j) {
				Entity entity1 = (Entity)list.get(j);
				AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expandXyz((double)entity1.getCollisionBorderSize());
				RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

				if (axisalignedbb.isVecInside(vec3d)) {
					if (d2 >= 0.0D) {
						d2 = 0.0D;
					}
				}
				else if (raytraceresult != null) {
					double d3 = vec3d.distanceTo(raytraceresult.hitVec);

					if (d3 < d2 || d2 == 0.0D) {
						if (entity1.getLowestRidingEntity() == shooter.getLowestRidingEntity() && !shooter.canRiderInteract()) {
							if (d2 == 0.0D) {
								result = new RayTraceResult(entity1, raytraceresult.hitVec);
							}
						}
						else {
							result = new RayTraceResult(entity1, raytraceresult.hitVec);
							d2 = d3;
						}
					}
				}
			}
		}

		if (result != null && result.entityHit instanceof EntityLivingBase)
			return result;
		else
			return null;
	}

	/**Copied from Entity#getvectorForRotation to make public*/
	public static Vec3d getLook(float pitch, float yawHead) {
		float f = MathHelper.cos(-yawHead * 0.017453292F - (float)Math.PI);
		float f1 = MathHelper.sin(-yawHead * 0.017453292F - (float)Math.PI);
		float f2 = -MathHelper.cos(-pitch * 0.017453292F);
		float f3 = MathHelper.sin(-pitch * 0.017453292F);
		return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
	}

}