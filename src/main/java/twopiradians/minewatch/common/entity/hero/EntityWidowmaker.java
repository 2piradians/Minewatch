package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityWidowmaker extends EntityHero {

	public EntityWidowmaker(World worldIn) {
		super(worldIn, EnumHero.WIDOWMAKER);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackWidowmaker(this, MovementType.STRAFING, 40));
	}

	public class EntityHeroAIAttackWidowmaker extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackWidowmaker(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			RayTraceResult result = EntityHelper.getMouseOverEntity(entity, (int) Math.sqrt(this.maxAttackDistance), false);
			if (canSee && result != null && result.entityHit == target) {
				// change to heal
				if (ItemMWWeapon.isAlternate(entity.getHeldItemMainhand()))
					ItemMWWeapon.setAlternate(entity.getHeldItemMainhand(), false);
				// scope
				if (distance > Math.sqrt(this.maxAttackDistance) / 4f) 
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
				else
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
				// poison trap
				if (this.shouldUseAbility())
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
	
}