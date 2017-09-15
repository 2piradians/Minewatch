package twopiradians.minewatch.common.tickhandler;

import java.util.ArrayList;

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

/**Used to easily create/manage tick timers and other tick-dependent things*/
public class TickHandler {

	/**Identifiers used in getHandler()*/
	public enum Identifier {
		NONE, REAPER_TELEPORT, GENJI_DEFLECT, GENJI_STRIKE, GENJI_SWORD, MCCREE_ROLL, MERCY_NOT_REGENING, MERCY_VOICE_COOLDOWN, WEAPON_WARNING, HANZO_SONIC, POTION_FROZEN, POTION_DELAY, ABILITY_USING;
	}

	private static ArrayList<Handler> clientHandlers = new ArrayList<Handler>();
	private static ArrayList<Handler> serverHandlers = new ArrayList<Handler>();

	// stall registration while ticking to prevent possible concurrentmodification
	private static boolean clientTicking;
	private static boolean serverTicking;
	private static ArrayList<Handler> clientHandlersStalled = new ArrayList<Handler>();
	private static ArrayList<Handler> serverHandlersStalled = new ArrayList<Handler>();

	/**Register a new handler to be tracked each tick, removes duplicate handlers and resets handlers before registering*/
	public static void register(boolean isRemote, Handler handler) {
		if (handler != null) {
			ArrayList<Handler> handlerList = isRemote ? clientHandlers : serverHandlers;
			if (isRemote && clientTicking)
				clientHandlers.add(handler);
			else if (!isRemote && serverTicking)
				serverHandlers.add(handler);
			else {
				// remove duplicates
				if (handlerList.contains(handler)) 
					handlerList.remove(handler);
				handlerList.add(handler.reset());
			}
		}
	}

	/**Unregister a handler*/
	public static void unregister(Handler handler) {
		if (handler != null && handler.entity != null) {
			ArrayList<Handler> handlerList = handler.entity.world.isRemote ? clientHandlers : serverHandlers;
			handlerList.remove(handler);
		}
	}

	/**Get a registered handler by its entity and/or identifier*/
	@Nullable
	public static Handler getHandler(Entity entity, Identifier identifier) {
		ArrayList<Handler> handlerList = entity.world.isRemote ? clientHandlers : serverHandlers;
		for (Handler handler : handlerList)
			if ((entity == null || handler.entity == entity) &&
					(identifier == null || identifier == handler.identifier))
				return handler;
		return null;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientSide(ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			ArrayList<Handler> handlersToRemove = new ArrayList<Handler>();
			clientTicking = true;
			for (Handler handler : clientHandlers) {
				if (handler.onClientTick()) {
					handler.onRemove();
					handlersToRemove.add(handler);
				}
			}
			for (Handler handler : handlersToRemove)
				clientHandlers.remove(handler);
			clientTicking = false;
			for (Handler handler : clientHandlersStalled)
				register(true, handler);
			clientHandlersStalled.clear();
		}
	}

	@SubscribeEvent
	public void serverSide(ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			serverTicking = true;
			ArrayList<Handler> handlersToRemove = new ArrayList<Handler>();
			for (Handler handler : serverHandlers) {
				System.out.println(serverHandlers.size()+", "+handler.identifier.name());
				if (handler.onServerTick()) {
					System.out.println("removing: "+handler.identifier.name());
					handler.onRemove();
					handlersToRemove.add(handler);
				}
			}
			for (Handler handler : handlersToRemove)
				serverHandlers.remove(handler);
			serverTicking = false;
			for (Handler handler : serverHandlersStalled)
				register(false, handler);
			serverHandlersStalled.clear();
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

		// variables that are only sometimes used by handlers are below
		@Nullable
		public Entity entity;
		@Nullable
		public EntityLivingBase entityLiving;
		@Nullable
		public EntityPlayer player;
		@Nullable
		public Vec3d position;

		public Handler() {
			this(Identifier.NONE);
		}

		public Handler(Identifier identifier) {
			this.identifier = identifier;
		}

		/**Called every tick, returns whether the handler should be removed afterwards*/
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			return --ticksLeft <= 0;
		}

		public boolean onServerTick() {
			return --ticksLeft <= 0;
		}

		/**Called before the handler is removed*/
		public void onRemove() {

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

		/**Overridden to check that only entities are equal and world.isRemote is equal*/
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

	}

}