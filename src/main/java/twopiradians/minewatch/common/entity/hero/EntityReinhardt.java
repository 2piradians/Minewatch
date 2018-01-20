package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityReinhardt extends EntityHero {

	public EntityReinhardt(World worldIn) {
		super(worldIn, EnumHero.REINHARDT);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackReinhardt(this, MovementType.STRAFING, 3));
	}

	public class EntityHeroAIAttackReinhardt extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackReinhardt(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget()) {
				ItemStack stack = entity.getHeldItemMainhand();
				if (stack != null && stack.getItem() == EnumHero.REINHARDT.weapon && 
						EnumHero.REINHARDT.weapon.canUse(entity, false, EnumHand.MAIN_HAND, false)) {
					if (distance <= Math.sqrt(this.maxAttackDistance)) {
						// basic attack
						if (distance <= 5) {
							this.entity.swingArm(EnumHand.MAIN_HAND);
							this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
						}
						// fire strike
						if (entity.shouldUseAbility())
							this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
						else
							this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
					}
					// charge
					if (entity.shouldUseAbility())
						this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
					else
						this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
				}
				else
					this.entity.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
			}
			else 
				this.resetKeybinds();
		}

		@Override
		protected void move(EntityLivingBase target, boolean canSee, double distanceSq) {
			if (!TickHandler.hasHandler(entity, Identifier.REINHARDT_CHARGE))
				super.move(target, canSee, distanceSq);
		}
	}

}