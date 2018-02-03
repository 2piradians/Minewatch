package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
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
import twopiradians.minewatch.packet.SPacketSimple;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;

@Mod.EventBusSubscriber
public class SetManager {

	// REPLACE EVENT - COMMENT OUT FOR RELEASE
	/*@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void replaceSpawns(LivingUpdateEvent event) {
		if (!event.getEntity().worldObj.isRemote && event.getEntity().ticksExisted == 1 && 
				!(event.getEntity() instanceof EntityHero) && 
				!event.getEntity().getEntityData().hasKey("Minewatch: Checked") && 
				event.getEntity().isCreatureType(EnumCreatureType.MONSTER, false)) { //if server side & newly spawned
			event.getEntity().getEntityData().setBoolean("Minewatch: Checked", true);

			// replace with random hero
			if (event.getEntity().worldObj.rand.nextInt(event.getEntity().worldObj.isDaytime() ? 150 : 100) <= Config.mobSpawnFreq*3) {
				EntityHero hero = new EntityHero(event.getEntity().worldObj);
				hero.copyLocationAndAnglesFrom(event.getEntity());
				event.getEntity().worldObj.spawnEntityInWorld(hero);
				event.getEntity().setDead();
			}
		}
	}*/

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

	/**Unregister client handlers to prevent Handlers using old objects*/
	@SubscribeEvent
	public static void clearHandlers(PlayerLoggedOutEvent event) {
		TickHandler.unregisterAllHandlers(true);
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
				getWornSet(entity.getPersistentID(), entity.worldObj.isRemote);
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
			boolean isRemote = event.player.worldObj.isRemote;

			//detect if player is wearing a set
			ItemStack helm = event.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
			EnumHero hero = null;
			boolean fullSet = helm != null && helm.getItem() instanceof ItemMWArmor;
			if (fullSet) {
				hero = ((ItemMWArmor)helm.getItem()).hero;
				for (EntityEquipmentSlot slot : ItemMWArmor.SLOTS) {
					ItemStack armor = event.player.getItemStackFromSlot(slot);
					if (armor == null || !(armor.getItem() instanceof ItemMWArmor)
							|| ((ItemMWArmor)(armor.getItem())).hero != hero) 
						fullSet = false;
				}
			}

			// clear toggles when switching to set or if not holding weapon
			if (hero != null && (event.player.getHeldItemMainhand() == null || 
					event.player.getHeldItemMainhand().getItem() != hero.weapon) || 
					(fullSet && (SetManager.getWornSet(event.player) == null ||
					SetManager.getWornSet(event.player) != hero)))
				for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
					ability.toggle(event.player, false);

			// update entitiesWearingSets
			if (fullSet) {
				// set changed
				if ((!SetManager.entitiesWearingSets(isRemote).containsKey(event.player.getPersistentID()) ||
						SetManager.entitiesWearingSets(isRemote).get(event.player.getPersistentID()) != hero) &&
						hero.selectSound != null) {
					// sound plays twice in MP - both on clientside so cooldown should handle
					if (!event.player.worldObj.isRemote && event.player instanceof EntityPlayerMP)
						Minewatch.network.sendTo(new SPacketSimple(50, true, event.player, hero.ordinal(), 0, 0), (EntityPlayerMP) event.player);
				}

				SetManager.entitiesWearingSets(isRemote).put(event.player.getPersistentID(), hero);

				// set changed from prev
				if (SetManager.lastWornSets(isRemote).get(event.player.getPersistentID()) != hero) {
					for (KeyBind key : Keys.KeyBind.values()) 
						if (key.getCooldown(event.player) > 0)
							key.setCooldown(event.player, 0, true);
					SetManager.lastWornSets(isRemote).put(event.player.getPersistentID(), hero);
				}
			}
			else
				SetManager.entitiesWearingSets(isRemote).remove(event.player.getPersistentID());
		}
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
		if (event.getEntity() instanceof EntityLivingBase && !event.getEntity().worldObj.isRemote &&
				SetManager.getWornSet(event.getEntity()) == EnumHero.JUNKRAT) {
			ModSoundEvents.JUNKRAT_DEATH.playSound(event.getEntity(), 1, 1);
			for (int i=0; i<6; ++i) {
				EntityJunkratGrenade grenade = new EntityJunkratGrenade(event.getEntity().worldObj, 
						(EntityLivingBase) event.getEntity(), -1);
				grenade.explodeTimer = 20+i*2;
				grenade.setPosition(event.getEntity().posX, event.getEntity().posY+event.getEntity().height/2d, event.getEntity().posZ);
				grenade.motionX = (event.getEntity().worldObj.rand.nextDouble()-0.5d)*0.1d;
				grenade.motionY = (event.getEntity().worldObj.rand.nextDouble()-0.5d)*0.1d;
				grenade.motionZ = (event.getEntity().worldObj.rand.nextDouble()-0.5d)*0.1d;
				event.getEntity().worldObj.spawnEntityInWorld(grenade);
				grenade.isDeathGrenade = true;
				Minewatch.network.sendToAll(new SPacketSimple(24, grenade, false, grenade.explodeTimer, 0, 0));
			}
		}
	}
}
