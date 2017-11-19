package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.EntityTracer.EntityHeroAIAttackTracer;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityTracer extends EntityHero {

	public EntityTracer(World worldIn) {
		super(worldIn, EnumHero.TRACER);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackTracer(this, MovementType.STRAFING, 15));
	}

	public class EntityHeroAIAttackTracer extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackTracer(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			RayTraceResult result = EntityHelper.getMouseOverEntity(entity, (int) Math.sqrt(this.maxAttackDistance), false);
			if (canSee && result != null && result.entityHit == target) {
				// blink
				if (--this.attackCooldown <= 0 && this.shouldUseAbility()) {
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
					this.attackCooldown = 100;
				}
				// normal attack
				else {
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				}
			}
			else 
				this.resetKeybinds();
		}

		@Override
		public void updateTask() {
			super.updateTask();
			
			// blink to target
			if (this.strafingTime == -1 && (entity.moveForward > 0 || entity.moveStrafing > 0) &&
					--this.attackCooldown <= 0) {
				this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
				this.attackCooldown = 30;
			}
			else
				this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);

		}

	}
	
}