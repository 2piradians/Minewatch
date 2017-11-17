package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackRanged;
import twopiradians.minewatch.common.hero.EnumHero;

public class EntityHanzo extends EntityHero {

	public EntityHanzo(World worldIn) {
		super(worldIn, EnumHero.HANZO);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackHanzo(this, 1, 20, 15));
	}

	public class EntityHeroAIAttackHanzo extends EntityHeroAIAttackRanged {

		public EntityHeroAIAttackHanzo(EntityHero entity, double speedAmplifier, int delay, float maxDistance) {
			super(entity, speedAmplifier, delay, maxDistance);
		}
		
		@Override
		public void resetTask() {
			super.resetTask();
			this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);
			
			if (this.entity.isHandActive()) {
				// stop pulling bow
				if (!canSee && this.seeTime < -60)
					this.entity.resetActiveHand();
				// attack
				else if (canSee) {
					int i = this.entity.getItemInUseMaxCount();

					if (i >= 50) {
						this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
						// sonic
						if (shouldUseAbility()) {
							this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
							this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
						}
						// scatter
						else if (shouldUseAbility() && KeyBind.ABILITY_2.getCooldown(entity) == 0) {
							this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
							this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
							this.entity.getLookHelper().setLookPosition(target.posX, target.posY-0.5d, target.posZ, 30, 30);
						}
						this.attackTime = this.attackCooldown;
					}
				}
			}
			// pull back bow
			else if (--this.attackTime <= 0 && this.seeTime >= -60) 
				this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
		}
	}

}