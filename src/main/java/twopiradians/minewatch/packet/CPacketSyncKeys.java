package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.client.key.Keys.KeyBind;

public class CPacketSyncKeys implements IMessage
{
	private boolean isKeyPressed;
	private float fov;
	private UUID player;
	private int key;
	private boolean isToggle;
	private float pitch;
	private float yawHead;

	public CPacketSyncKeys() {}

	public CPacketSyncKeys(KeyBind key, boolean isKeyPressed, UUID player, float pitch, float yawHead) {
		this(key, isKeyPressed, -1, player, false, pitch, yawHead);
	}

	public CPacketSyncKeys(KeyBind key, boolean isKeyPressed, UUID player, boolean isToggle) {
		this(key, isKeyPressed, -1, player, isToggle, -1, -1);
	}

	public CPacketSyncKeys(KeyBind key, float fov, UUID player) {
		this(key, false, fov, player, false, -1, -1);
	}

	public CPacketSyncKeys(KeyBind key, boolean isKeyPressed, float fov, UUID player, boolean isToggle, float pitch, float yawHead) {
		this.key = key == null ? -1 : key.ordinal();
		this.isKeyPressed = isKeyPressed;
		this.fov = fov;
		this.player = player;
		this.isToggle = isToggle;
		this.pitch = pitch;
		this.yawHead = yawHead;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.key = buf.readInt();
		this.isKeyPressed = buf.readBoolean();
		this.fov = buf.readFloat();
		this.player = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.isToggle = buf.readBoolean();
		this.pitch = buf.readFloat();
		this.yawHead = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.key);
		buf.writeBoolean(this.isKeyPressed);
		buf.writeFloat(this.fov);
		ByteBufUtils.writeUTF8String(buf, this.player.toString());
		buf.writeBoolean(this.isToggle);
		buf.writeFloat(this.pitch);
		buf.writeFloat(this.yawHead);
	}

	public static class Handler implements IMessageHandler<CPacketSyncKeys, IMessage> {
		@Override
		public IMessage onMessage(final CPacketSyncKeys packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					KeyBind key = packet.key > 0 && packet.key < KeyBind.values().length ? KeyBind.values()[packet.key] : null;

					if (key != null && packet.player != null) {
						if (packet.isToggle)
							key.toggle(packet.player, packet.isKeyPressed, false);
						else if (key == KeyBind.FOV && packet.fov != -1)
							key.setFOV(packet.player, packet.fov);
						else {
							// copy pitch / yaw from when button clicked
							if (packet.pitch != -1 || packet.yawHead != -1) {
								ctx.getServerHandler().player.rotationPitch = packet.pitch;
								ctx.getServerHandler().player.rotationYawHead = packet.yawHead;
							}
							key.setKeyDown(packet.player, packet.isKeyPressed, false);
						}
					}
				}
			});
			return null;
		}
	}
}
