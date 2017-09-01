package twopiradians.minewatch.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketTriggerAbility implements IMessage {

	private int type;

	public SPacketTriggerAbility() { }

	public SPacketTriggerAbility(int type) {
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
			        	if (vec.xCoord == 0 && vec.zCoord == 0) 
			        		vec = new Vec3d(player.getLookVec().xCoord, 0, player.getLookVec().zCoord);
			        	vec = vec.normalize().scale(9);
			        	player.move(MoverType.SELF, vec.xCoord, vec.yCoord, vec.zCoord);
					}
				}
			});
			return null;
		}
	}
}