package twopiradians.minewatch.common.hero;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.FollowingSound;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;

@Mod.EventBusSubscriber
public class EventManager {

	private static int cleanUpTimerC;
	//private static int cleanUpTimerS;

	public enum Type {
		DEATH, CONNECT, DISCONNECT, REMOVE_SET, RESPAWN
	}

	public static void onEvent(Type type, EntityLivingBase entity) {
		EnumHero hero = SetManager.getWornSet(entity);

		// clean up passives
		PassiveManager.playersClimbing.remove(entity);
		PassiveManager.playersHovering.remove(entity);
		PassiveManager.playersJumped.remove(entity);
		PassiveManager.playersFlying.remove(entity);
		PassiveManager.playersWallRiding.remove(entity);
		PassiveManager.prevWall.remove(entity);
		if (hero != null && (type == Type.DISCONNECT || type == Type.REMOVE_SET))
			for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3}) {
				Entity entity2 = ability.entities.get(entity);
				if (entity2 != null) {
					entity2.setDead();
					ability.entities.remove(entity);
				}
			}

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

	@SideOnly(Side.CLIENT)
	public static void onCleanUpC() {
		// clean up following sounds that aren't playing
		//Minewatch.logger.info("cleanup - before: "+FollowingSound.sounds);
		for (FollowingSound sound : FollowingSound.sounds) {
			if (!Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound)) {
				//Minewatch.logger.info("sound found: "+sound.getSoundLocation());
				FollowingSound.stopPlaying(sound);
			}
		}
		//Minewatch.logger.info("cleanup - after: "+FollowingSound.sounds);
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
	public static void onEvent(PlayerLoggedInEvent event) {
		onEvent(Type.CONNECT, event.player);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void clientSide(ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && !Minecraft.getMinecraft().isGamePaused() &&
				--cleanUpTimerC <= 0) {
			cleanUpTimerC = 400;
			onCleanUpC();
		}
	}
	/*
	@SubscribeEvent
	public static void serverSide(ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END &&
				--cleanUpTimerS <= 0) {
			cleanUpTimerS = 400;
		}
	}*/

}