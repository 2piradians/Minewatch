package twopiradians.minewatch.client.key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.item.weapon.ItemReinhardtHammer;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.CPacketSyncKeys;
import twopiradians.minewatch.packet.SPacketSyncCooldown;

public class Keys {
	// The keys that will display underneath the icon
	public enum KeyBind {
		NONE, ABILITY_1, ABILITY_2, RMB;

		private HashMap<UUID, Integer> clientCooldowns = Maps.newHashMap();
		private HashMap<UUID, Integer> serverCooldowns = Maps.newHashMap();
		public HashMap<UUID, Integer> abilityNotReadyCooldowns = Maps.newHashMap();
		public ArrayList<UUID> silentRecharge = new ArrayList<UUID>();

		private KeyBind() {
			MinecraftForge.EVENT_BUS.register(this);
		}

		@SubscribeEvent
		public void onPlayerTick(PlayerTickEvent event) {
			if (event.phase == Phase.END)
				if (event.player.world.isRemote) {
					for (UUID uuid : clientCooldowns.keySet())
						if (uuid.equals(event.player.getPersistentID())) {
							if (clientCooldowns.get(uuid) > 1)
								clientCooldowns.put(uuid, Math.max(clientCooldowns.get(uuid)-1, 0));
							else {
								clientCooldowns.remove(uuid);
								if (silentRecharge.contains(event.player.getPersistentID()))
									silentRecharge.remove(event.player.getPersistentID());
								else
									event.player.playSound(ModSoundEvents.abilityRecharge, 0.5f, 1.0f);
							}
							break;
						}
					for (UUID uuid : abilityNotReadyCooldowns.keySet())
						if (uuid.equals(event.player.getPersistentID())) {
							if (abilityNotReadyCooldowns.get(uuid) > 1)
								abilityNotReadyCooldowns.put(uuid, Math.max(abilityNotReadyCooldowns.get(uuid)-1, 0));
							else 
								abilityNotReadyCooldowns.remove(uuid);
							break;
						}
				}
				else 
					for (UUID uuid : serverCooldowns.keySet())
						if (uuid == event.player.getPersistentID()) {
							if (serverCooldowns.get(uuid) > 1)
								serverCooldowns.put(uuid, Math.max(serverCooldowns.get(uuid)-1, 0));
							else
								serverCooldowns.remove(uuid);
							break;
						}
		}

		public int getCooldown(EntityPlayer player) {
			if (player != null && player.world.isRemote && clientCooldowns.containsKey(player.getPersistentID()))
				return clientCooldowns.get(player.getPersistentID());
			else if (player != null && !player.world.isRemote && serverCooldowns.containsKey(player.getPersistentID()))
				return serverCooldowns.get(player.getPersistentID());
			else
				return 0;
		}

