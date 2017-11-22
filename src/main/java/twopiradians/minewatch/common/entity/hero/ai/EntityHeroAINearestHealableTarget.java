package twopiradians.minewatch.common.entity.hero.ai;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMercyWeapon;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityHeroAINearestHealableTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget {

	protected EntityHero entity;

	public EntityHeroAINearestHealableTarget(EntityHero hero, Class<T> classTarget, boolean checkSight) {
		super(hero, classTarget, checkSight);
		this.entity = hero;
	}

	@Override
	protected boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
		if (!EntityHelper.shouldHit(entity, target, true)||
				EntityHelper.shouldHit(entity, target, false) || 
				target == null || entity == target)
			return false;
		else if (target.getHealth() < target.getMaxHealth())
			return true;
		else if (entity instanceof EntityHero && ((EntityHero)entity).hero == EnumHero.MERCY) {
			// already being powered / healed
			for (EntityLivingBase entity2 : ItemMercyWeapon.beams.keySet())
				if (entity2 != entity && ItemMercyWeapon.beams.get(entity2) != null && ItemMercyWeapon.beams.get(entity2).target == target)
					return false;
			// if has heal target, return false
			if (target instanceof EntityHero && ((EntityHero)target).healTarget != null && 
					((EntityHero)target).healTarget.isEntityAlive())
				return false;
			// if has attack target, return true
			else if (target instanceof EntityLiving && ((EntityLiving)target).getAttackTarget() != null &&
					((EntityLiving)target).getAttackTarget().isEntityAlive())
				return true;
			// always allow players
			else
				return target instanceof EntityPlayer;
		}
		else
			return false;
	}

	@Override
	public void startExecuting() {
		if (entity instanceof EntityHero)
			((EntityHero)entity).healTarget = this.targetEntity;
		EntityLivingBase prevTarget = entity.getAttackTarget();
		super.startExecuting();
		entity.setAttackTarget(prevTarget);
	}
	
	@Override
    public void resetTask() {
		if (entity instanceof EntityHero)
			((EntityHero)entity).healTarget = null;
        this.target = null;
    }

}
