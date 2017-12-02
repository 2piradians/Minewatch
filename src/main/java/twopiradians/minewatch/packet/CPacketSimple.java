package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.item.weapon.ItemLucioSoundAmplifier;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class CPacketSimple implements IMessage {

	private int type;
	private boolean bool;
	private UUID uuid;
	private double x;
	private double y;
	private double z;
	private int id;
	private int id2;

	public CPacketSimple() { }

	public CPacketSimple(int type) {
		this(type, false, null, 0, 0, 0, null, null);
	}

	public CPacketSimple(int type, boolean bool) {
		this(type, bool, null, 0, 0, 0, null, null);
	}

	public CPacketSimple(int type, Entity entity, boolean bool) {
		this(type, bool, null, 0, 0, 0, entity, null);
	}

	public CPacketSimple(int type, Entity entity, boolean bool, Entity entity2) {
		this(type, bool, null, 0, 0, 0, entity, entity2);
	}

	public CPacketSimple(int type, Entity entity, boolean bool, double x, double y, double z) {
		this(type, bool, null, x, y, z, entity, null);
	}

	public CPacketSimple(int type, boolean bool, EntityPlayer player) {
		this(type, bool, player, 0, 0, 0, null, null);
	}

	public CPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z) {
		this(type, bool, player, x, y, z, null, null);
	}

	public CPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z, Entity entity) {
		this(type, bool, player, x, y, z, entity, null);
	}

	public CPacketSimple(int type, EntityPlayer player, double x, double y, double z) {
		this(type, false, player, x, y, z, null, null);
	}

	public CPacketSimple(int type, EntityPlayer player, double x, double y, double z, Entity entity) {
		this(type, false, player, x, y, z, entity, null);
	}

	public CPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z, Entity entity, Entity entity2) {
		this.type = type;
		this.bool = bool;
		this.uuid = player == null ? UUID.randomUUID() : player.getPersistentID();
		this.id = entity == null ? -1 : entity.getEntityId();
		this.id2 = entity2 == null ? -1 : entity2.getEntityId();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = buf.readInt();
		this.bool = buf.readBoolean();
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.id = buf.readInt();
		this.id2 = buf.readInt();
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type);
		buf.writeBoolean(this.bool);
		ByteBufUtils.writeUTF8String(buf, this.uuid.toString());
		buf.writeInt(this.id);
		buf.writeInt(this.id2);
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
	}

	public static class Handler implements IMessageHandler<CPacketSimple, IMessage> {
		@Override
		public IMessage onMessage(final CPacketSimple packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayerMP player = ctx.getServerHandler().playerEntity;
					EntityPlayer packetPlayer = packet.uuid == null ? null : player.worldObj.getPlayerEntityByUUID(packet.uuid);
					Entity entity = packet.id == -1 ? null : player.worldObj.getEntityByID(packet.id);
					//Entity entity2 = packet.id2 == -1 ? null : player.world.getEntityByID(packet.id2);

					// reset fall distance
					if (packet.type == 0 && entity != null) {
						entity.fallDistance = 0;
						ModSoundEvents.WALL_CLIMB.playSound(entity, 0.9f, 1);
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
							stack.stackSize > 0) {
								--stack.stackSize;
								packetPlayer.inventory.addItemStackToInventory(
										new ItemStack(EnumHero.values()[(int) packet.x].token));
								break;
							}
						// close screen if no wild cards left
						boolean hasWildCard = false;
						for (ItemStack stack : packetPlayer.getHeldEquipment())
							if (stack != null && stack.getItem() instanceof ItemMWToken.ItemWildCardToken && 
							stack.stackSize > 0)
								hasWildCard = true;
						if (!hasWildCard)
							packetPlayer.closeScreen();
					}
					// Switch alt weapon
					else if (packet.type == 3 && packetPlayer != null) {
						ItemStack stack = packetPlayer.getHeldItemMainhand();
						ItemMWWeapon.setAlternate(stack, !ItemMWWeapon.isAlternate(stack));
						Minewatch.network.sendToAll(new SPacketSimple(6, ItemMWWeapon.isAlternate(stack), packetPlayer));
					}
					// Lucio's soundwave
					else if (packet.type == 4 && packetPlayer != null) {
						ItemLucioSoundAmplifier.soundwave(packetPlayer, packet.x, packet.y, packet.z);
					}
				}
			});
			return null;
		}
	}
}
