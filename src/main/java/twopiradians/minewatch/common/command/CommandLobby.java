package twopiradians.minewatch.common.command;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.hero.RespawnManager;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class CommandLobby implements ICommand {

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "lobby";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		String f1 = TextFormatting.ITALIC+"";
		String f2 = TextFormatting.RESET+""+TextFormatting.RED;
		return "\n"
		+ f1 + "/lobby "+ f2 + Minewatch.translate("command.lobby.desc");
	}

	@Override
	public List<String> getAliases() {
		return Lists.newArrayList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);
		
		// remove items from player
		player.inventory.clear();
		player.inventoryContainer.detectAndSendChanges();
		
		// remove team
		if (player != null && player.getTeam() != null)
			player.world.getScoreboard().removePlayerFromTeams(player.getName());
		
		Handler handler = TickHandler.getHandler(player, Identifier.DEAD);
		// not dead, register DEAD and kill
		if (handler == null) { // delay player respawn a bit to prevent "Fetching addPacket for removed entity" warning in console
			TickHandler.register(false, RespawnManager.DEAD.setEntity(player).setTicks(2).setString(player.getTeam() != null ? player.getTeam().getName() : null).setNumber(player.interactionManager.getGameType().ordinal()).setBoolean2(true));
			player.onKillCommand();
		}
		// already dead, respawn
		else {
			handler.setBoolean2(true);
			TickHandler.unregister(false, handler);
		}
		
		sender.sendMessage(new TextComponentTranslation(TextFormatting.GREEN+"Successfully removed from team and respawned"));
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return Config.lobbyCommand;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		return new ArrayList<String>();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
	
}