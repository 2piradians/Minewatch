package twopiradians.minewatch.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CPacketSimple implements IMessage
{
	private int type;

	public CPacketSimple() {}

	public CPacketSimple(int type) {
		this.type = type;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type);
	}

	public static class Handler implements IMessageHandler<CPacketSimple, IMessage> {
		@Override
		public IMessage onMessage(final CPacketSimple packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayer player = ctx.getServerHandler().playerEntity;

				}
			});
			return null;
		}
	}
}
