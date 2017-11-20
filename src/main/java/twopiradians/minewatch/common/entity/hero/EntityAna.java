package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIHealBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAINearestHealableTarget;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityAna extends EntityHero {

	public EntityAna(World worldIn) {
		super(worldIn, EnumHero.ANA);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackAna(this, MovementType.STRAFING, 40));
		this.tasks.addTask(1, new EntityHeroAIHealAna(this, MovementType.HEAL, 40));
		this.targetTasks.addTask(2, new EntityHeroAINearestHealableTarget(this, EntityLivingBase.class, true));
	}

	public class EntityHeroAIAttackAna extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackAna(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// change to damage
				if (ItemMWWeapon.isAlternate(entity.getHeldItemMainhand()))
					ItemMWWeapon.setAlternate(entity.getHeldItemMainhand(), false);
				// scope
				if (distance > Math.sqrt(this.maxAttackDistance) / 2f) 
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				// sleep dart
				if (this.shouldUseAbility() && !TickHandler.hasHandler(target, Identifier.ANA_SLEEP) &&
						!TickHandler.hasHandler(target, Identifier.ANA_DAMAGE))
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
				// normal attack
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
			}
			else 
				this.resetKeybinds();
		}

	}

	public class EntityHeroAIHealAna extends EntityHeroAIHealBase {

		public EntityHeroAIHealAna(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// change to heal
				if (!ItemMWWeapon.isAlternate(entity.getHeldItemMainhand()))
					ItemMWWeapon.setAlternate(entity.getHeldItemMainhand(), true);
				// scope
				if (distance > Math.sqrt(this.maxAttackDistance) / 2f) 
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				// normal attack
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
			}
			else 
				this.resetKeybinds();
		}

	}

}