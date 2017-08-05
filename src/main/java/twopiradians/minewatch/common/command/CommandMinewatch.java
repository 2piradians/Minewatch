package twopiradians.minewatch.common.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

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
	public String getCommandName() {
		return "minewatch";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/mw <Hero>";
	}

	@Override
	public List<String> getCommandAliases() {
		return new ArrayList<String>() {{add("mw");}};
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer && args.length == 1) {
			EnumHero hero = null;
			for (EnumHero hero2 : EnumHero.values())
				if (hero2.name.equalsIgnoreCase(args[0]))
					hero = hero2;

			if (hero != null) {
				EntityPlayer player = (EntityPlayer) sender;
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					ItemStack stack = player.getItemStackFromSlot(slot);
					if (stack == null || stack.getItem() instanceof ItemMWArmor ||
							stack.getItem() instanceof ItemMWWeapon)
						player.setItemStackToSlot(slot, hero.getEquipment(slot) == null ? 
								null : new ItemStack(hero.getEquipment(slot)));
					else if (hero.getEquipment(slot) != null)
						player.inventory.addItemStackToInventory(new ItemStack(hero.getEquipment(slot)));
				}
				sender.addChatMessage(new TextComponentTranslation(TextFormatting.GREEN+"Spawned set for "+hero.name));
			}
			else
				sender.addChatMessage(new TextComponentTranslation(TextFormatting.RED+args[0]+" is not a valid hero"));
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender instanceof EntityPlayer && server.getPlayerList().canSendCommands(((EntityPlayer)sender).getGameProfile());
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 1)
			return CommandBase.getListOfStringsMatchingLastWord(args, ALL_HERO_NAMES);
		else
			return new ArrayList<String>();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
}