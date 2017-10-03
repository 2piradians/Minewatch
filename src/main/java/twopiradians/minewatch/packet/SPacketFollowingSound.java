package twopiradians.minewatch.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;

public class SPacketFollowingSound implements IMessage{
	
    private SoundEvent sound;
    private SoundCategory category;
    private int entity;
    private float volume;
    private float pitch;
    private boolean repeat;

	public SPacketFollowingSound() {}

	public SPacketFollowingSound(Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean repeat) {
        this.sound = sound;
        this.category = category;
        this.entity = entity.getEntityId();
        this.volume = volume;
        this.pitch = pitch;
        this.repeat = repeat;
    }

	@Override
	public void fromBytes(ByteBuf buf) {
        this.sound = SoundEvent.REGISTRY.getObjectById(buf.readInt());
        this.category = SoundCategory.values()[buf.readInt()];
        this.entity = buf.readInt();
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
        this.repeat = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
        buf.writeInt(SoundEvent.REGISTRY.getIDForObject(this.sound));
        buf.writeInt(this.category.ordinal());
        buf.writeInt(this.entity);
        buf.writeFloat(this.volume);
        buf.writeFloat(this.pitch);
        buf.writeBoolean(this.repeat);
	}

	public static class Handler implements IMessageHandler<SPacketFollowingSound, IMessage> {
		@Override
		public IMessage onMessage(final SPacketFollowingSound packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					Entity entity = Minecraft.getMinecraft().world.getEntityByID(packet.entity);
					Minewatch.proxy.playFollowingSound(entity, packet.sound, packet.category, packet.volume, packet.pitch, packet.repeat);
				}
			});
			return null;
		}
	}
}
