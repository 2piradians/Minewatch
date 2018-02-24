package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemDoomfistWeapon;

public class EntityDoomfist extends EntityHero {

	public EntityDoomfist(World worldIn) {
		super(worldIn, EnumHero.DOOMFIST);
	}
	
	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackDoomfist(this, MovementType.STRAFING, 15));
	}

	public class EntityHeroAIAttackDoomfist extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackDoomfist(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);
			
			// punching
			if (entity.isHandActive()) {
				if (ItemDoomfistWeapon.getCharge(entity) >= 1)
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
			}
			else if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// normal attack
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
				// uppercut
				if (entity.shouldUseAbility() && distance < 3) 
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
				else 
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
				// slam
				if (entity.shouldUseAbility())
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
				else 
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
				// punch
				if (entity.hero.ability1.keybind.getCooldown(entity) <= 0 && entity.shouldUseAbility()) {
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				}
				else 
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
			}
			else 
				this.resetKeybinds();
		}

	}

}