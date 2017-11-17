package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityReinhardt extends EntityHero {

	public EntityReinhardt(World worldIn) {
		super(worldIn, EnumHero.REINHARDT);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackReinhardt(this, MovementType.MELEE, 1.0D, 40, 3));
	}

	public class EntityHeroAIAttackReinhardt extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackReinhardt(EntityHero entity, MovementType type, double speedAmplifier, int delay, float maxDistance) {
			super(entity, type, speedAmplifier, delay, maxDistance);
		}

		@Override
		public void resetTask() {
			super.resetTask();
			this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
			this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (--this.attackTime <= 0 && canSee) {
				ItemStack stack = entity.getHeldItemMainhand();
				if (stack != null && stack.getItem() == EnumHero.REINHARDT.weapon && 
						EnumHero.REINHARDT.weapon.canUse(entity, false, EnumHand.MAIN_HAND, false)) {
					// basic attack
					if (distance <= 5) {
						this.entity.swingArm(EnumHand.MAIN_HAND);
						this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					}
					// fire strike
					if (this.shouldUseAbility())
						this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
					else
						this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
				}
				else
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
			}
			else 
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
		}
	}

}