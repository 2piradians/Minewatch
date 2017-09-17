package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

/**Sends client skin to server - which in turn sends all skins to all clients*/
public class CPacketSyncSkins implements IMessage {

	private UUID player;
	private int numSkins;
	private int[] skins;

	public CPacketSyncSkins() {}
	
	public CPacketSyncSkins(UUID uuid) {
		this.player = Minewatch.proxy.getClientUUID();
		this.numSkins = EnumHero.values().length;
		this.skins = new int[this.numSkins];
		for (int i=0; i<this.numSkins; ++i)
			this.skins[i] = EnumHero.values()[i].getSkin(this.player);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.player = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.numSkins = buf.readInt();
		this.skins = new int[this.numSkins];
		for (int i=0; i<this.numSkins; ++i)
			this.skins[i] = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.player.toString());
		buf.writeInt(this.numSkins);
		for (int i=0; i<this.numSkins; ++i)
			buf.writeInt(this.skins[i]);
	}

	public static class Handler implements IMessageHandler<CPacketSyncSkins, IMessage> {
		@Override
		public IMessage onMessage(final CPacketSyncSkins packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayer player = ctx.getServerHandler().playerEntity.worldObj.getPlayerEntityByUUID(packet.player);
					if (player != null && packet.numSkins == EnumHero.values().length) {
						for (int i=0; i<packet.numSkins; ++i)
							EnumHero.values()[i].setSkin(player.getPersistentID(), packet.skins[i]);
						Minewatch.network.sendToAll(new SPacketSyncSkins());
					}
				}
			});
			return null;
		}
	}
}
