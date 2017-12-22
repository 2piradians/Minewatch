package twopiradians.minewatch.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;

public class PacketSyncConfig implements IMessage {

	private boolean preventFallDamage;
	private boolean allowGunWarnings;
	private boolean projectilesCauseKnockback;
	private int tokenDropRate;
	private int wildCardRate;
	private float damageScale;
	private int durabilityOptionsArmor;
	private int durabilityOptionsWeapons;
	private boolean healMobs;
	private double healthPackHealMultiplier;
	private double healthPackRespawnMultiplier;

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

	public PacketSyncConfig() {
		this.preventFallDamage = Config.preventFallDamage;
		this.allowGunWarnings = Config.allowGunWarnings;
		this.projectilesCauseKnockback = Config.projectilesCauseKnockback;
		this.tokenDropRate = Config.tokenDropRate;
		this.wildCardRate = Config.wildCardRate;
		this.damageScale = Config.damageScale;
		this.durabilityOptionsArmor = Config.durabilityOptionArmors;
		this.durabilityOptionsWeapons = Config.durabilityOptionWeapons;
		this.healMobs = Config.healMobs;
		this.healthPackHealMultiplier = Config.healthPackHealMultiplier;
		this.healthPackRespawnMultiplier = Config.healthPackRespawnMultiplier;

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

	@Override
	public void fromBytes(ByteBuf buf) {
		this.preventFallDamage = buf.readBoolean();
		this.allowGunWarnings = buf.readBoolean();
		this.projectilesCauseKnockback = buf.readBoolean();
		this.tokenDropRate = buf.readInt();
		this.wildCardRate = buf.readInt();
		this.damageScale = buf.readFloat();
		this.durabilityOptionsArmor = buf.readInt();
		this.durabilityOptionsWeapons = buf.readInt();
		this.healMobs = buf.readBoolean();
		this.healthPackHealMultiplier = buf.readDouble();
		this.healthPackRespawnMultiplier = buf.readDouble();

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
		buf.writeBoolean(this.preventFallDamage);
		buf.writeBoolean(this.allowGunWarnings);
		buf.writeBoolean(this.projectilesCauseKnockback);
		buf.writeInt(this.tokenDropRate);
		buf.writeInt(this.wildCardRate);
		buf.writeFloat(this.damageScale);
		buf.writeInt(this.durabilityOptionsArmor);
		buf.writeInt(this.durabilityOptionsWeapons);
		buf.writeBoolean(this.healMobs);
		buf.writeDouble(this.healthPackHealMultiplier);
		buf.writeDouble(this.healthPackRespawnMultiplier);

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

	public void run() {
		Config.preventFallDamage = this.preventFallDamage;
		Config.allowGunWarnings = this.allowGunWarnings;
		Config.projectilesCauseKnockback = this.projectilesCauseKnockback;
		Config.tokenDropRate = this.tokenDropRate;
		Config.wildCardRate = this.wildCardRate;
		Config.damageScale = this.damageScale;
		Config.durabilityOptionArmors = this.durabilityOptionsArmor;
		Config.durabilityOptionWeapons = this.durabilityOptionsWeapons;
		Config.healMobs = this.healMobs;
		Config.healthPackHealMultiplier = this.healthPackHealMultiplier;
		Config.healthPackRespawnMultiplier = this.healthPackRespawnMultiplier;

		Config.mobRandomSkins = this.mobRandomSkins;
		Config.mobSpawn = this.mobSpawn;
		Config.mobSpawnFreq = this.mobSpawnFreq;
		Config.mobTargetPlayers = this.mobTargetPlayers;
		Config.mobTargetHostiles = this.mobTargetHostiles;
		Config.mobTargetPassives = this.mobTargetPassives;
		Config.mobTargetHeroes = this.mobTargetHeroes;
		Config.mobTokenDropRate = this.mobTokenDropRate;
		Config.mobWildCardDropRate = this.mobWildCardDropRate;
		Config.mobEquipmentDropRate = this.mobEquipmentDropRate;
		Config.mobAttackCooldown = this.mobAttackCooldown;
		Config.mobInaccuracy = this.mobInaccuracy;

		Config.syncConfig(true);
		Config.config.save();
	}

	/**Sync config to client on login or config synced to server via tab button / command*/
	public static class HandlerClient implements IMessageHandler<PacketSyncConfig, IMessage> {
		@Override
		public IMessage onMessage(final PacketSyncConfig packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					packet.run();
					Minewatch.logger.info("Synced config from server.");
				}
			});
			return null;
		}
	}

	/**Sync config to server with tab button / command*/
	public static class HandlerServer implements IMessageHandler<PacketSyncConfig, IMessage> {
		@Override
		public IMessage onMessage(final PacketSyncConfig packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayer player = ctx.getServerHandler().player;
					if (player != null) {
						if (player.getServer().getPlayerList().canSendCommands(player.getGameProfile())) {
							packet.run(); 
							Minewatch.network.sendToAll(new PacketSyncConfig()); // sync new config to all clients
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
