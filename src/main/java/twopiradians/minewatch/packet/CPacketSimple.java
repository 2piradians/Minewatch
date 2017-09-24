package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class CPacketSimple implements IMessage {
	
	private int type;
	private UUID uuid;

	public CPacketSimple() {}

	public CPacketSimple(int type) {
		this(type, null);
	}
	
	public CPacketSimple(int type, EntityPlayer player) {
		this.type = type;
		this.uuid = player == null ? UUID.randomUUID() : player.getPersistentID();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = buf.readInt();
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type);
		ByteBufUtils.writeUTF8String(buf, this.uuid.toString());
	}

	public static class Handler implements IMessageHandler<CPacketSimple, IMessage> {
		@Override
		public IMessage onMessage(final CPacketSimple packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayerMP player = ctx.getServerHandler().playerEntity;
					EntityPlayer packetPlayer = packet.uuid == null ? null : player.world.getPlayerEntityByUUID(packet.uuid);
					
					// reset fall distance
					if (packet.type == 0 && packetPlayer != null) {
						packetPlayer.fallDistance = 0;
						packetPlayer.world.playSound(null, packetPlayer.getPosition(), ModSoundEvents.wallClimb, 
								SoundCategory.PLAYERS, 0.9f, 1.0f);
					}
				}
			});
			return null;
		}
	}
}
