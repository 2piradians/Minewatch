package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIAttackBase.MovementType;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntitySombra extends EntityHero {

	public EntitySombra(World worldIn) {
		super(worldIn, EnumHero.SOMBRA);
	}

	@Override
	public void onUpdate() {
		if (!world.isRemote) {
			if (this.movingToHealthPack)
				this.getDataManager().set(KeyBind.RMB.datamanager, true);
			else if (this.getAttackTarget() == null)
				this.getDataManager().set(KeyBind.RMB.datamanager, false);
		}

		super.onUpdate();
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackSombra(this, MovementType.STRAFING, 15));
	}

	public class EntityHeroAIAttackSombra extends EntityHeroAIAttackBase {

		public EntityHeroAIAttackSombra(EntityHero entity, MovementType type, float maxDistance) {
			super(entity, type, maxDistance);
		}

		@Override
		protected void attackTarget(EntityLivingBase target, boolean canSee, double distance) {
			super.attackTarget(target, canSee, distance);

			// don't do anything if hacking
			if (TickHandler.hasHandler(handler->handler.identifier == Identifier.SOMBRA_HACK && handler.number > 0, false)) {
				this.resetKeybinds();
				this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
			}
			else {
				// invisibility
				if (distance > Math.sqrt(this.maxAttackDistance) && entity.shouldUseAbility()) {
					this.resetKeybinds();
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, true);
				}
				else {
					this.entity.getDataManager().set(KeyBind.ABILITY_1.datamanager, false);

					if (canSee && this.isFacingTarget() && distance <= Math.sqrt(this.maxAttackDistance)) {	
						// hack target
						if (this.attackCooldown > 0)
							this.entity.getDataManager().set(KeyBind.RMB.datamanager, true);
						else
							this.entity.getDataManager().set(KeyBind.RMB.datamanager, false);
						// normal attack
						if (!TickHandler.hasHandler(entity, Identifier.SOMBRA_INVISIBLE) && --this.attackCooldown <= 0) {
							this.entity.getDataManager().set(KeyBind.LMB.datamanager, true);
							// stop attacking to attempt hack
							if (this.entity.world.rand.nextInt(10) == 0 && 
									this.entity.hero.ability1.keybind.getCooldown(entity) <= 0 &&
									!TickHandler.hasHandler(target, Identifier.SOMBRA_HACKED))
								this.attackCooldown = 10;
						}
						else
							this.entity.getDataManager().set(KeyBind.LMB.datamanager, false);
					}
					else 
						this.resetKeybinds();
				}
			}
		}

		@Override
		public void updateTask() {

			super.updateTask();
		}

	}

}