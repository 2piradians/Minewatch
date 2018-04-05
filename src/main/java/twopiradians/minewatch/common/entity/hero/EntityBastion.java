package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.ChargeManager;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityBastion extends EntityHero {

	public EntityBastion(World worldIn) {
		super(worldIn, EnumHero.BASTION);
	}

	@Override
	public void onUpdate() {
		if (!this.world.isRemote) {
			// change to sentry when no attack target
			if (this.getAttackTarget() == null && ItemMWWeapon.isAlternate(this.getHeldItemMainhand()))
				this.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
			// don't change to turret when no attack target
			else if (this.getAttackTarget() == null && !ItemMWWeapon.isAlternate(this.getHeldItemMainhand()) &&
					KeyBind.ABILITY_1.isKeyDown(this))
				this.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);

			// heal
			if (((this.getHealth() < this.getMaxHealth() && this.getAttackTarget() == null) || 
					this.getHealth() < this.getMaxHealth()/2f) && 
					ChargeManager.getCurrentCharge(this) > ChargeManager.getMaxCharge(hero)*0.7f) 
				this.getDataManager().set(KeyBind.RMB.datamanager, true);
			else 
				this.getDataManager().set(KeyBind.RMB.datamanager, false);
		}

		super.onUpdate();
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackBastion(this, MovementType.STRAFING, 15));
	}

	public class EntityHeroAIAttackBastion extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackBastion(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			// stop moving when turret
			if (ItemMWWeapon.isAlternate(entity.getHeldItemMainhand()))
				entity.getMoveHelper().action = EntityMoveHelper.Action.WAIT;

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// normal attack
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
				if (--this.attackCooldown <= 0 && entity.shouldUseAbility()) {
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
					this.attackCooldown = ItemMWWeapon.isAlternate(entity.getHeldItemMainhand()) ? 200 : 70;
				}
				else 
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
			}
			else 
				this.resetKeybinds();
		}

		@Override
		protected void resetKeybinds() {
			super.resetKeybinds();

			// change to sentry
			if (ItemMWWeapon.isAlternate(entity.getHeldItemMainhand()))
				this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
			// don't change to turret
			else if (!ItemMWWeapon.isAlternate(entity.getHeldItemMainhand()) && KeyBind.ABILITY_1.isKeyDown(entity))
				this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
		}

	}

}