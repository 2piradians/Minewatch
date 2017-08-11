package twopiradians.minewatch.client.key;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.packet.PacketSyncKeys;

public class Keys {
	// The keys that will display underneath the icon
	public static enum KeyBind {
		NONE, ABILITY_1, ABILITY_2, RMB;

		private HashMap<UUID, Integer> cooldowns = Maps.newHashMap();

		private KeyBind() {
			MinecraftForge.EVENT_BUS.register(this);
		}

		@SubscribeEvent
		public void onPlayerTick(WorldTickEvent event) {
			if (event.phase == Phase.END && event.world.getTotalWorldTime() % 3 == 0)
				for (UUID uuid : cooldowns.keySet())
					if (cooldowns.get(uuid) != 0)
						cooldowns.put(uuid, Math.max(cooldowns.get(uuid)-1, 0));
		}

		public int getCooldown(EntityPlayer player) {
			if (player != null && cooldowns.containsKey(player.getPersistentID()))
				return cooldowns.get(player.getPersistentID());
			else
				return 0;
		}

		public void setCooldown(EntityPlayer player, int cooldown) {
			if (player != null)
				cooldowns.put(player.getPersistentID(), cooldown);
		}

		@SideOnly(Side.CLIENT)
		public String getKeyName() {
			switch (this) {
			case ABILITY_1:
				return Keys.ABILITY_1.getDisplayName();
			case ABILITY_2:
				return Keys.ABILITY_2.getDisplayName();
			default:
				return "";
			}
		}

