package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class CPacketSimple implements IMessage {

	private int type;
	private UUID uuid;
	private double x;
	private double y;
	private double z;

	public CPacketSimple() {}

	public CPacketSimple(int type) {
		this(type, null, 1, 0, 0);
	}

	public CPacketSimple(int type, EntityPlayer player) {
		this(type, player, 2, 0, 0);
	}

	public CPacketSimple(int type, EntityPlayer player, double x, double y, double z) {
		this.type = type;
		this.uuid = player == null ? UUID.randomUUID() : player.getPersistentID();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = buf.readInt();
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type);
		ByteBufUtils.writeUTF8String(buf, this.uuid.toString());
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
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
					// check if opped
					else if (packet.type == 1 && packetPlayer instanceof EntityPlayerMP && packetPlayer.getServer() != null) {
						if (packetPlayer.getServer().getPlayerList().canSendCommands(packetPlayer.getGameProfile()))
							Minewatch.network.sendTo(new SPacketSimple(18), (EntityPlayerMP) packetPlayer);
					}
					// Wild Card Token selection
					else if (packet.type == 2 && packetPlayer != null && packet.x >= 0 && packet.x < EnumHero.values().length) {
						// take wild card and give token
						for (ItemStack stack : packetPlayer.getHeldEquipment())
							if (stack != null && stack.getItem() instanceof ItemMWToken.ItemWildCardToken && 
							stack.getCount() > 0) {
								stack.shrink(1);
								packetPlayer.inventory.addItemStackToInventory(
										new ItemStack(EnumHero.values()[(int) packet.x].token));
								break;
							}
						// close screen if no wild cards left
						boolean hasWildCard = false;
						for (ItemStack stack : packetPlayer.getHeldEquipment())
							if (stack != null && stack.getItem() instanceof ItemMWToken.ItemWildCardToken && 
							stack.getCount() > 0)
								hasWildCard = true;
						if (!hasWildCard)
							packetPlayer.closeScreen();
					}
				}
			});
			return null;
		}
	}
}
