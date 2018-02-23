package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.command.CommandMinewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.RankManager;
import twopiradians.minewatch.common.hero.RankManager.Rank;
import twopiradians.minewatch.common.hero.RespawnManager;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.item.ItemTeamStick;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemLucioSoundAmplifier;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tileentity.TileEntityTeam;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class CPacketSimple implements IMessage {

	private int type;
	private boolean bool;
	private UUID uuid;
	private double x;
	private double y;
	private double z;
	private int id;
	private int id2;
	private String string;
	private String string2;

	public CPacketSimple() { }

	public CPacketSimple(int type) {
		this(type, false, null, 0, 0, 0, null, null, null);
	}

	public CPacketSimple(int type, boolean bool) {
		this(type, bool, null, 0, 0, 0, null, null, null);
	}

	public CPacketSimple(int type, Entity entity, boolean bool) {
		this(type, bool, null, 0, 0, 0, entity, null, null);
	}

	public CPacketSimple(int type, Entity entity, String string) {
		this(type, false, null, 0, 0, 0, entity, null, string);
	}

	public CPacketSimple(int type, Entity entity, boolean bool, Entity entity2) {
		this(type, bool, null, 0, 0, 0, entity, entity2, null);
	}

	public CPacketSimple(int type, Entity entity, boolean bool, double x, double y, double z) {
		this(type, bool, null, x, y, z, entity, null, null);
	}

	public CPacketSimple(int type, boolean bool, EntityPlayer player) {
		this(type, bool, player, 0, 0, 0, null, null, null);
	}

	public CPacketSimple(int type, String string, EntityPlayer player) {
		this(type, false, player, 0, 0, 0, null, null, string);
	}

	public CPacketSimple(int type, String string, EntityPlayer player, String string2) {
		this(type, false, player, 0, 0, 0, null, null, string, string2);
	}

	public CPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z) {
		this(type, bool, player, x, y, z, null, null, null);
	}

	public CPacketSimple(int type, String string, EntityPlayer player, double x, double y, double z) {
		this(type, false, player, x, y, z, null, null, string);
	}

	public CPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z, Entity entity) {
		this(type, bool, player, x, y, z, entity, null, null);
	}

	public CPacketSimple(int type, EntityPlayer player, double x, double y, double z) {
		this(type, false, player, x, y, z, null, null, null);
	}

	public CPacketSimple(int type, EntityPlayer player, double x, double y, double z, Entity entity) {
		this(type, false, player, x, y, z, entity, null, null);
	}

	public CPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z, Entity entity, Entity entity2, String string) {
		this(type, bool, player, x, y, z, entity, entity2, string, null);
	}

	public CPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z, Entity entity, Entity entity2, String string, String string2) {
		this.type = type;
		this.bool = bool;
		this.uuid = player == null ? UUID.randomUUID() : player.getPersistentID();
		this.id = entity == null ? -1 : entity.getEntityId();
		this.id2 = entity2 == null ? -1 : entity2.getEntityId();
		this.x = x;
		this.y = y;
		this.z = z;
		this.string = string == null ? "" : string;
		this.string2 = string2 == null ? "" : string2;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = buf.readInt();
		this.bool = buf.readBoolean();
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.id = buf.readInt();
		this.id2 = buf.readInt();
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
		this.string = ByteBufUtils.readUTF8String(buf);
		this.string2 = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type);
		buf.writeBoolean(this.bool);
		ByteBufUtils.writeUTF8String(buf, this.uuid.toString());
		buf.writeInt(this.id);
		buf.writeInt(this.id2);
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
		ByteBufUtils.writeUTF8String(buf, this.string);
		ByteBufUtils.writeUTF8String(buf, this.string2);
	}

	public static class Handler implements IMessageHandler<CPacketSimple, IMessage> {
		@Override
		public IMessage onMessage(final CPacketSimple packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayerMP player = ctx.getServerHandler().playerEntity;
					EntityPlayer packetPlayer = packet.uuid == null ? null : player.world.getPlayerEntityByUUID(packet.uuid);
					Entity entity = packet.id == -1 ? null : player.world.getEntityByID(packet.id);
					//Entity entity2 = packet.id2 == -1 ? null : player.world.getEntityByID(packet.id2);

					// reset fall distance
					if (packet.type == 0 && entity != null) {
						entity.fallDistance = 0;
						ModSoundEvents.WALL_CLIMB.playSound(entity, 0.9f, 1);
					}
					// check if opped
					else if (packet.type == 1 && packetPlayer instanceof EntityPlayerMP && packetPlayer.getServer() != null) {
						if (packetPlayer.getServer().getPlayerList().canSendCommands(packetPlayer.getGameProfile()) ||
								RankManager.getHighestRank(packetPlayer) == Rank.DEV)
							Minewatch.network.sendTo(new SPacketSimple(18), (EntityPlayerMP) packetPlayer);
					}
					// Wild Card Token selection
					else if (packet.type == 2 && packetPlayer != null && packet.x >= 0 && packet.x < EnumHero.values().length) {
						// take wild card and give token
						for (ItemStack stack : packetPlayer.getHeldEquipment())
							if (stack != null && stack.getItem() instanceof ItemMWToken.ItemWildCardToken && 
							stack.getCount() > 0) {
								stack.shrink(1);
								packetPlayer.inventory.addItemStackToInventory(
										new ItemStack(EnumHero.values()[(int) packet.x].token));
								break;
							}
						// close screen if no wild cards left
						boolean hasWildCard = false;
						for (ItemStack stack : packetPlayer.getHeldEquipment())
							if (stack != null && stack.getItem() instanceof ItemMWToken.ItemWildCardToken && 
							stack.getCount() > 0)
								hasWildCard = true;
						if (!hasWildCard)
							packetPlayer.closeScreen();
					}
					// Switch alt weapon
					else if (packet.type == 3 && packetPlayer != null) {
						ItemStack stack = packetPlayer.getHeldItemMainhand();
						ItemMWWeapon.setAlternate(stack, !ItemMWWeapon.isAlternate(stack));
						Minewatch.network.sendToAll(new SPacketSimple(6, ItemMWWeapon.isAlternate(stack), packetPlayer));
					}
					// Lucio's soundwave
					else if (packet.type == 4 && packetPlayer != null) {
						ItemLucioSoundAmplifier.soundwave(packetPlayer, packet.x, packet.y, packet.z);
					}
					// Team Selector set team
					else if (packet.type == 5 && packetPlayer != null && 
							EntityHelper.isHoldingItem(packetPlayer, ModItems.team_stick)) {
						Team team = packetPlayer.world.getScoreboard().getTeam(packet.string);
						for (ItemStack stack : packetPlayer.getHeldEquipment())
							if (stack != null && stack.getItem() == ModItems.team_stick) {
								ItemTeamStick.setTeam(stack, team);
								if (team == null)
									ItemTeamStick.sendChatMessage(packetPlayer, "Cleared selected team");
								else
									ItemTeamStick.sendChatMessage(packetPlayer, "Selected team: "+team.getChatFormat()+ItemTeamStick.getTeamName(team));
							}
					}
					// Team Selector gui set entity team
					else if (packet.type == 6 && packetPlayer != null && entity != null && 
							EntityHelper.isHoldingItem(packetPlayer, ModItems.team_stick)) {
						try {
							if (packet.string.isEmpty())
								packetPlayer.world.getScoreboard().removePlayerFromTeams(entity instanceof EntityPlayer ? entity.getName() : entity.getCachedUniqueIdString());
							else
								packetPlayer.world.getScoreboard().addPlayerToTeam(entity instanceof EntityPlayer ? entity.getName() : entity.getCachedUniqueIdString(), packet.string);
						}
						catch (Exception e) {}
					}
					// Team Selector set team color
					else if (packet.type == 7 && packet.x >= 0 && packet.x < 16 && 
							EntityHelper.isHoldingItem(packetPlayer, ModItems.team_stick)) {
						Team team = packetPlayer.world.getScoreboard().getTeam(packet.string);
						TextFormatting format = TextFormatting.fromColorIndex((int) packet.x);
						if (team instanceof ScorePlayerTeam) {
							((ScorePlayerTeam)team).setChatFormat(format);
							((ScorePlayerTeam)team).setNamePrefix(format.toString());
							((ScorePlayerTeam)team).setNameSuffix(TextFormatting.RESET.toString());
						}
					}
					// Team Selector delete team
					else if (packet.type == 8 && EntityHelper.isHoldingItem(packetPlayer, ModItems.team_stick)) {
						ScorePlayerTeam team = packetPlayer.world.getScoreboard().getTeam(packet.string);
						if (team != null)
							packetPlayer.world.getScoreboard().removeTeam(team);
					}
					// Team Selector set team display name
					else if (packet.type == 9 && EntityHelper.isHoldingItem(packetPlayer, ModItems.team_stick)) {
						ScorePlayerTeam team = packetPlayer.world.getScoreboard().getTeam(packet.string);
						if (team != null && !packet.string2.isEmpty()) {
							team.setTeamName(packet.string2); 
						}
					}
					// Team Selector create team
					else if (packet.type == 10 && EntityHelper.isHoldingItem(packetPlayer, ModItems.team_stick)) {
						try {
							ScorePlayerTeam team = packetPlayer.world.getScoreboard().createTeam(packet.string);
							if (team != null) {
								TextFormatting format = TextFormatting.fromColorIndex((int) packet.x);
								team.setChatFormat(format);
								team.setNamePrefix(format.toString());
								team.setNameSuffix(TextFormatting.RESET.toString());
							}
						}
						catch (Exception e) {}
					}
					// GuiTab select hero
					else if (packet.type == 11 && packetPlayer != null && packetPlayer.world.getMinecraftServer() != null) {
						packetPlayer.world.getMinecraftServer().commandManager.executeCommand(packetPlayer, packet.string);
					}
					// player death screen
					else if (packet.type == 12 && packetPlayer != null) {
						Minewatch.logger.info("received packet: bool = "+packet.bool+", hasHandler = "+TickHandler.hasHandler(packetPlayer, Identifier.DEAD)+", alive = "+packetPlayer.isEntityAlive()+", health = "+packetPlayer.getHealth()+", respawnable = "+RespawnManager.isRespawnablePlayer(packetPlayer));
						if (!TickHandler.hasHandler(packetPlayer, Identifier.DEAD) && !packetPlayer.isEntityAlive() && 
								RespawnManager.isRespawnablePlayer(packetPlayer)) {
							TickHandler.register(false, RespawnManager.DEAD.setEntity(packetPlayer).setTicks(packet.bool ? 0 : Config.respawnTime).setString(packetPlayer.getTeam() != null ? packetPlayer.getTeam().getRegisteredName() : null));
						}
					}
					// GuiTeamBlock set team
					else if (packet.type == 13 && packetPlayer != null && packetPlayer.isCreative() &&
							packetPlayer.world.getTileEntity(new BlockPos(packet.x, packet.y, packet.z)) instanceof TileEntityTeam) {
						Team team = packetPlayer.world.getScoreboard().getTeam(packet.string);
						TileEntityTeam te = (TileEntityTeam) packetPlayer.world.getTileEntity(new BlockPos(packet.x, packet.y, packet.z));
						te.setTeam(team);
					}
					// GuiTeamBlock set team
					else if (packet.type == 14 && packetPlayer != null && packetPlayer.isCreative() &&
							packetPlayer.world.getTileEntity(new BlockPos(packet.x, packet.y, packet.z)) instanceof TileEntityTeam) {
						TileEntityTeam te = (TileEntityTeam) packetPlayer.world.getTileEntity(new BlockPos(packet.x, packet.y, packet.z));
						if (te.isValidName(packet.string))
							te.setName(packet.string);
					}
					// GuiTeamBlock activate/deactivate
					else if (packet.type == 15 && packetPlayer != null && packetPlayer.isCreative() &&
							packetPlayer.world.getTileEntity(new BlockPos(packet.x, packet.y, packet.z)) instanceof TileEntityTeam) {
						TileEntityTeam te = (TileEntityTeam) packetPlayer.world.getTileEntity(new BlockPos(packet.x, packet.y, packet.z));
						te.setActivated(packet.bool);
					}
					// GuiTeamSpawn increase/decrease spawnRadius
					else if (packet.type == 16 && packetPlayer != null && packetPlayer.isCreative() &&
							packetPlayer.world.getTileEntity(new BlockPos(packet.x, packet.y, packet.z)) instanceof TileEntityTeamSpawn) {
						TileEntityTeamSpawn te = (TileEntityTeamSpawn) packetPlayer.world.getTileEntity(new BlockPos(packet.x, packet.y, packet.z));
						te.setSpawnRadius(packet.bool ? te.getSpawnRadius()+1 : te.getSpawnRadius()-1);
					}
					// Set death spectating entity
					else if (packet.type == 17 && packetPlayer instanceof EntityPlayerMP && entity != null) {
						((EntityPlayerMP)packetPlayer).setSpectatingEntity(entity);
					}
					// change hero from Hero Select gui
					else if (packet.type == 18 && TickHandler.hasHandler(packetPlayer, Identifier.TEAM_SPAWN_IN_RANGE) &&
							packet.x >= 0 && packet.x < EnumHero.values().length) {
						if (Config.heroSelectClearMWItems)
							for (int i = 0; i < packetPlayer.inventory.getSizeInventory(); ++i) {
								ItemStack stack = packetPlayer.inventory.getStackInSlot(i);
								if (stack != null && (stack.getItem() instanceof ItemMWArmor || stack.getItem() instanceof ItemMWWeapon))
									packetPlayer.inventory.removeStackFromSlot(i);
							}
						CommandMinewatch.equipWithHeroArmor(EnumHero.values()[(int) packet.x], packetPlayer, packetPlayer);
					}
				}
			});
			return null;
		}
	}

}