		public boolean isKeyDown(EntityPlayer player) {
			switch (this) {
			case ABILITY_1:
				return Minewatch.keys.ability1(player);
			case ABILITY_2:
				return Minewatch.keys.ability2(player);
			case RMB:
				return Minewatch.keys.rmb(player);
			default:
				return false;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static KeyBinding HERO_INFORMATION;
	@SideOnly(Side.CLIENT)
	public static KeyBinding RELOAD;
	@SideOnly(Side.CLIENT)
	public static KeyBinding ABILITY_1; 
	@SideOnly(Side.CLIENT)
	public static KeyBinding ABILITY_2; 
	@SideOnly(Side.CLIENT)
	public static KeyBinding ULTIMATE;

	/**True if key is pressed down*/
	public HashMap<UUID, Boolean> heroInformation = Maps.newHashMap();
	public HashMap<UUID, Boolean> reload = Maps.newHashMap();
	public HashMap<UUID, Boolean> ability1 = Maps.newHashMap();
	public HashMap<UUID, Boolean> ability2 = Maps.newHashMap();
	public HashMap<UUID, Boolean> ultimate = Maps.newHashMap();
	public HashMap<UUID, Boolean> weapon1 = Maps.newHashMap();
	public HashMap<UUID, Boolean> weapon2 = Maps.newHashMap();
	public HashMap<UUID, Boolean> lmb = Maps.newHashMap();
	public HashMap<UUID, Boolean> rmb = Maps.newHashMap();

	public Keys() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public boolean heroInformation(EntityPlayer player) {
		if (player != null)
			return heroInformation.containsKey(player.getPersistentID()) ? heroInformation.get(player.getPersistentID()) : false;
			return false;
	}

	public boolean reload(EntityPlayer player) {
		if (player != null)
			return reload.containsKey(player.getPersistentID()) ? reload.get(player.getPersistentID()) : false;
			return false;
	}

	public boolean ability1(EntityPlayer player) {
		if (player != null)
			return ability1.containsKey(player.getPersistentID()) ? ability1.get(player.getPersistentID()) : false;
			return false;
	}

	public boolean ability2(EntityPlayer player) {
		if (player != null)
			return ability2.containsKey(player.getPersistentID()) ? ability2.get(player.getPersistentID()) : false;
			return false;
	}

	public boolean ultimate(EntityPlayer player) {
		if (player != null)
			return ultimate.containsKey(player.getPersistentID()) ? ultimate.get(player.getPersistentID()) : false;
			return false;
	}

	public boolean weapon1(EntityPlayer player) {
		if (player != null)
			return weapon1.containsKey(player.getPersistentID()) ? weapon1.get(player.getPersistentID()) : false;
			return false;
	}

	public boolean weapon2(EntityPlayer player) {
		if (player != null)
			return weapon2.containsKey(player.getPersistentID()) ? weapon2.get(player.getPersistentID()) : false;
			return false;
	}

	public boolean lmb(EntityPlayer player) {
		if (player != null)
			return lmb.containsKey(player.getPersistentID()) ? lmb.get(player.getPersistentID()) : false;
			return false;
	}

	public boolean rmb(EntityPlayer player) {
		if (player != null)
			return rmb.containsKey(player.getPersistentID()) ? rmb.get(player.getPersistentID()) : false;
			return false;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void mouseEvents(MouseEvent event) {
		UUID player = Minecraft.getMinecraft().player.getPersistentID();
		ItemStack main = Minecraft.getMinecraft().player.getHeldItemMainhand();

		if ((event.isButtonstate() && main != null && main.getItem() instanceof ItemMWWeapon) || 
				!event.isButtonstate()) {
			if (event.getButton() == 0) {
				lmb.put(player, event.isButtonstate());
				Minewatch.network.sendToServer(new PacketSyncKeys("LMB", event.isButtonstate(), player));
				if (event.isButtonstate())
					event.setCanceled(true);
			}

			if (event.getButton() == 1) {
				rmb.put(player, event.isButtonstate());
				Minewatch.network.sendToServer(new PacketSyncKeys("RMB", event.isButtonstate(), player));
			}
		}

		if (main != null && main.getItem() instanceof ItemMWWeapon &&
				Minecraft.getMinecraft().player.isSneaking() && event.getDwheel() != 0 && 
				((ItemMWWeapon)main.getItem()).hero.hasAltWeapon) {
			EnumHero hero = ((ItemMWWeapon)main.getItem()).hero;
			hero.playersUsingAlt.put(player, 
					hero.playersUsingAlt.containsKey(player) ? !hero.playersUsingAlt.get(player) : true);
			Minewatch.network.sendToServer(new PacketSyncKeys("Alt Weapon", hero.playersUsingAlt.get(player), player));
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void updateKeys(ClientTickEvent event) {
		if (event.phase == Phase.END && Minecraft.getMinecraft().player != null) {
			UUID player = Minecraft.getMinecraft().player.getPersistentID();

			if (!heroInformation.containsKey(player) || HERO_INFORMATION.isKeyDown() != heroInformation.get(player)) {
				heroInformation.put(player, HERO_INFORMATION.isKeyDown());
				Minewatch.network.sendToServer(new PacketSyncKeys("Hero Information", HERO_INFORMATION.isKeyDown(), player));
			}
			if (!reload.containsKey(player) || RELOAD.isKeyDown() != reload.get(player)) {
				reload.put(player, RELOAD.isKeyDown());
				Minewatch.network.sendToServer(new PacketSyncKeys("Reload", RELOAD.isKeyDown(), player));
			}
			if (!ability1.containsKey(player) || ABILITY_1.isKeyDown() != ability1.get(player)) {
				ability1.put(player, ABILITY_1.isKeyDown());
				Minewatch.network.sendToServer(new PacketSyncKeys("Ability 1", ABILITY_1.isKeyDown(), player));
				// toggle ability
				if (ABILITY_1.isKeyDown() && ItemMWArmor.SetManager.playersWearingSets.containsKey(player)) {
					EnumHero hero = ItemMWArmor.SetManager.playersWearingSets.get(player);
					for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
						if (ability.isToggleable && ability.keybind == KeyBind.ABILITY_1 && 
						ability.keybind.getCooldown(Minecraft.getMinecraft().player) == 0) {
							boolean toggle = ability.toggled.containsKey(player) ? !ability.toggled.get(player) : true;
							hero.weapon.toggle(Minecraft.getMinecraft().player, ability, toggle);
							Minewatch.network.sendToServer(new PacketSyncKeys("Toggle Ability 1", toggle, player));
						}
				}
			}
			if (!ability2.containsKey(player) || ABILITY_2.isKeyDown() != ability2.get(player)) {
				ability2.put(player, ABILITY_2.isKeyDown());
				Minewatch.network.sendToServer(new PacketSyncKeys("Ability 2", ABILITY_2.isKeyDown(), player));
				// toggle ability
				if (ABILITY_2.isKeyDown() && ItemMWArmor.SetManager.playersWearingSets.containsKey(player)) {
					EnumHero hero = ItemMWArmor.SetManager.playersWearingSets.get(player);
					for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
						if (ability.isToggleable && ability.keybind == KeyBind.ABILITY_2 && 
						ability.keybind.getCooldown(Minecraft.getMinecraft().player) == 0) {
							boolean toggle = ability.toggled.containsKey(player) ? !ability.toggled.get(player) : true;
							hero.weapon.toggle(Minecraft.getMinecraft().player, ability, toggle);
							Minewatch.network.sendToServer(new PacketSyncKeys("Toggle Ability 2", toggle, player));
						}
				}
			}
			if (!ultimate.containsKey(player) || ULTIMATE.isKeyDown() != ultimate.get(player)) {
				ultimate.put(player, ULTIMATE.isKeyDown());
				Minewatch.network.sendToServer(new PacketSyncKeys("Ultimate", ULTIMATE.isKeyDown(), player));
			}
		}
	}
}
