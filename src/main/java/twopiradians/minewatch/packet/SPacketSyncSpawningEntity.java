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

public class SPacketSyncSpawningEntity implements IMessage{
	
	private UUID uuid;
	public float pitch;
	public float yaw;
	public double motionX; 
	public double motionY; 
	public double motionZ;
	public double posX; 
	public double posY;
	public double posZ;

	public SPacketSyncSpawningEntity() {}

	public SPacketSyncSpawningEntity(UUID uuid, float pitch, float yaw, double motionX, double motionY, double motionZ, double posX, double posY, double posZ) {
		this.uuid = uuid;
		this.pitch = pitch;
		this.yaw = yaw;
		this.motionX = motionX;
		this.motionY = motionY; 
		this.motionZ = motionZ;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.pitch = buf.readFloat();
		this.yaw = buf.readFloat();
		this.motionX = buf.readDouble();
		this.motionY = buf.readDouble();
		this.motionZ = buf.readDouble();
		this.posX = buf.readDouble();
		this.posY = buf.readDouble();
		this.posZ = buf.readDouble();
			
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, uuid.toString());
		buf.writeFloat(pitch);
		buf.writeFloat(yaw);
		buf.writeDouble(motionX);
		buf.writeDouble(motionY);
		buf.writeDouble(motionZ);
		buf.writeDouble(posX);
		buf.writeDouble(posY);
		buf.writeDouble(posZ);
	}

	public static class Handler implements IMessageHandler<SPacketSyncSpawningEntity, IMessage> {
		@Override
		public IMessage onMessage(final SPacketSyncSpawningEntity packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() 
			{
				@Override
				public void run() {
					ModEntities.spawningEntityPacket = packet;
					ModEntities.spawningEntityUUID = packet.uuid;
				}
			});
			return null;
		}
	}
}
