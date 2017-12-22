package twopiradians.minewatch.common.entity.hero.ai;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityHeroAINearestAttackableTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget {

	/**Added because the other one is private*/
	private int targetUnseenTicks;

	public EntityHeroAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight) {
		super(creature, classTarget, checkSight);
		this.unseenMemoryTicks = 120;
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		this.targetUnseenTicks = 0;
	}

	@Override
	protected boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
		return super.isSuitableTarget(target, includeInvincibles) && 
				EntityHelper.shouldHit(this.taskOwner, target, false) && target != this.taskOwner && 
				!TickHandler.hasHandler(target, Identifier.ANA_SLEEP) && 
				(!TickHandler.hasHandler(target, Identifier.SOMBRA_INVISIBLE) || 
						this.taskOwner.getDistanceToEntity(target) < 5) && 
				!TickHandler.hasHandler(target, Identifier.MEI_CRYSTAL);
	}

	@Override
	public boolean continueExecuting() {
		// check if it can see the target
		if (this.shouldCheckSight)
			if (this.taskOwner.getEntitySenses().canSee(this.targetEntity))
				this.targetUnseenTicks = 0;
			else if (++this.targetUnseenTicks > this.unseenMemoryTicks) 
				return false;

		// ignore sight when checking (because that's handled above)
		this.shouldCheckSight = false;
		boolean ret = this.isSuitableTarget(this.targetEntity, false);
		this.shouldCheckSight = true;
		return ret;
	}

}
