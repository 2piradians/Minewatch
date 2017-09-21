package twopiradians.minewatch.common.tickhandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.actors.threadpool.Arrays;
import twopiradians.minewatch.common.hero.Ability;

/**Used to easily create/manage tick timers and other tick-dependent things*/
public class TickHandler {

	/**Identifiers used in getHandler()*/
	public enum Identifier {
		NONE, REAPER_TELEPORT, GENJI_DEFLECT, GENJI_STRIKE, GENJI_SWORD, MCCREE_ROLL, MERCY_NOT_REGENING, MERCY_VOICE_COOLDOWN, WEAPON_WARNING, HANZO_SONIC, POTION_FROZEN, POTION_DELAY, ABILITY_USING, PREVENT_ROTATION, PREVENT_MOVEMENT, PREVENT_INPUT, ABILITY_MULTI_COOLDOWNS, REAPER_WRAITH, ANA_SLEEP;
	}

	private static CopyOnWriteArrayList<Handler> clientHandlers = new CopyOnWriteArrayList<Handler>();
	private static CopyOnWriteArrayList<Handler> serverHandlers = new CopyOnWriteArrayList<Handler>();

	/**Register a new handler to be tracked each tick, removes duplicate handlers and resets handlers before registering*/
	public static void register(boolean isRemote, Handler... handlers) {
		for (Iterator<Handler> it = Arrays.asList(handlers).iterator(); it.hasNext();) {
			Handler handler = it.next();
			CopyOnWriteArrayList<Handler> handlerList = isRemote ? clientHandlers : serverHandlers;
			// remove duplicates
			if (handlerList.contains(handler)) 
				handlerList.remove(handler);
			handlerList.add(handler.reset());
		}
	}

	/**Unregister a handler
	 * Note: this must use a registered Handler, not a static field Handler (needs entity)*/
	public static void unregister(boolean isRemote, Handler... handlers) {
		for (Iterator<Handler> it = Arrays.asList(handlers).iterator(); it.hasNext();) {
			Handler handler = it.next();
			if (handler != null) {
				CopyOnWriteArrayList<Handler> handlerList = isRemote ? clientHandlers : serverHandlers;
				handlerList.remove(handler.onRemove());
			}
		}
	}

	/**Get a registered handler by its entity and/or identifier*/
	@Nullable
	public static Handler getHandler(Entity entity, Identifier identifier) {
		CopyOnWriteArrayList<Handler> handlerList = entity.world.isRemote ? clientHandlers : serverHandlers;
		for (Iterator<Handler> it = handlerList.iterator(); it.hasNext();) {
			Handler handler = it.next();
			if ((entity == null || handler.entity == entity) &&
					(identifier == null || identifier == handler.identifier))
				return handler;
		}
		return null;
	}

	public static boolean hasHandler(Entity entity, Identifier identifier) {
		return getHandler(entity, identifier) != null;
	}

	/**Unregister all Handlers linked to this entity that are marked as interruptible.
	 * Used by stuns/similar to cancel active abilities*/
	public static void interrupt(Entity entity) {
		CopyOnWriteArrayList<Handler> handlerList = entity.world.isRemote ? clientHandlers : serverHandlers;
		for (Iterator<Handler> it = handlerList.iterator(); it.hasNext();) {
			Handler handler = it.next();
			if (handler.interruptible)
				unregister(entity.world.isRemote, handler);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientSide(ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) 
			for (Iterator<Handler> it = clientHandlers.iterator(); it.hasNext();) {
				Handler handler = it.next();
				System.out.println(handler.identifier+": "+handler.ticksLeft+", "+handler.entity.getName());//TODO remove
				if (handler.onClientTick()) 
					unregister(true, handler);
			}
	}

	@SubscribeEvent
	public void serverSide(ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) 
			for (Iterator<Handler> it = serverHandlers.iterator(); it.hasNext();) {
				Handler handler = it.next();
				System.out.println(handler.identifier+": "+handler.ticksLeft+", "+handler.entity.getName());//TODO remove
				if (handler.onServerTick()) 
					unregister(false, handler);
			}
	}

	/**Note: reuse instances (i.e. make static instances) so duplicates can be replaced, 
	 * do not create multiple instances that do the same thing*/
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
			return --ticksLeft <= 0;
		}

		/**Called every tick on server, returns whether the handler should be removed afterwards*/
		public boolean onServerTick() {
			return --ticksLeft <= 0;
		}

		/**Called before the handler is removed*/
		public Handler onRemove() {
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
			if (obj instanceof Handler && this.identifier == ((Handler)obj).identifier)
				if (this.entity != null && ((Handler)obj).entity != null)
					return this.entity == ((Handler)obj).entity && this.entity.world.isRemote == ((Handler)obj).entity.world.isRemote;
				else 
					return true;
			else
				return false;
		}

		// methods that are only sometimes used by handlers are below (for convenience)

		public Handler setEntity(Entity entity) {
			this.entity = entity;
			if (entity instanceof EntityLivingBase)
				this.entityLiving = (EntityLivingBase) entity;
			if (entity instanceof EntityPlayer)
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

	}

}