package twopiradians.minewatch.common.item.armor;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemMWArmor extends ItemArmor 
{
	public EnumHero hero;

	public static final EntityEquipmentSlot[] SLOTS = new EntityEquipmentSlot[] 
			{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

	public ItemMWArmor(EnumHero hero, ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.hero = hero;
	}

	@Mod.EventBusSubscriber
	public static class SetManager {
		/**List of players wearing full sets and the sets that they are wearing*/
		public static HashMap<UUID, EnumHero> playersWearingSets = Maps.newHashMap();	

		/**Update playersWearingSets each tick
		 * This way it's only checked once per tick, no matter what:
		 * very useful for checking if HUDs should be rendered*/
		@SubscribeEvent
		public static void updateSets(TickEvent.PlayerTickEvent event) {			
			if (event.phase == TickEvent.Phase.START) {
				//detect if player is wearing a set
				ItemStack helm = event.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
				EnumHero hero = null;
				boolean fullSet = helm != null && helm.getItem() instanceof ItemMWArmor;
				if (fullSet) {
					hero = ((ItemMWArmor)helm.getItem()).hero;
					for (EntityEquipmentSlot slot : SLOTS) {
						ItemStack armor = event.player.getItemStackFromSlot(slot);
						if (armor == null || !(armor.getItem() instanceof ItemMWArmor)
								|| ((ItemMWArmor)(armor.getItem())).hero != hero) 
							fullSet = false;
					}
				}

				// update playersWearingSets
				if (fullSet)
					SetManager.playersWearingSets.put(event.player.getPersistentID(), hero);
				else
					SetManager.playersWearingSets.remove(event.player.getPersistentID());
			}
		}
	}
}
