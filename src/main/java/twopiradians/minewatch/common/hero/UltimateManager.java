package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.hero.EntityHero;

@Mod.EventBusSubscriber
public class UltimateManager {
	
	public enum AttackType {
		DAMAGE, HEAL, SELF_HEAL
	}

	/**Map of player UUIDs and their current ult charge*/
	private static HashMap<UUID, Float> playerChargesClient = Maps.newHashMap();
	/**Map of player UUIDs and their current ult charge*/
	private static HashMap<UUID, Float> playerChargesServer = Maps.newHashMap();

	private static HashMap<UUID, Float> getPlayerCharges(boolean isRemote) {
		return isRemote ? playerChargesClient : playerChargesServer;
	}
	
	/**Does this entity have enough charge to use ult*/
	public static boolean canUseUltimate(Entity entity) {
		EnumHero hero = SetManager.getWornSet(entity);
		return hero != null && getCurrentCharge(entity) >= getMaxCharge(hero);
	}

	/**Get EntityHero or EntityPlayer's current ult charge*/
	public static float getCurrentCharge(Entity entity) {
		if (entity instanceof EntityHero)
			return ((EntityHero)entity).ultCharge;
		else if (entity instanceof EntityPlayer) {
			HashMap<UUID, Float> playerCharges = getPlayerCharges(entity.world.isRemote);
			if (playerCharges.containsKey(entity.getPersistentID()))
				return playerCharges.get(entity.getPersistentID());
		}
		return 0;
	}

	/**Get an entity's max charge to use ultimate*/
	public static float getMaxCharge(Entity entity) {
		EnumHero hero = SetManager.getWornSet(entity);
		if (hero != null)
			return getMaxCharge(hero);
		return 0;
	}

	/**Get a hero's max charge to use ultimate*/
	public static float getMaxCharge(EnumHero hero) {
		if (hero != null)
			return hero.ultimateChargeRequired;
		return 0;
	}

	/**Add ultimate charge for an entity - amount should be unscaled*/
	public static void addCharge(Entity entity, float amount) {
		if (entity instanceof EntityHero)
			amount += ((EntityHero)entity).ultCharge;
		else if (entity instanceof EntityPlayer) {
			HashMap<UUID, Float> playerCharges = getPlayerCharges(entity.world.isRemote);
			amount += playerCharges.containsKey(entity.getPersistentID()) ? playerCharges.get(entity.getPersistentID()) : 0;
		}
		setCharge(entity, amount);
	}

	/**Set ultimate charge for an entity - amount should be unscaled*/
	public static void setCharge(Entity entity, float amount) {
		if (entity instanceof EntityHero)
			((EntityHero)entity).ultCharge = amount;
		else if (entity instanceof EntityPlayer) {
			getPlayerCharges(entity.world.isRemote).put(entity.getPersistentID(), amount);
		}
	}
	
	/**Handles ultimate charge for attacks / abilities / healing*/
	public static void handleAbilityCharge(Entity actualThrower, Entity damageSource, float amount, AttackType type) {
		
	}

	@SubscribeEvent
	public static void handleNormalCharge(TickEvent.PlayerTickEvent event) {
		if (event.phase == Phase.END)
			handleNormalCharge(event.player);
	}

	/**Called once per tick for players and heroes - manages normal ult charge*/
	public static void handleNormalCharge(Entity entity) {
		if (entity != null && entity.ticksExisted % 4 == 0) {
			EnumHero hero = SetManager.getWornSet(entity);
			if (hero != null) {
				addCharge(entity, 1);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static void renderUltimateMeter(EntityPlayer player, EnumHero hero, double width, double height) {
		
	}

}