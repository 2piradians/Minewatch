package twopiradians.minewatch.client.key;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.Hero;
import twopiradians.minewatch.common.item.armor.ModArmor;
import twopiradians.minewatch.packet.PacketSyncKeys;

public class Keys {

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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void updateAltWeapon(MouseEvent event) {
		UUID player = Minecraft.getMinecraft().player.getPersistentID();
		Hero hero = ModArmor.SetManager.playersWearingSets.get(player);

		if (hero != null && Minecraft.getMinecraft().player.isSneaking() && event.getDwheel() != 0) {
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
			}
			if (!ability2.containsKey(player) || ABILITY_2.isKeyDown() != ability2.get(player)) {
				ability2.put(player, ABILITY_2.isKeyDown());
				Minewatch.network.sendToServer(new PacketSyncKeys("Ability 2", ABILITY_2.isKeyDown(), player));
			}
			if (!ultimate.containsKey(player) || ULTIMATE.isKeyDown() != ultimate.get(player)) {
				ultimate.put(player, ULTIMATE.isKeyDown());
				Minewatch.network.sendToServer(new PacketSyncKeys("Ultimate", ULTIMATE.isKeyDown(), player));
			}
		}
	}
}
