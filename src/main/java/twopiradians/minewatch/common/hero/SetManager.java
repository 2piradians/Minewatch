package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import twopiradians.minewatch.client.attachment.AttachmentManager;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.hero.EventManager.Type;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

@Mod.EventBusSubscriber
public class SetManager {

	/**List of players wearing full sets and the sets that they are wearing*/
	private static HashMap<UUID, EnumHero> entitiesWearingSetsClient = Maps.newHashMap();	
	/**List of players wearing full sets and the sets that they are wearing*/
	private static HashMap<UUID, EnumHero> entitiesWearingSetsServer = Maps.newHashMap();	

	/**List of players' last known full sets worn (for knowing when to reset cooldowns)*/
	private static HashMap<UUID, EnumHero> lastWornSetsClient = Maps.newHashMap();
	/**List of players' last known full sets worn (for knowing when to reset cooldowns)*/
	private static HashMap<UUID, EnumHero> lastWornSetsServer = Maps.newHashMap();
	/**If client player currently has run reassigned (or should, if it's disabled)*/
	private static boolean prevReassign;

	private static HashMap<UUID, EnumHero> entitiesWearingSets(boolean isRemote) {
		return isRemote ? entitiesWearingSetsClient : entitiesWearingSetsServer;
	}

	private static HashMap<UUID, EnumHero> lastWornSets(boolean isRemote) {
		return isRemote ? lastWornSetsClient : lastWornSetsServer;
	}

	/**Clear tracked worn sets - i.e. when reconnecting to world*/
	public static void clearWornSets(Entity entity, boolean isRemote) {
		if (entity != null)
			clearWornSets(entity.getPersistentID(), isRemote);
	}

	/**Clear tracked worn sets - i.e. when reconnecting to world*/
	public static void clearWornSets(UUID uuid, boolean isRemote) {
		if (uuid != null) {
			entitiesWearingSets(isRemote).remove(uuid);
			lastWornSets(isRemote).remove(uuid);
		}
	}

	@Nullable
	public static EnumHero getWornSet(Entity entity) {
		return entity == null ? null : 
			entity instanceof EntityHero ? ((EntityHero)entity).hero : 
				getWornSet(entity.getPersistentID(), entity.world.isRemote);
	}

	@Nullable
	public static EnumHero getWornSet(UUID uuid, boolean isRemote) {
		return entitiesWearingSets(isRemote).get(uuid);
	}

