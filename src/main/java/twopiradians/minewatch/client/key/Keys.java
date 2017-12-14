package twopiradians.minewatch.client.key;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;
import twopiradians.minewatch.packet.CPacketSyncKeys;
import twopiradians.minewatch.packet.SPacketSyncCooldown;

public class Keys {
	public enum KeyBind {
		NONE(Identifier.NONE, false), ABILITY_1(Identifier.KEYBIND_ABILITY_1, false), 
		ABILITY_2(Identifier.KEYBIND_ABILITY_2, false), RMB(Identifier.KEYBIND_RMB, false),
		LMB(Identifier.KEYBIND_LMB, false), HERO_INFORMATION(Identifier.KEYBIND_HERO_INFO, true), 
		RELOAD(Identifier.KEYBIND_RELOAD, false), ULTIMATE(Identifier.KEYBIND_ULTIMATE, false), 
		JUMP(Identifier.KEYBIND_JUMP, true), FOV(Identifier.KEYBIND_FOV, false); 

		public final Handler COOLDOWNS = new Handler(null, false) {
			@SideOnly(Side.CLIENT)
			@Override
			public Handler onClientRemove() {
				if (entity == Minewatch.proxy.getClientPlayer() && entity != null) {
					if (silentRecharge.contains(entity.getPersistentID()))
						silentRecharge.remove(entity.getPersistentID());
					else
						ModSoundEvents.ABILITY_RECHARGE.playSound(entity, 0.5f, 1.0f, true);
				}
				return super.onClientRemove();
			}
		};
		public final Handler ABILITY_NOT_READY = new Handler(Identifier.KEYBIND_ABILITY_NOT_READY, false) {};

		@Nullable
		public Identifier identifier;
		private HashSet<UUID> silentRecharge = new HashSet();
		private HashSet<UUID> keyDownEntities = new HashSet();
		public HashSet<UUID> keyPressedEntitiesClient = new HashSet();
		public HashSet<UUID> keyPressedEntitiesServer = new HashSet();
		private HashMap<UUID, Float> fov = Maps.newHashMap();
		@Nullable
		@SideOnly(Side.CLIENT)
		public KeyBinding keyBind;
		public final DataParameter<Boolean> datamanager = EntityDataManager.<Boolean>createKey(EntityHero.class, DataSerializers.BOOLEAN);
		public boolean updateWithoutWeapon;
		
		private KeyBind() {
			this(null, false);
		}

		private KeyBind(Identifier identifier, boolean updateWithoutWeapon) {
			this.identifier = identifier;
			COOLDOWNS.identifier = identifier;
			this.updateWithoutWeapon = updateWithoutWeapon;
		}

		public int getCooldown(EntityLivingBase player) {
			Handler handler = TickHandler.getHandler(player, Identifier.ABILITY_USING);
			if (handler != null && handler.ability != null && handler.ability.keybind == this)
				return 0;
			handler = TickHandler.getHandler(player, identifier);
			return handler == null ? 0 : handler.ticksLeft;
		}

		public int getCooldown(UUID uuid, boolean isRemote) {
			Handler handler = TickHandler.getHandler(uuid, Identifier.ABILITY_USING, isRemote);
			if (handler != null && handler.ability != null && handler.ability.keybind == this)
				return 0;
			handler = TickHandler.getHandler(uuid, identifier, isRemote);
			return handler == null ? 0 : handler.ticksLeft;
		}

		public void setCooldown(EntityLivingBase player, int cooldown, boolean silent) {
			if (player != null) {
				if (player instanceof EntityHero)
					cooldown *= Config.mobAttackCooldown;
				TickHandler.register(player.world.isRemote, COOLDOWNS.setEntity(player).setTicks(cooldown));
				if (player.world.isRemote && silent) {
					silentRecharge.add(player.getPersistentID());
				}
				else if (!player.world.isRemote && player instanceof EntityPlayerMP) 
					Minewatch.network.sendTo(new SPacketSyncCooldown(player.getPersistentID(), this, cooldown, silent), (EntityPlayerMP) player);
			}
		}

		@SideOnly(Side.CLIENT)
		public String getKeyName() {
			return this.keyBind == null ? "" : this.keyBind.getDisplayName();
		}

		public void setKeyDown(EntityLivingBase entity, boolean isKeyDown) {
			this.setKeyDown(entity == null ? null : entity.getPersistentID(), isKeyDown, entity != null ? entity.world.isRemote : true);
		}

