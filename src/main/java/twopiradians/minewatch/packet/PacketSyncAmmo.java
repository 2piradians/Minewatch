package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class PacketSyncAmmo implements IMessage{
	
	private UUID player;
	private int ammo;
	private EnumHand[] hands;

	public PacketSyncAmmo() {}

	public PacketSyncAmmo(UUID player, int ammo, EnumHand... hands) {
		this.player = player;
		this.ammo = ammo;
		this.hands = hands;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.player = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.ammo = buf.readInt();
		int numHands = buf.readInt();
		this.hands = new EnumHand[numHands];
		for (int i=0; i<numHands; ++i)
			this.hands[i] = EnumHand.valueOf(ByteBufUtils.readUTF8String(buf));
			
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, player.toString());
		buf.writeInt(this.ammo);
		buf.writeInt(this.hands.length);
		for (EnumHand hand : this.hands)
			ByteBufUtils.writeUTF8String(buf, hand.name());
	}

	public static class Handler implements IMessageHandler<PacketSyncAmmo, IMessage> {
		@Override
		public IMessage onMessage(final PacketSyncAmmo packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() 
			{
				@Override
				public void run() {
					EntityPlayer player = Minecraft.getMinecraft().thePlayer;
					ItemStack main = player.getHeldItemMainhand();
					if (main != null && main.getItem() instanceof ItemMWWeapon)
						((ItemMWWeapon)main.getItem()).setCurrentAmmo(player, packet.ammo, packet.hands);
				}
			});
			return null;
		}
	}
}
