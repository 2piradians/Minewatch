package twopiradians.minewatch.common.entity.hero.ai;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityHeroAINearestHealableTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget {

	public EntityHeroAINearestHealableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight) {
		super(creature, classTarget, checkSight);
	}

	@Override
	protected boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
		return EntityHelper.shouldHit(this.taskOwner, target, true) &&
				!EntityHelper.shouldHit(this.taskOwner, target, false) && 
				target != null && target.getHealth() < target.getMaxHealth() && 
				((this.taskOwner.getAttackTarget() == null || !this.taskOwner.getAttackTarget().isEntityAlive()) || 
						target.getHealth() <= target.getMaxHealth()/2f) &&
				this.taskOwner != target;
	}

	public void startExecuting() {
		if (this.taskOwner instanceof EntityHero)
			((EntityHero)this.taskOwner).healTarget = this.targetEntity;
		EntityLivingBase prevTarget = this.taskOwner.getAttackTarget();
		super.startExecuting();
		this.taskOwner.setAttackTarget(prevTarget);
	}

}
