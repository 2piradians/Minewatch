package twopiradians.minewatch.common.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandMinewatch implements ICommand {

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "minewatch";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "";
	}

	@Override
	public List<String> getAliases() {
		return new ArrayList<String>() {{add("mw");}};
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {

		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if (sender instanceof EntityPlayer)
			return server.getPlayerList().canSendCommands(((EntityPlayer)sender).getGameProfile());
		else
			return true;
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