package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.EntityReaper.EntityHeroAIAttackReaper;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ModItems;

public class EntityReaper extends EntityHero {

	public EntityReaper(World worldIn) {
		super(worldIn, EnumHero.REAPER);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackReaper(this, MovementType.STRAFING, 10));
	}

	public class EntityHeroAIAttackReaper extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackReaper(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);
			
			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {	
				// normal attack
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
			}
			else 
				this.resetKeybinds();
			

			// wraith
			if (entity.getHealth() < entity.getMaxHealth()/2f && this.shouldUseAbility()) 
				this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
			else
				this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
			
			// teleport
			if (distance > Math.sqrt(this.maxAttackDistance) && this.isFacingTarget()) {
				this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
				this.lookYOffset = -2f;
			}
			else {
				this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
				this.lookYOffset = 0f;
			}
		}

	}


}
