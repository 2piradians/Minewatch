package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;

public class SPacketTriggerAbility implements IMessage {

	private int type;
	private UUID uuid;
	private double x;
	private double y;
	private double z;

	public SPacketTriggerAbility() { }

	public SPacketTriggerAbility(int type) {
		this(type, null, 0, 0, 0);
	}

	public SPacketTriggerAbility(int type, EntityPlayer player, double x, double y, double z) {
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

	public static class Handler implements IMessageHandler<SPacketTriggerAbility, IMessage> {
		@Override
		public IMessage onMessage(final SPacketTriggerAbility packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayer player = Minecraft.getMinecraft().player;

					// Tracer's dash
					if (packet.type == 0) {
						Vec3d vec = new Vec3d(player.motionX, 0, player.motionZ);
						if (vec.x == 0 && vec.z == 0) 
							vec = new Vec3d(player.getLookVec().x, 0, player.getLookVec().z);
						vec = vec.normalize().scale(9);
						player.move(MoverType.SELF, vec.x, vec.y, vec.z);
					}
					// Reaper's teleport
					else if (packet.type == 1) {
						EntityPlayer player2 = player.world.getPlayerEntityByUUID(packet.uuid);
						if (player2 != null) {
							ItemReaperShotgun.clientTps.put(player2, new Tuple(70, new Vec3d(packet.x, packet.y, packet.z)));
							Minewatch.proxy.spawnParticlesReaperTeleport(player2.world, player2, true, 0);
						}
					}
				}
			});
			return null;
		}
	}
}