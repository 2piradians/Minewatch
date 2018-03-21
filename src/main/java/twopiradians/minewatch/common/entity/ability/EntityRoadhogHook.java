package twopiradians.minewatch.common.entity.ability;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.hero.RenderManager;
import twopiradians.minewatch.common.hero.RenderManager.MessageTypes;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityRoadhogHook extends EntityMW {

	private EntityLivingBase hooked;

	public EntityRoadhogHook(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityRoadhogHook(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.2f, 0.2f);
		this.lifetime = 100;
		this.setNoGravity(false);
	}

	public boolean isRetracting() {
		return this.ticksExisted > 10;
	}

	public void setRetracting() {
		if (this.ticksExisted < 11) {
			this.ticksExisted = 11;
			if (this.hooked == null && world.isRemote)
				ModSoundEvents.ROADHOG_HOOK_HIT_BLOCK.playSound(this, 2, 1, false);
		}
	}

	@Nullable
	public EntityLivingBase getHooked() {
		return hooked;
	}

	public void setHooked(EntityLivingBase entity) {
		if (entity != null) {
			// sounds
			if (!world.isRemote) {
				ModSoundEvents.ROADHOG_HOOK_HIT_ENTITY.playFollowingSound(entity, 1, 1, false);
				ModSoundEvents.ROADHOG_HOOK_HIT_ENTITY.playSound(getThrower(), 1, 1, true);
			}
			if (entity.isEntityAlive()) {
				this.hooked = entity;
				this.width = entity.width;
				this.height = entity.height;
				this.setPosition(entity.posX, entity.posY, entity.posZ);
				this.motionX = this.motionY = this.motionZ = 0;
				TickHandler.interrupt(entity);
				TickHandler.register(entity.world.isRemote, Handlers.PREVENT_INPUT.setEntity(entity).setTicks(100),
						Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks(100),
						Handlers.PREVENT_ROTATION.setEntity(entity).setTicks(100));
				if (entity.world.isRemote && entity == Minewatch.proxy.getClientPlayer())
					TickHandler.register(true, RenderManager.MESSAGES.setEntity(entity).setTicks(40).setString(TextFormatting.DARK_RED+""+TextFormatting.BOLD+"STUNNED").setNumber(MessageTypes.TOP.ordinal()));
			}
		}
		else
			this.hooked = entity;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		this.noClip = hooked == null && this.isRetracting();
		this.notDeflectible = this.noClip;

		if (getThrower() != null) {
			// face thrower
			Vector2f look = EntityHelper.getDirectLookAngles(getThrower().getPositionVector().addVector(0, getThrower().height/2f, 0), this.getPositionVector());
			this.rotationYaw = look.x;
			this.rotationPitch = look.y;

			if (this.isRetracting()) {
				Vec3d target = null;
				// retracting with no hooked
				if (this.hooked == null) 
					target = getThrower().getPositionVector().addVector(0, getThrower().height/2f-this.height/2f, 0);
				// retracting with hooked
				else {
					target = getThrower().getPositionVector().addVector(0, getThrower().height/2f-this.height/2f, 0).add(getThrower().getLookVec().scale(this.hooked.width/2f+1));
					Vector2f angles = EntityHelper.getDirectLookAngles(this.hooked, getThrower());
					this.hooked.rotationYaw = angles.x;
					this.hooked.rotationYawHead = angles.x;
					this.hooked.rotationPitch = angles.y;
					Handlers.copyRotations(this.hooked);
					this.hooked.setPosition(posX, posY, posZ);
					this.hooked.fallDistance = 0;
				}
				// move to target
				if (this.hooked == null || this.ticksExisted > 22) {
					// sounds when it starts retracting
					if (world.isRemote && ((this.hooked != null && this.ticksExisted == 23) || (this.hooked == null && this.ticksExisted == 12))) {
						ModSoundEvents.ROADHOG_HOOK_THROW.stopFollowingSound(getThrower());
						ModSoundEvents.ROADHOG_HOOK_RETRACT_ENTITY.playFollowingSound(getThrower(), 2, 1, false);
					}
					Vec3d motion = new Vec3d(target.x-this.posX, target.y-this.posY, target.z-this.posZ).scale(1.5d); 
					Vec3d normalized = motion.normalize().scale(40d/20d);
					if (normalized.lengthVector() < motion.lengthVector())
						motion = normalized;
						this.motionX = motion.x;
						this.motionY = motion.y;
						this.motionZ = motion.z;
					// kill when close to target
					if (!world.isRemote && this.getPositionVector().distanceTo(target) < 0.3d)
						this.setDead();
				}
			}
			else if (this.isCollided) {
				this.setRetracting();
			}
		}
	}
	
	@Override
	public AxisAlignedBB getImpactBoundingBox() {
		return this.getEntityBoundingBox().grow(1f);
	}

	@Override
	protected boolean isValidImpact(RayTraceResult result, boolean nearest) {
		return result != null && result.typeOfHit != RayTraceResult.Type.MISS && 
				((!isRetracting() && hooked == null && result.typeOfHit == RayTraceResult.Type.ENTITY && EntityHelper.shouldHit(getThrower(), result.entityHit, isFriendly) && nearest && !EntityHelper.shouldIgnoreEntity(result.entityHit)) || // hook target
						(isRetracting() && hooked == null && result.typeOfHit == Type.ENTITY && result.entityHit == getThrower()) || // finished retracting
						(!this.noClip && result.typeOfHit == Type.BLOCK)); // hit block 
	}

	@Override
	protected void onImpactMoveToHitPosition(RayTraceResult result) {}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		// hit block - start retracting
		if (result.typeOfHit == Type.BLOCK) {
			if (this.isRetracting() && !world.isRemote)
				this.setDead();
			this.setRetracting();
		}
		// done retracting with no hooked entity (should be caught in onUpdate, but this is backup)
		else if (!world.isRemote && result.entityHit == getThrower())
			this.setDead();
		// hit entity to hook (server only)
		else if (result.entityHit instanceof EntityLivingBase && !EntityHelper.shouldIgnoreEntity(result.entityHit) && 
				EntityHelper.attemptDamage(this, result.entityHit, 30, true)) {
			this.setHooked((EntityLivingBase) result.entityHit);
			this.setRetracting();
			Minewatch.network.sendToDimension(new SPacketSimple(77, this, false, this.hooked), world.provider.getDimension());
		}
	}
	
	@Override
	public void onDeflect() {
		super.onDeflect();
		
		this.setRetracting();
		Minewatch.network.sendToDimension(new SPacketSimple(76, this, false, this.hooked), world.provider.getDimension());
	}

}