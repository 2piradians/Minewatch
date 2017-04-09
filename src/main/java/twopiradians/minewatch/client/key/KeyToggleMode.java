package twopiradians.minewatch.client.key;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.packet.PacketToggleMode;

public class KeyToggleMode {
	@SideOnly(Side.CLIENT)
	public static KeyBinding TOGGLE_MODE;
	/**True if key is pressed down*/
	public HashMap<UUID, Boolean> isKeyDown = Maps.newHashMap();

	public KeyToggleMode() {}
	
	public boolean isKeyDown(EntityPlayer player) {
		if (player != null)
			return isKeyDown.containsKey(player.getPersistentID()) ? isKeyDown.get(player.getPersistentID()) : false;
		return false;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void playerTick(ClientTickEvent event) {
		if (event.phase == Phase.END && Minecraft.getMinecraft().player != null) {
			UUID player = Minecraft.getMinecraft().player.getPersistentID();
			if (!isKeyDown.containsKey(player) || TOGGLE_MODE.isKeyDown() != isKeyDown.get(player)) {
				isKeyDown.put(player, TOGGLE_MODE.isKeyDown());
				Minewatch.network.sendToServer(new PacketToggleMode(TOGGLE_MODE.isKeyDown(), player));
			}
		}
	}
}
