package twopiradians.minewatch.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.packet.SPacketSimple;

/**Used to easily create/manage tick timers and other tick-dependent things*/
public class TickHandler {

	/**Identifiers used in getHandler()*/
	public enum Identifier {
		NONE, REAPER_TELEPORT, GENJI_DEFLECT, GENJI_STRIKE, GENJI_SWORD, MCCREE_ROLL, MERCY_NOT_REGENING, WEAPON_WARNING, HANZO_SONIC, POTION_FROZEN, POTION_DELAY, ABILITY_USING, PREVENT_ROTATION, PREVENT_MOVEMENT, PREVENT_INPUT, ABILITY_MULTI_COOLDOWNS, REAPER_WRAITH, ANA_SLEEP, ACTIVE_HAND, KEYBIND_ABILITY_NOT_READY, KEYBIND_ABILITY_1, KEYBIND_ABILITY_2, KEYBIND_RMB, HERO_SNEAKING, HERO_MESSAGES, HIT_OVERLAY, KILL_OVERLAY, HERO_MULTIKILL, MERCY_ANGEL, HERO_DAMAGE_TIMER, ANA_DAMAGE, JUNKRAT_TRAP, SOMBRA_INVISIBLE, WIDOWMAKER_POISON, SOMBRA_TELEPORT, BASTION_TURRET, MEI_CRYSTAL, REINHARDT_STRIKE, SOMBRA_OPPORTUNIST, WEAPON_COOLDOWN, LUCIO_SONIC, KEYBIND_LMB, KEYBIND_HERO_INFO, KEYBIND_ULTIMATE, KEYBIND_JUMP, KEYBIND_RELOAD, KEYBIND_FOV, LUCIO_AMP, VOICE_COOLDOWN, ZENYATTA_VOLLEY, ZENYATTA_HARMONY, ZENYATTA_DISCORD, HEALTH_PARTICLES, MEI_ICICLE, GENJI_SHURIKEN, WEAPON_CHARGE, MOIRA_HEAL, MOIRA_ORB, MOIRA_FADE, MOIRA_DAMAGE, GLOWING, MOIRA_ORB_SELECT, MCCREE_FAN, ANA_GRENADE_DAMAGE, ANA_GRENADE_HEAL, TRACER_RECALL, INVULNERABLE, WIDOWMAKER_HOOK, TRACER_RECOLOR, REINHARDT_CHARGE, FORCE_VIEW, SOMBRA_HACK, SOMBRA_HACKED, DOOMFIST_RELOAD, DOOMFIST_PUNCH, DOOMFIST_PUNCHED, DOOMFIST_UPPERCUTTING, DOOMFIST_UPPERCUT, DOOMFIST_SLAM, DEAD, TEAM_BLOCK_WARNING_COOLDOWN, TEAM_SPAWN_IN_RANGE, DOOMFIST_PUNCH_ANIMATIONS, HEALTH_FIGHTING, HEALTH_ARMOR_ABILITY, HEALTH_SHIELD_ABILITY, HEALTH_PREVENT_SHIELD_REGEN, HEALTH_SHOW_BAR, HEALTH_NON_HEALTH_SHIELD, HEALTH_SHIELD_ABILITY_DECAY_DELAY, ROADHOG_HEALING, ROADHOG_HOOKED, ROADHOG_HOOKING;
	}

	private static CopyOnWriteArrayList<Handler> clientHandlers = new CopyOnWriteArrayList<Handler>();
	private static CopyOnWriteArrayList<Handler> serverHandlers = new CopyOnWriteArrayList<Handler>();
	
	/**Register a new handler to be tracked each tick, removes duplicate handlers and resets handlers before registering*/
	public static void register(boolean isRemote, Handler... handlers) {
		for (Iterator<Handler> it = Arrays.asList(handlers).iterator(); it.hasNext();) {
			Handler handler = it.next();
			CopyOnWriteArrayList<Handler> handlerList = isRemote ? clientHandlers : serverHandlers;
			// remove duplicates
			if (handlerList.contains(handler)) {
				boolean prevBool = handler.bool;
				handlerList.remove(Minewatch.proxy.onHandlerRemove(isRemote, handler));
				handler.bool = prevBool;
			}
			handlerList.add(handler.reset());
		}
	}

