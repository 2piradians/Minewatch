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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityJunkratGrenade;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;

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

	/**Clear cooldowns of players logging in (for when switching worlds)*/
	@SubscribeEvent
	public static void resetCooldowns(PlayerLoggedInEvent event) {
		for (KeyBind key : Keys.KeyBind.values()) 
			if (key.getCooldown(event.player) > 0)
				key.setCooldown(event.player, 0, false);
		for (EnumHero hero : EnumHero.values())
			for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3}) 
				if (ability.multiAbilityUses.remove(event.player.getPersistentID()) != null &&
				event.player instanceof EntityPlayerMP) {
					Minewatch.network.sendTo(
							new SPacketSyncAbilityUses(event.player.getPersistentID(), hero, ability.getNumber(), 
									ability.maxUses, false), (EntityPlayerMP) event.player);
				}
	}

	/**Unregister server handlers to make sure onServerRemove is called*/
	@SubscribeEvent
	public static void clearHandlers(PlayerLoggedOutEvent event) {
		// only called on server as far as I can tell
		if (event.player.getServer() != null && event.player.getServer().isSinglePlayer()) { // unregister everything bc sp
			TickHandler.unregisterAllHandlers(event.player.world.isRemote);
		}
		else { // unregister handlers for the player - to make sure .onServerRemove is called
			TickHandler.unregister(event.player.world.isRemote, TickHandler.getHandlers(event.player, (Identifier)null).toArray(new Handler[0]));
		}
	}

	/**Unregister client handlers to make sure onClientRemove is called*/
	@SubscribeEvent
	public static void clearHandlers(WorldEvent.Unload event) {
		// basically PlayerLoggedOutEvent for client
		if (event.getWorld().isRemote) { // unregister handlers for the player - to make sure .onClientRemove is called
			TickHandler.unregister(event.getWorld().isRemote, TickHandler.getHandlers(Minewatch.proxy.getClientPlayer(), (Identifier)null).toArray(new Handler[0]));

		}
	}

	private static HashMap<UUID, EnumHero> entitiesWearingSets(boolean isRemote) {
		return isRemote ? entitiesWearingSetsClient : entitiesWearingSetsServer;
	}

	private static HashMap<UUID, EnumHero> lastWornSets(boolean isRemote) {
		return isRemote ? lastWornSetsClient : lastWornSetsServer;
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

	/**Clear cooldowns of players respawning*/
	@SubscribeEvent
	public static void resetCooldowns(PlayerRespawnEvent event) {
		for (KeyBind key : Keys.KeyBind.values()) 
			if (key.getCooldown(event.player) > 0)
				key.setCooldown(event.player, 0, false);
	}

	/**Update entitiesWearingSets each tick
	 * This way it's only checked once per tick, no matter what:
	 * very useful for checking if HUDs should be rendered*/
	@SubscribeEvent
	public static void updateSets(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			updateSets(event.player);
		}
	}

	/**Update worn sets*/
	public static void updateSets(EntityPlayer player) {
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

	}

	public static void onSetChanged(EntityPlayer player, @Nullable EnumHero prevHero, @Nullable EnumHero newHero) {
		// update entitiesWearingSets
		if (newHero == null)
			SetManager.entitiesWearingSets(player.world.isRemote).remove(player.getPersistentID());
		else
			SetManager.entitiesWearingSets(player.world.isRemote).put(player.getPersistentID(), newHero);

		if (newHero != null) {
			// play selection sound - plays twice in MP - both on clientside so cooldown should handle
			if (newHero.selectSound != null && !player.world.isRemote && player instanceof EntityPlayerMP)
				Minewatch.network.sendTo(new SPacketSimple(50, true, player, newHero.ordinal(), 0, 0), (EntityPlayerMP) player);

			// reset keybinds, reset ultimate charge, kill entitylivingbasemw, and update lastWornSets
			if (prevHero != newHero) {
				for (KeyBind key : Keys.KeyBind.values()) 
					if (key.getCooldown(player) > 0)
						key.setCooldown(player, 0, true);
				SetManager.lastWornSets(player.world.isRemote).put(player.getPersistentID(), newHero);
				UltimateManager.setCharge(player, 0);
				// kill old entities
				if (prevHero != null)
					for (Ability ability : new Ability[] {prevHero.ability1, prevHero.ability2, prevHero.ability3}) {
						Entity entity = ability.entities.get(player);
						if (entity != null) {
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

	}

	@SubscribeEvent
	public static void preventFallDamage(LivingFallEvent event) {
		// prevent fall damage if enabled in config and wearing set
		if (Config.preventFallDamage && event.getEntity() != null &&
				SetManager.getWornSet(event.getEntity()) != null)
			event.setCanceled(true);
		// genji fall
		else if (event.getEntity() != null && 
				SetManager.getWornSet(event.getEntity()) == EnumHero.GENJI) 
			event.setDistance(event.getDistance()*0.8f);
	}

	@SubscribeEvent
	public static void junkratDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof EntityLivingBase && !event.getEntity().world.isRemote &&
				SetManager.getWornSet(event.getEntity()) == EnumHero.JUNKRAT) {
			ModSoundEvents.JUNKRAT_DEATH.playSound(event.getEntity(), 1, 1);
			for (int i=0; i<6; ++i) {
				EntityJunkratGrenade grenade = new EntityJunkratGrenade(event.getEntity().world, 
						(EntityLivingBase) event.getEntity(), -1);
				grenade.explodeTimer = 20+i*2;
				grenade.setPosition(event.getEntity().posX, event.getEntity().posY+event.getEntity().height/2d, event.getEntity().posZ);
				grenade.motionX = (event.getEntity().world.rand.nextDouble()-0.5d)*0.1d;
				grenade.motionY = (event.getEntity().world.rand.nextDouble()-0.5d)*0.1d;
				grenade.motionZ = (event.getEntity().world.rand.nextDouble()-0.5d)*0.1d;
				event.getEntity().world.spawnEntity(grenade);
				grenade.isDeathGrenade = true;
				Minewatch.network.sendToAll(new SPacketSimple(24, grenade, false, grenade.explodeTimer, 0, 0));
			}
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