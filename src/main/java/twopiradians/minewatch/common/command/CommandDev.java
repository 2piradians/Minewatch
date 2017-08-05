package twopiradians.minewatch.common.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class CommandDev implements ICommand {
	
	public static final ArrayList<UUID> DEVS = new ArrayList<UUID>() {{
		add(UUID.fromString("f08951bc-e379-4f19-a113-7728b0367647")); // Furgl
		add(UUID.fromString("93d28330-e1e2-447b-b552-00cb13e9afbd")); // 2piradians
	}};

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "dev";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "";
	}

	@Override
	public List<String> getCommandAliases() {
		return new ArrayList<String>();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
	}
	
	/**Actually runs the command (for chat event), returns if message was a valid command (and chat should be hidden)*/
	public static boolean runCommand(MinecraftServer server, ICommandSender sender, String[] args) {
		if (sender instanceof EntityPlayer && args.length == 2 && args[0].equalsIgnoreCase("hero")) {
			EnumHero hero = null;
			for (EnumHero hero2 : EnumHero.values())
				if (hero2.name.equalsIgnoreCase(args[1]))
					hero = hero2;

			if (hero != null) {
				EntityPlayer player = (EntityPlayer) sender;
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					ItemStack stack = player.getItemStackFromSlot(slot);
					ItemStack newStack = hero.getEquipment(slot) == null ? 
							null : new ItemStack(hero.getEquipment(slot));
					if (newStack != null) {
						NBTTagCompound nbt = new NBTTagCompound();
						nbt.setBoolean("devSpawned", true);
						newStack.setTagCompound(nbt);
					}
					if (stack == null || stack.getItem() instanceof ItemMWArmor ||
							stack.getItem() instanceof ItemMWWeapon)
						player.setItemStackToSlot(slot, newStack);
					else if (hero.getEquipment(slot) != null)
						player.inventory.addItemStackToInventory(newStack);
				}
				sender.addChatMessage(new TextComponentTranslation(TextFormatting.GREEN+"Spawned set for "+hero.name));
			}
			else
				sender.addChatMessage(new TextComponentTranslation(TextFormatting.RED+args[1]+" is not a valid hero"));
			return true;
		}
		return false;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if (sender instanceof EntityPlayer)
			return DEVS.contains(((EntityPlayer) sender).getPersistentID());
		return false;
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 1)
			return CommandBase.getListOfStringsMatchingLastWord(args, new ArrayList<String>() {{add("hero");}});
		else if (args.length == 2 && args[0].equalsIgnoreCase("hero"))
			return CommandBase.getListOfStringsMatchingLastWord(args, CommandMinewatch.ALL_HERO_NAMES);
		else
			return new ArrayList<String>();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
}