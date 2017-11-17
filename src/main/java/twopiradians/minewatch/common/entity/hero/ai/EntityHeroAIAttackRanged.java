package twopiradians.minewatch.common.entity.hero.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityHeroAIAttackRanged extends EntityAIBase {
	
	protected final EntityHero entity;
	protected final double moveSpeedAmp;
	/**Delay between attacks*/
	protected int attackCooldown;
	protected final float maxAttackDistance;
	/**Current time between attacks*/
	protected int attackTime = -1;
	protected int seeTime;
	protected boolean strafingClockwise;
	protected boolean strafingBackwards;
	protected int strafingTime = -1;

	public EntityHeroAIAttackRanged(EntityHero entity, double speedAmplifier, int delay, float maxDistance) {
		this.entity = entity;
		this.moveSpeedAmp = speedAmplifier;
		this.attackCooldown = delay;
		this.maxAttackDistance = maxDistance * maxDistance; //XXX customizable
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		return EntityHelper.shouldHit(entity, entity.getAttackTarget(), false);
	}

	@Override
	public boolean continueExecuting() {
		return (this.shouldExecute() || !this.entity.getNavigator().noPath());
	}

	@Override
	public void resetTask() {
		super.resetTask();
		this.seeTime = 0;
		this.attackTime = -1;
		this.entity.resetActiveHand();
	}

	@Override
	public void updateTask() {
		EntityLivingBase target = this.entity.getAttackTarget();

		if (target != null) {
			double distanceSq = this.entity.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
			boolean canSee = this.entity.getEntitySenses().canSee(target);
			boolean positiveSeeTime = this.seeTime > 0;

			if (canSee != positiveSeeTime)
				this.seeTime = 0;

			if (canSee)
				++this.seeTime;
			else
				--this.seeTime;

			if (distanceSq <= (double)this.maxAttackDistance && this.seeTime >= 20) {
				this.entity.getNavigator().clearPathEntity();
				++this.strafingTime;
			}
			else {
				this.entity.getNavigator().tryMoveToEntityLiving(target, this.moveSpeedAmp);
				this.strafingTime = -1;
			}

			if (this.strafingTime >= 20) {
				if ((double)this.entity.getRNG().nextFloat() < 0.3D)
					this.strafingClockwise = !this.strafingClockwise;
				if ((double)this.entity.getRNG().nextFloat() < 0.3D)
					this.strafingBackwards = !this.strafingBackwards;
				this.strafingTime = 0;
			}

			if (this.strafingTime > -1) {
				if (distanceSq > (double)(this.maxAttackDistance * 0.9F))
					this.strafingBackwards = false;
				else if (distanceSq < (double)(this.maxAttackDistance * 0.1F))
					this.strafingBackwards = true;

				this.entity.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
				this.entity.faceEntity(target, 30.0F, 30.0F);
			}
			else
				this.entity.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
			
			this.attackTarget(target, canSee, Math.sqrt(distanceSq));
		}
	}

	protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {}
	
	protected boolean shouldUseAbility() {
		return entity.getRNG().nextInt(5) == 0; // XXX customizable
	}
	
}