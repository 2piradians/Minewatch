package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
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
	public int useCooldown;
	public HashMap<UUID, Integer> multiAbilityUses = Maps.newHashMap();
	public static final Handler ABILITY_MULTI_COOLDOWNS = new Handler(Identifier.ABILITY_MULTI_COOLDOWNS, false) {
		@Override
		public Handler onServerRemove() {
			if (!entityLiving.world.isRemote && ability != null) {
				UUID uuid = entityLiving.getPersistentID();
				if (ability.multiAbilityUses.containsKey(uuid)) {
					ability.multiAbilityUses.put(uuid, Math.min(ability.maxUses, ability.multiAbilityUses.get(uuid)+1));
					if (ability.multiAbilityUses.get(uuid) < ability.maxUses) 
						this.setTicks(Math.max(2, (int) (ability.useCooldown*Config.abilityCooldownMultiplier)));
				}
				else
					ability.multiAbilityUses.put(uuid, ability.maxUses);
				if (entityLiving instanceof EntityPlayerMP)
					Minewatch.network.sendTo(
							new SPacketSyncAbilityUses(uuid, ability.hero, ability.getNumber(), 
									ability.multiAbilityUses.get(uuid), true), (EntityPlayerMP) entityLiving);
			}
			return (this.ticksLeft <= 0 || !entityLiving.isEntityAlive()) ? super.onServerRemove() : null;
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
		if (TickHandler.getHandler(entity, Identifier.ABILITY_USING) == null && isEnabled && 
				!TickHandler.hasHandler(entity, Identifier.SOMBRA_HACKED) && 
				!TickHandler.hasHandler(entity, Identifier.PREVENT_INPUT)) {
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
		if (TickHandler.getHandler(uuid, Identifier.ABILITY_USING, isRemote) == null && isEnabled && 
				!TickHandler.hasHandler(uuid, Identifier.SOMBRA_HACKED, isRemote) && 
				!TickHandler.hasHandler(uuid, Identifier.PREVENT_INPUT, isRemote)) {
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
	public boolean isSelected(EntityLivingBase entity, boolean isPressed, KeyBind keybind) {
		if (entity.world.isRemote && this.getCooldown(entity) > 0 && keybind.isKeyDown(entity) &&
				!TickHandler.hasHandler(entity, Identifier.KEYBIND_ABILITY_NOT_READY)/* && 
				(this.maxUses == 0 || this.getUses(entity) == 0)*/) {
			ModSoundEvents.ABILITY_NOT_READY.playSound(entity, 1.0f, 1.0f, true);
			TickHandler.register(true, this.keybind.ABILITY_NOT_READY.setEntity(entity).setTicks(20));
		}

		KeyBind prev = this.keybind;
		this.keybind = keybind;
		boolean ret = isSelected(entity, isPressed) && prev.getCooldown(entity) == 0;
		this.keybind = prev;

		if (this.hero == EnumHero.TRACER && this.keybind == KeyBind.RMB)
			this.keybind = KeyBind.ABILITY_1;

		return ret;
	}

	/**Is this ability selected and able to be used*/
	public boolean isSelected(EntityLivingBase player) {
		return isSelected(player, false);
	}

	/**Is this ability selected and able to be used*/
	public boolean isSelected(EntityLivingBase player, boolean isPressed) {
		return isSelected(player, isPressed, new Ability[0]);
	}

	/**Is this ability selected and able to be used*/
	public boolean isSelected(EntityLivingBase player, boolean isPressed, Ability...ignoreAbilities) {
		if (player instanceof EntityPlayer && ((EntityPlayer)player).isSpectator())
			return false;

		// not ready sound
		if (player.world.isRemote && this.keybind.getCooldown(player) > 0 && keybind.isKeyDown(player) &&
				!TickHandler.hasHandler(player, Identifier.KEYBIND_ABILITY_NOT_READY)) {
			ModSoundEvents.ABILITY_NOT_READY.playSound(player, 1.0f, 1.0f, true);
			TickHandler.register(true, this.keybind.ABILITY_NOT_READY.setEntity(player).setTicks(20));
		}
		
		// if ultimate and doesn't have charge
		if (this.keybind == KeyBind.ULTIMATE && !UltimateManager.canUseUltimate(player))
			return false;

		boolean ret = (maxUses == 0 || getUses(player) > 0) && ((player.getActivePotionEffect(ModPotions.frozen) == null || 
				player.getActivePotionEffect(ModPotions.frozen).getDuration() == 0 || 
				player.getActivePotionEffect(ModPotions.frozen).getAmplifier() > 0) &&
				SetManager.getWornSet(player) == hero) &&
				keybind.getCooldown(player) == 0 && ((!isPressed && keybind.isKeyDown(player)) ||
						(isPressed && keybind.isKeyPressed(player))||
						toggled.contains(player.getPersistentID())) &&
				!TickHandler.hasHandler(player, Identifier.SOMBRA_HACKED);

		// ABILITY_USING handler
		Handler handler = TickHandler.getHandler(player, Identifier.ABILITY_USING);
		boolean ignoreAbility = false;
		for (Ability ability : ignoreAbilities)
			if (handler != null && handler.ability == ability)
				ignoreAbility = true;
		if (handler != null && handler.ability != null && !handler.bool && !ignoreAbility) 
			return this == handler.ability;

		if (ret && player.world.isRemote)
			TickHandler.register(true, this.keybind.ABILITY_NOT_READY.setEntity(player).setTicks(20));
		return ret;
	}

	/**Get cooldown of keybind or multi-use cooldown*/
	public int getCooldown(EntityLivingBase entity) {
		if (this.maxUses > 0 && this.getUses(entity) == 0 && TickHandler.hasHandler(entity, Identifier.ABILITY_MULTI_COOLDOWNS))
			return TickHandler.getHandler(entity, Identifier.ABILITY_MULTI_COOLDOWNS).ticksLeft;
		else
			return keybind.getCooldown(entity);
	}

	/**Get number of available uses for multi-use ability (i.e. Tracer's Blink)*/
	public int getUses(EntityLivingBase player) {
		if (!(player instanceof EntityLivingBase) || maxUses == 0)
			return maxUses;
		else if (!multiAbilityUses.containsKey(player.getPersistentID()))
			multiAbilityUses.put(player.getPersistentID(), maxUses);

		return multiAbilityUses.get(player.getPersistentID());
	}

	/**Use one of the multi-uses*/
	public void subtractUse(EntityLivingBase entity) {
		if (entity != null && !entity.world.isRemote && getUses(entity) > 0) {
			multiAbilityUses.put(entity.getPersistentID(), multiAbilityUses.get(entity.getPersistentID())-1);
			if (!TickHandler.hasHandler(entity, Identifier.ABILITY_MULTI_COOLDOWNS))
				TickHandler.register(false, ABILITY_MULTI_COOLDOWNS.setEntity(entity).setAbility(this).setTicks(Math.max(2, (int) (useCooldown*Config.abilityCooldownMultiplier))));
			if (entity instanceof EntityPlayerMP)
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
	
	/**Currently only for ultimates*/
	public ResourceLocation getTexture() {
		return new ResourceLocation(Minewatch.MODID, "textures/gui/"+hero.name+"_ultimate.png");
	}

}