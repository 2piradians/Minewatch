package twopiradians.minewatch.common.entity.hero;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.EntityMercy.EntityHeroAIAttackMercy;
import twopiradians.minewatch.common.entity.hero.EntityMercy.EntityHeroAIHealMercy;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIHealBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAINearestHealableTarget;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.item.weapon.ItemMercyWeapon;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityMercy extends EntityHero {

	public EntityMercy(World worldIn) {
		super(worldIn, EnumHero.MERCY);
	}

	@Override
	public void onUpdate() {
		if (!this.onGround)
			this.getDataManager().set(KeyBind.JUMP.datamanager, true);
		else
			this.getDataManager().set(KeyBind.JUMP.datamanager, false);
		
		super.onUpdate();
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackMercy(this, MovementType.STRAFING, 40));
		this.tasks.addTask(1, new EntityHeroAIHealMercy(this, MovementType.HEAL, 40));
		this.targetTasks.addTask(2, new EntityHeroAINearestHealableTarget(this, EntityLivingBase.class, true));
	}

	public class EntityHeroAIAttackMercy extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackMercy(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) { 
				// change to damage
				if (!ItemMWWeapon.isAlternate(entity.getHeldItemMainhand()))
					ItemMWWeapon.setAlternate(entity.getHeldItemMainhand(), true);
				// normal attack
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
			}
			else 
				this.resetKeybinds();
		}

	}

	public class EntityHeroAIHealMercy extends EntityHeroAIHealBase {

		public EntityHeroAIHealMercy(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// change to heal
				if (ItemMWWeapon.isAlternate(entity.getHeldItemMainhand()))
					ItemMWWeapon.setAlternate(entity.getHeldItemMainhand(), false);
				// normal heal
				if (target.getHealth() < target.getMaxHealth()) {
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				}
				// power
				else {
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
				}
				// stop healing if target changes
				if (ItemMercyWeapon.beams.get(entity) != null && 
						ItemMercyWeapon.beams.get(entity).target != entity.healTarget)
					this.resetKeybinds();
				// fly to target
				if (distance > 10 && !TickHandler.hasHandler(entity, Identifier.MERCY_ANGEL))
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
			}
			else 
				this.resetKeybinds();
		}

	}

}