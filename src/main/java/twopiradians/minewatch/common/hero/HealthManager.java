package twopiradians.minewatch.common.hero;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

@Mod.EventBusSubscriber
public class HealthManager {

	/**
	 * Minecraft representations:
	 *  absorption = non-mw absorption	
	 *  shields from abilities = absorption
	 * 	shields = health boost (regenerates 30 points/second after no damage for 3 seconds)
	 * 	armor from abilities = absorption + ARMOR_ABILITY handler (damage reduction)	
	 * 	armor = health boost (damage reduction)
	 * 	health = health boost
	 */

	/**entity = entity w/ health, number = absorption amount (scaled) - HANDLED WITH METHODS HERE ONLY*/
	public static final Handler ARMOR_ABILITY = new Handler(Identifier.HEALTH_ARMOR_ABILITY, false) {
		@Override
		public Handler onServerRemove() {
			entityLiving.setAbsorptionAmount(Math.max(0, entityLiving.getAbsorptionAmount() - (float)this.number/10f));
			return super.onServerRemove();
		}
		/**Kill handler if number reaches 0, also manages absorption*/
		@Override
		public Handler setNumber(double number) {
			number = Math.max(0, number);
			// set absorption
			if (!entity.world.isRemote) {
				double diff = number - this.number;
				entityLiving.setAbsorptionAmount((float) (entityLiving.getAbsorptionAmount() + diff/10f));
			}
			// kill handler if no absorption left
			if (number <= 0)
				this.ticksLeft = 1;
			return super.setNumber(number);
		}
	};
	/**entity = entity w/ health, number = absorption amount (scaled), number2 = amount to decay, number3 = decay rate - HANDLED WITH METHODS HERE ONLY*/
	public static final Handler SHIELD_ABILITY = new Handler(Identifier.HEALTH_SHIELD_ABILITY, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// decay
			if (number2 > 0 && number3 > 0) {
				this.setNumber(number - number3);
				number2 -= number3;
			}
			else {
				number2 = 0;
				number3 = 0;
			}
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			// decay
			if (number2 > 0 && number3 > 0) {
				this.setNumber(number - number3);
				number2 -= number3;
			}
			else {
				number2 = 0;
				number3 = 0;
			}
			return super.onServerTick();
		}
		@Override
		public Handler onServerRemove() {
			entityLiving.setAbsorptionAmount(Math.max(0, entityLiving.getAbsorptionAmount() - (float)this.number/10f));
			return super.onServerRemove();
		}
		/**Kill handler if number reaches 0, also manages absorption*/
		@Override
		public Handler setNumber(double number) {
			number = Math.max(0, number);
			// set absorption
			if (!entity.world.isRemote) {
				double diff = number - this.number;
				entityLiving.setAbsorptionAmount((float) (entityLiving.getAbsorptionAmount() + diff/10f));
			}
			// kill handler if no absorption left
			if (number <= 0)
				this.ticksLeft = 1;
			return super.setNumber(number);
		}
	};
	/**entity = entity w/ health, number = amount to decay, number2 = decay rate - HANDLED WITH METHODS HERE ONLY*/
	public static final Handler SHIELD_ABILITY_DECAY_DELAY = new Handler(Identifier.HEALTH_SHIELD_ABILITY_DECAY_DELAY, false) {
		@Override
		public Handler onServerRemove() {
			if (entityLiving != null)
				HealthManager.setShieldAbilityDecay(entityLiving, (float) number, (float) number2, 0);
			return super.onServerRemove();
		}
	};

	/**Health types in the order that damage is taken in*/
	public static enum Type {
		ABSORPTION(false), SHIELD_ABILITY(true), SHIELD(false), ARMOR_ABILITY(true), ARMOR(false), HEALTH(true);

		public boolean abilityGranted;

		Type(boolean abilityGranted) {
			this.abilityGranted = abilityGranted;
		}
	}

	/**entity = entity with shield, number = amount of shield*/
	public static final Handler NON_HEALTH_SHIELD = new Handler(Identifier.HEALTH_NON_HEALTH_SHIELD, false) {
		/**Kill handler if number reaches 0, also manages absorption*/
		@Override
		public Handler setNumber(double number) {
			number = Math.max(0, number);

			// kill handler if no absorption left
			if (number <= 0)
				this.ticksLeft = 1;
			return super.setNumber(number);
		}
	};
	/**entity = recently hurt entity*/
	public static final Handler PREVENT_SHIELD_REGEN = new Handler(Identifier.HEALTH_PREVENT_SHIELD_REGEN, false) {};
	/**entity = recently hurt entity*/
	public static final Handler FIGHTING = new Handler(Identifier.HEALTH_FIGHTING, false) {};
	/**entity = entity to show health for (this is only for clients)*/
	public static final Handler SHOW_HEALTH = new Handler(Identifier.HEALTH_SHOW_BAR, false) {};

	/**Saved values to be restored after absorption is reduced*/
	private static float savedAbsorption;
	private static EntityLivingBase savedEntity;

	/**
	 * BASE HERO HEALTH
	 */

	public static float getBaseHealth(EnumHero hero, Type...types) {
		float health = 0;
		if (hero != null) 
			for (Type type : types)
				switch (type) {
				case HEALTH:
					health += hero.baseHealth;
					break;
				case ARMOR:
					health += hero.baseArmor;
					break;
				case SHIELD:
					health += hero.baseShield;
					break;
				}
		return health;
	}

	public static float getTotalBaseHealth(EnumHero hero) {
		return getBaseHealth(hero, Type.values());
	}

	/**
	 * ENTITY CURRENT HEALTH
	 */

	/**Get map of all types of health*/
	public static HashMap<Type, Float> getAllCurrentHealth(EntityLivingBase entity, EnumHero hero) {
		HashMap<Type, Float> map = Maps.newHashMap();
		float current = getCurrentCombinedHealth(entity);
		float absorption = entity.getAbsorptionAmount()*10f;
		Handler shieldHandler = TickHandler.getHandler(entity, Identifier.HEALTH_NON_HEALTH_SHIELD);
		float nonHealthShield = shieldHandler == null ? 0 : (float) shieldHandler.number;

		float health = MathHelper.clamp(getBaseHealth(hero, Type.HEALTH), 0, current-nonHealthShield);
		map.put(Type.HEALTH, health);
		float armor = MathHelper.clamp(getBaseHealth(hero, Type.ARMOR), 0, current-health);
		map.put(Type.ARMOR, armor);
		float shield = MathHelper.clamp(getBaseHealth(hero, Type.SHIELD), 0, current-health-armor);
		map.put(Type.SHIELD, shield);
		Handler armorAbilityHandler = TickHandler.getHandler(entity, Identifier.HEALTH_ARMOR_ABILITY);
		float armorAbility = armorAbilityHandler == null ? 0 : Math.min((float) armorAbilityHandler.number, absorption);
		map.put(Type.ARMOR_ABILITY, armorAbility);
		Handler shieldAbilityHandler = TickHandler.getHandler(entity, Identifier.HEALTH_SHIELD_ABILITY);
		float shieldAbility = shieldAbilityHandler == null ? 0 : Math.min((float) shieldAbilityHandler.number, absorption);
		map.put(Type.SHIELD_ABILITY, shieldAbility);
		float extraAbsorption = Math.max(0, absorption - shieldAbility - armorAbility);
		map.put(Type.ABSORPTION, extraAbsorption);
		
		// add extra to health (maybe from health boost potion)
		health += current - getSum(map);
		map.put(Type.HEALTH, health);
		
		return map;
	}

	/**Get a single type's health (has to calculate all types, so use getAllCurrentHealth if possible)*/
	public static float getCurrentHealth(EntityLivingBase entity, EnumHero hero, Type...types) {
		float health = 0;
		HashMap<Type, Float> map = getAllCurrentHealth(entity, hero);
		for (Type type : types)
			health += map.get(type);
		return health;
	}

	/**Get scaled max health (taking into account absorption)*/
	public static float getMaxCombinedHealth(EntityLivingBase entity) {
		float health = 0;
		if (entity != null)
			health = entity.getMaxHealth()*10f + entity.getAbsorptionAmount()*10f;
		return health;
	}

	/**Get scaled current health*/
	public static float getCurrentCombinedHealth(EntityLivingBase entity) {
		float health = 0;
		if (entity != null)
			health = Math.min(entity.getHealth()*10f + entity.getAbsorptionAmount()*10f, getMaxCombinedHealth(entity));
		return health;
	}

	/**Get the active health type that damage will apply to first*/
	public static Type getCurrentHealthType(EntityLivingBase entity, EnumHero hero) {
		HashMap<Type, Float> map = getAllCurrentHealth(entity, hero);
		return getCurrentHealthType(map);
	}

	/**Get the active health type that damage will apply to first*/
	public static Type getCurrentHealthType(HashMap<Type, Float> map) {
		Type ret = Type.HEALTH;
		for (Type type : Type.values()) {
			ret = type;
			if (map.containsKey(type) && map.get(type) > 0)
				return type;
		}
		return ret;
	}

	/**
	 * SETTING ENTITY CURRENT HEALTH
	 */

	/**Adds a type of health (only used for SHIELD_ABILITY and ARMOR_ABILITY) - only call on server (packet handled automatically)
	 * @param amount to add (scaled) */
	public static void addHealth(EntityLivingBase entity, Type type, float amount) {
		if (entity != null) 
			switch (type) {
			case SHIELD_ABILITY:
				// send packet
				if (!entity.world.isRemote)
					Minewatch.network.sendToDimension(new SPacketSimple(70, entity, true, amount, type.ordinal(), 0), entity.world.provider.getDimension());
				// add amount to handler
				Handler handler = TickHandler.getHandler(entity, Identifier.HEALTH_SHIELD_ABILITY);
				if (handler == null)
					TickHandler.register(entity.world.isRemote, SHIELD_ABILITY.setEntity(entity).setTicks(999999).setNumber(amount));
				else
					handler.setNumber(handler.number + amount);
				break;
			case SHIELD:
				break;
			case ARMOR_ABILITY:
				// send packet
				if (!entity.world.isRemote)
					Minewatch.network.sendToDimension(new SPacketSimple(70, entity, true, amount, type.ordinal(), 0), entity.world.provider.getDimension());
				// add amount to handler
				handler = TickHandler.getHandler(entity, Identifier.HEALTH_ARMOR_ABILITY);
				if (handler == null)
					TickHandler.register(entity.world.isRemote, ARMOR_ABILITY.setEntity(entity).setTicks(999999).setNumber(amount));
				else
					handler.setNumber(handler.number + amount);
				break;
			case ARMOR:
				break;
			case HEALTH:
				break;
			}
	}

	/**Removes a type of health (only used for SHIELD_ABILITY, ARMOR_ABILITY, and ABSORPTION) - only call on server (packet handled automatically)
	 * @param amount to remove (scaled)
	 * @return amount of health removed */
	public static float removeHealth(EntityLivingBase entity, Type type, @Nullable EnumHero hero, float amount) {
		if (entity != null) 
			switch (type) {
			case SHIELD_ABILITY:
				// send packet
				if (!entity.world.isRemote)
					Minewatch.network.sendToDimension(new SPacketSimple(70, entity, false, amount, type.ordinal(), 0), entity.world.provider.getDimension());
				// remove amount from handler
				Handler handler = TickHandler.getHandler(entity, Identifier.HEALTH_SHIELD_ABILITY);
				if (handler != null) {
					double prevNumber = handler.number;
					handler.setNumber(handler.number - amount);
					return (float) Math.abs(prevNumber - handler.number);
				}
				break;
			case ARMOR_ABILITY:
				// send packet
				if (!entity.world.isRemote)
					Minewatch.network.sendToDimension(new SPacketSimple(70, entity, false, amount, type.ordinal(), 0), entity.world.provider.getDimension());
				// remove amount from handler
				handler = TickHandler.getHandler(entity, Identifier.HEALTH_ARMOR_ABILITY);
				if (handler != null) {
					double prevNumber = handler.number;
					handler.setNumber(handler.number - amount);
					return (float) Math.abs(prevNumber - handler.number);
				}
				break;
			case ABSORPTION:
				// set absorption
				if (!entity.world.isRemote && hero != null) {
					float absorption = getCurrentHealth(entity, hero, Type.ABSORPTION);
					float prev = entity.getAbsorptionAmount();
					entity.setAbsorptionAmount(entity.getAbsorptionAmount() - Math.min(amount/10f, absorption/10f));
					return entity.getAbsorptionAmount() - prev;
				}
				break;
			case SHIELD:
				// send packet
				if (!entity.world.isRemote)
					Minewatch.network.sendToDimension(new SPacketSimple(70, entity, false, amount, type.ordinal(), 0), entity.world.provider.getDimension());
				// remove amount from handler
				handler = TickHandler.getHandler(entity, Identifier.HEALTH_NON_HEALTH_SHIELD);
				if (handler != null) {
					handler.setNumber(Math.max(handler.number - amount, 0));
				}
				break;
			}
		return 0;
	}

	/**Sets decay for shield ability - only call on server (packet handled automatically)*/
	public static void setShieldAbilityDecay(EntityLivingBase entity, float amount, float decayPerSecond, int delayTicks) {
		Handler handler = TickHandler.getHandler(entity, Identifier.HEALTH_SHIELD_ABILITY);
		if (handler != null) {
			// delay
			if (delayTicks > 0) {
				if (!entity.world.isRemote) {
					// copy over delay ticks
					amount += handler.number2;
					decayPerSecond = (float) Math.max(handler.number3*20f, decayPerSecond);
					handler.number2 = 0;
					handler.number3 = 0;
					
					handler = TickHandler.getHandler(entity, Identifier.HEALTH_SHIELD_ABILITY_DECAY_DELAY);
					if (handler == null)
						TickHandler.register(entity.world.isRemote, SHIELD_ABILITY_DECAY_DELAY.setEntity(entity).setTicks(delayTicks).setNumber(amount).setNumber2(decayPerSecond));
					else
						handler.setTicks(delayTicks).setNumber(handler.number+amount).setNumber2(decayPerSecond);
				}
			}
			// decay
			else 
				handler.setNumber2(amount).setNumber3(decayPerSecond/20f);
			// send packet
			if (!entity.world.isRemote)
				Minewatch.network.sendToDimension(new SPacketSimple(72, entity, true, amount, decayPerSecond, delayTicks), entity.world.provider.getDimension());
		}
	}

	/**
	 * EVENTS
	 */

	@SubscribeEvent
	public static void displayHealthBarWhenHurt(LivingAttackEvent event) {	
		if (event.getAmount() > 0 &&
				(event.getEntityLiving() instanceof EntityPlayer || 
						event.getEntityLiving() instanceof EntityHero)) {
			// server - prevent shield regen for 3 seconds when hit
			if (!event.getEntityLiving().world.isRemote)
				TickHandler.register(false, PREVENT_SHIELD_REGEN.setEntity(event.getEntityLiving()).setTicks(60));

			if (Config.healthBars) {
				// client - register for fighting (don't know who attacked on client)
				if (event.getEntityLiving().world.isRemote) { 
					TickHandler.register(true, FIGHTING.setEntity(event.getEntity()).setTicks(100));
				}
				// server - tell client to show health bar
				else if (event.getSource().getTrueSource() instanceof EntityLivingBase) {
					EntityLivingBase source = ((EntityLivingBase)event.getSource().getTrueSource());
					EntityLivingBase target = event.getEntityLiving();

					if (source instanceof EntityPlayerMP)
						Minewatch.network.sendTo(new SPacketSimple(69, target, false), (EntityPlayerMP) source);
				}
			}
		}
	}

	@SubscribeEvent
	public static void saveAbsorption(LivingHurtEvent event) {	
		EnumHero hero = SetManager.getWornSet(event.getEntityLiving());
		if (!event.getEntityLiving().world.isRemote && hero != null && event.getAmount() > 0) {
			savedAbsorption = event.getEntityLiving().getAbsorptionAmount();
			savedEntity = event.getEntityLiving();
			event.getEntityLiving().setAbsorptionAmount(0);
		}
	}

	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void handleHealth(LivingDamageEvent event) {	
		if (event.getEntityLiving() == savedEntity) {
			// restore absorption bc we handle it manually afterwards
			event.getEntityLiving().setAbsorptionAmount(savedAbsorption);
			savedAbsorption = 0;
			savedEntity = null;					

			EnumHero hero = SetManager.getWornSet(event.getEntityLiving());
			if (!event.getEntityLiving().world.isRemote && hero != null && event.getAmount() > 0) {
				savedAbsorption = 0;
				savedEntity = null;
				HashMap<Type, Float> currentMap = getAllCurrentHealth(event.getEntityLiving(), hero);
				HashMap<Type, Float> map = Maps.newHashMap(currentMap);
				Type type = getCurrentHealthType(map);
				Type newType = type;
				float amount = event.getAmount();

				do {
					// reset type if it's changed
					type = newType;

					// armor damage reduction
					if (type == Type.ARMOR || type == Type.ARMOR_ABILITY) {
						if (amount >= 0.5f)
							amount -= 0.25f;
						else
							amount /= 2f;
					}

					// reduce tracked ability health and modify absorption
					amount = Math.max(0, amount - removeHealth(event.getEntityLiving(), type, hero, amount*10f)/10f);

					// subtract from map and update amount
					float remaining = Math.max(0, map.get(type) - amount*10f);
					amount -= (map.get(type) - remaining)/10f;
					map.put(type, remaining);

					// recalculate type
					newType = getCurrentHealthType(map);
				}
				while(newType != type && amount > 0);

				event.setAmount((getSum(currentMap) - getSum(map))/10f); // set amount to the difference in maps
			}
		}
	}

	public static float getSum(HashMap<Type, Float> map) {
		float sum = 0;
		if (map != null)
			for (Type type : map.keySet())
				sum += map.get(type);
		return sum;
	}

	/**Removed extra absorption - may come from mw if mc is closed improperly and onServerRemove isn't called*/
	@SubscribeEvent
	public static void removeUnaccountedForAbsorption(PlayerLoggedInEvent event) {
		if (!event.player.world.isRemote) {
			PotionEffect effect = event.player.getActivePotionEffect(MobEffects.ABSORPTION);
			Handler armorAbility = TickHandler.getHandler(event.player, Identifier.HEALTH_ARMOR_ABILITY);
			Handler shieldAbility = TickHandler.getHandler(event.player, Identifier.HEALTH_SHIELD_ABILITY);
			float allowedAbsorption = (effect != null ? (effect.getAmplifier()+1)*4 : 0) +
					(armorAbility != null ? (float) armorAbility.number : 0) +
					(shieldAbility != null ? (float) shieldAbility.number : 0);
			if (event.player.getAbsorptionAmount() > allowedAbsorption) {
				Minewatch.logger.info("Player "+event.player.getName()+" had too much absorption ("+event.player.getAbsorptionAmount()+"), reduced to "+allowedAbsorption);
				event.player.setAbsorptionAmount(allowedAbsorption);
			}
		}
	}

	@SubscribeEvent
	public static void healShields(TickEvent.PlayerTickEvent event) {
		EnumHero hero = SetManager.getWornSet(event.player);
		if (!event.player.world.isRemote && event.phase == Phase.END && hero != null)
			handleShieldRegen(event.player, hero);
	}

	/**Called once per tick for players and heroes*/
	public static void handleShieldRegen(EntityLivingBase entity, EnumHero hero) {
		if (entity != null && hero != null && entity.ticksExisted % 5 == 0 &&
				(getCurrentHealth(entity, hero, Type.SHIELD) < getBaseHealth(hero, Type.SHIELD)/* || TickHandler.hasHandler(entity, Identifier.HEALTH_NON_HEALTH_SHIELD)*/) &&
				!TickHandler.hasHandler(entity, Identifier.HEALTH_PREVENT_SHIELD_REGEN)) {
			Handler handler = TickHandler.getHandler(entity, Identifier.HEALTH_NON_HEALTH_SHIELD);
			float amount = Math.min(30f/20f*5f, getBaseHealth(hero, Type.SHIELD)-getCurrentHealth(entity, hero, Type.SHIELD));
			entity.heal(amount/10f);
			if (handler == null) 
				TickHandler.register(false, NON_HEALTH_SHIELD.setEntity(entity).setTicks(999999).setNumber(amount));
			else 
				handler.setNumber(Math.max(handler.number + amount, 0));
			if (handler == null)
				handler = TickHandler.getHandler(entity, Identifier.HEALTH_NON_HEALTH_SHIELD);
			amount = (float) handler.number;
			Minewatch.network.sendToDimension(new SPacketSimple(71, entity, false, amount, 0, 0), entity.world.provider.getDimension());
		}
	}

}