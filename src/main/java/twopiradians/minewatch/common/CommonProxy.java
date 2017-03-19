package twopiradians.minewatch.common;

import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class CommonProxy 
{
	public void preInit() {
		ModEntities.registerEntities();
		ModItems.preInit();
		ModSoundEvents.preInit();
	}

	public void init() {
		registerCraftingRecipes();
	}

	public void postInit() {}


	private void registerCraftingRecipes() {

	}
}
