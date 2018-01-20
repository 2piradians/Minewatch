package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;

public class EntitySoldier76 extends EntityHero {

	public EntitySoldier76(World worldIn) {
		super(worldIn, EnumHero.SOLDIER76);
	}

	@Override
	public void onUpdate() {
		if (!world.isRemote) {
			// heal
			if (this.getHealth() < this.getMaxHealth()/2f || 
					(this.getHealth() < this.getMaxHealth() && this.getAttackTarget() == null))
				this.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
			else
				this.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
		}

		super.onUpdate();
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackSoldier76(this, MovementType.STRAFING, 15));
	}

	public class EntityHeroAIAttackSoldier76 extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackSoldier76(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// helix rockets
				if (entity.shouldUseAbility()) {
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				}
				// normal attack
				else {
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				}
			}
			else {
				this.resetKeybinds();
				this.entity.setSprinting(true);
			}
		}

	}

}