package twopiradians.minewatch.common.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;

@Mod.EventBusSubscriber
public class EventManager {

	public enum Type {
		DEATH, CONNECT, DISCONNECT, REMOVE_SET, RESPAWN
	}

	public static void onEvent(Type type, EntityLivingBase entity) {
		EnumHero hero = SetManager.getWornSet(entity);

		PassiveManager.playersClimbing.remove(entity);
		PassiveManager.playersHovering.remove(entity);
		PassiveManager.playersJumped.remove(entity);
		PassiveManager.playersFlying.remove(entity);

		// Unregister handlers to make sure onClientRemove and onServerRemove is called
		if (type == Type.DISCONNECT) {
			// only called on server as far as I can tell
			if (entity.getServer() != null && entity.getServer().isSinglePlayer()) { // unregister everything bc sp
				TickHandler.unregisterAllHandlers(entity.world.isRemote);
			}
			else { // unregister handlers for the player - to make sure .onServerRemove is called
				TickHandler.unregister(entity.world.isRemote, TickHandler.getHandlers(entity, (Identifier)null).toArray(new Handler[0]));
			}
		}
		else if (type == Type.RESPAWN || type == Type.CONNECT) {
			// reset cooldowns
			for (KeyBind key : Keys.KeyBind.values()) 
				if (key.getCooldown(entity) > 0)
					key.setCooldown(entity, 0, false);

			if (hero != null) {
				// reset multi-use cooldowns
				for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3}) 
					if (ability.multiAbilityUses.remove(entity.getPersistentID()) != null &&
					entity instanceof EntityPlayerMP) {
						Minewatch.network.sendTo(
								new SPacketSyncAbilityUses(entity.getPersistentID(), hero, ability.getNumber(), 
										ability.maxUses, false), (EntityPlayerMP) entity);
					}
				
				// refill ammo
				ItemStack stack = entity.getHeldItemMainhand();
				if (stack != null && stack.getItem() instanceof ItemMWWeapon)
					((ItemMWWeapon)stack.getItem()).setCurrentAmmo(entity, ((ItemMWWeapon)stack.getItem()).getMaxAmmo(entity));
			}
		}
	}

	@SubscribeEvent
	public static void onEvent(LivingDeathEvent event) {
		onEvent(Type.DEATH, event.getEntityLiving());
	}

	@SubscribeEvent
	public static void onEvent(PlayerEvent.PlayerLoggedOutEvent event) {
		onEvent(Type.DISCONNECT, event.player);
	}

	@SubscribeEvent
	public static void onEvent(PlayerEvent.PlayerRespawnEvent event) {
		onEvent(Type.RESPAWN, event.player);
	}

	@SubscribeEvent
	public static void onEvent(WorldEvent.Unload event) {
		// basically PlayerLoggedOutEvent for client
		if (event.getWorld().isRemote) 
			onEvent(Type.DISCONNECT, Minewatch.proxy.getClientPlayer());
	}

	@SubscribeEvent
	public static void resetCooldowns(PlayerLoggedInEvent event) {
		onEvent(Type.CONNECT, event.player);
	}

}