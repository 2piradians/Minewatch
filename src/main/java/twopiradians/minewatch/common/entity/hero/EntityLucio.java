package twopiradians.minewatch.common.entity.hero;

import java.util.ArrayList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIHealBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAINearestHealableTarget;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityLucio extends EntityHero {

	public ArrayList<EntityLivingBase> affectedEntities = new ArrayList<EntityLivingBase>();

	public EntityLucio(World worldIn) {
		super(worldIn, EnumHero.LUCIO);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(1, new EntityHeroAIAttackLucio(this, MovementType.STRAFING, 15));
		this.tasks.addTask(2, new EntityHeroAIHealLucio(this, MovementType.HEAL, 8));
		this.targetTasks.addTask(2, new EntityHeroAINearestHealableTarget(this, EntityLivingBase.class, true));
	}

	@Override
	public void onUpdate() {
		if (!this.worldObj.isRemote) {
			// change to speed when no heal target and full health
			if (this.healTarget == null && this.getHealth() >= this.getMaxHealth() && 
					ItemMWWeapon.isAlternate(this.getHeldItemMainhand()))
				this.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
			// change to heal when healtarget or less than full health
			else if (((this.healTarget != null && this.getDistanceToEntity(this.healTarget) <= 10) || this.getHealth() < this.getMaxHealth()) && 
					!ItemMWWeapon.isAlternate(this.getHeldItemMainhand()))
				this.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
			else
				this.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);
			// amp
			if (this.shouldUseAbility() && 
					(this.getAttackTarget() != null || ItemMWWeapon.isAlternate(this.getHeldItemMainhand())))
				this.getDataManager().set(KeyBind.ABILITY_2.datamanager, true);
			else
				this.getDataManager().set(KeyBind.ABILITY_2.datamanager, false);
		}
		
		super.onUpdate();
	}

	public class EntityHeroAIAttackLucio extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackLucio(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {
				// soundwave
				if (distance < 5 && entity.shouldUseAbility()) 
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
	
	public class EntityHeroAIHealLucio extends EntityHeroAIHealBase {

		public EntityHeroAIHealLucio(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}
		
	}
	
}