package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.entity.ModEntities;

public class PacketSyncSpawningEntity implements IMessage{
	
	private UUID uuid;
	public float pitch;
	public float yaw;
	public double motionX; 
	public double motionY; 
	public double motionZ;

	public PacketSyncSpawningEntity() {}

	public PacketSyncSpawningEntity(UUID uuid, float pitch, float yaw, double motionX, double motionY, double motionZ) {
		this.uuid = uuid;
		this.pitch = pitch;
		this.yaw = yaw;
		this.motionX = motionX;
		this.motionY = motionY; 
		this.motionZ = motionZ;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.pitch = buf.readFloat();
		this.yaw = buf.readFloat();
		this.motionX = buf.readDouble();
		this.motionY = buf.readDouble();
		this.motionZ = buf.readDouble();
			
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, uuid.toString());
		buf.writeFloat(pitch);
		buf.writeFloat(yaw);
		buf.writeDouble(motionX);
		buf.writeDouble(motionY);
		buf.writeDouble(motionZ);
	}

	public static class Handler implements IMessageHandler<PacketSyncSpawningEntity, IMessage> {
		@Override
		public IMessage onMessage(final PacketSyncSpawningEntity packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() 
			{
				@Override
				public void run() {
					ModEntities.spawningEntities.put(packet.uuid, packet);
				}
			});
			return null;
		}
	}
}
