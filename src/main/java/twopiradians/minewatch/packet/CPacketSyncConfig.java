package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;

public class CPacketSyncConfig implements IMessage {

	private UUID uuid;
	private boolean preventFallDamage;
	private boolean allowGunWarnings;
	private boolean projectilesCauseKnockback;
	private double tokenDropRate;
	private double wildCardRate;
	private float damageScale;
	private int durabilityOptionsArmor;
	private int durabilityOptionsWeapons;

	public CPacketSyncConfig() {
		if (Minewatch.proxy.getClientUUID() != null) {
			this.uuid = Minewatch.proxy.getClientUUID();
			this.preventFallDamage = Config.preventFallDamage;
			this.allowGunWarnings = Config.allowGunWarnings;
			this.projectilesCauseKnockback = Config.projectilesCauseKnockback;
			this.tokenDropRate = Config.tokenDropRate;
			this.wildCardRate = Config.wildCardRate;
			this.damageScale = Config.damageScale;
			this.durabilityOptionsArmor = Config.durabilityOptionArmors;
			this.durabilityOptionsWeapons = Config.durabilityOptionWeapons;
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.preventFallDamage = buf.readBoolean();
		this.allowGunWarnings = buf.readBoolean();
		this.projectilesCauseKnockback = buf.readBoolean();
		this.tokenDropRate = buf.readDouble();
		this.wildCardRate = buf.readDouble();
		this.damageScale = buf.readFloat();
		this.durabilityOptionsArmor = buf.readInt();
		this.durabilityOptionsWeapons = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.uuid.toString());
		buf.writeBoolean(this.preventFallDamage);
		buf.writeBoolean(this.allowGunWarnings);
		buf.writeBoolean(this.projectilesCauseKnockback);
		buf.writeDouble(this.tokenDropRate);
		buf.writeDouble(this.wildCardRate);
		buf.writeFloat(this.damageScale);
		buf.writeInt(this.durabilityOptionsArmor);
		buf.writeInt(this.durabilityOptionsWeapons);
	}

	public static class Handler implements IMessageHandler<CPacketSyncConfig, IMessage> {
		@Override
		public IMessage onMessage(final CPacketSyncConfig packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayer player = ctx.getServerHandler().player;
					if (player != null) {
						if (player.getServer().getPlayerList().canSendCommands(player.getGameProfile())) {
							Config.preventFallDamage = packet.preventFallDamage;
							Config.allowGunWarnings = packet.allowGunWarnings;
							Config.projectilesCauseKnockback = packet.projectilesCauseKnockback;
							Config.tokenDropRate = packet.tokenDropRate;
							Config.wildCardRate = packet.wildCardRate;
							Config.damageScale = packet.damageScale;
							Config.durabilityOptionArmors = packet.durabilityOptionsArmor;
							Config.durabilityOptionWeapons = packet.durabilityOptionsWeapons;
							Config.config.save();
							player.sendMessage(new TextComponentString(TextFormatting.GREEN+"Successfully synced config to server."));
						}
						else
							player.sendMessage(new TextComponentString(TextFormatting.RED+"You do not have permission to do that."));
					}
				}
			});
			return null;
		}
	}
}
