package twopiradians.minewatch.common.hero;

import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

@Mod.EventBusSubscriber
public class HealthManager {

	/**
	 * Minecraft representations:
	 * 	shields from abilities = absorption (default for non-mw absorption)
	 * 	shields = health boost (regenerates 30 points/second after no damage for 3 seconds)
	 * 	armor from abilities = absorption + ARMOR_ABILITY handler (damage reduction)	
	 * 	armor = health boost (damage reduction)
	 * 	health = health boost
	 */

	// TODO how to manage temp absorption? remove unaccounted for absorption or just remove in onServerRemove and forget about glitched absorption
	public static final Handler ARMOR_ABILITY = new Handler(Identifier.HEALTH_ARMOR_ABILITY, false) {
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
			entityLiving.setAbsorptionAmount(0);
			System.out.println("removing absorption"); // TODO
			return super.onServerRemove();
		}
	};

	/**Health types in the order that damage is taken in*/
	public static enum Type {
		SHIELD_ABILITY(true), SHIELD(false), ARMOR_ABILITY(true), ARMOR(false), HEALTH(true);

		public boolean abilityGranted;

		Type(boolean abilityGranted) {
			this.abilityGranted = abilityGranted;
		}
	}

	/**entity = recently hurt entity*/
	public static final Handler FIGHTING = new Handler(Identifier.HEALTH_FIGHTING, false) {};
	/**entity = entity to show health for (this is only for clients)*/
	public static final Handler SHOW_HEALTH = new Handler(Identifier.SHOW_HEALTH, false) {};

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

		float health = MathHelper.clamp(getBaseHealth(hero, Type.HEALTH), 0, current);
		map.put(Type.HEALTH, health);
		float armor = MathHelper.clamp(getBaseHealth(hero, Type.ARMOR), 0, current-health);
		map.put(Type.ARMOR, armor);
		float shield = MathHelper.clamp(getBaseHealth(hero, Type.SHIELD), 0, current-health-armor);
		map.put(Type.SHIELD, shield);
		Handler handler = TickHandler.getHandler(entity, Identifier.HEALTH_ARMOR_ABILITY);
		float armorAbility = handler == null ? 0 : Math.min((float) handler.number, absorption);
		map.put(Type.ARMOR_ABILITY, armorAbility);
		float shieldAbility = absorption - armorAbility;
		map.put(Type.SHIELD_ABILITY, shieldAbility);
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

	public static void addHealth(EntityLivingBase entity, Type type, float amount) {
		if (entity != null) 
			switch (type) {
			case SHIELD_ABILITY:
				if (!entity.world.isRemote)
					entity.setAbsorptionAmount(amount);
				TickHandler.register(entity.world.isRemote, ARMOR_ABILITY.setEntity(entity).setTicks(999999));
				break;
			case SHIELD:
				break;
			case ARMOR_ABILITY:
				break;
			case ARMOR:
				break;
			case HEALTH:
				break;
			}
	}

	/**
	 * EVENTS
	 */

	@SubscribeEvent
	public static void displayHealthBarWhenHurt(LivingAttackEvent event) {		
		if (Config.healthBars && event.getEntityLiving() != null) {
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

	@SubscribeEvent
	public static void armorReduction(LivingHurtEvent event) {	
		EnumHero hero = SetManager.getWornSet(event.getEntityLiving());
		if (hero != null) {
			Type type = getCurrentHealthType(event.getEntityLiving(), hero);
			float amount = event.getAmount();

			// armor damage reduction
			if (type == Type.ARMOR || type == Type.ARMOR_ABILITY) {
				if (amount >= 5)
					amount -= 2.5f;
				else
					amount /= 2f;
				event.setAmount(amount);
			}
		}
	}

}