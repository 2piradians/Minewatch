package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
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
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;

public class Ability {

	public static final Handler ABILITY_USING = new Handler(Identifier.ABILITY_USING) {};

	public EnumHero hero;
	public KeyBind keybind;
	public boolean isEnabled;
	public boolean isToggleable;
	private HashMap<UUID, Boolean> toggled = Maps.newHashMap();

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

	/**Toggle this ability - untoggles all other abilities*/
	public void toggle(Entity entity, boolean toggle) {
		if (TickHandler.getHandler(entity, Identifier.ABILITY_USING) == null && isToggleable && isEnabled) {
			if (toggle) 
				for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
					ability.toggled.remove(entity.getPersistentID());
			toggled.put(entity.getPersistentID(), toggle);
		}
	}

	public boolean isToggled(Entity entity) {
		return isToggleable && toggled.containsKey(entity.getPersistentID()) && toggled.get(entity.getPersistentID());
	}

	/**Is this ability selected and able to be used (for abilities with alternate keybinds, like Tracer's Blink)*/
	public boolean isSelected(EntityPlayer player, KeyBind keybind) {
		if (player.worldObj.isRemote && this.keybind.getCooldown(player) > 0 && keybind.isKeyDown(player) &&
				!this.keybind.abilityNotReadyCooldowns.containsKey(player.getPersistentID())) {
			player.playSound(ModSoundEvents.abilityNotReady, 1.0f, 1.0f);
			this.keybind.abilityNotReadyCooldowns.put(player.getPersistentID(), 20);
		}

		KeyBind prev = this.keybind;
		this.keybind = keybind;
		boolean ret = isSelected(player) && prev.getCooldown(player) == 0;
		this.keybind = prev;

		if (this.hero == EnumHero.TRACER && this.keybind == KeyBind.RMB)
			this.keybind = KeyBind.ABILITY_1;

		if (ret && player.worldObj.isRemote)
			this.keybind.abilityNotReadyCooldowns.put(player.getPersistentID(), 20);

		return ret;
	}

	/**Is this ability selected and able to be used*/
	public boolean isSelected(EntityPlayer player) {
		if (player.worldObj.isRemote && keybind.getCooldown(player) > 0 && keybind.isKeyDown(player) &&
				!keybind.abilityNotReadyCooldowns.containsKey(player.getPersistentID())) {
			player.playSound(ModSoundEvents.abilityNotReady, 1.0f, 1.0f);
			keybind.abilityNotReadyCooldowns.put(player.getPersistentID(), 20);
		}

		boolean ret = (maxUses == 0 || getUses(player) > 0) && ((player.getActivePotionEffect(ModPotions.frozen) == null || 
				player.getActivePotionEffect(ModPotions.frozen).getDuration() == 0 || 
				player.getActivePotionEffect(ModPotions.frozen).getAmplifier() > 0) &&
				ItemMWArmor.SetManager.playersWearingSets.containsKey(player.getPersistentID()) &&
				ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) == hero) &&
				keybind.getCooldown(player) == 0 && (keybind.isKeyDown(player) ||
						(toggled.containsKey(player.getPersistentID()) && toggled.get(player.getPersistentID())));

		if (TickHandler.getHandler(player, Identifier.ABILITY_USING) != null && !this.isToggled(player))
			return false;
		
		if (ret && player.worldObj.isRemote)
			keybind.abilityNotReadyCooldowns.put(player.getPersistentID(), 20);

		return ret;
	}

	/**Get number of available uses for multi-use ability (i.e. Tracer's Blink)*/
	public int getUses(EntityPlayer player) {
		if (player == null || maxUses == 0)
			return maxUses;
		else if (!multiAbilityUses.containsKey(player.getPersistentID()))
			multiAbilityUses.put(player.getPersistentID(), maxUses);

		return multiAbilityUses.get(player.getPersistentID());
	}

	/**Use one of the multi-uses*/
	public void subtractUse(EntityPlayer player) {
		if (player != null && !player.worldObj.isRemote && getUses(player) > 0 && player instanceof EntityPlayerMP) {
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
		if (event.phase == Phase.END && !event.player.worldObj.isRemote) 
			for (UUID uuid : multiAbilityCooldowns.keySet()) 
				if (uuid.equals(event.player.getPersistentID())) {
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
