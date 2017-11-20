package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ModItems;

public class EntityJunkrat extends EntityHero {

	public EntityJunkrat(World worldIn) {
		super(worldIn, EnumHero.JUNKRAT);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackJunkrat(this, MovementType.STRAFING, 15));
	}

	public class EntityHeroAIAttackJunkrat extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackJunkrat(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			// trap
			if (this.shouldUseAbility())
				this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
			else
				this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {	
				// normal attack
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);

				// mine
				if (this.shouldUseAbility()) {
					// trigger
					if (this.entity.getHeldItemOffhand() != null && 
							this.entity.getHeldItemOffhand().getItem() == ModItems.junkrat_trigger)
						this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					// place
					else
						this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
				}
				else {
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				}
			}
			else 
				this.resetKeybinds();
		}

	}

}