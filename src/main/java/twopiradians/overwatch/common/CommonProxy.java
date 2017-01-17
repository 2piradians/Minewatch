package twopiradians.overwatch.common;

import twopiradians.overwatch.common.entity.ModEntities;
import twopiradians.overwatch.common.item.ModItems;

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
