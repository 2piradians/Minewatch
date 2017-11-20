package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.EntityGenji.EntityHeroAIAttackGenji;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

public class EntityGenji extends EntityHero {

	public EntityGenji(World worldIn) {
		super(worldIn, EnumHero.GENJI);
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

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {	
				// wall-climb
				this.entity.getDataManager().set(KeyBind.JUMP.datamanager, true);
				
				// swift strike
				if (distance <= 10 && this.shouldUseAbility())
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
				
				// deflect
				if (this.entity.hurtTime > 0 && this.shouldUseAbility())
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
				
				// triple attack
				if (distance < Math.sqrt(this.maxAttackDistance)/3f) {
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				}
				// normal attack
				else {
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				}
			}
			else 
				this.resetKeybinds();
		}

		@Override
		public void updateTask() {
			// stop moving while striking
			if (TickHandler.hasHandler(entity, Identifier.GENJI_STRIKE)) 
				this.entity.getNavigator().clearPathEntity();

			super.updateTask();
		}

	}
	
}