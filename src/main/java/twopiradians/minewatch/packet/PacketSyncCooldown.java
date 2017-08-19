package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.client.key.Keys.KeyBind;

public class PacketSyncCooldown implements IMessage{
	
	private UUID player;
	private String keybind;
	private int cooldown;

	public PacketSyncCooldown() {}

	public PacketSyncCooldown(UUID player, KeyBind keybind, int cooldown) {
		this.player = player;
		this.keybind = keybind.name();
		this.cooldown = cooldown;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.player = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.keybind = ByteBufUtils.readUTF8String(buf);
		this.cooldown = buf.readInt();			
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.player.toString());
		ByteBufUtils.writeUTF8String(buf, this.keybind);
		buf.writeInt(this.cooldown);
	}

	public static class Handler implements IMessageHandler<PacketSyncCooldown, IMessage> {
		@Override
		public IMessage onMessage(final PacketSyncCooldown packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayer player = Minecraft.getMinecraft().thePlayer;
					KeyBind keybind = KeyBind.valueOf(packet.keybind);
					if (player != null && keybind != null)
						keybind.setCooldown(player, packet.cooldown);
				}
			});
			return null;
		}
	}
}
