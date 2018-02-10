package twopiradians.minewatch.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.hero.EnumHero;

/**Sends all skins to all clients*/
public class SPacketSyncSkins implements IMessage {

	public SPacketSyncSkins() {}

	@Override
	public void fromBytes(ByteBuf buf) {
		try {
			for (int i=0; i<EnumHero.values().length; ++i) {
				EnumHero.values()[i].skins.clear();
				int numSkins = buf.readInt();
				for (int j=0; j<numSkins; ++j) 
					EnumHero.values()[i].skins.put(ByteBufUtils.readUTF8String(buf), buf.readInt());
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		try {
			for (int i=0; i<EnumHero.values().length; ++i) {
				buf.writeInt(EnumHero.values()[i].skins.size());
				for (String uuid : EnumHero.values()[i].skins.keySet()) {
					ByteBufUtils.writeUTF8String(buf, uuid);
					buf.writeInt(EnumHero.values()[i].skins.get(uuid)); // FIXME
				}
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	public static class Handler implements IMessageHandler<SPacketSyncSkins, IMessage> {
		@Override
		public IMessage onMessage(final SPacketSyncSkins packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {}
			});
			return null;
		}
	}
}