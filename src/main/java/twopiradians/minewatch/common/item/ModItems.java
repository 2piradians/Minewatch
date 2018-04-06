package twopiradians.minewatch.common.item;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.block.ModBlocks;
import twopiradians.minewatch.common.block.invis.BlockDeath;
import twopiradians.minewatch.common.block.invis.BlockPush;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.creativetab.IMinewatchTab;

public class ModItems {

	public static ArrayList<Item> staticModelItems  = new ArrayList<Item>();
	public static ArrayList<IChangingModel> changingModelItems  = new ArrayList<IChangingModel>();
	public static ArrayList<Item> allItems  = new ArrayList<Item>();

	public static Item wild_card_token;
	public static Item genji_shuriken_single; // used for projectile
	public static Item junkrat_trigger; // used with Junkrat's mine
	public static Item sombra_hack; // used with Sombra's hack
	public static Item roadhog_health; // used with Roadhog's heal
	public static Item team_stick;

	@Mod.EventBusSubscriber
	public static class RegistrationHandler {

		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event) {
			// hero armor, weapons, and tokens
			for (EnumHero hero : EnumHero.values()) {
				hero.token = (ItemMWToken) registerItem(event.getRegistry(), new ItemMWToken(hero), 
						hero.name.toLowerCase()+"_token", Minewatch.tabArmorWeapons, false);
				hero.material = EnumHelper.addArmorMaterial(hero.name.toLowerCase(), 
						Minewatch.MODNAME+":"+hero.name.toLowerCase(), 20, new int[] {0,0,0,0}, 0, 
						SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0); 
				hero.helmet = (ItemMWArmor) registerItem(event.getRegistry(), new ItemMWArmor(hero, hero.material, 0, EntityEquipmentSlot.HEAD),
						hero.name.toLowerCase()+"_helmet", Minewatch.tabArmorWeapons, false);
				hero.chestplate = (ItemMWArmor) registerItem(event.getRegistry(), new ItemMWArmor(hero, hero.material, 0, EntityEquipmentSlot.CHEST), 
						hero.name.toLowerCase()+"_chestplate", Minewatch.tabArmorWeapons, false);
				hero.leggings = (ItemMWArmor) registerItem(event.getRegistry(), new ItemMWArmor(hero, hero.material, 0, EntityEquipmentSlot.LEGS), 
						hero.name.toLowerCase()+"_leggings", Minewatch.tabArmorWeapons, false);
				hero.boots = (ItemMWArmor) registerItem(event.getRegistry(), new ItemMWArmor(hero, hero.material, 0, EntityEquipmentSlot.FEET), 
						hero.name.toLowerCase()+"_boots", Minewatch.tabArmorWeapons, false);
				hero.weapon = (ItemMWWeapon) registerItem(event.getRegistry(), hero.weapon, 
						hero.name.toLowerCase()+"_weapon", Minewatch.tabArmorWeapons, true);
			}

			// misc. hero items
			wild_card_token = registerItem(event.getRegistry(), new ItemMWToken.ItemWildCardToken(), "wild_card_token", Minewatch.tabArmorWeapons, false);
			genji_shuriken_single = registerItem(event.getRegistry(), new ItemGenjiShuriken(), "genji_shuriken_single", null, true);
			((ItemGenjiShuriken)genji_shuriken_single).hero = EnumHero.GENJI;
			junkrat_trigger = registerItem(event.getRegistry(), new ItemJunkratTrigger(), "junkrat_trigger", null, true);
			sombra_hack = registerItem(event.getRegistry(), new ItemSombraHack(), "sombra_hack", null, true);
			roadhog_health = registerItem(event.getRegistry(), new ItemRoadhogHealth(), "roadhog_health", null, true);

			// other items
			team_stick = registerItem(event.getRegistry(), new ItemTeamStick(), "team_stick", Minewatch.tabMapMaking, false);

			// item blocks
			registerItem(event.getRegistry(), new ItemBlock(ModBlocks.healthPackSmall), ModBlocks.healthPackSmall.getUnlocalizedName().replace("tile.", ""), Minewatch.tabMapMaking, false);
			registerItem(event.getRegistry(), new ItemBlock(ModBlocks.healthPackLarge), ModBlocks.healthPackLarge.getUnlocalizedName().replace("tile.", ""), Minewatch.tabMapMaking, false);
			registerItem(event.getRegistry(), new ItemBlock(ModBlocks.teamSpawn), ModBlocks.teamSpawn.getUnlocalizedName().replace("tile.", ""), Minewatch.tabMapMaking, false);
			registerItem(event.getRegistry(), new BlockDeath.ItemBlockDeath(ModBlocks.deathBlock), ModBlocks.deathBlock.getUnlocalizedName().replace("tile.", ""), Minewatch.tabMapMaking, false);
			registerItem(event.getRegistry(), new BlockPush.ItemBlockPush(ModBlocks.pushBlock), ModBlocks.pushBlock.getUnlocalizedName().replace("tile.", ""), Minewatch.tabMapMaking, false);
		}
	}

	private static Item registerItem(IForgeRegistry<Item> registry, Item item, String unlocalizedName, @Nullable IMinewatchTab tab, boolean usesObjModel) {
		if (item instanceof IChangingModel)
			changingModelItems.add((IChangingModel) item);
		else
			staticModelItems.add(item);
		allItems.add(item);
		item.setUnlocalizedName(unlocalizedName);
		item.setRegistryName(Minewatch.MODID, unlocalizedName);
		if (tab != null && tab instanceof CreativeTabs) {
			item.setCreativeTab((CreativeTabs) tab);
			tab.getOrderedStacks().add(new ItemStack(item));
		}
		registry.register(item);
		return item;
	}

}
