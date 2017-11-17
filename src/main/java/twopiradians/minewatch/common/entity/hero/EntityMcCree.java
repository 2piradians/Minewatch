package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.EntityHanzo.EntityHeroAIAttackHanzo;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackRanged;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityMcCree extends EntityHero {

	public EntityMcCree(World worldIn) {
		super(worldIn, EnumHero.MCCREE);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackMcCree(this, 1, 20, 15));
	}

	public class EntityHeroAIAttackMcCree extends EntityHeroAIAttackRanged {

		public EntityHeroAIAttackMcCree(EntityHero entity, double speedAmplifier, int delay, float maxDistance) {
			super(entity, speedAmplifier, delay, maxDistance);
		}

		@Override
		public void resetTask() {
			super.resetTask();
			this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
			this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
			this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			RayTraceResult result = EntityHelper.getMouseOverEntity(entity, 512, false);
			if (--this.attackTime <= 0 && canSee && result != null && result.entityHit == target) {
				// fan the hammer
				if (entity.isHandActive() || (entity.hero.weapon.getCurrentAmmo(entity) >= 4 &&
						distance <= 5 && this.shouldUseAbility())) {
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
				}
				// normal attack
				else {
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				}
				// roll
				if (distance <= 7 && entity.hero.weapon.getCurrentAmmo(entity) <= 2 && this.shouldUseAbility())
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false); // TODO fix look while aiming? test with giant
			}
			else 
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
		}
	}

}