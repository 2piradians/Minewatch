package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityRoadhog extends EntityHero {

	public EntityRoadhog(World worldIn) {
		super(worldIn, EnumHero.ROADHOG);
	}

	@Override 
	public void onUpdate() {
		if (!this.world.isRemote) {
			// heal
			if (((this.getHealth() < this.getMaxHealth() && this.getAttackTarget() == null) || 
					this.getHealth() < this.getMaxHealth()/2f)) 
				this.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
			else
				this.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
		}

		super.onUpdate();
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackBastion(this, MovementType.STRAFING, 15));
	}

	public class EntityHeroAIAttackBastion extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackBastion(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// primary
				if (distance < 9) {
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				}
				// secondary
				else {
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
				}

				// hook
				if (--this.attackCooldown <= 0 && distance < 17 && entity.shouldUseAbility()) {
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
					this.attackCooldown = 100;
				}
				else 
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
			}
			else 
				this.resetKeybinds();
		}

	}

}