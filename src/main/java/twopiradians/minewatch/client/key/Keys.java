package twopiradians.minewatch.client.key;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
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
import twopiradians.minewatch.packet.SPacketSimple;
import twopiradians.minewatch.packet.SPacketSyncCooldown;

public class Keys {
	// The keys that will display underneath the icon
	public enum KeyBind {
		NONE(Identifier.NONE), ABILITY_1(Identifier.KEYBIND_ABILITY_1), 
		ABILITY_2(Identifier.KEYBIND_ABILITY_2), RMB(Identifier.KEYBIND_RMB),
		LMB, HERO_INFORMATION, RELOAD, ULTIMATE, JUMP, FOV, ALT_WEAPON; // TODO test alt (maybe disable when switching heroes)

		public final Handler COOLDOWNS = new Handler(null, false) {
			@SideOnly(Side.CLIENT)
			@Override
			public Handler onClientRemove() {
				if (player == Minewatch.proxy.getClientPlayer() && player != null) {
					if (silentRecharge.contains(player.getPersistentID()))
						silentRecharge.remove(player.getPersistentID());
					else
						player.playSound(ModSoundEvents.abilityRecharge, 0.5f, 1.0f);
				}
				return super.onClientRemove();
			}
		};
		public final Handler ABILITY_NOT_READY = new Handler(Identifier.KEYBIND_ABILITY_NOT_READY, false) {};

		@Nullable
		public Identifier identifier;
		public HashSet<UUID> silentRecharge = new HashSet();
		public HashSet<UUID> keyDownEntities = new HashSet();
		public HashMap<UUID, Float> fov = Maps.newHashMap();
		@Nullable
		@SideOnly(Side.CLIENT)
		public KeyBinding keyBind;

		private KeyBind() {
			this(null);
		}

		private KeyBind(Identifier identifier) {
			this.identifier = identifier;
			COOLDOWNS.identifier = identifier;
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
			if (player instanceof EntityPlayer) {
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

		public void setKeyDown(EntityLivingBase entity, boolean isKeyDown, boolean isRemote) {
			this.setKeyDown(entity == null ? null : entity.getPersistentID(), isKeyDown, isRemote);
		}

		public void setKeyDown(UUID uuid, boolean isKeyDown, boolean isRemote) {
			if (uuid != null) {
				System.out.println(this+": "+isKeyDown); // FIXME TODO
				// set key down
				if (isKeyDown)
					this.keyDownEntities.add(uuid);
				else
					this.keyDownEntities.remove(uuid);
				// set toggles
				if (this == ABILITY_1 || this == ABILITY_2) {
					if (ItemMWArmor.SetManager.entitiesWearingSets.containsKey(uuid) &&
							TickHandler.getHandler(uuid, Identifier.ABILITY_USING, isRemote) == null) {
						EnumHero hero = ItemMWArmor.SetManager.entitiesWearingSets.get(uuid);
						for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
							if (ability.isToggleable && ability.keybind == this && this.getCooldown(uuid, isRemote) == 0) 
								ability.toggle(uuid, !ability.isToggled(uuid), isRemote);
					}
				}
				// sync alt weapon to all players
				if (this == ALT_WEAPON && !isRemote)
					Minewatch.network.sendToAll(new SPacketSimple(6, uuid, isKeyDown));
			}
		}

		public boolean isKeyDown(EntityLivingBase entity) {
			return entity != null && this.isKeyDown(entity.getPersistentID());
		}

		public boolean isKeyDown(UUID uuid) {
			return uuid != null && this.keyDownEntities.contains(uuid);
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
				return this.keyBind.isPressed();
			else if (this == JUMP)
				return mc.player.movementInput.jump;
			else
				return false;
		}
	}

	public Keys() {
		MinecraftForge.EVENT_BUS.register(this);
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
				KeyBind.LMB.setKeyDown(uuid, event.isButtonstate(), true);
				Minewatch.network.sendToServer(new CPacketSyncKeys(KeyBind.LMB, event.isButtonstate(), uuid));
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
				KeyBind.RMB.setKeyDown(uuid, event.isButtonstate(), true);
				Minewatch.network.sendToServer(new CPacketSyncKeys(KeyBind.RMB, event.isButtonstate(), uuid));
			}
		}

		if (main != null && main.getItem() instanceof ItemMWWeapon &&
				player.isSneaking() && event.getDwheel() != 0 && 
				((ItemMWWeapon)main.getItem()).hero.hasAltWeapon && 
				((ItemMWWeapon)main.getItem()).hero != EnumHero.BASTION) {
			KeyBind.ALT_WEAPON.setKeyDown(uuid, !KeyBind.ALT_WEAPON.isKeyDown(uuid), true);
			Minewatch.network.sendToServer(new CPacketSyncKeys(KeyBind.ALT_WEAPON, KeyBind.ALT_WEAPON.isKeyDown(uuid), uuid));
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

			// disable lmb and rmb if not in game screen
			if (mc.currentScreen != null)
				for (KeyBind key : new KeyBind[] {KeyBind.LMB, KeyBind.RMB})
					if (key.isKeyDown(player)) {
						key.setKeyDown(player, false, true);
						Minewatch.network.sendToServer(new CPacketSyncKeys(key, false, uuid));
					}

			// sync keys
			for (KeyBind key : KeyBind.values()) {
				boolean isKeyDown = key.checkIsKeyDown(mc);
				if (key == KeyBind.FOV && mc.gameSettings.fovSetting != key.getFOV(uuid)) {
					key.setFOV(uuid, mc.gameSettings.fovSetting);
					Minewatch.network.sendToServer(new CPacketSyncKeys(key, mc.gameSettings.fovSetting, uuid));
				}
				else if (isKeyDown != key.isKeyDown(uuid)) {
					key.setKeyDown(uuid, isKeyDown, true);
					Minewatch.network.sendToServer(new CPacketSyncKeys(key, isKeyDown, uuid));
				}
			}
		}
	}
}