	/**Unregister a handler
	 * Note: this must use a registered Handler, not a static field Handler (needs entity)*/
	public static void unregister(boolean isRemote, Handler... handlers) {
		if (handlers != null)
			for (Iterator<Handler> it = Arrays.asList(handlers).iterator(); it.hasNext();) {
				Handler handler = it.next();
				if (handler != null) {
					CopyOnWriteArrayList<Handler> handlerList = isRemote ? clientHandlers : serverHandlers;
					try {
						handlerList.remove(Minewatch.proxy.onHandlerRemove(isRemote, handler));
					}
					catch (Exception e) {
						handlerList.remove(handler);
						e.printStackTrace();
					}
				}
			}
	}
	
	/**Get a registered handler that matches a predicate*/
	@Nullable
	public static Handler getHandler(Predicate<Handler> predicate, boolean isRemote) {
		if (predicate != null) {
			CopyOnWriteArrayList<Handler> handlerList = isRemote ? clientHandlers : serverHandlers;
			for (Iterator<Handler> it = handlerList.iterator(); it.hasNext();) {
				Handler handler = it.next();
				if (predicate.apply(handler))
					return handler;
			}
		}
		return null;
	}
	
	/**Get a registered handler by its entity and/or identifier*/
	@Nullable
	public static Handler getHandler(Entity entity, Identifier identifier) {
		return entity == null ? null : getHandler(entity.getPersistentID(), identifier, entity.world.isRemote);
	}
	
	/**Get a registered handler by its entity and/or identifier*/
	@Nullable
	public static Handler getHandler(UUID uuid, Identifier identifier, boolean isRemote) {
		if (uuid != null) {
			CopyOnWriteArrayList<Handler> handlerList = isRemote ? clientHandlers : serverHandlers;
			for (Iterator<Handler> it = handlerList.iterator(); it.hasNext();) {
				Handler handler = it.next();
				if ((uuid == null || (handler.entity != null && uuid.equals(handler.entity.getPersistentID()))) &&
						(identifier == null || identifier == handler.identifier))
					return handler;
			}
		}
		return null;
	}
	
	public static boolean hasHandler(UUID uuid, Identifier identifier, boolean isRemote) {
		return getHandler(uuid, identifier, isRemote) != null;
	}

	public static boolean hasHandler(Entity entity, Identifier identifier) {
		return getHandler(entity, identifier) != null;
	}
	
	public static boolean hasHandler(Predicate<Handler> predicate, boolean isRemote) {
		return getHandler(predicate, isRemote) != null;
	}

	/**Get all registered handlers by their entity and/or identifier*/
	public static ArrayList<Handler> getHandlers(Entity entity, @Nullable Identifier identifier) {
		return entity == null ? new ArrayList<Handler>() : getHandlers(entity.world.isRemote, entity, identifier, null);
	}
	
	/**Get all registered handlers matching a predicate*/
	public static ArrayList<Handler> getHandlers(Entity entity, Predicate<Handler> predicate) {
		return entity == null ? new ArrayList<Handler>() : getHandlers(entity.world.isRemote, entity, null, predicate);
	}
	
	/**Get all registered handlers by their entity and/or identifier*/
	public static ArrayList<Handler> getHandlers(boolean isRemote, @Nullable Entity entity, @Nullable Identifier identifier, @Nullable Predicate<Handler> predicate) {
		ArrayList<Handler> handlers = new ArrayList<Handler>();
		CopyOnWriteArrayList<Handler> handlerList = isRemote ? clientHandlers : serverHandlers;
		for (Iterator<Handler> it = handlerList.iterator(); it.hasNext();) {
			Handler handler = it.next();
			if ((entity == null || handler.entity == entity) &&
					(identifier == null || identifier == handler.identifier) &&
					(predicate == null || predicate.apply(handler)))
				handlers.add(handler);
		}
		return handlers;
	}
	
