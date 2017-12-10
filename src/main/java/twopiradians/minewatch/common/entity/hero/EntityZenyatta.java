package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

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
	}

	public class EntityHeroAIAttackZenyatta extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackZenyatta(EntityHero entity, MovementType type, float maxDistance) {
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
		}
		
	}

}