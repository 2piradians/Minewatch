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

public class SPacketSyncCooldown implements IMessage{
	
	private UUID player;
	private String keybind;
	private int cooldown;
	private boolean silent;

	public SPacketSyncCooldown() {}

	public SPacketSyncCooldown(UUID player, KeyBind keybind, int cooldown, boolean silent) {
		this.player = player;
		this.keybind = keybind.name();
		this.cooldown = cooldown;
		this.silent = silent;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.player = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.keybind = ByteBufUtils.readUTF8String(buf);
		this.cooldown = buf.readInt();	
		this.silent = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.player.toString());
		ByteBufUtils.writeUTF8String(buf, this.keybind);
		buf.writeInt(this.cooldown);
		buf.writeBoolean(this.silent);
	}

	public static class Handler implements IMessageHandler<SPacketSyncCooldown, IMessage> {
		@Override
		public IMessage onMessage(final SPacketSyncCooldown packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayer player = Minecraft.getMinecraft().thePlayer;
					KeyBind keybind = KeyBind.valueOf(packet.keybind);
					if (player != null && keybind != null)
						keybind.setCooldown(player, packet.cooldown, packet.silent);
				}
			});
			return null;
		}
	}
}
