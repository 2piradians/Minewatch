package twopiradians.minewatch.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.sound.ModSoundEvents.ModSoundEvent;

public class SPacketFollowingSound implements IMessage{
	
    private ModSoundEvent sound;
    private SoundCategory category;
    private int entity;
    private float volume;
    private float pitch;
    private boolean repeat;
	private int attentuationType;

	public SPacketFollowingSound() {}

	public SPacketFollowingSound(Entity entity, ModSoundEvent sound, SoundCategory category, float volume, float pitch, boolean repeat, int attentuationType) {
        this.sound = sound;
        this.category = category;
        this.entity = entity.getEntityId();
        this.volume = volume;
        this.pitch = pitch;
        this.repeat = repeat;
        this.attentuationType = attentuationType;
    }

	@Override
	public void fromBytes(ByteBuf buf) {
        this.sound = ModSoundEvents.values()[buf.readInt()].event;
        this.category = SoundCategory.values()[buf.readInt()];
        this.entity = buf.readInt();
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
        this.repeat = buf.readBoolean();
        this.attentuationType = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
        buf.writeInt(this.sound.event.ordinal());
        buf.writeInt(this.category.ordinal());
        buf.writeInt(this.entity);
        buf.writeFloat(this.volume);
        buf.writeFloat(this.pitch);
        buf.writeBoolean(this.repeat);
        buf.writeInt(this.attentuationType);
	}

	public static class Handler implements IMessageHandler<SPacketFollowingSound, IMessage> {
		@Override
		public IMessage onMessage(final SPacketFollowingSound packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					Entity entity = Minecraft.getMinecraft().world.getEntityByID(packet.entity);
					packet.sound.event.playFollowingSound(entity, packet.volume, packet.pitch, packet.repeat, packet.attentuationType);
				}
			});
			return null;
		}
	}
}