		public void setKeyDown(UUID uuid, boolean isKeyDown, boolean isRemote) {
 			if (uuid != null) {
				// set key down
				if (isKeyDown) {
					this.keyDownEntities.add(uuid);
					this.getKeyPressedEntities(isRemote).add(uuid);
				}
				else {
					this.keyDownEntities.remove(uuid);
					this.getKeyPressedEntities(isRemote).remove(uuid);
				}
			}
		}

		private HashSet<UUID> getKeyPressedEntities(boolean isRemote) {
			return isRemote ? this.keyPressedEntitiesClient : this.keyPressedEntitiesServer;
		}

		public boolean isKeyDown(EntityLivingBase entity) {
			return isKeyDown(entity, false);
		}
		
		public boolean isKeyDown(EntityLivingBase entity, boolean ignorePreventInput) {
			return entity != null && this.isKeyDown(entity.getPersistentID()) && (ignorePreventInput || !TickHandler.hasHandler(entity, Identifier.PREVENT_INPUT));
		}

		public boolean isKeyDown(UUID uuid) {
			return uuid != null && this.keyDownEntities.contains(uuid);
		}

		public boolean isKeyPressed(EntityLivingBase entity) {
			return isKeyPressed(entity, false);
		}

		public boolean isKeyPressed(EntityLivingBase entity, boolean ignorePreventInput) {
			return entity != null && this.isKeyPressed(entity.getPersistentID(), entity.world.isRemote) && (ignorePreventInput || !TickHandler.hasHandler(entity, Identifier.PREVENT_INPUT));
		}

		public boolean isKeyPressed(UUID uuid, boolean isRemote) {
			return uuid != null && this.getKeyPressedEntities(isRemote).contains(uuid);
		}

		public void setFOV(EntityLivingBase entity, float fov) {
			this.setFOV(entity == null ? null : entity.getPersistentID(), fov);
		}

		public void setFOV(UUID uuid, float fov) {
			this.fov.put(uuid, fov);
		}

		public float getFOV(EntityLivingBase entity) {
			return this.getFOV(entity == null ? null : entity.getPersistentID());
		}

		private float getFOV(UUID uuid) {
			return uuid != null && this.fov.containsKey(uuid) ? this.fov.get(uuid) : 70f;
		}

		/**Get the current value of the key - for updating isKeyDown*/
		@SideOnly(Side.CLIENT)
		public boolean checkIsKeyDown(Minecraft mc) {
			if (this.keyBind != null)
				return this.keyBind.isKeyDown();
			else if (this == JUMP)
				return mc.player.movementInput.jump;
			else if (this == RMB)
				return mc.gameSettings.keyBindUseItem.isKeyDown();
			// check keyboard directly because we set the mc keybind to false
			else if (this == LMB && mc.gameSettings.keyBindAttack.getKeyCode() < Keyboard.KEYBOARD_SIZE && mc.gameSettings.keyBindAttack.getKeyCode() >= 0) 
				return Keyboard.isKeyDown(mc.gameSettings.keyBindAttack.getKeyCode());
			else
				return false;
		}

		/**Toggle ability - toggles and sends packet to toggle on client, toggles on server*/
		public void toggle(UUID uuid, boolean toggle, boolean isRemote) {
			if (uuid != null && ItemMWArmor.SetManager.getWornSet(uuid) != null) {
				EnumHero hero = ItemMWArmor.SetManager.getWornSet(uuid);
				for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
					if (ability.isToggleable && ability.keybind == this && 
					ability.keybind.getCooldown(uuid, isRemote) == 0) {
						if (isRemote) {
							ability.toggle(uuid, !ability.isToggled(uuid), isRemote);
							Minewatch.network.sendToServer(new CPacketSyncKeys(this, ability.isToggled(uuid), uuid, true));
						}
						else
							ability.toggle(uuid, toggle, isRemote);
					}
			}
		}
	}

