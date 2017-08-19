package twopiradians.minewatch.common.potion;

import java.util.ArrayList;

import net.minecraft.potion.Potion;

public class ModPotions {
	
	public static ArrayList<Potion> potions = new ArrayList<Potion>();
	
	public static PotionFrozen frozen;

	public static void init() {
		frozen = (PotionFrozen) (new PotionFrozen(true, 13565951)).setPotionName("effect.frozen");
		potions.add(frozen);
	}
}
