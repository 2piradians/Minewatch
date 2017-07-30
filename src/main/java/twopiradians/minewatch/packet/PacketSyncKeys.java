package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.Hero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;

public class PacketSyncKeys implements IMessage
{
	private boolean isKeyPressed;
	private UUID player;
	private String keyName;

	public PacketSyncKeys() {}

	public PacketSyncKeys(String keyName, boolean isKeyPressed, UUID player) {
		this.keyName = keyName;
		this.isKeyPressed = isKeyPressed;
		this.player = player;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.keyName = ByteBufUtils.readUTF8String(buf);
		this.isKeyPressed = buf.readBoolean();
		this.player = UUID.fromString(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, keyName);
		buf.writeBoolean(this.isKeyPressed);
		ByteBufUtils.writeUTF8String(buf, player.toString());
	}

	public static class Handler implements IMessageHandler<PacketSyncKeys, IMessage> {
		@Override
		public IMessage onMessage(final PacketSyncKeys packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world;
			mainThread.addScheduledTask(new Runnable() 
			{
				@Override
				public void run() {
					if (packet.keyName.equals("Hero Information"))
						Minewatch.keys.heroInformation.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Reload"))
						Minewatch.keys.reload.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Ability 1"))
						Minewatch.keys.ability1.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Ability 2"))
						Minewatch.keys.ability2.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Ultimate"))
						Minewatch.keys.ultimate.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Alt Weapon")) {
							Hero hero = ItemMWArmor.SetManager.playersWearingSets.get(packet.player);
							if (hero != null)
								hero.playersUsingAlt.put(packet.player, packet.isKeyPressed);
					}
					else if (packet.keyName.equals("LMB"))
						Minewatch.keys.lmb.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("RMB"))
						Minewatch.keys.rmb.put(packet.player, packet.isKeyPressed);
				}
			});
			return null;
		}
	}
}