	@Mod.EventBusSubscriber(Side.CLIENT)
	public static class KeyHandler {

		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public static void mouseEvents(MouseEvent event) {
			if (Minecraft.getMinecraft().player != null) {
				Minecraft mc = Minecraft.getMinecraft();
				EntityPlayerSP player = mc.player;
				ItemStack main = player.getHeldItemMainhand();
				ItemStack off = player.getHeldItemOffhand();

				// update lmb if necessary (lmbDown == null if not bound to the mouse or mouseEvent not caused by it)
				Boolean lmbDown = isLMBOnMouse(mc) && 
						((event.getButton() == 0 && mc.gameSettings.keyBindAttack.getKeyCode() == -100) ||
								(event.getButton() == 1 && mc.gameSettings.keyBindAttack.getKeyCode() == -99) ||
								(event.getButton() == 2 && mc.gameSettings.keyBindAttack.getKeyCode() == -98)) && 
						((main != null && main.getItem() instanceof ItemMWWeapon) || 
								(off != null && off.getItem() instanceof ItemMWWeapon)) ? 
										(event.isButtonstate() && mc.currentScreen == null) :
											null;
										updateKeys(lmbDown);

										// prevent further lmb processing
										if (lmbDown != null && lmbDown) // TODO remove this stupid logic and just do it in the item
											event.setCanceled(true);

										// switch to alt weapon
										if (main != null && main.getItem() instanceof ItemMWWeapon &&
												player.isSneaking() && event.getDwheel() != 0 && 
												((ItemMWWeapon)main.getItem()).hero.hasAltWeapon && 
												((ItemMWWeapon)main.getItem()).hero.switchAltWithScroll) {
											Minewatch.network.sendToServer(new CPacketSimple(3, false, player));
											event.setCanceled(true);
										}
			}
		}

		@SubscribeEvent(priority=EventPriority.LOWEST)
		@SideOnly(Side.CLIENT)
		public static void updateKeys(ClientTickEvent event) {
			if (event.phase == Phase.END) {
				Minecraft mc = Minecraft.getMinecraft();
				updateKeys(isLMBOnMouse(mc) ? null : KeyBind.LMB.checkIsKeyDown(mc));
			}
		}

		@SideOnly(Side.CLIENT)
		private static void updateKeys(@Nullable Boolean lmbDown) {
			if (Minecraft.getMinecraft().player != null) {
				Minecraft mc = Minecraft.getMinecraft();
				EntityPlayerSP player = mc.player;
				UUID uuid = player.getPersistentID();
				ItemStack main = player.getHeldItemMainhand();
				ItemStack off = player.getHeldItemOffhand();

				// sync keys
				for (KeyBind key : new KeyBind[] {KeyBind.HERO_INFORMATION, KeyBind.RELOAD, KeyBind.ABILITY_1, KeyBind.ABILITY_2, KeyBind.ULTIMATE, KeyBind.JUMP, KeyBind.FOV, KeyBind.RMB, KeyBind.LMB}) {
					// only update lmb if needed
					if (key == KeyBind.LMB && lmbDown == null && mc.currentScreen == null)
						break;
					// check key - false if currentScreen != null
					boolean isKeyDown = key == KeyBind.LMB && lmbDown != null ? lmbDown : mc.currentScreen == null && key.checkIsKeyDown(mc);
					if (key == KeyBind.FOV && mc.gameSettings.fovSetting != key.getFOV(uuid)) {
						key.setFOV(uuid, mc.gameSettings.fovSetting);
						Minewatch.network.sendToServer(new CPacketSyncKeys(key, mc.gameSettings.fovSetting, uuid));
					}
					else if (isKeyDown != key.isKeyDown(uuid) && ((isKeyDown && 
							(key.updateWithoutWeapon || (main != null && main.getItem() instanceof ItemMWWeapon) || 
									(off != null && off.getItem() instanceof ItemMWWeapon)) &&
							mc.currentScreen == null) || !isKeyDown)) {
						key.setKeyDown(uuid, isKeyDown, true);
						Minewatch.network.sendToServer(new CPacketSyncKeys(key, isKeyDown, uuid));
						if (isKeyDown && (key == KeyBind.ABILITY_1 || key == KeyBind.ABILITY_2))
							key.toggle(uuid, true, true);
					}
				}

				// disable vanilla lmb if holding weapon and lmb down
				if (!isLMBOnMouse(mc) && KeyBind.LMB.isKeyDown(player))
					KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
			}		
		}

		/**Is the left mouse button bound to a button on the mouse or a key*/
		@SideOnly(Side.CLIENT)
		private static boolean isLMBOnMouse(Minecraft mc) {
			return mc.gameSettings.keyBindAttack.getKeyCode() == -100 ||
					mc.gameSettings.keyBindAttack.getKeyCode() == -99 ||
					mc.gameSettings.keyBindAttack.getKeyCode() == -98;
		}

	}

}