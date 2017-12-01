package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;

public class EntityHanzo extends EntityHero {

	public EntityHanzo(World worldIn) {
		super(worldIn, EnumHero.HANZO);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackHanzo(this, MovementType.STRAFING, 15));
	}
	
	/**May be used in the future*/
	@Override
	public boolean shouldUseAbility() {
		return this.getRNG().nextBoolean();
	}

	public class EntityHeroAIAttackHanzo extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackHanzo(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);
			
			if (distance <= Math.sqrt(this.maxAttackDistance)) {
				if (this.entity.isHandActive()) {
					// wall-climb
					this.entity.getDataManager().set(KeyBind.JUMP.datamanager, true);
					
					// stop pulling bow
					if (!canSee && this.seeTime < -60)
						this.entity.resetActiveHand();
					// attack
					else if (canSee) {
						int i = this.entity.getItemInUseMaxCount();

						if (i >= 50) {
							this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
							// sonic
							if ((target.getActivePotionEffect(MobEffects.GLOWING) == null || 
									target.getActivePotionEffect(MobEffects.GLOWING).getDuration() == 0) && 
									shouldUseAbility()) {
								this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
								this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
							}
							// scatter
							else if (shouldUseAbility() && KeyBind.ABILITY_2.getCooldown(entity) == 0) {
								this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
								this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
							}
							this.attackCooldown = 20;
						}
					}
				}
				// pull back bow
				else if (--this.attackCooldown <= 0 && this.seeTime >= -60) 
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
			}
			else
				this.resetKeybinds();
			
			if (entity.hero.ability1.isSelected(entity)) 
				this.lookYOffset = (float) (-target.getEyeHeight()-0.1d);
			else
				this.lookYOffset = 0;
		}
	}

}