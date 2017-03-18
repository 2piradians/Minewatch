package twopiradians.minewatch.common;

import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.common.item.ModItems;

public class CommonProxy 
{
	public void preInit() {
		ModEntities.registerEntities();
		ModItems.init();
	}

	public void init() {
		registerCraftingRecipes();
	}

	public void postInit() {}


	private void registerCraftingRecipes() {

	}
}
