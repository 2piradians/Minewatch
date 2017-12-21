package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityMei extends EntityHero {

	public EntityMei(World worldIn) {
		super(worldIn, EnumHero.MEI);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackMei(this, MovementType.STRAFING, 15));
	}

	@Override
	public void onUpdate() {		
		// crystal
		if (!world.isRemote)
			if (this.getHealth() < this.getMaxHealth() / 2f && !TickHandler.hasHandler(this, Identifier.MEI_CRYSTAL))
				this.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
			else
				this.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);

		super.onUpdate();
	}

	public class EntityHeroAIAttackMei extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackMei(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance) &&
					(!TickHandler.hasHandler(entity, Identifier.MEI_CRYSTAL) || entity.getHealth() >= entity.getMaxHealth())) {	
				// icicle attack
				Handler handler = TickHandler.getHandler(target, Identifier.POTION_FROZEN);
				if ((handler != null && handler.ticksLeft >= 30) || distance >= 10) {
					this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
					this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
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

	}

}