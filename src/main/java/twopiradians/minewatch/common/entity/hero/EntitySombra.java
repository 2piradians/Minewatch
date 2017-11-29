package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

public class EntitySombra extends EntityHero {

	public EntitySombra(World worldIn) {
		super(worldIn, EnumHero.SOMBRA);
	}


	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackGenji(this, MovementType.STRAFING, 15));
	}

	public class EntityHeroAIAttackGenji extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackGenji(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			// invisibility
			if (distance > Math.sqrt(this.maxAttackDistance) && entity.shouldUseAbility()) {
				this.resetKeybinds();
				this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
			}
			else {
				this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);

				if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {	
					// normal attack
					if (!TickHandler.hasHandler(entity, Identifier.SOMBRA_INVISIBLE))
						this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					else
						this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				}
				else 
					this.resetKeybinds();
			}
		}

		@Override
		public void updateTask() {

			super.updateTask();
		}

	}

}