		public void setCooldown(EntityPlayer player, int cooldown, boolean silent) {
			if (player != null) {
				if (player.world.isRemote) {
					clientCooldowns.put(player.getPersistentID(), cooldown);
					if (silent)
						silentRecharge.add(player.getPersistentID());
				}
				else if (player instanceof EntityPlayerMP) {
					serverCooldowns.put(player.getPersistentID(), cooldown);
					Minewatch.network.sendTo(new SPacketSyncCooldown(player.getPersistentID(), this, cooldown, silent), (EntityPlayerMP) player);
				}
			}
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
		EntityPlayer player = Minecraft.getMinecraft().player;
		UUID uuid = player.getPersistentID();
		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();

		if ((event.isButtonstate() && ((main != null && main.getItem() instanceof ItemMWWeapon) || 
				(off != null && off.getItem() instanceof ItemMWWeapon))) || 
				!event.isButtonstate()) {
			if (event.getButton() == 0) {
				lmb.put(uuid, event.isButtonstate());
				Minewatch.network.sendToServer(new CPacketSyncKeys("LMB", event.isButtonstate(), uuid));
				if (event.isButtonstate())
					if (!(main.getItem() instanceof ItemReinhardtHammer))
						event.setCanceled(true);
					else {
						if (((ItemMWWeapon) main.getItem()).canUse(player, false)) 
							((ItemMWWeapon) main.getItem()).onItemLeftClick(main, player.world, player, EnumHand.MAIN_HAND);
						event.setCanceled(true);
					}
			}

			if (event.getButton() == 1) {
				rmb.put(uuid, event.isButtonstate());
				Minewatch.network.sendToServer(new CPacketSyncKeys("RMB", event.isButtonstate(), uuid));
			}
		}

		if (main != null && main.getItem() instanceof ItemMWWeapon &&
				player.isSneaking() && event.getDwheel() != 0 && 
				((ItemMWWeapon)main.getItem()).hero.hasAltWeapon) {
			EnumHero hero = ((ItemMWWeapon)main.getItem()).hero;
			hero.playersUsingAlt.put(uuid, 
					hero.playersUsingAlt.containsKey(uuid) ? !hero.playersUsingAlt.get(uuid) : true);
			Minewatch.network.sendToServer(new CPacketSyncKeys("Alt Weapon", hero.playersUsingAlt.get(uuid), uuid));
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void updateKeys(ClientTickEvent event) {
		if (event.phase == Phase.END && Minecraft.getMinecraft().player != null) {
			UUID player = Minecraft.getMinecraft().player.getPersistentID();

			// disable lmb if not in game screen
			if (Minecraft.getMinecraft().currentScreen != null && this.lmb(Minecraft.getMinecraft().player)) {
				lmb.put(player, false);
				Minewatch.network.sendToServer(new CPacketSyncKeys("LMB", false, player));
			}
			// disable rmb if not in game screen
			if (Minecraft.getMinecraft().currentScreen != null && this.rmb(Minecraft.getMinecraft().player)) {
				rmb.put(player, false);
				Minewatch.network.sendToServer(new CPacketSyncKeys("RMB", false, player));
			}

			// sync keys
			if (!heroInformation.containsKey(player) || HERO_INFORMATION.isKeyDown() != heroInformation.get(player)) {
				heroInformation.put(player, HERO_INFORMATION.isKeyDown());
				Minewatch.network.sendToServer(new CPacketSyncKeys("Hero Information", HERO_INFORMATION.isKeyDown(), player));
			}
			if (!reload.containsKey(player) || RELOAD.isKeyDown() != reload.get(player)) {
				reload.put(player, RELOAD.isKeyDown());
				Minewatch.network.sendToServer(new CPacketSyncKeys("Reload", RELOAD.isKeyDown(), player));
			}
			if (!ability1.containsKey(player) || ABILITY_1.isKeyDown() != ability1.get(player)) {
				ability1.put(player, ABILITY_1.isKeyDown());
				Minewatch.network.sendToServer(new CPacketSyncKeys("Ability 1", ABILITY_1.isKeyDown(), player));
				// toggle ability
				if (ABILITY_1.isKeyDown() && ItemMWArmor.SetManager.playersWearingSets.containsKey(player)) {
					EnumHero hero = ItemMWArmor.SetManager.playersWearingSets.get(player);
					for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
						if (ability.isToggleable && ability.keybind == KeyBind.ABILITY_1 && 
						ability.keybind.getCooldown(Minecraft.getMinecraft().player) == 0) {
							boolean toggle = ability.toggled.containsKey(player) ? !ability.toggled.get(player) : true;
							hero.weapon.toggle(Minecraft.getMinecraft().player, ability, toggle);
							Minewatch.network.sendToServer(new CPacketSyncKeys("Toggle Ability 1", toggle, player));
						}
				}
			}
			if (!ability2.containsKey(player) || ABILITY_2.isKeyDown() != ability2.get(player)) {
				ability2.put(player, ABILITY_2.isKeyDown());
				Minewatch.network.sendToServer(new CPacketSyncKeys("Ability 2", ABILITY_2.isKeyDown(), player));
				// toggle ability
				if (ABILITY_2.isKeyDown() && ItemMWArmor.SetManager.playersWearingSets.containsKey(player)) {
					EnumHero hero = ItemMWArmor.SetManager.playersWearingSets.get(player);
					for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
						if (ability.isToggleable && ability.keybind == KeyBind.ABILITY_2 && 
						ability.keybind.getCooldown(Minecraft.getMinecraft().player) == 0) {
							boolean toggle = ability.toggled.containsKey(player) ? !ability.toggled.get(player) : true;
							hero.weapon.toggle(Minecraft.getMinecraft().player, ability, toggle);
							Minewatch.network.sendToServer(new CPacketSyncKeys("Toggle Ability 2", toggle, player));
						}
				}
			}
			if (!ultimate.containsKey(player) || ULTIMATE.isKeyDown() != ultimate.get(player)) {
				ultimate.put(player, ULTIMATE.isKeyDown());
				Minewatch.network.sendToServer(new CPacketSyncKeys("Ultimate", ULTIMATE.isKeyDown(), player));
			}
		}
	}
}
