package twopiradians.minewatch.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.potion.PotionFrozen;

public class PacketPotionEffect implements IMessage {
	
	private int entity;
	private String potion;
	private int duration;
	private int amplifier;
	private boolean ambient;
	private boolean showParticles;

	public PacketPotionEffect() { }

	public PacketPotionEffect(EntityLivingBase entity, PotionEffect effect) {
		this.entity = entity.getEntityId();
		this.potion = effect.getPotion().getName();
		this.duration = effect.getDuration();
		this.amplifier = effect.getAmplifier();
		this.ambient = effect.getIsAmbient();
		this.showParticles = effect.doesShowParticles();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.entity = buf.readInt();
		this.potion = ByteBufUtils.readUTF8String(buf);
		this.duration = buf.readInt();
		this.amplifier = buf.readInt();
		this.ambient = buf.readBoolean();
		this.showParticles = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.entity);
		ByteBufUtils.writeUTF8String(buf, this.potion);
		buf.writeInt(this.duration);
		buf.writeInt(this.amplifier);
		buf.writeBoolean(this.ambient);
		buf.writeBoolean(this.showParticles);
	}

	public static class Handler implements IMessageHandler<PacketPotionEffect, IMessage> {
		@Override
		public IMessage onMessage(final PacketPotionEffect packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					Entity entity = Minecraft.getMinecraft().world.getEntityByID(packet.entity);
					Potion potion = null;
					for (Potion potion2 : ModPotions.potions)
						if (potion2.getName().equalsIgnoreCase(packet.potion))
							potion = potion2;
					if (potion != null && entity instanceof EntityLivingBase)
					((EntityLivingBase) entity).addPotionEffect
					(new PotionEffect(potion, packet.duration, packet.amplifier, packet.ambient, packet.showParticles));
					if (potion == ModPotions.frozen)
						PotionFrozen.rotations.put(entity.getPersistentID(), new Tuple(entity.rotationPitch, entity.rotationYaw));
				}
			});
			return null;
		}
	}
}