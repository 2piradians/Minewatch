package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIHealBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAINearestHealableTarget;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityMoira extends EntityHero {

	public EntityMoira(World worldIn) {
		super(worldIn, EnumHero.MOIRA);
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

			// fade
			if (this.entity.shouldUseAbility())
				this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
			else
				this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);

			if (canSee && this.isFacingTarget()) {
				// orb
				if (this.entity.shouldUseAbility() && KeyBind.ABILITY_2.getCooldown(entity) <= 0) {
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				}
				// normal attack
				else if (distance <= Math.sqrt(this.maxAttackDistance)) {
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				}
				else {
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
				}


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
			return super.shouldExecute() && this.entity.hero.weapon.getCurrentCharge(this.entity) > 0;
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget()) { 
				// orb
				if (this.entity.shouldUseAbility() && KeyBind.ABILITY_2.getCooldown(entity) <= 0) {
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
				}// heal
				else if (distance <= Math.sqrt(this.maxAttackDistance)*0.7f) {
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
				}
				else {
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				}


			}
			else 
				this.resetKeybinds();
		}

	}
}