package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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

	/**boolean represents if it should allow other abilities to be used while using this one*/
	public static final Handler ABILITY_USING = new Handler(Identifier.ABILITY_USING, true) {};

	public EnumHero hero;
	public KeyBind keybind;
	public boolean isEnabled;
	public boolean isToggleable;
	public HashMap<EntityLivingBase, Entity> entities = Maps.newHashMap();
	private HashSet<UUID> toggled = new HashSet();

	// multi use ability stuff
	public int maxUses;
	private int useCooldown;
	public HashMap<UUID, Integer> multiAbilityUses = Maps.newHashMap();
	public static final Handler ABILITY_MULTI_COOLDOWNS = new Handler(Identifier.ABILITY_MULTI_COOLDOWNS, false) {
		@Override
		public Handler onServerRemove() {
			if (!player.world.isRemote) {
				UUID uuid = player.getPersistentID();
				if (ability.multiAbilityUses.containsKey(uuid)) {
					ability.multiAbilityUses.put(uuid, Math.min(ability.maxUses, ability.multiAbilityUses.get(uuid)+1));
					if (ability.multiAbilityUses.get(uuid) < ability.maxUses) 
						this.setTicks(ability.useCooldown);
				}
				else
					ability.multiAbilityUses.put(uuid, ability.maxUses);
				if (player instanceof EntityPlayerMP)
					Minewatch.network.sendTo(
							new SPacketSyncAbilityUses(uuid, ability.hero, ability.getNumber(), 
									ability.multiAbilityUses.get(uuid), true), (EntityPlayerMP) player);
			}
			return (this.ticksLeft <= 0 || !player.isEntityAlive()) ? super.onServerRemove() : null;
		}
	};

	public Ability(KeyBind keybind, boolean isEnabled, boolean isToggleable) {
		this(keybind, isEnabled, isToggleable, 0, 0);
	}

	public Ability(KeyBind keybind, boolean isEnabled, boolean isToggleable, int maxUses, int useCooldown) {
		this.keybind = keybind;
		this.isEnabled = isEnabled;
		this.isToggleable = isToggleable;
		this.maxUses = maxUses;
		this.useCooldown = useCooldown;
	}

	/**Returns this ability's number relative to the hero's ability set*/
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
		if (TickHandler.getHandler(entity, Identifier.ABILITY_USING) == null && isEnabled) {
			if (toggle) {
				for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
					ability.toggled.remove(entity.getPersistentID());
				toggled.add(entity.getPersistentID());
			}
			else
				toggled.remove(entity.getPersistentID());
		}
	}

	/**Toggle this ability - untoggles all other abilities*/
	public void toggle(UUID uuid, boolean toggle, boolean isRemote) {
		if (TickHandler.getHandler(uuid, Identifier.ABILITY_USING, isRemote) == null && isEnabled) {
			if (toggle) {
				for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
					ability.toggled.remove(uuid);
				toggled.add(uuid);
			}
			else
				toggled.remove(uuid);
		}
	}

	public boolean isToggled(Entity entity) {
		return toggled.contains(entity.getPersistentID());
	}

	public boolean isToggled(UUID uuid) {
		return toggled.contains(uuid);
	}

	/**Is this ability selected and able to be used (for abilities with alternate keybinds, like Tracer's Blink)*/
	public boolean isSelected(EntityLivingBase entity, KeyBind keybind) {
		if (entity.world.isRemote && this.keybind.getCooldown(entity) > 0 && keybind.isKeyDown(entity) &&
				!TickHandler.hasHandler(entity, Identifier.KEYBIND_ABILITY_NOT_READY)) {
			entity.playSound(ModSoundEvents.abilityNotReady, 1.0f, 1.0f);
			TickHandler.register(true, this.keybind.ABILITY_NOT_READY.setEntity(entity).setTicks(20));
		}

		KeyBind prev = this.keybind;
		this.keybind = keybind;
		boolean ret = isSelected(entity) && prev.getCooldown(entity) == 0;
		this.keybind = prev;

		if (this.hero == EnumHero.TRACER && this.keybind == KeyBind.RMB)
			this.keybind = KeyBind.ABILITY_1;

		return ret;
	}

	/**Is this ability selected and able to be used*/
	public boolean isSelected(EntityLivingBase player) {
		if (player.world.isRemote && this.keybind.getCooldown(player) > 0 && keybind.isKeyDown(player) &&
				!TickHandler.hasHandler(player, Identifier.KEYBIND_ABILITY_NOT_READY)) {
			player.playSound(ModSoundEvents.abilityNotReady, 1.0f, 1.0f);
			TickHandler.register(true, this.keybind.ABILITY_NOT_READY.setEntity(player).setTicks(20));
		}

		boolean ret = (maxUses == 0 || getUses(player) > 0) && ((player.getActivePotionEffect(ModPotions.frozen) == null || 
				player.getActivePotionEffect(ModPotions.frozen).getDuration() == 0 || 
				player.getActivePotionEffect(ModPotions.frozen).getAmplifier() > 0) &&
				ItemMWArmor.SetManager.getWornSet(player) == hero) &&
				keybind.getCooldown(player) == 0 && ((/*!this.isToggleable && */keybind.isKeyDown(player)) ||
						toggled.contains(player.getPersistentID()));

		Handler handler = TickHandler.getHandler(player, Identifier.ABILITY_USING);
		if (handler != null && handler.ability != null && !handler.bool)
			return this == handler.ability;

		if (ret && player.world.isRemote)
			TickHandler.register(true, this.keybind.ABILITY_NOT_READY.setEntity(player).setTicks(20));
		return ret;
	}

	/**Get number of available uses for multi-use ability (i.e. Tracer's Blink)*/
	public int getUses(EntityLivingBase player) {
		if (!(player instanceof EntityPlayer) || maxUses == 0)
			return maxUses;
		else if (!multiAbilityUses.containsKey(player.getPersistentID()))
			multiAbilityUses.put(player.getPersistentID(), maxUses);

		return multiAbilityUses.get(player.getPersistentID());
	}

	/**Use one of the multi-uses*/
	public void subtractUse(EntityLivingBase entity) {
		if (entity != null && !entity.world.isRemote && getUses(entity) > 0 && entity instanceof EntityPlayerMP) {
			multiAbilityUses.put(entity.getPersistentID(), multiAbilityUses.get(entity.getPersistentID())-1);
			if (!TickHandler.hasHandler(entity, Identifier.ABILITY_MULTI_COOLDOWNS))
				TickHandler.register(false, ABILITY_MULTI_COOLDOWNS.setAbility(this).setEntity(entity).setTicks(useCooldown));
			Minewatch.network.sendTo(
					new SPacketSyncAbilityUses(entity.getPersistentID(), hero, getNumber(), 
							multiAbilityUses.get(entity.getPersistentID()), false), (EntityPlayerMP) entity);
		}
	}

	/**Should the keybind be visible? - not unable to be used*/
	public boolean showKeybind(EntityPlayer player) {
		return keybind.getCooldown(player) <= 0 && 
				(!isSelected(player) || (isToggled(player))) &&
				(maxUses == 0 || getUses(player) > 0);
	}

}
