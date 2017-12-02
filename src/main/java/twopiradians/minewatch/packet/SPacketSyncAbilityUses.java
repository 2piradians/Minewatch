package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class SPacketSyncAbilityUses implements IMessage{

	private UUID player;
	private String hero;
	private int ability;
	private int uses;
	private boolean playSound;

	public SPacketSyncAbilityUses() {}

	public SPacketSyncAbilityUses(UUID player, EnumHero hero, int ability, int uses, boolean playSound) {
		this.player = player;
		this.hero = hero.name();
		this.ability = ability;
		this.uses = uses;
		this.playSound = playSound;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.player = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.hero = ByteBufUtils.readUTF8String(buf);
		this.ability = buf.readInt();	
		this.uses = buf.readInt();
		this.playSound = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.player.toString());
		ByteBufUtils.writeUTF8String(buf, this.hero);
		buf.writeInt(this.ability);
		buf.writeInt(this.uses);
		buf.writeBoolean(this.playSound);
	}

	public static class Handler implements IMessageHandler<SPacketSyncAbilityUses, IMessage> {
		@Override
		public IMessage onMessage(final SPacketSyncAbilityUses packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayer player = Minecraft.getMinecraft().player;
					EnumHero hero = EnumHero.valueOf(packet.hero);
					Ability ability = null;
					if (packet.ability == 1)
						ability = hero.ability1;
					else if (packet.ability == 2)
						ability = hero.ability2;
					else if (packet.ability == 3)
						ability = hero.ability3;

					if (player != null && ability != null) {
						if (packet.playSound && 
								ItemMWArmor.SetManager.getWornSet(player) == hero) {
							if (packet.uses == 1)
								ModSoundEvents.ABILITY_RECHARGE.playSound(player, 0.5f, 1.0f, true);
							ModSoundEvents.ABILITY_MULTI_RECHARGE.playSound(player, 0.5f, 1.0f, true);
						}
						ability.multiAbilityUses.put(player.getPersistentID(), packet.uses);
					}
				}
			});
			return null;
		}
	}
}
