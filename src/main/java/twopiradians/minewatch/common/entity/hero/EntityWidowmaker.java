package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.UltimateManager;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityWidowmaker extends EntityHero {

	public EntityWidowmaker(World worldIn) {
		super(worldIn, EnumHero.WIDOWMAKER);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackWidowmaker(this, MovementType.STRAFING, 40));
	}

	public class EntityHeroAIAttackWidowmaker extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackWidowmaker(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) { 
				// scope
				if (distance > Math.sqrt(this.maxAttackDistance) / 4f) 
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				// poison trap
				if (entity.shouldUseAbility())
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
				// normal attack
				if (!this.entity.getDataManager().get(KeyBind.RMB.datamanager) || --this.attackCooldown <= 0) {
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					this.attackCooldown = 40;
				}
				else
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
			}
			else 
				this.resetKeybinds();
			
			// ultimate
			if (entity.shouldUseAbility() && UltimateManager.canUseUltimate(entity) && 
					!TickHandler.hasHandler(entity, Identifier.WIDOWMAKER_ULTIMATE))
				this.entity.getDataManager().set(KeyBind.ULTIMATE.datamanager, true);
			else
				this.entity.getDataManager().set(KeyBind.ULTIMATE.datamanager, false);
		}

	}

}