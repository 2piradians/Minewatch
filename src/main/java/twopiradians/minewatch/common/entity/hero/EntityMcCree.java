package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;

public class EntityMcCree extends EntityHero {

	public EntityMcCree(World worldIn) {
		super(worldIn, EnumHero.MCCREE);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackMcCree(this, MovementType.STRAFING, 15));
	}

	public class EntityHeroAIAttackMcCree extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackMcCree(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// fan the hammer
				if (entity.isHandActive() || (entity.hero.weapon.getCurrentAmmo(entity) >= 4 &&
						distance <= 5 && this.shouldUseAbility())) {
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				}
				// normal attack
				else {
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				}
				// roll
				if (distance <= 7 && entity.hero.weapon.getCurrentAmmo(entity) <= 2 && this.shouldUseAbility())
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
			}
			else
				this.resetKeybinds();
		}

	}

}