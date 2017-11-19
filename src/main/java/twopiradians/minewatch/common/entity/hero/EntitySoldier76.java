package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntitySoldier76 extends EntityHero {

	public EntitySoldier76(World worldIn) {
		super(worldIn, EnumHero.SOLDIER76);
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

			RayTraceResult result = EntityHelper.getMouseOverEntity(entity, (int) Math.sqrt(this.maxAttackDistance), false);
			if (canSee && result != null && result.entityHit == target) {
				// helix rockets
				if (this.shouldUseAbility()) {
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
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
			}
		}

		@Override
		public void updateTask() {
			if (this.strafingTime == -1 && (entity.moveForward > 0 || entity.moveStrafing > 0))
				entity.setSprinting(true);
			else
				entity.setSprinting(false);

			super.updateTask();
		}

	}

}