package twopiradians.minewatch.common.entity.hero;

import net.minecraft.world.World;
import twopiradians.minewatch.common.hero.EnumHero;

public class EntityMcCree extends EntityHero {

	public EntityMcCree(World worldIn) {
		super(worldIn, EnumHero.MCCREE);
	}

	/*@Override
    protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(2, new EntityHeroAIAttackRanged(this, 1.0D, false));
	}*/
	
}