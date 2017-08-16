package twopiradians.minewatch.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;

public class PacketSpawnParticle implements IMessage{
	
	private int type;
	private double x;
	private double y;
	private double z;
	private int color;
	private int colorFade;
	private int scale;
	private int maxAge;

	public PacketSpawnParticle() {}

	public PacketSpawnParticle(int type, double x, double y, double z, int color, int colorFade, int scale, int maxAge) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.z = z;
		this.color = color;
		this.colorFade = colorFade;
		this.scale = scale;
		this.maxAge = maxAge;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = buf.readInt();
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
		this.color = buf.readInt();
		this.colorFade = buf.readInt();
		this.scale = buf.readInt();
		this.maxAge = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type);
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
		buf.writeInt(this.color);
		buf.writeInt(this.colorFade);
		buf.writeInt(this.scale);
		buf.writeInt(this.maxAge);
	}

	public static class Handler implements IMessageHandler<PacketSpawnParticle, IMessage> {
		@Override
		public IMessage onMessage(final PacketSpawnParticle packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() 
			{
				@Override
				public void run() {
					if (packet.type == 0) 
						Minewatch.proxy.spawnParticlesSmoke(Minecraft.getMinecraft().world, 
								packet.x, packet.y, packet.z, packet.color, packet.colorFade, packet.scale, packet.maxAge);
					else if (packet.type == 1)
						Minewatch.proxy.spawnParticlesSpark(Minecraft.getMinecraft().world, 
								packet.x, packet.y, packet.z, packet.color, packet.colorFade, packet.scale, packet.maxAge);
				}
			});
			return null;
		}
	}
}
