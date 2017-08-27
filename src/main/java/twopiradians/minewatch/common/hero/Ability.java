package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;

public class Ability {

	public EnumHero hero;
	public KeyBind keybind;
	public boolean isEnabled;
	public boolean isToggleable;
	public HashMap<UUID, Boolean> toggled = Maps.newHashMap();

	// multi use ability stuff
	public int maxUses;
	private int useCooldown;
	public HashMap<UUID, Integer> multiAbilityUses = Maps.newHashMap();
	private HashMap<UUID, Integer> multiAbilityCooldowns = Maps.newHashMap();

	public Ability(KeyBind keybind, boolean isEnabled, boolean isToggleable, int maxUses, int useCooldown) {
		this.keybind = keybind;
		this.isEnabled = isEnabled;
		this.isToggleable = isToggleable;
		this.maxUses = maxUses;
		this.useCooldown = useCooldown;

		if (this.maxUses > 0) 
			MinecraftForge.EVENT_BUS.register(this);
	}

	public int getNumber() {
		if (hero.ability1 == this)
			return 1;
		else if (hero.ability2 == this)
			return 2;
		else
			return 3;
	}

	/**Is this ability selected and able to be used (for abilities with alternate keybinds, like Tracer's dash)*/
	public boolean isSelected(EntityPlayer player, KeyBind keybind) {
		if (player.world.isRemote && this.keybind.getCooldown(player) > 0 && keybind.isKeyDown(player) &&
				!this.keybind.abilityNotReadyCooldowns.containsKey(player.getPersistentID())) {
			player.playSound(ModSoundEvents.abilityNotReady, 1.0f, 1.0f);
			this.keybind.abilityNotReadyCooldowns.put(player.getPersistentID(), 20);
		}

		KeyBind prev = this.keybind;
		this.keybind = keybind;
		boolean ret = isSelected(player) && prev.getCooldown(player) == 0;
		this.keybind = prev;
		
		if (this.hero == EnumHero.TRACER && this.keybind == KeyBind.RMB) {
			System.out.println("wtf: "+this.keybind.name()); //TODO
		}

		if (ret && player.world.isRemote)
			this.keybind.abilityNotReadyCooldowns.put(player.getPersistentID(), 20);

		return ret;
	}

	/**Is this ability selected and able to be used*/
	public boolean isSelected(EntityPlayer player) {
		if (player.world.isRemote && keybind.getCooldown(player) > 0 && keybind.isKeyDown(player) &&
				!keybind.abilityNotReadyCooldowns.containsKey(player.getPersistentID())) {
			player.playSound(ModSoundEvents.abilityNotReady, 1.0f, 1.0f);
			keybind.abilityNotReadyCooldowns.put(player.getPersistentID(), 20);
		}

		boolean ret = (maxUses == 0 || getUses(player) > 0) && ((player.getActivePotionEffect(ModPotions.frozen) == null || 
				player.getActivePotionEffect(ModPotions.frozen).getDuration() == 0) &&
				ItemMWArmor.SetManager.playersWearingSets.containsKey(player.getPersistentID()) &&
				ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) == hero) &&
				keybind.getCooldown(player) == 0 && (keybind.isKeyDown(player) ||
						(toggled.containsKey(player.getPersistentID()) && toggled.get(player.getPersistentID())));

		if (ret && player.world.isRemote)
			keybind.abilityNotReadyCooldowns.put(player.getPersistentID(), 20);

		return ret;
	}

	public int getUses(EntityPlayer player) {
		if (player == null || maxUses == 0)
			return maxUses;
		else if (!multiAbilityUses.containsKey(player.getPersistentID()))
			multiAbilityUses.put(player.getPersistentID(), maxUses);

		return multiAbilityUses.get(player.getPersistentID());
	}

	public void subtractUse(EntityPlayer player) {
		if (player != null && !player.world.isRemote && getUses(player) > 0 && player instanceof EntityPlayerMP) {
			multiAbilityUses.put(player.getPersistentID(), multiAbilityUses.get(player.getPersistentID())-1);
			if (!multiAbilityCooldowns.containsKey(player.getPersistentID()))
				multiAbilityCooldowns.put(player.getPersistentID(), useCooldown);
			Minewatch.network.sendTo(
					new SPacketSyncAbilityUses(player.getPersistentID(), hero, getNumber(), 
							multiAbilityUses.get(player.getPersistentID()), false), (EntityPlayerMP) player);
		}
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if (event.phase == Phase.END && !event.player.world.isRemote)
			for (UUID uuid : multiAbilityCooldowns.keySet())
				if (uuid == event.player.getPersistentID()) {
					if (multiAbilityCooldowns.get(uuid) > 1)
						multiAbilityCooldowns.put(uuid, Math.max(multiAbilityCooldowns.get(uuid)-1, 0));
					else {
						multiAbilityCooldowns.remove(uuid);

						if (this.multiAbilityUses.containsKey(uuid)) {
							this.multiAbilityUses.put(uuid, Math.min(maxUses, multiAbilityUses.get(uuid)+1));
							if (this.multiAbilityUses.get(uuid) < maxUses)
								this.multiAbilityCooldowns.put(uuid, useCooldown);
						}
						else
							this.multiAbilityUses.put(uuid, maxUses);
						if (event.player instanceof EntityPlayerMP)
							Minewatch.network.sendTo(
									new SPacketSyncAbilityUses(event.player.getPersistentID(), hero, getNumber(), 
											multiAbilityUses.get(event.player.getPersistentID()), true), (EntityPlayerMP) event.player);
					}
					break;
				}
	}

}
