package twopiradians.minewatch.common.hero;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

/**Cleans up entity storage when needed*/
@Mod.EventBusSubscriber
public class CleanUpManager {

	public enum Type {
		DEATH, DISCONNECT, REMOVE_SET
	}

	public static void onCleanUp(Type type, EntityLivingBase entity) {
		PassiveManager.playersClimbing.remove(entity);
		PassiveManager.playersHovering.remove(entity);
		PassiveManager.playersJumped.remove(entity);

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
	}

	@SubscribeEvent
	public static void onEvent(LivingDeathEvent event) {
		onCleanUp(Type.DEATH, event.getEntityLiving());
	}

	@SubscribeEvent
	public static void onEvent(PlayerEvent.PlayerLoggedOutEvent event) {
		onCleanUp(Type.DISCONNECT, event.player);
	}

	@SubscribeEvent
	public static void onEvent(WorldEvent.Unload event) {
		// basically PlayerLoggedOutEvent for client
		if (event.getWorld().isRemote) 
			onCleanUp(Type.DISCONNECT, Minewatch.proxy.getClientPlayer());
	}

}