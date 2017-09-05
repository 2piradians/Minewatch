package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemMcCreeGun;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;

public class SPacketTriggerAbility implements IMessage {

	private int type;
	private boolean bool;
	private UUID uuid;
	private double x;
	private double y;
	private double z;

	public SPacketTriggerAbility() { }

	public SPacketTriggerAbility(int type) {
		this(type, null, 0, 0, 0);
	}

	public SPacketTriggerAbility(int type, boolean bool) {
		this(type, bool, null, 0, 0, 0);
	}

	public SPacketTriggerAbility(int type, EntityPlayer player, double x, double y, double z) {
		this(type, false, player, x, y, z);
	}

	public SPacketTriggerAbility(int type, boolean bool, EntityPlayer player, double x, double y, double z) {
		this.type = type;
		this.bool = bool;
		this.uuid = player == null ? UUID.randomUUID() : player.getPersistentID();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = buf.readInt();
		this.bool = buf.readBoolean();
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type);
		buf.writeBoolean(this.bool);
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
					EntityPlayerSP player = Minecraft.getMinecraft().player;

					// Tracer's dash
					if (packet.type == 0) 
						move(player, 9, false);
					// Reaper's teleport
					else if (packet.type == 1) {
						EntityPlayer player2 = player.world.getPlayerEntityByUUID(packet.uuid);
						if (player2 != null) {
							ItemReaperShotgun.clientTps.put(player2, new Tuple(70, new Vec3d(packet.x, packet.y, packet.z)));
							Minewatch.proxy.spawnParticlesReaperTeleport(player2.world, player2, true, 0);
						}
					}
					// McCree's roll
					else if (packet.type == 2) {
						player.onGround = true;
						player.movementInput.sneak = true;
						if (packet.bool)
							ItemMcCreeGun.clientRolling.put(player, 10);
						Minewatch.proxy.spawnParticlesSmoke(player.world, 
								player.posX+player.world.rand.nextDouble()-0.5d, 
								player.posY+player.world.rand.nextDouble(), 
								player.posZ+player.world.rand.nextDouble()-0.5d, 
								0xB4907B, 0xE6C4AC, 15+player.world.rand.nextInt(5), 10);
						move(player, 1.0d, false);
					}
					// Genji's strike
					else if (packet.type == 3) {
						if (packet.bool)
							ItemGenjiShuriken.clientStriking.put(player, 8);
						player.setActiveHand(EnumHand.MAIN_HAND);
						move(player, 1.8d, true);
						int numParticles = (int) ((Math.abs(player.chasingPosX-player.posX)+Math.abs(player.chasingPosY-player.posY)+Math.abs(player.chasingPosZ-player.posZ))*40d);
						for (int i=0; i<numParticles; ++i) {
							Minewatch.proxy.spawnParticlesTrail(player.world, 
									player.posX+(player.chasingPosX-player.posX)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d-0.2d, 
									player.posY+(player.chasingPosY-player.posY)*i/numParticles+player.height/2+(player.world.rand.nextDouble()-0.5d)*0.1d-0.2d, 
									player.posZ+(player.chasingPosZ-player.posZ)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d, 
									0, 0, 0, 0xAAB85A, 0xF4FCB6, 1, 7, 1);
							Minewatch.proxy.spawnParticlesTrail(player.world, 
									player.posX+(player.chasingPosX-player.posX)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d+0.2d, 
									player.posY+(player.chasingPosY-player.posY)*i/numParticles+player.height/2+(player.world.rand.nextDouble()-0.5d)*0.1d+0.2d, 
									player.posZ+(player.chasingPosZ-player.posZ)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d, 
									0, 0, 0, 0xAAB85A, 0xF4FCB6, 1, 7, 1);
						}
					}
				}
			});
			return null;
		}

		private void move(EntityPlayer player, double scale, boolean useLook) {
			Vec3d vec = new Vec3d(player.motionX, 0, player.motionZ);
			if (vec.xCoord == 0 && vec.zCoord == 0) 
				vec = new Vec3d(player.getLookVec().xCoord, 0, player.getLookVec().zCoord);
			if (useLook)
				vec = new Vec3d(player.getLookVec().xCoord, player.getLookVec().yCoord, player.getLookVec().zCoord);
			if (!player.onGround) {
				player.motionY = 0.24d;
				player.velocityChanged = true;
			}
			vec = vec.normalize().scale(scale);
			player.move(MoverType.SELF, vec.xCoord, vec.yCoord, vec.zCoord);
		}
	}
}