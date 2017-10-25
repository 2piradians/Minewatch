package twopiradians.minewatch.client.key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.item.weapon.ItemReinhardtHammer;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSyncKeys;
import twopiradians.minewatch.packet.SPacketSyncCooldown;

public class Keys {
	// The keys that will display underneath the icon
	public enum KeyBind {
		NONE(Identifier.NONE), ABILITY_1(Identifier.KEYBIND_ABILITY_1), 
		ABILITY_2(Identifier.KEYBIND_ABILITY_2), RMB(Identifier.KEYBIND_RMB);

		public Identifier identifier;
		public ArrayList<UUID> silentRecharge = new ArrayList<UUID>();
		public final Handler COOLDOWNS = new Handler(identifier, false) {
			@Override
			public Handler onRemove() {
				if (player.world.isRemote && player == Minewatch.proxy.getClientPlayer()) {
					if (silentRecharge.contains(player.getPersistentID()))
						silentRecharge.remove(player.getPersistentID());
					else
						player.playSound(ModSoundEvents.abilityRecharge, 0.5f, 1.0f);
				}
				return super.onRemove();
			}
		};
		public final Handler ABILITY_NOT_READY = new Handler(Identifier.KEYBIND_ABILITY_NOT_READY, false) {};

		private KeyBind(Identifier identifier) {
			this.identifier = identifier;
			COOLDOWNS.identifier = identifier;
		}

		public int getCooldown(EntityPlayer player) {
			Handler handler = TickHandler.getHandler(player, Identifier.ABILITY_USING);
			if (handler != null && handler.ability != null && handler.ability.keybind == this)
				return 0;
			handler = TickHandler.getHandler(player, identifier);
			return handler == null ? 0 : handler.ticksLeft;
		}

		public void setCooldown(EntityPlayer player, int cooldown, boolean silent) {
			if (player != null) {
				TickHandler.register(player.world.isRemote, COOLDOWNS.setEntity(player).setTicks(cooldown));
				if (player.world.isRemote && silent) 
					silentRecharge.add(player.getPersistentID());
				else if (!player.world.isRemote && player instanceof EntityPlayerMP) 
					Minewatch.network.sendTo(new SPacketSyncCooldown(player.getPersistentID(), this, cooldown, silent), (EntityPlayerMP) player);
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
	public HashMap<UUID, Boolean> jump = Maps.newHashMap();
	public HashMap<UUID, Float> fov = Maps.newHashMap();

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

	public boolean jump(EntityPlayer player) {
		if (player != null)
			return jump.containsKey(player.getPersistentID()) ? jump.get(player.getPersistentID()) : false;
			return false;
	}
	
	public float fov(EntityPlayer player) {
		if (player != null)
			return fov.containsKey(player.getPersistentID()) ? fov.get(player.getPersistentID()) : 70f;
			return 70f;
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
						if (((ItemMWWeapon) main.getItem()).canUse(player, false, EnumHand.MAIN_HAND, false)) 
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
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayerSP player = mc.player;
			UUID uuid = player.getPersistentID();

			// disable lmb if not in game screen
			if (mc.currentScreen != null && this.lmb(player)) {
				lmb.put(uuid, false);
				Minewatch.network.sendToServer(new CPacketSyncKeys("LMB", false, uuid));
			}
			// disable rmb if not in game screen
			if (mc.currentScreen != null && this.rmb(player)) {
				rmb.put(uuid, false);
				Minewatch.network.sendToServer(new CPacketSyncKeys("RMB", false, uuid));
			}

			// sync keys
			if (!heroInformation.containsKey(uuid) || HERO_INFORMATION.isKeyDown() != heroInformation.get(uuid)) {
				heroInformation.put(uuid, HERO_INFORMATION.isKeyDown());
				Minewatch.network.sendToServer(new CPacketSyncKeys("Hero Information", HERO_INFORMATION.isKeyDown(), uuid));
			}
			if (!reload.containsKey(uuid) || RELOAD.isKeyDown() != reload.get(uuid)) {
				reload.put(uuid, RELOAD.isKeyDown());
				Minewatch.network.sendToServer(new CPacketSyncKeys("Reload", RELOAD.isKeyDown(), uuid));
			}
			if (!ability1.containsKey(uuid) || ABILITY_1.isKeyDown() != ability1.get(uuid)) {
				ability1.put(uuid, ABILITY_1.isKeyDown());
				Minewatch.network.sendToServer(new CPacketSyncKeys("Ability 1", ABILITY_1.isKeyDown(), uuid));
				// toggle ability
				if (ABILITY_1.isKeyDown() && ItemMWArmor.SetManager.entitiesWearingSets.containsKey(uuid) &&
						TickHandler.getHandler(player, Identifier.ABILITY_USING) == null) {
					EnumHero hero = ItemMWArmor.SetManager.entitiesWearingSets.get(uuid);
					for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
						if (ability.isToggleable && ability.keybind == KeyBind.ABILITY_1 && 
						ability.keybind.getCooldown(player) == 0) {
							ability.toggle(player, !ability.isToggled(player));
							Minewatch.network.sendToServer(new CPacketSyncKeys("Toggle Ability 1", ability.isToggled(player), uuid));
						}
				}
			}
			if (!ability2.containsKey(uuid) || ABILITY_2.isKeyDown() != ability2.get(uuid)) {
				ability2.put(uuid, ABILITY_2.isKeyDown());
				Minewatch.network.sendToServer(new CPacketSyncKeys("Ability 2", ABILITY_2.isKeyDown(), uuid));
				// toggle ability
				if (ABILITY_2.isKeyDown() && ItemMWArmor.SetManager.entitiesWearingSets.containsKey(uuid) &&
						TickHandler.getHandler(player, Identifier.ABILITY_USING) == null) {
					EnumHero hero = ItemMWArmor.SetManager.entitiesWearingSets.get(uuid);
					for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
						if (ability.isToggleable && ability.keybind == KeyBind.ABILITY_2 && 
						ability.keybind.getCooldown(player) == 0) {
							ability.toggle(player, !ability.isToggled(player));
							Minewatch.network.sendToServer(new CPacketSyncKeys("Toggle Ability 2", ability.isToggled(player), uuid));
						}
				}
			}
			if (!ultimate.containsKey(uuid) || ULTIMATE.isKeyDown() != ultimate.get(uuid)) {
				ultimate.put(uuid, ULTIMATE.isKeyDown());
				Minewatch.network.sendToServer(new CPacketSyncKeys("Ultimate", ULTIMATE.isKeyDown(), uuid));
			}
			if (!jump.containsKey(uuid) || player.movementInput.jump != jump.get(uuid)) {
				jump.put(uuid, player.movementInput.jump);
				Minewatch.network.sendToServer(new CPacketSyncKeys("Jump", player.movementInput.jump, uuid));
			}
			if (!fov.containsKey(uuid) || mc.gameSettings.fovSetting != fov.get(uuid)) {
				fov.put(uuid, mc.gameSettings.fovSetting);
				Minewatch.network.sendToServer(new CPacketSyncKeys("Fov", mc.gameSettings.fovSetting, uuid));
			}
		}
	}
}
