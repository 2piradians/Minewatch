package twopiradians.minewatch.common.entity.hero.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemHanzoBow;
import twopiradians.minewatch.common.util.AIHelper;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityHeroAIAttackRanged extends EntityAIBase {
	
	private final EntityHero entity;
	private final double moveSpeedAmp;
	private int attackCooldown;
	private final float maxAttackDistance;
	private int attackTime = -1;
	private int seeTime;
	private boolean strafingClockwise;
	private boolean strafingBackwards;
	private int strafingTime = -1;

	public EntityHeroAIAttackRanged(EntityHero entity, double speedAmplifier, int delay, float maxDistance) {
		this.entity = entity;
		this.moveSpeedAmp = speedAmplifier;
		this.attackCooldown = delay;
		this.maxAttackDistance = maxDistance * maxDistance;
		this.setMutexBits(3);
	}

	public void setAttackCooldown(int cooldown) {
		this.attackCooldown = cooldown;
	}

	@Override
	public boolean shouldExecute() {
		return EntityHelper.shouldHit(entity, entity.getAttackTarget(), false) && this.isBowInMainhand();
	}

	protected boolean isBowInMainhand() {
		return !this.entity.getHeldItemMainhand().isEmpty() && this.entity.getHeldItemMainhand().getItem() instanceof ItemHanzoBow;
	}

	@Override
	public boolean continueExecuting() {
		return (this.shouldExecute() || !this.entity.getNavigator().noPath()) && this.isBowInMainhand();
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
		EntityLivingBase entitylivingbase = this.entity.getAttackTarget();

		if (entitylivingbase != null) {
			double d0 = this.entity.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
			boolean flag = this.entity.getEntitySenses().canSee(entitylivingbase);
			boolean flag1 = this.seeTime > 0;

			if (flag != flag1)
				this.seeTime = 0;

			if (flag)
				++this.seeTime;
			else
				--this.seeTime;

			if (d0 <= (double)this.maxAttackDistance && this.seeTime >= 20) {
				this.entity.getNavigator().clearPathEntity();
				++this.strafingTime;
			}
			else {
				this.entity.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.moveSpeedAmp);
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
				if (d0 > (double)(this.maxAttackDistance * 0.9F))
					this.strafingBackwards = false;
				else if (d0 < (double)(this.maxAttackDistance * 0.1F))
					this.strafingBackwards = true;

				this.entity.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
				this.entity.faceEntity(entitylivingbase, 30.0F, 30.0F);
			}
			else
				this.entity.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);

			ItemStack bow = AIHelper.getHeldItems(this.entity, EnumHero.HANZO.weapon, EnumHand.MAIN_HAND);
			if (this.entity.isHandActive() && bow != null) {
				if (!flag && this.seeTime < -60)
					this.entity.resetActiveHand();
				else if (flag) {
					int i = this.entity.getItemInUseMaxCount();

					if (i >= 50 && this.isBowInMainhand()) {
						bow.getItem().onPlayerStoppedUsing(bow, this.entity.world, this.entity, this.entity.getItemInUseCount());
						this.entity.resetActiveHand();
						//this.entity.attackEntityWithRangedAttack(entitylivingbase, ItemBow.getArrowVelocity(i));
						this.attackTime = this.attackCooldown;
					}
				}
			}
			else if (--this.attackTime <= 0 && this.seeTime >= -60 && bow != null) {
				((ItemHanzoBow)bow.getItem()).onItemRightClick(this.entity.world, this.entity, EnumHand.MAIN_HAND);
				//this.entity.setActiveHand(EnumHand.MAIN_HAND);
			}
		}
	}
}