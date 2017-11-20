package twopiradians.minewatch.common.entity.hero.ai;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMercyWeapon;
import twopiradians.minewatch.common.util.EntityHelper;

public abstract class EntityHeroAIHealBase extends EntityHeroAIAttackBase {

	public EntityHeroAIHealBase(EntityHero entity, MovementType type, float maxDistance) {
		super(entity, type, maxDistance);
	}

	@Override
	public boolean shouldExecute() {
		if (!EntityHelper.shouldHit(entity, entity.healTarget, true)||
				EntityHelper.shouldHit(entity, entity.healTarget, false) || 
				entity.healTarget == null || entity == entity.healTarget)
			return false;
		else if (entity.healTarget.getHealth() < entity.healTarget.getMaxHealth())
			return true;
		else if (entity instanceof EntityHero && ((EntityHero)entity).hero == EnumHero.MERCY) {
			// already being powered / healed
			for (EntityLivingBase entity2 : ItemMercyWeapon.beams.keySet())
				if (entity != entity2 && ItemMercyWeapon.beams.get(entity2) != null && ItemMercyWeapon.beams.get(entity2).target == entity.healTarget)
					return false;
			// if has heal target, return false
			if (entity.healTarget instanceof EntityHero && ((EntityHero)entity.healTarget).healTarget != null && 
					((EntityHero)entity.healTarget).healTarget.isEntityAlive())
				return false;
			// if has attack target, return true
			else if (entity.healTarget instanceof EntityLiving && ((EntityLiving)entity.healTarget).getAttackTarget() != null &&
					((EntityLiving)entity.healTarget).getAttackTarget().isEntityAlive())
				return true;
			// always allow players
			else
				return entity.healTarget instanceof EntityPlayer;
		}
		else
			return false;
	}

	@Nullable
	public EntityLivingBase getTarget() {
		return this.entity.healTarget;
	}

}