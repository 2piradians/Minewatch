package twopiradians.minewatch.common.entity.hero;

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
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityMercy extends EntityHero {

	public EntityMercy(World worldIn) {
		super(worldIn, EnumHero.MERCY);
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

			RayTraceResult result = EntityHelper.getMouseOverEntity(entity, (int) Math.sqrt(this.maxAttackDistance), false);
			if (canSee && result != null && result.entityHit == target) {
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

			RayTraceResult result = EntityHelper.getMouseOverEntity(entity, (int) Math.sqrt(this.maxAttackDistance), true);
			if (canSee && result != null && result.entityHit == target) {
				// change to heal
				if (ItemMWWeapon.isAlternate(entity.getHeldItemMainhand()))
					ItemMWWeapon.setAlternate(entity.getHeldItemMainhand(), false);
				// normal heal
				this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
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