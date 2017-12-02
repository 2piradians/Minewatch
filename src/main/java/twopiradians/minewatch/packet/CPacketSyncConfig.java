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
	private int tokenDropRate;
	private int wildCardRate;
	private float damageScale;
	private int durabilityOptionsArmor;
	private int durabilityOptionsWeapons;
	private boolean healMobs;
	
	private boolean mobRandomSkins;
	private int mobSpawn;
	private int mobSpawnFreq;
	private boolean mobTargetPlayers;
	private boolean mobTargetHostiles;
	private boolean mobTargetPassives;
	private boolean mobTargetHeroes;
	private int mobTokenDropRate;
	private int mobWildCardDropRate;
	private float mobEquipmentDropRate;
	private double mobAttackCooldown;
	private double mobInaccuracy;

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
			this.healMobs = Config.healMobs;
			
			this.mobRandomSkins = Config.mobRandomSkins;
			this.mobSpawn = Config.mobSpawn;
			this.mobSpawnFreq = Config.mobSpawnFreq;
			this.mobTargetPlayers = Config.mobTargetPlayers;
			this.mobTargetHostiles = Config.mobTargetHostiles;
			this.mobTargetPassives = Config.mobTargetPassives;
			this.mobTargetHeroes = Config.mobTargetHeroes;
			this.mobTokenDropRate = Config.mobTokenDropRate;
			this.mobWildCardDropRate = Config.mobWildCardDropRate;
			this.mobEquipmentDropRate = Config.mobEquipmentDropRate;
			this.mobAttackCooldown = Config.mobAttackCooldown;
			this.mobInaccuracy = Config.mobInaccuracy;
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.preventFallDamage = buf.readBoolean();
		this.allowGunWarnings = buf.readBoolean();
		this.projectilesCauseKnockback = buf.readBoolean();
		this.tokenDropRate = buf.readInt();
		this.wildCardRate = buf.readInt();
		this.damageScale = buf.readFloat();
		this.durabilityOptionsArmor = buf.readInt();
		this.durabilityOptionsWeapons = buf.readInt();
		this.healMobs = buf.readBoolean();
		
		this.mobRandomSkins = buf.readBoolean();
		this.mobSpawn = buf.readInt();
		this.mobSpawnFreq = buf.readInt();
		this.mobTargetPlayers = buf.readBoolean();
		this.mobTargetHostiles = buf.readBoolean();
		this.mobTargetPassives = buf.readBoolean();
		this.mobTargetHeroes = buf.readBoolean();
		this.mobTokenDropRate = buf.readInt();
		this.mobWildCardDropRate = buf.readInt();
		this.mobEquipmentDropRate = buf.readFloat();
		this.mobAttackCooldown = buf.readDouble();
		this.mobInaccuracy = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.uuid.toString());
		buf.writeBoolean(this.preventFallDamage);
		buf.writeBoolean(this.allowGunWarnings);
		buf.writeBoolean(this.projectilesCauseKnockback);
		buf.writeInt(this.tokenDropRate);
		buf.writeInt(this.wildCardRate);
		buf.writeFloat(this.damageScale);
		buf.writeInt(this.durabilityOptionsArmor);
		buf.writeInt(this.durabilityOptionsWeapons);
		buf.writeBoolean(this.healMobs);
		
		buf.writeBoolean(this.mobRandomSkins);
		buf.writeInt(this.mobSpawn);
		buf.writeInt(this.mobSpawnFreq);
		buf.writeBoolean(this.mobTargetPlayers);
		buf.writeBoolean(this.mobTargetHostiles);
		buf.writeBoolean(this.mobTargetPassives);
		buf.writeBoolean(this.mobTargetHeroes);
		buf.writeInt(this.mobTokenDropRate);
		buf.writeInt(this.mobWildCardDropRate);
		buf.writeFloat(this.mobEquipmentDropRate);
		buf.writeDouble(this.mobAttackCooldown);
		buf.writeDouble(this.mobInaccuracy);
	}

	public static class Handler implements IMessageHandler<CPacketSyncConfig, IMessage> {
		@Override
		public IMessage onMessage(final CPacketSyncConfig packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayer player = ctx.getServerHandler().playerEntity;
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
							Config.healMobs = packet.healMobs;
							
							Config.mobRandomSkins = packet.mobRandomSkins;
							Config.mobSpawn = packet.mobSpawn;
							Config.mobSpawnFreq = packet.mobSpawnFreq;
							Config.mobTargetPlayers = packet.mobTargetPlayers;
							Config.mobTargetHostiles = packet.mobTargetHostiles;
							Config.mobTargetPassives = packet.mobTargetPassives;
							Config.mobTargetHeroes = packet.mobTargetHeroes;
							Config.mobTokenDropRate = packet.mobTokenDropRate;
							Config.mobWildCardDropRate = packet.mobWildCardDropRate;
							Config.mobEquipmentDropRate = packet.mobEquipmentDropRate;
							Config.mobAttackCooldown = packet.mobAttackCooldown;
							Config.mobInaccuracy = packet.mobInaccuracy;
							
							Config.config.save();
							player.addChatMessage(new TextComponentString(TextFormatting.GREEN+"Successfully synced config to server."));
						}
						else
							player.addChatMessage(new TextComponentString(TextFormatting.RED+"You do not have permission to do that."));
					}
				}
			});
			return null;
		}
	}
}
