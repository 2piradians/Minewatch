package twopiradians.minewatch.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.entity.ability.EntityAnaGrenade;
import twopiradians.minewatch.common.entity.ability.EntityReinhardtStrike;
import twopiradians.minewatch.common.entity.ability.EntityRoadhogHook;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.hero.EntityLucio;
import twopiradians.minewatch.common.entity.projectile.EntityHanzoArrow;
import twopiradians.minewatch.common.hero.UltimateManager;
import twopiradians.minewatch.common.hero.UltimateManager.AttackType;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.tileentity.TileEntityHealthPack;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityHelper {

	private static final Handler HEALTH_PARTICLES = new Handler(Identifier.HEALTH_PARTICLES, false) {};

	/**Copied from EntityThrowable*/
	public static ArrayList<RayTraceResult> checkForImpact(Entity entityIn, boolean friendly) {
		ArrayList<RayTraceResult> results = new ArrayList<RayTraceResult>();
		Vec3d posVec = EntityHelper.getEntityPartialPos(entityIn).addVector(0, entityIn.height/2f, 0);
		if (entityIn instanceof EntityReinhardtStrike) // use prevPos so it doesn't clip in ground if shot too close to ground
			posVec = new Vec3d(entityIn.prevPosX, entityIn.prevPosY+entityIn.height/2d, entityIn.prevPosZ);
		Vec3d motionVec = new Vec3d(entityIn.motionX, entityIn.motionY, entityIn.motionZ);
		Vec3d posMotionVec = posVec.add(motionVec).add(motionVec.normalize().scale(entityIn.width*0.75f));
		RayTraceResult result = entityIn.world.rayTraceBlocks(posVec, posMotionVec, false, true, true);
		if (result != null)
			results.add(result);

		// if entityIn moving more than its collision box per tick - check for lookVec intercept, otherwise just check collision boxes
		boolean fast = Math.abs(entityIn.motionX) > entityIn.width || Math.abs(entityIn.motionY) > entityIn.height || Math.abs(entityIn.motionZ) > entityIn.width;
		AxisAlignedBB aabb = entityIn.getEntityBoundingBox();
		if (fast) 
			aabb = aabb.expand(entityIn.motionX, entityIn.motionY, entityIn.motionZ);
		// list of entities in (possibly very big) area
		HashSet<Entity> list = Sets.newHashSet(entityIn.world.getEntitiesWithinAABBExcludingEntity(entityIn, aabb));
		// entities in collision box
		if (entityIn instanceof EntityMW)
			aabb = ((EntityMW)entityIn).getImpactBoundingBox();
		else if (entityIn instanceof EntityLivingBaseMW)
			aabb = ((EntityLivingBaseMW)entityIn).getImpactBoundingBox();
		else
			aabb = entityIn.getEntityBoundingBox();
		List<Entity> entitiesColliding = entityIn.world.getEntitiesWithinAABBExcludingEntity(entityIn, aabb);
		list.addAll(entitiesColliding);
		for (Entity entity : list) {
			if ((shouldHit(entityIn, entity, friendly) || (entityIn instanceof EntityAnaGrenade && shouldHit(entityIn, entity, !friendly)) ||
					entityIn instanceof EntityRoadhogHook)) {
				double x2 = entity instanceof EntityPlayer ? ((EntityPlayer)entity).chasingPosX : entity.prevPosX;
				double y2 = entity instanceof EntityPlayer ? ((EntityPlayer)entity).chasingPosY : entity.prevPosY;
				double z2 = entity instanceof EntityPlayer ? ((EntityPlayer)entity).chasingPosZ : entity.prevPosZ;
				// move to prev pos
				aabb = entity.getEntityBoundingBox().offset(new Vec3d(x2-entity.posX, y2-entity.posY, z2-entity.posZ));

				if (!fast || aabb.calculateIntercept(posVec, posMotionVec) != null || entitiesColliding.contains(entity)) 
					results.add(new RayTraceResult(entity));
			}
		}

		return results;
	}

	/**Get the position that an entity should be thrown/shot from - make sure yaw is rotationYawHead*/ 
	public static Vec3d getShootingPos(EntityLivingBase shooter, float pitch, float yaw, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust) {
		return getShootingPos(shooter, pitch, yaw, hand, verticalAdjust, horizontalAdjust, 1);
	}

	/**Get the position that an entity should be thrown/shot from - make sure yaw is rotationYawHead*/ 
	public static Vec3d getShootingPos(EntityLivingBase shooter, float pitch, float yaw, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust, float distance) {
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

		Vec3d lookVec = getLook(pitch+verticalAdjust, yaw).scale(distance);
		Vec3d horizontalVec = new Vec3d(-lookVec.z, 0, lookVec.x).normalize().scale(horizontalAdjust);
		if (pitch+verticalAdjust > 90)
			horizontalVec = horizontalVec.scale(-1);
		Vec3d posVec = EntityHelper.getEntityPartialPos(shooter);
		return posVec.add(lookVec).add(horizontalVec).addVector(0, shooter.getEyeHeight(), 0);
	}

	/**Aim the entity at the target. Hitscan if metersPerSecond == -1*/
	public static void setAim(Entity entity, EntityLivingBase shooter, Entity target, float metersPerSecond, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust) {
		setAim(entity, shooter, target, metersPerSecond, hand, verticalAdjust, horizontalAdjust, false);
	}

	/**Aim the entity at the target. Hitscan if metersPerSecond == -1*/
	public static void setAim(Entity entity, EntityLivingBase shooter, Entity target, float metersPerSecond, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust, boolean ignoreHeroInaccuracy) {
		setAim(entity, shooter, target, shooter.rotationPitch, shooter.rotationYawHead, metersPerSecond, 0, hand, verticalAdjust, horizontalAdjust, 1, ignoreHeroInaccuracy);
	}

	/**Aim the entity in the proper direction to be thrown/shot. Hitscan if metersPerSecond == -1. Make sure yaw is rotationYawHead*/
	public static void setAim(Entity entity, EntityLivingBase shooter, float pitch, float yaw, float metersPerSecond, float inaccuracy, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust) {
		setAim(entity, shooter, null, pitch, yaw, metersPerSecond, inaccuracy, hand, verticalAdjust, horizontalAdjust, 1, false);
	}

	/**Aim the entity in the proper direction to be thrown/shot. Hitscan if metersPerSecond == -1. Make sure yaw is rotationYawHead*/
	public static void setAim(Entity entity, EntityLivingBase shooter, float pitch, float yaw, float metersPerSecond, float inaccuracy, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust, boolean ignoreHeroInaccuracy) {
		setAim(entity, shooter, null, pitch, yaw, metersPerSecond, inaccuracy, hand, verticalAdjust, horizontalAdjust, 1, ignoreHeroInaccuracy);
	}

	/**Aim the entity in the proper direction to be thrown/shot. Hitscan if metersPerSecond == -1*/
	public static void setAim(Entity entity, EntityLivingBase shooter, float pitch, float yaw, float metersPerSecond, float inaccuracy, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust, float distance) {
		setAim(entity, shooter, null, pitch, yaw, metersPerSecond, inaccuracy, hand, verticalAdjust, horizontalAdjust, distance, false);
	}

	/**Aim the entity in the proper direction to be thrown/shot. Hitscan if metersPerSecond == -1*/
	public static void setAim(Entity entity, EntityLivingBase shooter, @Nullable Entity target, float pitch, float yaw, float metersPerSecond, float inaccuracy, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust, float distance, boolean ignoreHeroInaccuracy) {
		boolean friendly = isFriendly(entity);
		Vec3d vec = getShootingPos(shooter, pitch, yaw, hand, verticalAdjust, horizontalAdjust, distance);

		if (shooter instanceof EntityHero && !ignoreHeroInaccuracy)
			inaccuracy = (float) (Math.max(0.5f, inaccuracy) * Config.mobInaccuracy);
		pitch += (entity.world.rand.nextFloat()-0.5f)*inaccuracy;
		yaw += (entity.world.rand.nextFloat()-0.5f)*inaccuracy;

		pitch = MathHelper.wrapDegrees(pitch);
		yaw = MathHelper.wrapDegrees(yaw);

		// get block that shooter is looking at
		double blockDistance = Double.MAX_VALUE;
		RayTraceResult blockTrace = shooter instanceof EntityHero ? null : EntityHelper.getMouseOverBlock(shooter, 512, pitch, yaw);
		if (blockTrace != null && blockTrace.typeOfHit == RayTraceResult.Type.BLOCK)
			blockDistance = Math.sqrt(vec.squareDistanceTo(blockTrace.hitVec.x, blockTrace.hitVec.y, blockTrace.hitVec.z));
		// get entity that shooter is looking at
		double entityDistance = Double.MAX_VALUE;
		RayTraceResult entityTrace = null;
		if (target != null)
			entityTrace = new RayTraceResult(target, new Vec3d(target.posX, target.posY+target.height/2d, target.posZ));
		/*// aim bot (eventually used with Soldier's ult)
		else if () {
			EntityLivingBase targetEntity = EntityHelper.getTargetInFieldOfVision(shooter, shooter instanceof EntityHero ? 64 : 512, 15, friendly);
				if (targetEntity != null) { 
				Vec3d targetHit = EntityHelper.getClosestPointOnBoundingBox(vec, shooter.getLookVec(), targetEntity);
				if (targetHit != null)
					entityTrace = new RayTraceResult(target, targetHit);
			}
		}*/
		else  
			entityTrace = EntityHelper.getMouseOverEntity(shooter, shooter instanceof EntityHero ? 64 : 512, friendly, pitch, yaw);

		if (entityTrace != null && entityTrace.typeOfHit == RayTraceResult.Type.ENTITY)
			entityDistance = Math.sqrt(vec.squareDistanceTo(entityTrace.hitVec.x, entityTrace.hitVec.y, entityTrace.hitVec.z));

		double x, y, z;
		// block is closest
		if (target == null && blockDistance < entityDistance && blockDistance < Double.MAX_VALUE) {
			x = blockTrace.hitVec.x - vec.x;
			y = blockTrace.hitVec.y - vec.y - entity.height/2d;
			z = blockTrace.hitVec.z - vec.z;
		}
		// entity is closest
		else if (target != null || (entityDistance < blockDistance && entityDistance < Double.MAX_VALUE)) {
			x = entityTrace.hitVec.x - vec.x;
			y = entityTrace.hitVec.y - vec.y - entity.height/2d;
			z = entityTrace.hitVec.z - vec.z; 
			
/*			if (metersPerSecond == -1 && entity instanceof EntityMW) {
				((EntityMW)entity).hitscan = true;
				entity.setLocationAndAngles(vec.x, vec.y, vec.z, entity.rotationYaw, entity.rotationPitch);
				entity.prevPosX = vec.x;
				entity.prevPosY = vec.y;
				entity.prevPosZ = vec.z;
				EntityHelper.moveToHitPosition(entity, entityTrace);
				((EntityMW)entity).onImpact(entityTrace);
				//((EntityMW)entity).spawnTrailParticles();
				entity.isDead = false;
				//entity.getDataManager().set(EntityMW.HITSCAN, true);
				//entity.getDataManager().set(EntityMW.HITSCAN, new Rotations((float)vec.x, (float)vec.y, (float)vec.z));
				DataParameter<Rotations> data = getVelocityParameter(entity);
				if (data != null)
					entity.getDataManager().set(data, new Rotations((float)x, (float)y, (float)z));
				else
					Minewatch.logger.error("Missing velocity parameter for: "+entity);
				return;
			}*/
		}
		// not looking at block/entity
		else {
			Vec3d look = getLook(pitch, yaw).scale(metersPerSecond == -1 ? 100 : 1);
			x = look.x;
			y = look.y;
			z = look.z;
		}

		entity.setLocationAndAngles(vec.x, vec.y, vec.z, entity.rotationYaw, entity.rotationPitch);
		
		//if (metersPerSecond == -1 && entity instanceof EntityMW)
		//	((EntityMW)entity).hitscan = true;

		// send velocity to server/client
		Vec3d scaledVelocity = new Vec3d(x, y, z);
		if (metersPerSecond != -1) // hitscan if -1
			scaledVelocity = scaledVelocity.normalize().scale(metersPerSecond/20d);
		else if (entityTrace != null && entityTrace.entityHit != null) // scale velocity by hit entity width (for leeway since lifetime is 1)
			scaledVelocity = scaledVelocity.add(scaledVelocity.normalize().scale(entityTrace.entityHit.width));
		else // go a little faster for blocks when hitscanning - so it only takes 1 tick to hit
			scaledVelocity = scaledVelocity.add(scaledVelocity.normalize().scale(0.1d));

		DataParameter<Rotations> data = getVelocityParameter(entity);
		if (data != null)
			entity.getDataManager().set(data, new Rotations((float)scaledVelocity.x, (float)scaledVelocity.y, (float)scaledVelocity.z));
		else
			Minewatch.logger.error("Missing velocity parameter for: "+entity);
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
		float f = MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z);
		entity.rotationYaw = (float)(MathHelper.atan2(vec.x, vec.z) * (180D / Math.PI));
		entity.rotationPitch = (float)(MathHelper.atan2(vec.y, (double)f) * (180D / Math.PI));
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
		else if (entity instanceof EntityArrow)
			return ((EntityArrow)entity).shootingEntity;
		else if (entity instanceof EntityFireball)
			return ((EntityFireball)entity).shootingEntity;
		else if (entity instanceof EntityTNTPrimed)
			return ((EntityTNTPrimed)entity).getTntPlacedBy();
		return entity;
	}

	/**Should entityHit be hit by thrower projectile.
	 * @param friendly - should this hit teammates or enemies?*/
	public static boolean shouldHit(Entity thrower, Entity entityHit, boolean friendly) {
		Entity actualThrower = getThrower(thrower);
		DamageSource source = actualThrower instanceof EntityLivingBase ? DamageSource.causeIndirectDamage(thrower, (EntityLivingBase) actualThrower) : null;
		return source != null && shouldHit(actualThrower, entityHit, friendly, source);
	}

	/**Should entityHit be hit by thrower projectile.
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
		// prevent hitting armor stands
		if (entityHit instanceof EntityArmorStand)
			return false;
		thrower = getThrower(thrower);
		entityHit = getThrower(entityHit);
		return shouldTarget(thrower, entityHit, friendly) && 
				((entityHit instanceof EntityLivingBase && ((EntityLivingBase)entityHit).getHealth() > 0) || 
						entityHit instanceof MultiPartEntityPart || entityHit instanceof EntityEnderCrystal) && !entityHit.isEntityInvulnerable(source); 
	}

	/**Should target be hit by entity / should entity render red*/
	public static boolean shouldTarget(Entity entity, @Nullable Entity target, boolean friendly) {
		if (target == null)
			target = Minewatch.proxy.getClientPlayer();
		entity = getThrower(entity);
		target = getThrower(target);
		Team entityTeam = getTeam(entity);
		Team targetTeam = getTeam(target);

		// prevent EntityHero attacking/targeting things it shouldn't (unless friendly and on same team)
		if (entity instanceof EntityHero && target != null && 
				!(friendly && entityTeam != null && entityTeam == targetTeam) &&
				!(friendly && entity == target) && 
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
		// prevent healing mobs not on team with config option disabled
		if (!Config.healMobs && friendly && target != null && entity != null && target != entity && (targetTeam == null || targetTeam != entityTeam || targetTeam.getAllowFriendlyFire()))
			return false;
		return entity != null && target != null && (target != entity || friendly) &&
				(entityTeam == null || targetTeam == null || 
				(entityTeam == targetTeam) == friendly || entityTeam.getAllowFriendlyFire());
	}

	/**Get an entity's team - namely for getting a dead entity's team (since they are technically removed from team on death)*/
	@Nullable
	public static Team getTeam(Entity entity) {
		if (entity == null)
			return null;
		else if (entity.isEntityAlive() || entity.getTeam() != null)
			return entity.getTeam();
		else if (entity.getEntityData().hasKey("minewatch:team"))
			return entity.world.getScoreboard().getTeam(entity.getEntityData().getString("minewatch:team"));
		else
			return null;
	}

	/**Attempts do damage with falloff returns true if successful on server*/
	public static <T extends Entity & IThrowableEntity> boolean attemptFalloffImpact(T projectile, Entity shooter, Entity entityHit, boolean friendly, float minDamage, float maxDamage, float minFalloff, float maxFalloff) {
		if (EntityHelper.shouldHit(shooter, entityHit, friendly)) {
			double distance = Math.max(0, projectile.getPositionVector().distanceTo(shooter.getPositionVector())-shooter.width/2f-entityHit.width/2f);
			float damage = maxDamage;
			if (distance > maxFalloff)
				damage = 0;
			else if (distance > minFalloff && distance < maxFalloff)
				damage *= 1f- (distance-minFalloff)/(maxFalloff-minFalloff);
			if (damage > 0 && attemptDamage(projectile, entityHit, damage, friendly)) 
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
				if (state.getMaterial() != Material.AIR) {
					projectile.setPosition(result.hitVec.x, result.hitVec.y, result.hitVec.z);
					if (kill)
						projectile.setDead();
				}
			}
			// move to collide with entity
			else if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
				Vec3d vec3d = new Vec3d(projectile.posX, projectile.posY, projectile.posZ);
				Vec3d vec3d1 = new Vec3d(projectile.posX + projectile.motionX, projectile.posY + projectile.motionY, projectile.posZ + projectile.motionZ);
				AxisAlignedBB aabb = result.entityHit.getEntityBoundingBox().grow(0.05D);
				RayTraceResult ray = aabb.calculateIntercept(vec3d, vec3d1);
				if (ray != null) {
					projectile.posX = ray.hitVec.x;
					projectile.posY = ray.hitVec.y;
					projectile.posZ = ray.hitVec.z; 
				}

				// change prevPos to deflect pos so particles follow properly
				if (TickHandler.hasHandler(result.entityHit, Identifier.GENJI_DEFLECT) && 
						result.entityHit instanceof EntityLivingBase && 
						ItemGenjiShuriken.canDeflect((EntityLivingBase) result.entityHit, projectile, projectile)) {
					//if (projectile instanceof EntityMW)
					//projectile.getDataManager().set(EntityMW.POSITION, new Rotations((float)projectile.posX, (float)projectile.posY, (float)projectile.posZ));
				}
				// don't kill if deflecting
				else if (kill)
					projectile.setDead();
			}
		}
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful
	 * If damage is negative, entity will be healed by that amount
	 * THROWER SHOULD BE THE ACTUAL SOURCE OF DAMAGE (NOT PLAYER THROWER) - SO GENJI CAN DEFLECT*/
	public static boolean attemptDamage(Entity thrower, Entity entityHit, float damage, boolean neverKnockback) {
		return attemptDamage(thrower, entityHit, damage, neverKnockback, true);
	}
	
	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful
	 * If damage is negative, entity will be healed by that amount
	 * DAMAGESOURCE SHOULD BE THE ACTUAL SOURCE OF DAMAGE (NOT PLAYER THROWER) - FOR GENJI DEFLECT AND ULT CHARGE*/
	public static boolean attemptDamage(Entity damageSource, Entity entityHit, float damage, boolean neverKnockback, boolean ignoreHurtResist) {
		return attemptDamage(damageSource, entityHit, damage, neverKnockback, ignoreHurtResist, false);	
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful
	 * If damage is negative, entity will be healed by that amount
	 * DAMAGESOURCE SHOULD BE THE ACTUAL SOURCE OF DAMAGE (NOT PLAYER THROWER) - FOR GENJI DEFLECT AND ULT CHARGE*/
	public static boolean attemptDamage(Entity damageSource, Entity entityHit, float damage, boolean neverKnockback, boolean ignoreHurtResist, boolean skipShouldHit) {
		Entity actualThrower = getThrower(damageSource);
		DamageSource source = actualThrower instanceof EntityLivingBase ? DamageSource.causeIndirectDamage(damageSource, (EntityLivingBase) actualThrower) : null;
		return source != null && attemptDamage(damageSource, actualThrower, entityHit, damage, neverKnockback, ignoreHurtResist, skipShouldHit, source);	
	}
	
	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful
	 * If damage is negative, entity will be healed by that amount
	 * DAMAGESOURCE SHOULD BE THE ACTUAL SOURCE OF DAMAGE (NOT PLAYER THROWER) - FOR GENJI DEFLECT AND ULT CHARGE*/
	public static boolean attemptDamage(Entity damageSource, Entity entityHit, float damage, boolean neverKnockback, DamageSource source) {
		return attemptDamage(damageSource, entityHit, damage, neverKnockback, false, source);
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful
	 * If damage is negative, entity will be healed by that amount
	 * DAMAGESOURCE SHOULD BE THE ACTUAL SOURCE OF DAMAGE (NOT PLAYER THROWER) - FOR GENJI DEFLECT AND ULT CHARGE*/
	public static boolean attemptDamage(Entity damageSource, Entity entityHit, float damage, boolean neverKnockback, boolean skipShouldHit, DamageSource source) {
		Entity actualThrower = getThrower(damageSource);
		return attemptDamage(damageSource, actualThrower, entityHit, damage, neverKnockback, true, skipShouldHit, source);
	}

	/**Attempts to damage entity (damage parameter should be unscaled) - returns if successful
	 * If damage is negative, entity will be healed by that amount*/
	public static boolean attemptDamage(Entity damageSource, Entity actualThrower, Entity entityHit, float damage, boolean neverKnockback, boolean ignoreHurtResist, boolean skipShouldHit, DamageSource source) {
		if (!actualThrower.world.isRemote && (skipShouldHit || shouldHit(actualThrower, entityHit, damage < 0))) {
			// change dragon to part
			if (entityHit instanceof EntityDragon)
				entityHit = ((EntityDragon)entityHit).dragonPartHead;
			
			// heal
			if (damage < 0 && entityHit instanceof EntityLivingBase) {
				heal(damageSource, actualThrower, (EntityLivingBase)entityHit, damage);
				return true;
			}
			// damage
			else if (damage >= 0) {
				float amountDamaged = entityHit instanceof EntityLivingBase ? ((EntityLivingBase)entityHit).getHealth() : 0;
				boolean damaged = false;
				// save prev hurtResistTime (use EntityDragon's resist if this is EntityDragonPart)
				int prevHurtResist = entityHit instanceof MultiPartEntityPart && ((MultiPartEntityPart)entityHit).parent instanceof EntityDragon ? ((EntityDragon) ((MultiPartEntityPart)entityHit).parent).hurtResistantTime : entityHit.hurtResistantTime;
				if (ignoreHurtResist)
					entityHit.hurtResistantTime = 0;
				if ((!Config.projectilesCauseKnockback || neverKnockback) && entityHit instanceof EntityLivingBase) {
					double prev = ((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue();
					((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
					damaged = entityHit.attackEntityFrom(source, (float) (damage*Config.getDamageScale(actualThrower)));
					((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(prev);
				}
				else
					damaged = entityHit.attackEntityFrom(source, (float) (damage*Config.getDamageScale(actualThrower)));

				// reset hurt resist
				if (damaged && ignoreHurtResist)
					if (entityHit instanceof MultiPartEntityPart && ((MultiPartEntityPart)entityHit).parent instanceof EntityDragon) 
						((EntityDragon) ((MultiPartEntityPart)entityHit).parent).hurtResistantTime = prevHurtResist;
					else
						entityHit.hurtResistantTime = prevHurtResist;
				
				// ultimate charge
				if (damaged) {
					amountDamaged -= entityHit instanceof EntityLivingBase ? ((EntityLivingBase)entityHit).getHealth() : 0;
					if (amountDamaged > 0 && actualThrower != entityHit)
						UltimateManager.handleAbilityCharge(actualThrower, damageSource, amountDamaged, AttackType.DAMAGE);
				}

				return damaged;
			}
		}

		return false;
	}

	/**Heal the entity by the specified (unscaled) amount - does not do any shouldTarget checking
	 * DAMAGESOURCE SHOULD BE THE ACTUAL SOURCE OF DAMAGE (NOT PLAYER THROWER) - FOR ULT CHARGE*/
	public static void heal(@Nullable Entity damageSource, @Nullable Entity actualThrower, EntityLivingBase entity, float amount) {
		if (entity != null && entity.getHealth() < entity.getMaxHealth() && 
				!TickHandler.hasHandler(entity, Identifier.ANA_GRENADE_DAMAGE)) {
			if (TickHandler.hasHandler(entity, Identifier.ANA_GRENADE_HEAL))
				amount *= 2f;
			float amountHealed = entity.getHealth();
			entity.heal((float) Math.abs(amount*Config.getDamageScale(entity)));
			amountHealed = entity.getHealth() - amountHealed;
			spawnHealParticles(entity, false);
			
			// ultimate charge
			if (amountHealed > 0 && damageSource != null && actualThrower != null)
				UltimateManager.handleAbilityCharge(actualThrower, damageSource, amountHealed, actualThrower == entity ? AttackType.SELF_HEAL : AttackType.HEAL);
		}
	}

	/**Spawn healing particles on entity - sends packet to clients if called on server*/
	public static void spawnHealParticles(Entity entity, boolean ignoreCooldown) {
		if (entity != null && (ignoreCooldown || !TickHandler.hasHandler(entity, Identifier.HEALTH_PARTICLES)) && 
				!TickHandler.hasHandler(entity, Identifier.MOIRA_FADE) && 
				!TickHandler.hasHandler(entity, Identifier.SOMBRA_INVISIBLE)) {
			if (!entity.world.isRemote)
				Minewatch.network.sendToDimension(new SPacketSimple(44, entity, ignoreCooldown), entity.world.provider.getDimension());
			else {
				float size = Math.min(entity.height, entity.width);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, entity.world, entity, 0xCFC77F, 0xCFC77F, 0.3f, 
						40, size*8f, size*8f/1.1f, 0, 0);
				for (int i=0; i<15; ++i)
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.HEALTH_PLUS, entity.world, 
							entity.posX+(entity.world.rand.nextFloat()-0.5f)*entity.width/2f, entity.posY+entity.height/1.5f+(entity.world.rand.nextFloat()-0.5f)*entity.height/2f, entity.posZ+(entity.world.rand.nextFloat()-0.5f)*entity.width/2f, 
							(entity.world.rand.nextFloat()-0.5f)*0.25f*size, entity.world.rand.nextFloat()*0.2f*size, (entity.world.rand.nextFloat()-0.5f)*0.25f*size, 
							0xFFFFFF, 0xC0C0C0, 1, 
							20, size*1.4f, size*0.9f, 0, 0);
			}
			TickHandler.register(entity.world.isRemote, HEALTH_PARTICLES.setEntity(entity).setTicks(20));
		}
	}

	/**Spawn trail particles behind entity based on entity's prevPos and current motion*/
	public static void spawnTrailParticles(Entity entity, double amountPerBlock, double random, int color, int colorFade, float scale, int maxAge, float alpha) {
		spawnTrailParticles(entity, amountPerBlock, random, 0, 0, 0, color, colorFade, scale, maxAge, alpha);
	}

	/**Spawn trail particles behind entity based on entity's prevPos and current motion*/
	public static void spawnTrailParticles(Entity entity, double amountPerBlock, double random, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge, float alpha) {
		spawnTrailParticles(entity, amountPerBlock, random, motionX, motionY, motionZ, color, colorFade, scale, maxAge, alpha, entity.getPositionVector(), getPrevPositionVector(entity));
	}

	/**Spawn trail particles behind entity based on entity's prevPos and current motion*/
	public static void spawnTrailParticles(Entity entity, double amountPerBlock, double random, int color, int colorFade, float scale, int maxAge, float alpha, Vec3d pos, Vec3d prevPos) {
		spawnTrailParticles(entity, amountPerBlock, random, 0, 0, 0, color, colorFade, scale, maxAge, alpha, pos, prevPos);
	}

	/**Spawn trail particles behind entity based on entity's prevPos and current motion*/
	public static void spawnTrailParticles(Entity entity, double amountPerBlock, double random, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge, float alpha, Vec3d pos, Vec3d prevPos) {
		int numParticles = MathHelper.ceil(amountPerBlock * Math.sqrt(entity.getDistanceSq(prevPos.x, prevPos.y, prevPos.z)));
		for (float i=0; i<numParticles; ++i) 
			Minewatch.proxy.spawnParticlesTrail(entity.world, 
					pos.x+(prevPos.x-pos.x)*i/numParticles+(entity.world.rand.nextDouble()-0.5d)*random, 
					pos.y+(entity instanceof EntityHanzoArrow ? 0 : entity.height/2d)+(prevPos.y-pos.y)*i/numParticles+(entity.world.rand.nextDouble()-0.5d)*random, 
					pos.z+(prevPos.z-pos.z)*i/numParticles+(entity.world.rand.nextDouble()-0.5d)*random, 
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
		Vec3d vec3d2 = vec3d.addVector(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance);
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
			Vec3d vec3d2 = vec3d.addVector(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0);
			List<Entity> list = shooter.world.getEntitiesInAABBexcluding(shooter, shooter.getEntityBoundingBox().expand(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0).grow(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
				public boolean apply(@Nullable Entity entity) {
					return entity != null/* && entity.canBeCollidedWith()*/ && shouldTarget(shooter, entity, friendly);
				}
			}));
			double d2 = d1;

			for (int j = 0; j < list.size(); ++j) {
				Entity entity1 = (Entity)list.get(j);
				AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow((double)entity1.getCollisionBorderSize());
				RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

				if (axisalignedbb.contains(vec3d)) {
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

	/**Copied from {@link Entity#getPositionEyes(float)} for server (clientside in 1.10.2)*/
	public static Vec3d getPositionEyes(Entity entity) {
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
				if (result != null && result.hitVec != null) {
					double distance = entity.getDistanceSq(result.hitVec.x, result.hitVec.y, result.hitVec.z);
					if (distance < nearestDistance) {
						nearest = result;
						nearestDistance = distance;
					}
				}
		}
		return nearest;
	}

	/**Get center of aabb, because clientside*/
	public static Vec3d getCenter(AxisAlignedBB aabb) {
		return new Vec3d(aabb.minX + (aabb.maxX - aabb.minX) * 0.5D, aabb.minY + (aabb.maxY - aabb.minY) * 0.5D, aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5D);
	}

	/**Returns if e1 is with maxAngle degrees of looking at e2*/
	public static boolean isInFieldOfVision(Entity e1, Entity e2, float maxAngle, float yaw, float pitch){
		return getMaxFieldOfVisionAngle(e1, e2, yaw, pitch) <= maxAngle;
	}

	/**Returns if e1 is with maxAngle degrees of looking at e2*/
	public static boolean isInFieldOfVision(Entity e1, Entity e2, float maxAngle){
		return getMaxFieldOfVisionAngle(e1, e2) <= maxAngle;
	}

	/**Returns if e1 is with maxAngle degrees of looking at pos*/
	public static boolean isInFieldOfVision(Entity e1, Vec3d pos, float maxAngle){
		return getMaxFieldOfVisionAngle(e1, pos) <= maxAngle;
	}

	/**Returns angles if e1 was directly facing e2*/
	public static Vector2f getDirectLookAngles(Entity e1, Entity e2) {
		Vec3d e1EyePos = EntityHelper.getEntityPartialPos(e1).addVector(0, e1.getEyeHeight(), 0);
		return getDirectLookAngles(e1EyePos,  
				getClosestPointOnBoundingBox(getPositionEyes(e1), e1.getLook(1), e2));
	}

	/**Returns angles if e1 was directly facing e2*/
	public static Vector2f getDirectLookAngles(Entity e1, Entity e2, float yaw, float pitch) {
		Vec3d e1EyePos = EntityHelper.getEntityPartialPos(e1).addVector(0, e1.getEyeHeight(), 0);
		return getDirectLookAngles(e1EyePos,  
				getClosestPointOnBoundingBox(getPositionEyes(e1), EntityHelper.getLook(pitch, yaw), e2));
	}

	/**Returns angles if e1 was directly facing e2*/
	public static Vector2f getDirectLookAngles(Vec3d e1EyePos, Vec3d e2Vec) {
		double d0 = e2Vec.x - e1EyePos.x;
		double d1 = e2Vec.y - e1EyePos.y;
		double d2 = e2Vec.z - e1EyePos.z;
		double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
		float facingYaw = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
		float facingPitch = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
		return new Vector2f(facingYaw, facingPitch);
	}

	/**Get closest point on an e2's bounding box to e1's look*/
	public static Vec3d getClosestPointOnBoundingBox(Vec3d eyePos, Vec3d lookVec, Entity e2) {
		Vec3d closest = getCenter(e2.getEntityBoundingBox());
		lookVec = eyePos.add(lookVec.scale(50));
		for (double scale=0; scale<10; scale+= 0.1d) {
			AxisAlignedBB aabb = e2.getEntityBoundingBox();
			Vec3d min = new Vec3d(aabb.minX, aabb.minY, aabb.minZ);
			Vec3d max = new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ);
			Vec3d center = getCenter(aabb);
			min = min.subtract(center).scale(scale+1).add(center);
			max = max.subtract(center).scale(scale+1).add(center);
			aabb = new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
			RayTraceResult intercept = aabb.calculateIntercept(eyePos, lookVec);
			if (intercept != null) {
				//RenderManager.boundingBoxesToRender.add(aabb);
				//closest = intercept.hitVec;
				//Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, e2.world, closest.x, closest.y, closest.z, 0, 0, 0, 0xFF0000, 0xFF0000, 1, 100, 1, 1, 0, 0);
				closest = intercept.hitVec.subtract(getCenter(aabb)).scale(1/(scale+1)).add(getCenter(aabb));
				//Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, e2.world, closest.x, closest.y, closest.z, 0, 0, 0, 0x00FF00, 0x00FF00, 1, 100, 1, 1, 0, 0);
				break;
			}
		}

		return closest;
	}

	/**Returns maxAngle degrees between e1's look and e2*/
	public static float getMaxFieldOfVisionAngle(Entity e1, Entity e2, float yaw, float pitch){
		Vector2f facing = getDirectLookAngles(e1, e2, yaw, pitch);
		// calculate difference between facing and current angles
		float deltaYaw = Math.abs(MathHelper.wrapDegrees(yaw - facing.x));
		float deltaPitch = Math.abs(pitch-facing.y);
		return Math.max(deltaYaw, deltaPitch);
	}

	/**Returns maxAngle degrees between e1's look and e2*/
	public static float getMaxFieldOfVisionAngle(Entity e1, Entity e2){
		Vector2f facing = getDirectLookAngles(e1, e2);
		// calculate difference between facing and current angles
		float deltaYaw = Math.abs(MathHelper.wrapDegrees(e1.rotationYaw - facing.x));
		float deltaPitch = Math.abs(e1.rotationPitch-facing.y);
		return Math.max(deltaYaw, deltaPitch);
	}

	/**Returns maxAngle degrees between e1's look and e2*/
	public static float getMaxFieldOfVisionAngle(Entity e1, Vec3d pos){
		Vector2f facing = getDirectLookAngles(getPositionEyes(e1), pos);
		// calculate difference between facing and current angles
		float deltaYaw = Math.abs(MathHelper.wrapDegrees(e1.rotationYaw - facing.x));
		float deltaPitch = Math.abs(e1.rotationPitch-facing.y);
		return Math.max(deltaYaw, deltaPitch);
	}

	/**Get target within maxAngle degrees of being looked at by shooter*/
	@Nullable
	public static EntityLivingBase getTargetInFieldOfVision(EntityLivingBase shooter, float range, float maxAngle, boolean friendly, boolean ignoreEntityLivingBaseMW) {
		return getTargetInFieldOfVision(shooter, range, maxAngle, friendly, ignoreEntityLivingBaseMW, null);
	}

	/**Get target within maxAngle degrees of being looked at by shooter*/
	@Nullable
	public static EntityLivingBase getTargetInFieldOfVision(EntityLivingBase shooter, float range, float maxAngle, boolean friendly, boolean ignoreEntityLivingBaseMW, @Nullable Predicate<EntityLivingBase> predicate) {
		Vec3d lookVec = shooter.getLookVec().scale(range-1);
		AxisAlignedBB aabb = shooter.getEntityBoundingBox().grow(5).expand(lookVec.x, lookVec.y, lookVec.z);

		EntityLivingBase closest = null;
		float angle = Float.MAX_VALUE;
		for (Entity entity : shooter.world.getEntitiesInAABBexcluding(shooter, aabb, new Predicate<Entity>() {
			@Override
			public boolean apply(Entity input) {
				return input instanceof EntityLivingBase && EntityHelper.shouldHit(shooter, input, friendly) && !shouldIgnoreEntity(input, friendly, !ignoreEntityLivingBaseMW) && 
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
		//RenderManager.boundingBoxesToRender.clear(); 
		//RenderManager.boundingBoxesToRender.add(aabb);

		return angle <= maxAngle ? closest : null;
	}

	/**Get target within maxAngle degrees of being looked at by shooter*/
	@Nullable
	public static Vec3d getHealthPackInFieldOfVision(EntityLivingBase shooter, float range, float maxAngle) {
		Vec3d lookVec = shooter.getLookVec().scale(range-1);
		AxisAlignedBB aabb = shooter.getEntityBoundingBox().grow(5).expand(lookVec.x, lookVec.y, lookVec.z);

		RayTraceResult result = EntityHelper.getMouseOverBlock(shooter, range);
		if (result != null && shooter.world.getTileEntity(result.getBlockPos()) instanceof TileEntityHealthPack && 
				((TileEntityHealthPack)shooter.world.getTileEntity(result.getBlockPos())).canBeHacked(shooter))
			return new Vec3d(result.getBlockPos());

		BlockPos closest = null;
		float angle = Float.MAX_VALUE;
		HashSet<BlockPos> posToRemove = new HashSet<BlockPos>();
		for (BlockPos pos : TileEntityHealthPack.healthPackPositions) {
			if (pos == null || !(shooter.world.getTileEntity(pos) instanceof TileEntityHealthPack))
				posToRemove.add(pos);
			else if (aabb.contains(new Vec3d(pos).addVector(0.5d, 0, 0.5d)) && 
					Math.sqrt(shooter.getDistanceSqToCenter(pos)) < range && 
					((TileEntityHealthPack)shooter.world.getTileEntity(pos)).canBeHacked(shooter)) {
				float newAngle = EntityHelper.getMaxFieldOfVisionAngle(shooter, new Vec3d(pos).addVector(0.5d, 0, 0.5d));
				if (closest == null || newAngle < angle) {
					closest = pos;
					angle = newAngle;
				}
			}
		}
		TileEntityHealthPack.healthPackPositions.removeAll(posToRemove);

		// debug visualize
		//RenderManager.boundingBoxesToRender.clear(); 
		//RenderManager.boundingBoxesToRender.add(aabb);

		return angle <= maxAngle && closest != null ? new Vec3d(closest) : null;
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

	/**Get exact entity rotations - accounting for partial ticks and lastTickPos
	 * new Vector2f(pitch, yaw)*/
	public static Vector2f getEntityPartialRotations(Entity entity) {
		if (entity != null) {
			float partialTicks = Minewatch.proxy.getRenderPartialTicks();
			float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
			float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
			// use yawHead for EntityLivingBase - using prevRotationYawHead is bit buggy (cuz of changing to look where aiming?)
			//if (entity instanceof EntityLivingBase)
			//	yaw = ((EntityLivingBase) entity).rotationYawHead;
			return new Vector2f(pitch, yaw);
		}
		return new Vector2f(0, 0);
	}

	/**Change entity's velocity to bounce off the side hit*/
	public static void bounce(Entity entity, EnumFacing sideHit, double min, double scalar) {
		if (sideHit == null) 
			return; 

		switch(sideHit) {
		case DOWN:
			entity.motionY = -Math.max(Math.abs(entity.motionY * scalar), min);
			break;
		case EAST:
			entity.motionX = Math.max(Math.abs(entity.motionX * scalar), min);
			break;
		case NORTH:
			entity.motionZ = -Math.max(Math.abs(entity.motionZ * scalar), min);
			break;
		case SOUTH:
			entity.motionZ = Math.max(Math.abs(entity.motionZ * scalar), min);
			break;
		case UP:
			entity.motionY = Math.max(Math.abs(entity.motionY * scalar), min);
			break;
		case WEST:
			entity.motionX = -Math.max(Math.abs(entity.motionX * scalar), min);
			break;
		}
	}

	/**Attempt to teleport to position - same as EntityLivingBase#attemptTeleport except w/o moving pos to ground*/
	public static boolean attemptTeleport(Entity entity, double x, double y, double z) {
		double d0 = entity.posX;
		double d1 = entity.posY;
		double d2 = entity.posZ;
		entity.posX = x;
		entity.posY = y;
		entity.posZ = z;
		boolean flag = false;
		BlockPos blockpos = new BlockPos(entity);
		World world = entity.world;
		//Random random = entity.world.rand;

		if (world.isBlockLoaded(blockpos))
		{
			boolean flag1 = true;/*false;

            while (!flag1 && blockpos.getY() > 0)
            {
                BlockPos blockpos1 = blockpos.down();
                IBlockState iblockstate = world.getBlockState(blockpos1);

                if (iblockstate.getMaterial().blocksMovement())
                {
                    flag1 = true;
                }
                else
                {
                    --entity.posY;
                    blockpos = blockpos1;
                }
            }*/

			if (flag1)
			{
				entity.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);

				if (world.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty()/* && !world.containsAnyLiquid(entity.getEntityBoundingBox())*/)
				{
					flag = true;
				}
			}
		}

		if (!flag)
		{
			entity.setPositionAndUpdate(d0, d1, d2);
			return false;
		}
		else
		{
			//int i = 128;

			/*for (int j = 0; j < 128; ++j)
			{
				double d6 = (double)j / 127.0D;
				float f = (random.nextFloat() - 0.5F) * 0.2F;
				float f1 = (random.nextFloat() - 0.5F) * 0.2F;
				float f2 = (random.nextFloat() - 0.5F) * 0.2F;
				double d3 = d0 + (entity.posX - d0) * d6 + (random.nextDouble() - 0.5D) * (double)entity.width * 2.0D;
				double d4 = d1 + (entity.posY - d1) * d6 + random.nextDouble() * (double)entity.height;
				double d5 = d2 + (entity.posZ - d2) * d6 + (random.nextDouble() - 0.5D) * (double)entity.width * 2.0D;
				world.spawnParticle(EnumParticleTypes.PORTAL, d3, d4, d5, (double)f, (double)f1, (double)f2, new int[0]);
			}*/

			if (entity instanceof EntityCreature)
			{
				((EntityCreature)entity).getNavigator().clearPathEntity();
			}

			return true;
		}
	}

	/**Similar to {@link EntityLivingBase#canEntityBeSeen(Entity)}, but from a generic lookPos*/
	public static boolean canEntityBeSeen(Vec3d lookPos, Entity entity) {
		return entity != null && lookPos != null && canBeSeen(entity.world, lookPos, new Vec3d(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ));
	}

	/**Similar to {@link EntityLivingBase#canEntityBeSeen(Entity)}, but to a generic pos*/
	public static boolean canBeSeen(World world, Vec3d lookPos, Vec3d pos) {
		return pos != null && lookPos != null && world.rayTraceBlocks(lookPos, pos, false, true, false) == null;
	}

	/**Get prev position vector - uses chasing pos for EntityPlayer*/
	public static Vec3d getPrevPositionVector(Entity entity) {
		double x = entity instanceof EntityPlayer ? ((EntityPlayer)entity).chasingPosX : entity.prevPosX;
		double y = entity instanceof EntityPlayer ? ((EntityPlayer)entity).chasingPosY : entity.prevPosY;
		double z = entity instanceof EntityPlayer ? ((EntityPlayer)entity).chasingPosZ : entity.prevPosZ;
		return new Vec3d(x, y, z);
	}

	/**Returns entity's name*/
	public static String getName(Entity entity) {
		return entity == null ? "" : entity.getName().equalsIgnoreCase("entity.zombie.name") ? "Zombie Villager" : entity.getName();
	}

	/**Checks if there is 2 blocks of non-collidable blocks above pos*/
	public static boolean isValidTeleportLocation(BlockPos pos, World world) {
		IBlockState down = world.getBlockState(pos.down());
		IBlockState equal = world.getBlockState(pos);
		IBlockState up = world.getBlockState(pos.up());
		// valid spot found
		return !down.getBlock().isPassable(world, pos) && 
				(world.isAirBlock(pos) || equal.getCollisionBoundingBox(world, pos) == null) &&
				(world.isAirBlock(pos.up()) || up.getCollisionBoundingBox(world, pos.up()) == null);
	}

	/**Should ignore for things - namely EntityLivingBaseMW and EntityArmorStand*/
	public static boolean shouldIgnoreEntity(Entity entity) {
		return shouldIgnoreEntity(entity, false, false);
	}

	/**Should ignore for things - namely EntityLivingBaseMW and EntityArmorStand*/
	public static boolean shouldIgnoreEntity(Entity entity, boolean allowInvulnerable, boolean allowEntityLivingBaseMW) {
		return entity == null || (!allowEntityLivingBaseMW && entity instanceof EntityLivingBaseMW) || entity instanceof EntityArmorStand || 
				(!allowInvulnerable && TickHandler.hasHandler(entity, Identifier.INVULNERABLE)) ||
				(entity instanceof EntityPlayer && ((EntityPlayer)entity).isSpectator());
	}

	/**Should ignore for things - namely barriers*/
	public static boolean shouldIgnoreBlock(Block block) {
		return block == null || block == Blocks.BARRIER;
	}


	/**Modified from EntityLivingBase#collideWithNearbyEntities() to make accessible for all entities*/
	public static void collideWithNearbyEntities(Entity entityIn) {
		List<Entity> list = entityIn.world.getEntitiesInAABBexcluding(entityIn, entityIn.getEntityBoundingBox(), EntitySelectors.getTeamCollisionPredicate(entityIn));

		if (!list.isEmpty())
		{
			int i = entityIn.world.getGameRules().getInt("maxEntityCramming");

			if (i > 0 && list.size() > i - 1 && entityIn.world.rand.nextInt(4) == 0)
			{
				int j = 0;

				for (int k = 0; k < list.size(); ++k)
				{
					if (!((Entity)list.get(k)).isRiding())
					{
						++j;
					}
				}

				if (j > i - 1)
				{
					entityIn.attackEntityFrom(DamageSource.CRAMMING, 6.0F);
				}
			}

			for (int l = 0; l < list.size(); ++l)
			{
				Entity entity = list.get(l);
				entityIn.applyEntityCollision(entity);
			}
		}
	}

}