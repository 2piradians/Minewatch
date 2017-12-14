package twopiradians.minewatch.common.entity.hero.ai;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityHeroAINearestAttackableTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget {

    public EntityHeroAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight) {
    	super(creature, classTarget, checkSight);
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
    	return this.isSuitableTarget(this.targetEntity, false);
    }
	
}
