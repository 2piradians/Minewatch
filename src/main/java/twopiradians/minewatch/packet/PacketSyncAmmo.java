package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class PacketSyncAmmo implements IMessage{
	
	private UUID player;
	private int ammo;

	public PacketSyncAmmo() {}

	public PacketSyncAmmo(UUID player, int ammo) {
		this.player = player;
		this.ammo = ammo;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.player = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.ammo = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, player.toString());
		buf.writeInt(this.ammo);
	}

	public static class Handler implements IMessageHandler<PacketSyncAmmo, IMessage> {
		@Override
		public IMessage onMessage(final PacketSyncAmmo packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() 
			{
				@Override
				public void run() {
					EntityPlayer player = Minecraft.getMinecraft().player;
					ItemStack main = player.getHeldItemMainhand();
					if (main != null && main.getItem() instanceof ItemMWWeapon)
						((ItemMWWeapon)main.getItem()).setCurrentAmmo(player, packet.ammo);
				}
			});
			return null;
		}
	}
}
