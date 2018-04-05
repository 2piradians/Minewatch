package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.UltimateManager;

public class EntityPharah extends EntityHero {

	public EntityPharah(World worldIn) {
		super(worldIn, EnumHero.PHARAH);
	}

	@Override 
	public void onUpdate() {
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

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// primary
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);

				// boop
				if (--this.attackCooldown <= 0 && entity.shouldUseAbility()) {
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
					this.attackCooldown = 100;
				}
				else 
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
				
				if (target.posY >= entity.posY) {
					// jet pack
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					
					// jet
					if (entity.shouldUseAbility())
						this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
					else
						this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
				}
				else {
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
				}
				
				// ultimate
				if (distance < 20 && entity.shouldUseAbility() && UltimateManager.canUseUltimate(entity))
					this.entity.getDataManager().set(KeyBind.ULTIMATE.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.ULTIMATE.datamanager, false);
			}
			else 
				this.resetKeybinds();
		}

	}

}