	/**Unregister all handlers*/
	public static void unregisterAllHandlers(boolean isRemote) {
		CopyOnWriteArrayList<Handler> handlerList = isRemote ? clientHandlers : serverHandlers;
		unregister(isRemote, handlerList.toArray(new Handler[0]));
	}

	/**Unregister all Handlers linked to this entity that are marked as interruptible.
	 * Used by stuns/similar to cancel active abilities - only needs to be called on SERVER*/
	public static void interrupt(Entity entity) {
		if (entity != null) {
			CopyOnWriteArrayList<Handler> handlerList = entity.world.isRemote ? clientHandlers : serverHandlers;
			for (Iterator<Handler> it = handlerList.iterator(); it.hasNext();) {
				Handler handler = it.next();
				if (handler.interruptible && entity != null && entity == handler.entity) 
					unregister(entity.world.isRemote, handler);
			}
			if (!entity.world.isRemote)
				Minewatch.network.sendToAll(new SPacketSimple(16, entity, false));
			if (entity instanceof EntityLivingBase)
				((EntityLivingBase)entity).stopActiveHand();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientSide(ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && !Minecraft.getMinecraft().isGamePaused()) {
			for (Iterator<Handler> it = clientHandlers.iterator(); it.hasNext();) {
				Handler handler = it.next();
				//Minewatch.logger.info(handler); 
				try {
					if (handler.onClientTick()) 
						unregister(true, handler);
				}
				catch (Exception e) {
					unregister(true, handler);
					e.printStackTrace();
				}
			}
			
			for (KeyBind key : KeyBind.values())
				key.keyPressedEntitiesClient.clear();
		}
	}

	@SubscribeEvent
	public void serverSide(ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			for (Iterator<Handler> it = serverHandlers.iterator(); it.hasNext();) {
				Handler handler = it.next();
				//Minewatch.logger.info(handler);
				try {
					if (handler.onServerTick()) 
						unregister(false, handler);
				}
				catch (Exception e) {
					unregister(false, handler);
					e.printStackTrace();
				}
			}
			
			for (KeyBind key : KeyBind.values())
				key.keyPressedEntitiesServer.clear();
		}
	}

	/**Note: reuse instances (i.e. make static instances) so duplicates can be replaced, 
	 * do not create multiple instances that do the same thing
	 * Note: onTick can possibly run once after onRemove if the Handler is removed during another Handler's onRemove*/
	public static abstract class Handler implements Cloneable {

		/**Used in getHandler()*/
		public Identifier identifier;
		/**Number of total ticks that this handler should be registered*/
		public int initialTicks;
		/**Number of ticks until this handler will be removed*/
		public int ticksLeft;
		/**Can this handler be interrupted by things like Mei's freeze, Ana's sleep dart, McCree's stun, etc.*/
		public boolean interruptible;

		// variables that are only sometimes used by handlers are below
		@Nullable
		public Entity entity;
		@Nullable
		public EntityLivingBase entityLiving;
		@Nullable
		public EntityPlayer player;
		@Nullable
		public Vec3d position;
		@Nullable
		public Ability ability;
		public double number;
		public double number2;
		public double number3;
		@Nullable
		public String string;
		public Boolean bool = false;
		public boolean allowDead = false;

		public Handler(boolean interruptible) {
			this(Identifier.NONE, interruptible);
		}

		public Handler(Identifier identifier, boolean interruptible) {
			this.identifier = identifier;
			this.interruptible = interruptible;
		}

		/**Called every tick on client, returns whether the handler should be removed afterwards*/
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			return --ticksLeft <= 0 || (!allowDead && entity != null && !entity.isEntityAlive());
		}

