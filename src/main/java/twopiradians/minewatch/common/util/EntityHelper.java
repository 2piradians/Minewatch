package twopiradians.minewatch.common.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.hero.EntityLucio;
import twopiradians.minewatch.common.entity.projectile.EntityHanzoArrow;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityHelper {

	/**Copied from EntityThrowable*/
	public static ArrayList<RayTraceResult> checkForImpact(Entity entityIn) {
		ArrayList<RayTraceResult> results = new ArrayList<RayTraceResult>();
		Vec3d vec3d = new Vec3d(entityIn.posX, entityIn.posY+entityIn.height/2d, entityIn.posZ);
		Vec3d vec3d1 = new Vec3d(entityIn.posX + entityIn.motionX, entityIn.posY+entityIn.height/2d + entityIn.motionY, entityIn.posZ + entityIn.motionZ);
		RayTraceResult result = entityIn.world.rayTraceBlocks(vec3d, vec3d1, false, true, true);
		if (result != null)
			results.add(result);

		// if entityIn moving more than its collision box per tick - check for lookVec intercept, otherwise just check collision boxes
		boolean fast = Math.abs(entityIn.motionX) > entityIn.width || Math.abs(entityIn.motionY) > entityIn.height || Math.abs(entityIn.motionZ) > entityIn.width;
		AxisAlignedBB aabb = entityIn.getEntityBoundingBox();
		if (fast)
			aabb = aabb.addCoord(entityIn.motionX, entityIn.motionY, entityIn.motionZ);
		// list of entities in (possibly very big) area
		List<Entity> list = entityIn.world.getEntitiesWithinAABBExcludingEntity(entityIn, aabb);
		for (int i = 0; i < list.size(); ++i) {
			Entity entity = list.get(i);
			if (entity.canBeCollidedWith() || (entity instanceof EntityLivingBaseMW && shouldHit(entityIn, entity, false))) {
				aabb = entity.getEntityBoundingBox();
				if (!fast || aabb.calculateIntercept(vec3d, vec3d1) != null) 
					results.add(new RayTraceResult(entity));
			}
		}

		return results;
	}

	/**Get the position that an entity should be thrown/shot from*/ 
	public static Vec3d getShootingPos(EntityLivingBase shooter, float pitch, float yaw, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust) {
		// adjust based on hand
		if (hand == EnumHand.OFF_HAND)
			horizontalAdjust *= -1;

		// adjust based on fov (only client-side: mainly for muzzle particles and Mercy beam)
		if (shooter.world.isRemote && shooter instanceof EntityPlayer) {
			float fovSettings = KeyBind.FOV.getFOV((EntityPlayer)shooter)-70f;
			float fov = getFovModifier((EntityPlayer)shooter)-1+fovSettings;
			horizontalAdjust += fov / 80f;
			verticalAdjust += fov / 5f;
		}

		Vec3d lookVec = getLook(pitch+verticalAdjust, yaw);
		Vec3d horizontalVec = new Vec3d(-lookVec.zCoord, 0, lookVec.xCoord).normalize().scale(horizontalAdjust);
		if (pitch+verticalAdjust > 90)
			horizontalVec = horizontalVec.scale(-1);
		Vec3d posVec = new Vec3d(shooter.lastTickPosX+(shooter.posX-shooter.lastTickPosX)*Minewatch.proxy.getRenderPartialTicks(), shooter.lastTickPosY+(shooter.posY-shooter.lastTickPosY)*Minewatch.proxy.getRenderPartialTicks(), shooter.lastTickPosZ+(shooter.posZ-shooter.lastTickPosZ)*Minewatch.proxy.getRenderPartialTicks());
		return posVec.add(lookVec).add(horizontalVec).addVector(0, shooter.getEyeHeight(), 0);
	}

	/**Aim the entity in the proper direction to be thrown/shot. Hitscan if metersPerSecond == -1*/
	public static void setAim(Entity entity, EntityLivingBase shooter, float pitch, float yaw, float metersPerSecond, float inaccuracy, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust) {
		boolean friendly = isFriendly(entity);
		Vec3d vec = getShootingPos(shooter, pitch, yaw, hand, verticalAdjust, horizontalAdjust);

		if (shooter instanceof EntityHero)
			inaccuracy = (float) (Math.max(0.5f, inaccuracy) * Config.mobInaccuracy);
		pitch += (entity.world.rand.nextFloat()-0.5f)*inaccuracy;
		yaw += (entity.world.rand.nextFloat()-0.5f)*inaccuracy;

		// get block that shooter is looking at
		double blockDistance = Double.MAX_VALUE;
		RayTraceResult blockTrace = shooter instanceof EntityHero ? null : EntityHelper.getMouseOverBlock(shooter, 512, pitch, yaw);
		if (blockTrace != null && blockTrace.typeOfHit == RayTraceResult.Type.BLOCK)
			blockDistance = Math.sqrt(vec.squareDistanceTo(blockTrace.hitVec.xCoord, blockTrace.hitVec.yCoord, blockTrace.hitVec.zCoord));
		// get entity that shooter is looking at
		double entityDistance = Double.MAX_VALUE;
		RayTraceResult entityTrace = EntityHelper.getMouseOverEntity(shooter, shooter instanceof EntityHero ? 64 : 512, friendly, pitch, yaw);
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
		else if (entityTrace != null && entityTrace.entityHit != null) // scale velocity by hit entity width (for leeway since lifetime is 1)
			scaledVelocity = scaledVelocity.add(scaledVelocity.normalize().scale(entityTrace.entityHit.width));

		DataParameter<Rotations> data = getVelocityParameter(entity);
		if (data != null)
			entity.getDataManager().set(data, new Rotations((float)scaledVelocity.xCoord, (float)scaledVelocity.yCoord, (float)scaledVelocity.zCoord));
		else
			System.out.println("Missing velocity parameter for: "+entity);
	}

	/**Get DataParemeter for setting velocity for entity*/
	public static DataParameter<Rotations> getVelocityParameter(Entity entity) {
		return entity instanceof EntityMW ? EntityMW.VELOCITY : 
			entity instanceof EntityLivingBaseMW ? EntityLivingBaseMW.VELOCITY : 
				entity instanceof EntityHanzoArrow ? EntityHanzoArrow.VELOCITY : null;
	}

	/**Set rotations based on entity's motion*/
	public static void setRotations(Entity entity) {
		Vec3d vec = new Vec3d(entity.motionX, entity.motionY, entity.motionZ).normalize();
		float f = MathHelper.sqrt(vec.xCoord * vec.xCoord + vec.zCoord * vec.zCoord);
		entity.rotationYaw = (float)(MathHelper.atan2(vec.xCoord, vec.zCoord) * (180D / Math.PI));
		entity.rotationPitch = (float)(MathHelper.atan2(vec.yCoord, (double)f) * (180D / Math.PI));
		if (!(entity instanceof EntityArrow)) {
			entity.rotationYaw *= -1;
			entity.rotationPitch *= -1;
		}
		entity.prevRotationYaw = entity.rotationYaw;
		entity.prevRotationPitch = entity.rotationPitch;
	}

	/**Is an entity friendly - i.e. will it heal or damage*/
	public static boolean isFriendly(Entity entity) {
		return (entity instanceof EntityMW && ((EntityMW)entity).isFriendly) ||
				(entity instanceof EntityLivingBaseMW && ((EntityLivingBaseMW)entity).isFriendly);
	}

	/**Get actual thrower from entity if IThrowableEntity*/
	public static Entity getThrower(Entity entity) {
		if (entity instanceof IThrowableEntity)
			return ((IThrowableEntity)entity).getThrower();
		return entity;
	}

	/**Should entity entity be hit by entity projectile.
	 * @param friendly - should this hit teammates or enemies?*/
	public static boolean shouldHit(Entity thrower, Entity entityHit, boolean friendly) {
		Entity actualThrower = getThrower(thrower);
		DamageSource source = actualThrower instanceof EntityLivingBase ? DamageSource.causeIndirectDamage(thrower, (EntityLivingBase) actualThrower) : null;
		return source != null && shouldHit(actualThrower, entityHit, friendly, source);
	}

	/**Should entity entity be hit by entity projectile.
	 * @param friendly - should this hit teammates or enemies?*/
	public static boolean shouldHit(Entity thrower, Entity entityHit, boolean friendly, DamageSource source) {
		// prevent hitting EntityMW
		if (entityHit instanceof EntityMW)
			return false;
		// prevent healing EntityLivingBaseMW
		if (entityHit instanceof EntityLivingBaseMW && friendly)
			return false;
		// can't hit creative players
		if (entityHit instanceof EntityPlayer && ((EntityPlayer)entityHit).isCreative())
			return false;
		thrower = getThrower(thrower);
		entityHit = getThrower(entityHit);
		return shouldTarget(thrower, entityHit, friendly) && 
				((entityHit instanceof EntityLivingBase && ((EntityLivingBase)entityHit).getHealth() > 0) || 
						entityHit instanceof EntityDragonPart) && !entityHit.isEntityInvulnerable(source); 
	}

	/**Should target be hit by entity / should entity render red*/
	public static boolean shouldTarget(Entity entity, @Nullable Entity target, boolean friendly) {
		if (target == null)
			target = Minewatch.proxy.getClientPlayer();
		entity = getThrower(entity);
		target = getThrower(target);
		// prevent EntityHero attacking/targeting things it shouldn't
		if (entity instanceof EntityHero && target != null &&
				((target instanceof EntityPlayer && Config.mobTargetPlayers == friendly) ||
						(target.isCreatureType(EnumCreatureType.MONSTER, false) && Config.mobTargetHostiles == friendly && !(target instanceof EntityPlayer) && !(target instanceof EntityHero)) ||
						(!target.isCreatureType(EnumCreatureType.MONSTER, false) && Config.mobTargetPassives == friendly && !(target instanceof EntityPlayer) && !(target instanceof EntityHero)) ||
						(target instanceof EntityHero && Config.mobTargetHeroes == friendly)))
			return false;
		// prevent EntityHero healing other than healTarget (except Lucio)
		if (friendly && entity instanceof EntityHero && !(entity instanceof EntityLucio) && 
				((EntityHero)entity).healTarget != null && ((EntityHero)entity).healTarget != target)
			return false;
		// prevent healing attacking enemy
		if (friendly && target instanceof EntityLiving && 
				((EntityLiving)target).getAttackTarget() == entity)
			return false;
		// prevent healing mobs with config option disabled
		if (!Config.healMobs && friendly && !(target instanceof EntityPlayer || target instanceof EntityHero))
			return false;
		return entity != null && target != null && (target != entity || friendly) &&
				(entity.getTeam() == null || target.getTeam() == null || 
				entity.isOnSameTeam(target) == friendly);
	}

	/**Attempts do damage with falloff returns true if successful on server*/
	public static <T extends Entity & IThrowableEntity> boolean attemptFalloffImpact(T projectile, Entity shooter, Entity entityHit, boolean friendly, float minDamage, float maxDamage, float minFalloff, float maxFalloff) {
		if (EntityHelper.shouldHit(shooter, entityHit, friendly)) {
			double distance = projectile.getPositionVector().distanceTo(new Vec3d(projectile.prevPosX, projectile.prevPosY, projectile.prevPosZ));
			if (distance <= maxFalloff && attemptDamage(projectile, entityHit, (float) (maxDamage-(maxDamage-minDamage) * MathHelper.clamp((distance-minFalloff) / (maxFalloff-minFalloff), 0, 1)), friendly)) 
				return true;
		}
		return false;
	}

	/**Move projectile to where it would collide with the ray - kills if successful*/
	public static void moveToHitPosition(Entity projectile, RayTraceResult result) {
		moveToHitPosition(projectile, result, true);
	}

	/**Move projectile to where it would collide with the ray - kills if successful*/
	public static void moveToHitPosition(Entity projectile, RayTraceResult result, boolean kill) {
		if (projectile != null && result != null) {
			// move to collide with block
			if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
				IBlockState state = projectile.world.getBlockState(result.getBlockPos());
				if (!state.getBlock().isPassable(projectile.world, result.getBlockPos()) && state.getMaterial() != Material.AIR) {
					projectile.setPosition(result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord);
					if (kill)
						projectile.setDead();
				}
			}
			// move to collide with entity
			else if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
				Vec3d vec3d = new Vec3d(projectile.posX, projectile.posY, projectile.posZ);
				Vec3d vec3d1 = new Vec3d(projectile.posX + projectile.motionX, projectile.posY + projectile.motionY, projectile.posZ + projectile.motionZ);
				AxisAlignedBB aabb = result.entityHit.getEntityBoundingBox().expandXyz(0.05D);
				RayTraceResult ray = aabb.calculateIntercept(vec3d, vec3d1);
				if (ray != null) {
					projectile.posX = ray.hitVec.xCoord;
					projectile.posY = ray.hitVec.yCoord;
					projectile.posZ = ray.hitVec.zCoord; 
				}

				// change prevPos to deflect pos so particles follow properly
				if (TickHandler.hasHandler(result.entityHit, Identifier.GENJI_DEFLECT) && 
						result.entityHit instanceof EntityLivingBase && 
						ItemGenjiShuriken.canDeflect((EntityLivingBase) result.entityHit, projectile)) {
					if (projectile instanceof EntityMW)
						projectile.getDataManager().set(EntityMW.POSITION, new Rotations((float)projectile.posX, (float)projectile.posY, (float)projectile.posZ));
				}
				// don't kill if deflecting
				else if (kill)
					projectile.setDead();
			}
		}
	}

	public static boolean attemptDamage(Entity thrower, Entity entityHit, float damage, boolean neverKnockback) {
		return attemptDamage(thrower, entityHit, damage, neverKnockback, true);
	}

	public static boolean attemptDamage(Entity thrower, Entity entityHit, float damage, boolean neverKnockback, boolean ignoreHurtResist) {
		Entity actualThrower = getThrower(thrower);
		DamageSource source = actualThrower instanceof EntityLivingBase ? DamageSource.causeIndirectDamage(thrower, (EntityLivingBase) actualThrower) : null;
		return source != null && attemptDamage(actualThrower, entityHit, damage, neverKnockback, ignoreHurtResist, source);	
	}

	public static boolean attemptDamage(Entity thrower, Entity entityHit, float damage, boolean neverKnockback, DamageSource source) {
		return attemptDamage(thrower, entityHit, damage, neverKnockback, true, source);
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful
	 * If damage is negative, entity will be healed by that amount*/
	public static boolean attemptDamage(Entity thrower, Entity entityHit, float damage, boolean neverKnockback, boolean ignoreHurtResist, DamageSource source) {
		if (shouldHit(thrower, entityHit, damage < 0) && !thrower.world.isRemote) {
			// heal
			if (damage < 0 && entityHit instanceof EntityLivingBase) {
				((EntityLivingBase)entityHit).heal(Math.abs(damage*Config.damageScale));
				return true;
			}
			// damage
			else if (damage >= 0) {
				boolean damaged = false;
				int prevHurtResist = entityHit.hurtResistantTime;
				if (ignoreHurtResist)
					entityHit.hurtResistantTime = 0;
				if ((!Config.projectilesCauseKnockback || neverKnockback) && entityHit instanceof EntityLivingBase) {
					double prev = ((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue();
					((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
					damaged = entityHit.attackEntityFrom(source, damage*Config.damageScale);
					((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(prev);
				}
				else
					damaged = entityHit.attackEntityFrom(source, damage*Config.damageScale);

				if (damaged && ignoreHurtResist)
					entityHit.hurtResistantTime = prevHurtResist;

				return damaged;
			}
		}

		return false;
	}

	/**Spawn trail particles behind entity based on entity's prevPos and current motion*/
	public static void spawnTrailParticles(Entity entity, double amountPerBlock, double random, int color, int colorFade, float scale, int maxAge, float alpha) {
		spawnTrailParticles(entity, amountPerBlock, random, 0, 0, 0, color, colorFade, scale, maxAge, alpha);
	}

	/**Spawn trail particles behind entity based on entity's prevPos and current motion*/
	public static void spawnTrailParticles(Entity entity, double amountPerBlock, double random, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge, float alpha) {
		int numParticles = MathHelper.ceil(amountPerBlock * Math.sqrt(entity.getDistanceSq(entity.prevPosX, entity.prevPosY, entity.prevPosZ)));
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
		Vec3d vec3d = getPositionEyes(shooter);
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
			Vec3d vec3d = getPositionEyes(shooter);
			double d1 = d0;
			Vec3d vec3d1 = getLook(pitch, yawHead);
			Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0);
			List<Entity> list = shooter.world.getEntitiesInAABBexcluding(shooter, shooter.getEntityBoundingBox().addCoord(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0).expand(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
				public boolean apply(@Nullable Entity entity) {
					return entity != null && entity.canBeCollidedWith() && shouldTarget(shooter, entity, friendly);
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

	/**Copied from {@link AbstractClientPlayer#getFovModifier()} to make public*/
	public static float getFovModifier(EntityPlayer player) {
		float f = 1.0F;
		if (player.capabilities.isFlying)
			f *= 1.1F;
		IAttributeInstance iattributeinstance = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
		f = (float)((double)f * ((iattributeinstance.getAttributeValue() / (double)player.capabilities.getWalkSpeed() + 1.0D) / 2.0D));
		if (player.capabilities.getWalkSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f))
			f = 1.0F;
		if (player.isHandActive() && player.getActiveItemStack().getItem() == Items.BOW) {
			int i = player.getItemInUseMaxCount();
			float f1 = (float)i / 20.0F;
			if (f1 > 1.0F)
				f1 = 1.0F;
			else
				f1 = f1 * f1;

			f *= 1.0F - f1 * 0.15F;
		}
		return net.minecraftforge.client.ForgeHooksClient.getOffsetFOV(player, f);
	}

	/**Copied from {@link Entity#getPositionEyes(float)} to make public*/
	public static Vec3d getPositionEyes(EntityLivingBase entity) {
		float partialTicks = Minewatch.proxy.getRenderPartialTicks(); 
		if (entity == null)
			return Vec3d.ZERO;
		else if (partialTicks == 1.0F)
			return new Vec3d(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ);
		else {
			double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
			double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)entity.getEyeHeight();
			double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;
			return new Vec3d(d0, d1, d2);
		}
	}

	/**Returns the result with the closest entityHit to the entity*/
	@Nullable
	public static RayTraceResult getNearestImpact(Entity entity, ArrayList<RayTraceResult> results) {
		RayTraceResult nearest = null;
		if (entity != null && results != null) {
			double nearestDistance = Double.MAX_VALUE;
			for (RayTraceResult result : results)
				if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY) {
					double distance = entity.getDistanceSqToEntity(result.entityHit);
					if (distance < nearestDistance) {
						nearest = result;
						nearestDistance = distance;
					}
				}
		}
		return nearest;
	}

	/**Returns if e1 is with maxAngle degrees of looking at e2*/
	public static boolean isInFieldOfVision(Entity e1, Entity e2, float maxAngle){
		return getMaxFieldOfVisionAngle(e1, e2) <= maxAngle;
	}
	
	/**Returns maxAngle degrees between e1's look and e2*/
	public static float getMaxFieldOfVisionAngle(Entity e1, Entity e2){
		// calculate angles if e1 was directly facing e2
		double d0 = e2.posX - e1.posX;
		double d1 = (e2.getEntityBoundingBox().minY + e2.getEntityBoundingBox().maxY) / 2.0D - (e1.posY + (double)e1.getEyeHeight());
		double d2 = e2.posZ - e1.posZ;
		double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
		float facingYaw = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
		float facingPitch = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
		// calculate difference between facing and current angles
		float deltaYaw = Math.abs(MathHelper.wrapDegrees(e1.rotationYaw - facingYaw));
		float deltaPitch = Math.abs(e1.rotationPitch-facingPitch);
		return Math.max(deltaYaw, deltaPitch);
	}
	
	/**Get target within maxAngle degrees of being looked at by shooter*/
	@Nullable
	public static EntityLivingBase getTargetInFieldOfVision(EntityLivingBase shooter, float range, float maxAngle, boolean friendly) {
		return getTargetInFieldOfVision(shooter, range, maxAngle, friendly, null);
	}
	
	/**Get target within maxAngle degrees of being looked at by shooter*/
	@Nullable
	public static EntityLivingBase getTargetInFieldOfVision(EntityLivingBase shooter, float range, float maxAngle, boolean friendly, @Nullable Predicate<EntityLivingBase> predicate) {
		Vec3d look = shooter.getLookVec().scale(range-1);
		AxisAlignedBB aabb = shooter.getEntityBoundingBox().expandXyz(5).addCoord(look.xCoord, look.yCoord, look.zCoord);
		EntityLivingBase closest = null;
		float angle = Float.MAX_VALUE;
		for (Entity entity : shooter.world.getEntitiesInAABBexcluding(shooter, aabb, new Predicate<Entity>() {
			@Override
			public boolean apply(Entity input) {
				return input instanceof EntityLivingBase && EntityHelper.shouldHit(shooter, input, friendly) && 
						shooter.canEntityBeSeen(input) && shooter.getDistanceToEntity(input) <= range && (predicate == null || predicate.apply((EntityLivingBase) input));
			}
		})) {
			float newAngle = EntityHelper.getMaxFieldOfVisionAngle(shooter, entity);
			if (closest == null || newAngle < angle) {
				closest = (EntityLivingBase) entity;
				angle = newAngle;
			}
		}
		
		// debug visualize
		//EnumHero.RenderManager.boundingBoxesToRender.clear();
		//EnumHero.RenderManager.boundingBoxesToRender.add(aabb);
		return angle <= maxAngle ? closest : null;
	}

	/**Is entity holding the item in either hand*/
	public static boolean isHoldingItem(EntityLivingBase entity, Item item) {
		return isHoldingItem(entity, item, EnumHand.values());
	}

	/**Is entity holding the item in any of the specified hands*/
	public static boolean isHoldingItem(EntityLivingBase entity, Item item, EnumHand...hands) {
		return getHeldItem(entity, item, hands) != null;
	}

	/**Get the item in any of the specified hands*/
	@Nullable
	public static ItemStack getHeldItem(EntityLivingBase entity, Item item, EnumHand...hands) {
		if (entity != null && item != null) 
			for (EnumHand hand : hands) {
				ItemStack stack = entity.getHeldItem(hand);
				if (stack != null && stack.getItem() == item)
					return stack;
			}
		return null;
	}

	/**Is entity holding an instanceof the item in any of the specified hands*/
	public static boolean isHoldingItem(EntityLivingBase entity, Class<? extends Item> item, EnumHand...hands) {
		return getHeldItem(entity, item, hands) != null;
	}

	/**Get an instanceof the item in any of the specified hands*/
	@Nullable
	public static ItemStack getHeldItem(EntityLivingBase entity, Class<? extends Item> item, EnumHand...hands) {
		if (entity != null && item != null) 
			for (EnumHand hand : hands) {
				ItemStack stack = entity.getHeldItem(hand);
				if (stack != null && item.isInstance(stack.getItem()))
					return stack;
			}
		return null;
	}

	/**Get exact entity position - accounting for partial ticks and lastTickPos*/
	public static Vec3d getEntityPartialPos(Entity entity) {
		if (entity != null) {
			float partialTicks = Minewatch.proxy.getRenderPartialTicks();
			double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
			double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
			double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
			return new Vec3d(x, y, z);
		}
		return Vec3d.ZERO;
	}

}