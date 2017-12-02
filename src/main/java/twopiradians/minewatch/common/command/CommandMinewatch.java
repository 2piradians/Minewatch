package twopiradians.minewatch.common.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.packet.SPacketSimple;

public class CommandMinewatch implements ICommand {

	public static final ArrayList<String> ALL_HERO_NAMES = new ArrayList<String>();
	static {
		for (EnumHero hero : EnumHero.values())
			ALL_HERO_NAMES.add(hero.name);
	}

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
		return "/mw hero <Hero> [target] or /mw syncConfigToServer";
	}

	@Override
	public List<String> getAliases() {
		return new ArrayList<String>() {{add("mw");}};
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		// sync config
		if (args.length == 1 && args[0].equalsIgnoreCase("syncConfigToServer") && sender instanceof EntityPlayerMP) {
			if (server.isSinglePlayer())
				sender.sendMessage(new TextComponentString(TextFormatting.RED+"The config only needs to be synced on a multiplayer server."));
			else
				Minewatch.network.sendTo(new SPacketSimple(17), (EntityPlayerMP) sender);
		}
		// hero [target]
		else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("hero")) {
			EnumHero hero = null;
			for (EnumHero hero2 : EnumHero.values())
				if (hero2.name.equalsIgnoreCase(args[1]))
					hero = hero2;

			if (hero != null) {
				EntityLivingBase entity = args.length == 3 ? 
						(EntityLivingBase)CommandBase.getEntity(server, sender, args[2], EntityLivingBase.class) :
							CommandBase.getCommandSenderAsPlayer(sender);
						if (!(entity instanceof EntityLivingBaseMW)) {
							for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
								ItemStack stack = entity.getItemStackFromSlot(slot);
								if (stack == null || stack.isEmpty() || stack.getItem() instanceof ItemMWArmor ||
										stack.getItem() instanceof ItemMWWeapon)
									entity.setItemStackToSlot(slot, hero.getEquipment(slot) == null ? 
											ItemStack.EMPTY : new ItemStack(hero.getEquipment(slot)));
								else if (hero.getEquipment(slot) != null && entity instanceof EntityPlayer)
									((EntityPlayer)entity).inventory.addItemStackToInventory(new ItemStack(hero.getEquipment(slot)));
							}
							sender.sendMessage(new TextComponentTranslation(TextFormatting.GREEN+"Spawned set for "+hero.name+
									(args.length == 3 ? " on "+entity.getName() : "")));
						}
			}
			else
				sender.sendMessage(new TextComponentTranslation(TextFormatting.RED+args[1]+" is not a valid hero"));
		}
		else
			throw new WrongUsageException(this.getUsage(sender), new Object[0]);
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return (sender instanceof EntityPlayer && server.getPlayerList().canSendCommands(((EntityPlayer)sender).getGameProfile())) ||
				!(sender instanceof Entity);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 1) {
			if (!server.isSinglePlayer())
				return CommandBase.getListOfStringsMatchingLastWord(args, new String[] {"hero", "syncConfigToServer"});
			else
				return CommandBase.getListOfStringsMatchingLastWord(args, new String[] {"hero"});
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("hero"))
			return CommandBase.getListOfStringsMatchingLastWord(args, ALL_HERO_NAMES);
		else if (args.length == 3 && args[0].equalsIgnoreCase("hero"))
			return CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		else
			return new ArrayList<String>();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return args.length == 3 && args[0].equalsIgnoreCase("hero");
	}
}