		/**Called every tick on server, returns whether the handler should be removed afterwards*/
		public boolean onServerTick() {
			return --ticksLeft <= 0 || (!allowDead && entity != null && !entity.isEntityAlive());
		}

		/**Called before the handler is removed*/
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			return this;
		}
		
		/**Called before the handler is removed*/
		public Handler onServerRemove() {
			return this;
		}

		/**Called when registered by the tick handler, used to reset counter and clone (to allow multiple instances)*/
		public Handler reset() {
			ticksLeft = initialTicks;
			try {
				return (Handler) this.clone();
			} catch (CloneNotSupportedException e) {
				return null; // will never happen
			}
		}

		/**Sets number of ticks until this is removed*/
		public Handler setTicks(int ticks) {
			this.initialTicks = ticks;
			this.ticksLeft = ticks;
			return this;
		}

		/**Overridden to check that only entities, identifiers, and world.isRemote are equal*/
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Handler && this.identifier == ((Handler)obj).identifier && 
					(this.string == null || this.string == ((Handler)obj).string))
				if (this.entity != null && ((Handler)obj).entity != null)
					return this.entity == ((Handler)obj).entity && this.entity.world.isRemote == ((Handler)obj).entity.world.isRemote;
				else 
					return true;
			else
				return false;
		}

		@Override
		public String toString() {
			return identifier+": "+ticksLeft+(entity == null ? "" : ", "+entity.getName())+
					(string == null ? "" : ", "+TextFormatting.getTextWithoutFormattingCodes(string))+
					(number == 0 ? "" : ", "+number);
		}

		/**Assumed that this is always called and is called before other .set methods, to properly reset them*/
		public Handler setEntity(Entity entity) {
			this.allowDead = false;
			this.bool = false;
			this.number = 0;
			this.number2 = 0;
			this.number3 = 0;
			this.ability = null;
			this.position = null;
			this.string = null;
			
			this.entity = entity;
			if (entity instanceof EntityLivingBase)
				this.entityLiving = (EntityLivingBase) entity;
			else
				this.entityLiving = null;
			if (entity instanceof EntityPlayer)
				this.player = (EntityPlayer) entity;
			else 
				this.player = null;
			return this;
		}

		// methods that are only sometimes used by handlers are below (for convenience)
		
		public Handler setEntityLiving(EntityLivingBase entity) {
			this.entityLiving = (EntityLivingBase) entity;
			if (entity instanceof EntityPlayer)
				this.player = (EntityPlayer) entity;
			else 
				this.player = null;
			return this;
		}

		public Handler setPlayer(EntityPlayer entity) {
			this.player = (EntityPlayer) entity;
			return this;
		}

		public Handler setPosition(Vec3d position) {
			this.position = position;
			return this;
		}

		public Handler setAbility(Ability ability) {
			this.ability = ability;
			return this;
		}

		public Handler setNumber(double number) {
			this.number = number;
			return this;
		}
		
		public Handler setNumber2(double number) {
			this.number2 = number;
			return this;
		}
		
		public Handler setNumber3(double number) {
			this.number3 = number;
			return this;
		}

		public Handler setString(String string) {
			this.string = string;
			return this;
		}

		public Handler setBoolean(Boolean bool) {
			this.bool = bool;
			return this;
		}
		
		public Handler setAllowDead(boolean allowDead) {
			this.allowDead = allowDead;
			return this;
		}

	}

}

/*
public static final Handler NAME = new Handler(Identifier.IDENTIFIER, false) {
	@Override
	@SideOnly(Side.CLIENT)
	public boolean onClientTick() {
		
		return super.onClientTick();
	}
	@Override
	public boolean onServerTick() {
		
		return super.onServerTick();
	}
	@Override
	@SideOnly(Side.CLIENT)
	public Handler onClientRemove() {
		
		return super.onClientRemove();
	}
	@Override
	public Handler onServerRemove() {
		
		return super.onServerRemove();
	}
};
*/