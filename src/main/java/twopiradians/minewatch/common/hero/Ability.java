package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;

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
		if (player.world.isRemote && keybind.getCooldown(player) > 0 && keybind.isKeyDown(player) &&
				!keybind.abilityNotReadyCooldowns.containsKey(player.getPersistentID())) {
			player.playSound(ModSoundEvents.abilityNotReady, 1.0f, 1.0f);
			keybind.abilityNotReadyCooldowns.put(player.getPersistentID(), 20);
		}
		
		boolean ret = ((player.getActivePotionEffect(ModPotions.frozen) == null || 
				player.getActivePotionEffect(ModPotions.frozen).getDuration() == 0) &&
				ItemMWArmor.SetManager.playersWearingSets.containsKey(player.getPersistentID()) &&
				ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) == hero) &&
				keybind.getCooldown(player) == 0 && (keybind.isKeyDown(player) ||
		(toggled.containsKey(player.getPersistentID()) && toggled.get(player.getPersistentID())));
		
		if (ret && player.world.isRemote)
			keybind.abilityNotReadyCooldowns.put(player.getPersistentID(), 20);
		
		return ret;
	}
	
}
