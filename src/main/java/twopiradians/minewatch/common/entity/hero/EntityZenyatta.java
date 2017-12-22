package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIHealBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAINearestHealableTarget;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityZenyatta extends EntityHero {

	public EntityZenyatta(World worldIn) {
		super(worldIn, EnumHero.ZENYATTA);
	}

	@Override
	public void onUpdate() {

		super.onUpdate();
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackZenyatta(this, MovementType.STRAFING, 15));
		this.tasks.addTask(1, new EntityHeroAIHealZenyatta(this, MovementType.HEAL, 15));
		this.targetTasks.addTask(2, new EntityHeroAINearestHealableTarget(this, EntityLivingBase.class, true));
	}

	public class EntityHeroAIAttackZenyatta extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackZenyatta(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget()) 
				if (distance <= Math.sqrt(this.maxAttackDistance)) {
					// discord
					if (this.entity.shouldUseAbility() && !TickHandler.hasHandler(handler -> handler.identifier == Identifier.ZENYATTA_DISCORD && handler.entityLiving == target, false))
						this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
					else
						this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);

					// normal attack
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				}
				else { // charge volley if out of range
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
				}
			else 
				this.resetKeybinds();
		}

	}

	public class EntityHeroAIHealZenyatta extends EntityHeroAIHealBase {

		public EntityHeroAIHealZenyatta(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		public boolean shouldExecute() {
			return super.shouldExecute() && !TickHandler.hasHandler(handler -> handler.identifier == Identifier.ZENYATTA_HARMONY && handler.entityLiving == entity.healTarget, false);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// harmony
				this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);

				this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
			}
			else 
				this.resetKeybinds();
		}

	}
}