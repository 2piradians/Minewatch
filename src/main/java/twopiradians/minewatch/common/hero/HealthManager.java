package twopiradians.minewatch.common.hero;

import net.minecraftforge.fml.common.Mod;
import twopiradians.minewatch.common.config.Config;

@Mod.EventBusSubscriber
public class HealthManager {

	public static float getMaxHealth(EnumHero hero) {
		float health = 0;
		if (hero != null)
			health = hero.baseHealth;
		return (float) Math.max(health * Config.healthScale, 1);
	}
	
	public static float getMaxArmor(EnumHero hero) {
		float armor = 0;
		if (hero != null)
			armor = hero.baseArmor;
		return (float) (armor * Config.healthScale);
	}
	
	public static float getMaxShield(EnumHero hero) {
		float shield = 0;
		if (hero != null)
			shield = hero.baseShield;
		return (float) (shield * Config.healthScale);
	}

	public static float getMaxTotalHealth(EnumHero hero) {
		return getMaxHealth(hero)+getMaxArmor(hero)+getMaxShield(hero);
	}
	
}