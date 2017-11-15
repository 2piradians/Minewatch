package twopiradians.minewatch.common.entity.hero.ai;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityHeroAIHurtByTarget extends EntityAIHurtByTarget {

	public EntityHeroAIHurtByTarget(EntityCreature creature, boolean entityCallsForHelp, Class<?>[] targetClasses) {
		super(creature, entityCallsForHelp, targetClasses);
	}

	 @Override
	    protected boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
	    	return super.isSuitableTarget(target, includeInvincibles) && 
	    			EntityHelper.shouldTarget(this.taskOwner, target, false) && 
	    			!(target instanceof EntityHero && this.taskOwner.getTeam() == null && target.getTeam() == null);
	    }

}
