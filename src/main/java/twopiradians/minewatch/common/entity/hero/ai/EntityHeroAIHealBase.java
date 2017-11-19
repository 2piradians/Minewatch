package twopiradians.minewatch.common.entity.hero.ai;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.util.EntityHelper;

public abstract class EntityHeroAIHealBase extends EntityHeroAIAttackBase {

	public EntityHeroAIHealBase(EntityHero entity, MovementType type, float maxDistance) {
		super(entity, type, maxDistance);
	}

	@Override
	public boolean shouldExecute() {
		return EntityHelper.shouldHit(entity, entity.healTarget, true) && 
				!EntityHelper.shouldHit(entity, entity.healTarget, false) && entity.healTarget != null && 
				entity.isEntityAlive() && entity.healTarget.isEntityAlive() && 
				entity.healTarget.getHealth() < entity.healTarget.getMaxHealth() &&
				entity != entity.healTarget;
	}
	
	@Nullable
	public EntityLivingBase getTarget() {
		return this.entity.healTarget;
	}
	
}