	/**Update entitiesWearingSets each tick
	 * This way it's only checked once per tick, no matter what:
	 * very useful for checking if HUDs should be rendered*/
	@SubscribeEvent
	public static void updateSets(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			EnumHero hero = updateSets(event.player);
			if (hero != null)
				PassiveManager.onUpdate(event.player.world, event.player, hero);
			// update run keybind reassigning
			if (event.player.world.isRemote && event.player == Minewatch.proxy.getClientPlayer()) {
				boolean reassign = hero != null && event.player.getHeldItemMainhand() != null && 
						event.player.getHeldItemMainhand().getItem() == hero.weapon;
				if (reassign != prevReassign) {
					Minewatch.proxy.reassignRunKeybind(reassign);
					prevReassign = reassign;
				}
			}
		}
	}

	/**Update worn sets*/
	@Nullable
	public static EnumHero updateSets(EntityPlayer player) {
		boolean isRemote = player.world.isRemote;

		//detect if player is wearing a set
		ItemStack helm = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		EnumHero hero = null;
		boolean fullSet = helm != null && helm.getItem() instanceof ItemMWArmor;
		if (fullSet) {
			hero = ((ItemMWArmor)helm.getItem()).hero;
			for (EntityEquipmentSlot slot : ItemMWArmor.SLOTS) {
				ItemStack armor = player.getItemStackFromSlot(slot);
				if (armor == null || !(armor.getItem() instanceof ItemMWArmor)
						|| ((ItemMWArmor)(armor.getItem())).hero != hero) 
					fullSet = false;
			}
		}

		// clear toggles when switching to set or if not holding weapon
		if (hero != null && (player.getHeldItemMainhand() == null || 
				player.getHeldItemMainhand().getItem() != hero.weapon) || 
				(fullSet && (SetManager.getWornSet(player) == null ||
				SetManager.getWornSet(player) != hero)))
			for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
				ability.toggle(player, false);

		// onSetChanged
		if ((fullSet && (!SetManager.entitiesWearingSets(isRemote).containsKey(player.getPersistentID()) ||
				SetManager.entitiesWearingSets(isRemote).get(player.getPersistentID()) != hero)) ||
				(!fullSet && SetManager.entitiesWearingSets(isRemote).containsKey(player.getPersistentID())))
			onSetChanged(player, SetManager.lastWornSets(isRemote).get(player.getPersistentID()), fullSet ? hero : null);

		return fullSet ? hero : null;
	}

	/**Called when set changed, first logging in, or hero spawned*/
	public static void onSetChanged(EntityLivingBase player, @Nullable EnumHero prevHero, @Nullable EnumHero newHero) {
		EventManager.onEvent(Type.CHANGE_SET, player);
		AttachmentManager.onSetChanged(player, prevHero, newHero);

		Minewatch.proxy.onSetChanged(player, prevHero, newHero);

		if (player instanceof EntityPlayer) {
			// update entitiesWearingSets
			if (newHero == null)
				SetManager.entitiesWearingSets(player.world.isRemote).remove(player.getPersistentID());
			else
				SetManager.entitiesWearingSets(player.world.isRemote).put(player.getPersistentID(), newHero);

			if (newHero != null) {
				// play selection sound - plays twice in MP - both on clientside so cooldown should handle
				if (newHero.selectSound != null && !player.world.isRemote && player instanceof EntityPlayerMP)
					Minewatch.network.sendTo(new SPacketSimple(50, true, (EntityPlayer)player, newHero.ordinal(), 0, 0), (EntityPlayerMP) player);

				// reset keybinds, reset ultimate charge, reset charge, kill entitylivingbasemw, and update lastWornSets
				if (prevHero != newHero) {
					for (KeyBind key : Keys.KeyBind.values()) 
						if (key.getCooldown(player) > 0)
							key.setCooldown(player, 0, true);
					SetManager.lastWornSets(player.world.isRemote).put(player.getPersistentID(), newHero);
					UltimateManager.setCharge(player, 0, true);
					ChargeManager.setCurrentCharge(player, ChargeManager.getMaxCharge(newHero), false);
					// kill old entities
					if (prevHero != null)
						for (Ability ability : new Ability[] {prevHero.ability1, prevHero.ability2, prevHero.ability3}) {
							Entity entity = ability.entities.get(player);
							if (entity instanceof EntityLivingBaseMW) {
								entity.setDead();
								ability.entities.remove(player);
							}
						}
				}

				// remove temp shields/armor
				if (!player.world.isRemote && prevHero != null && newHero != prevHero) {
					for (HealthManager.Type type : HealthManager.Type.values())
						HealthManager.removeHealth(player, type, newHero, 9999);
				}
			}

			// set step height (needed to sync stepHeight to server for .collidedHorizontally to work)
			player.stepHeight = newHero != null && Config.stepAssist ? 1 : 0.6f;

			// Lucio's view bobbing
			if (player.world.isRemote && player == Minewatch.proxy.getClientPlayer())
				if (newHero == EnumHero.LUCIO)
					TickHandler.register(true, Handlers.VIEW_BOBBING.setEntity(player).setBoolean(false).setTicks(999999));
				else 
					TickHandler.unregister(true, TickHandler.getHandler(player, Identifier.VIEW_BOBBING));
		}
	}

	/**Heal entity to full - for when set switched / respawned*/
	public static void healToFull(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer)
			updateSets((EntityPlayer) entity);
		EnumHero hero = getWornSet(entity);
		if (entity != null && !entity.world.isRemote && hero != null)
			entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 60, 31, false, false));
	}

}