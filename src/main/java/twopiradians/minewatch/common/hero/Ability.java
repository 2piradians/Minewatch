package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.potion.ModPotions;

public class Ability {

	public EnumHero hero;
	public KeyBind keybind;
	public boolean isEnabled;
	public boolean isToggleable;
	public HashMap<UUID, Boolean> toggled = Maps.newHashMap();
	
	public Ability(KeyBind keybind, boolean isEnabled, boolean isToggleable) {
		this.keybind = keybind;
		this.isEnabled = isEnabled;
		this.isToggleable = isToggleable;
	}
	
	public boolean isSelected(EntityPlayer player) {
		return ((player.getActivePotionEffect(ModPotions.frozen) == null || 
				player.getActivePotionEffect(ModPotions.frozen).getDuration() == 0) &&
				ItemMWArmor.SetManager.playersWearingSets.containsKey(player.getPersistentID()) &&
				ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) == hero) &&
				keybind.getCooldown(player) == 0 && (keybind.isKeyDown(player) ||
		(toggled.containsKey(player.getPersistentID()) && toggled.get(player.getPersistentID())));
	}
	
}
