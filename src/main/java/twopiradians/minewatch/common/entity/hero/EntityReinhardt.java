package twopiradians.minewatch.common.entity.hero;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityReinhardt extends EntityHero {

	public EntityReinhardt(World worldIn) {
		super(worldIn, EnumHero.REINHARDT);
	}
	
	@Override
    protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackReinhardt(this, 1.0D, false));
	}
	
	private class EntityHeroAIAttackReinhardt extends EntityAIAttackMelee {

		public EntityHeroAIAttackReinhardt(EntityCreature creature, double speedIn, boolean useLongMemory) {
			super(creature, speedIn, useLongMemory);
		}
		
		@Override
		protected void checkAndPerformAttack(EntityLivingBase entity, double distanceSq) {
	        double reachSq = this.getAttackReachSqr(entity);

	        if (distanceSq <= reachSq && this.attackTick <= 0) {
	            this.attackTick = 40;
	            if (this.attacker.getHeldItemMainhand() != null && 
	            		this.attacker.getHeldItemMainhand().getItem() instanceof ItemMWWeapon) {
	            	((ItemMWWeapon)this.attacker.getHeldItemMainhand().getItem()).onItemLeftClick(this.attacker.getHeldItemMainhand(), world, this.attacker, EnumHand.MAIN_HAND);
	            }
	            this.attacker.swingArm(EnumHand.MAIN_HAND);
	        }
	    }

	    protected double getAttackReachSqr(EntityLivingBase attackTarget) {
	        return super.getAttackReachSqr(attackTarget) * 2d;
	    }
		
	}

}