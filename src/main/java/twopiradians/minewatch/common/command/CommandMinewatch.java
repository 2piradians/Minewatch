package twopiradians.minewatch.common.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
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
import twopiradians.minewatch.common.hero.RespawnManager;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.hero.UltimateManager;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class CommandMinewatch implements ICommand {

	private enum EnumFlag {
		KILL_ITEMS("k", "killItems"), CLEAR_PLAYER_INVENTORIES("c", "clearPlayerInventories"),
		IGNORE_CREATIVE("ic", "ignoreCreativePlayers"), IGNORE_SPECTATOR("is", "ignoreSpectatorPlayers");

		public String shortName;
		public String longName;

		EnumFlag(String shortName, String longName) {
			this.shortName = "-"+shortName;
			this.longName = "-"+longName;
		}

		/**Does this list contain the flag - removes matching string from list if it does*/
		public boolean hasFlag(ArrayList<String> list) {
			if (list != null)
				for (String str : list)
					if (this.matches(str)) {
						list.remove(str);
						return true;
					}
			return false;
		}

		/**Does this string match the flag*/
		public boolean matches(String string) {
			return string != null && (string.equalsIgnoreCase(shortName) || string.equalsIgnoreCase(longName));
		}

		/**Returns valid flags in the list*/
		public static Set<EnumFlag> getFlags(ArrayList<String> list) {
			Set<EnumFlag> flags = Sets.newHashSet();
			for (EnumFlag flag : EnumFlag.values())
				if (flag.hasFlag(list)) 
					flags.add(flag);
			return flags;
		}
	}


	public static final ArrayList<String> ALL_HERO_NAMES = new ArrayList<String>();
	public static final ArrayList<String> ALL_FLAG_NAMES = new ArrayList<String>();
	static {
		for (EnumHero hero : EnumHero.values())
			ALL_HERO_NAMES.add(hero.name);
		ALL_HERO_NAMES.add("random");

		for (EnumFlag flag : EnumFlag.values())
			ALL_FLAG_NAMES.add(flag.longName);
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
		String flagUsage = "";
		for (EnumFlag flag : EnumFlag.values())
			flagUsage += " ["+flag.shortName+"|"+flag.longName+"]";
		String f1 = TextFormatting.ITALIC+"";
		String f2 = TextFormatting.RESET+""+TextFormatting.RED;
		return "\n"
		+ f1 + "/mw hero <hero|random> [target] "+ f2 + Minewatch.translate("command.hero.desc")+"\n"
		+ f1 + "/mw syncConfigToServer "+ f2 + Minewatch.translate("command.sync.desc")+"\n"
		+ f1 + "/mw teamSpawn <name> <activate|deactivate> "+ f2 + Minewatch.translate("command.team_spawn.desc")+"\n"
		+ f1 + "/mw ult <target> [charge] "+ f2 + Minewatch.translate("command.ult.desc")+"\n"
		+ f1 + "/mw reset"+flagUsage+" "+ f2 + Minewatch.translate("command.reset.desc");
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
		// hero <hero> [target]
		else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("hero")) {
			EnumHero hero = null;
			for (EnumHero hero2 : EnumHero.values())
				if (hero2.name.equalsIgnoreCase(args[1]))
					hero = hero2;
			if (hero == null && args[1].equalsIgnoreCase("random"))
				hero = EnumHero.values()[sender.getEntityWorld().rand.nextInt(EnumHero.values().length)];

			if (hero != null) {
				EntityLivingBase entity = args.length == 3 ? 
						(EntityLivingBase)CommandBase.getEntity(server, sender, args[2], EntityLivingBase.class) :
							CommandBase.getCommandSenderAsPlayer(sender);
						equipWithHeroArmor(hero, entity, sender);
			}
			else
				sender.sendMessage(new TextComponentTranslation(TextFormatting.RED+args[1]+" is not a valid hero"));
		}
		// teamSpawn <name> <activate|deactivate>
		else if (args.length == 3 && args[0].equalsIgnoreCase("teamSpawn") && (args[2].equalsIgnoreCase("activate") || args[2].equalsIgnoreCase("deactivate"))) {
			BlockPos pos = null;
			for (BlockPos pos2 : TileEntityTeamSpawn.teamSpawnPositions.keySet())
				if (TileEntityTeamSpawn.teamSpawnPositions.get(pos2).equals(args[1])) {
					pos = pos2;
					break;
				}
			if (pos == null || !(sender.getEntityWorld().getTileEntity(pos) instanceof TileEntityTeamSpawn))
				sender.sendMessage(new TextComponentTranslation(TextFormatting.RED+args[1]+" is not a valid Team Spawn name or is not in this dimension"));
			else {
				TileEntityTeamSpawn te = (TileEntityTeamSpawn) sender.getEntityWorld().getTileEntity(pos);
				boolean activate = args[2].equalsIgnoreCase("activate");
				te.setActivated(activate);
				sender.sendMessage(new TextComponentTranslation(TextFormatting.GREEN+args[1]+" is now: "+(activate ? TextFormatting.DARK_GREEN+"Activated" : TextFormatting.DARK_RED+"Deactivated")));
			}
		}
		// ult <target> [percent]
		else if (args.length >= 2 && args[0].equalsIgnoreCase("ult")) {
			Entity entity = CommandBase.getEntity(server, sender, args[1]);
			double charge = args.length > 2 ? CommandBase.parseDouble(args[2], 0, 100) : 100;
			if (UltimateManager.getMaxCharge(entity) > 0) {
				UltimateManager.setCharge(entity, (float) (charge/100d*UltimateManager.getMaxCharge(entity)), true);
				sender.sendMessage(new TextComponentTranslation(TextFormatting.GREEN+"Set "+entity.getName()+"'s ultimate charge to "+charge));
			}
		}
		// reset
		else if (args.length >= 1 && args[0].equalsIgnoreCase("reset")) {
			ArrayList<String> flagStrings = new ArrayList(Arrays.asList(args));
			flagStrings.remove(args[0]);
			Set<EnumFlag> flags = EnumFlag.getFlags(flagStrings);

			// unregister dead handler for players that are already dead (don't appear in loadedEntities)
			List<EntityPlayer> players = Lists.newArrayList(sender.getEntityWorld().playerEntities);
			for (EntityPlayer player : players) {
				Handler handler = TickHandler.getHandler(player, Identifier.DEAD);

				// already dead, respawn
				if (handler != null) {
					TickHandler.unregister(false, handler);
					if (player instanceof EntityPlayerMP) {
						Minewatch.network.sendTo(new SPacketSimple(65, player, false), (EntityPlayerMP) player);
					}
				}
			}

			// kill and respawn
			List<Entity> entities = Lists.newArrayList(sender.getEntityWorld().loadedEntityList); // copy to prevent concurrentModification
			for (Entity entity : entities) {
				// kill respawnable entities
				if (RespawnManager.isRespawnableEntity(entity) || RespawnManager.isRespawnablePlayer(entity)) {
					Handler handler = TickHandler.getHandler(entity, Identifier.DEAD);

					// ignore creative
					if (flags.contains(EnumFlag.IGNORE_CREATIVE) && entity instanceof EntityPlayer && ((EntityPlayer)entity).isCreative())
						break;

					// ignore spectator
					if (flags.contains(EnumFlag.IGNORE_SPECTATOR) && entity instanceof EntityPlayer && ((EntityPlayer)entity).isSpectator())
						break;

					// clear player inventories
					if (entity instanceof EntityPlayer && flags.contains(EnumFlag.CLEAR_PLAYER_INVENTORIES)) {
						((EntityPlayer)entity).inventory.clear();
						((EntityPlayer)entity).inventoryContainer.detectAndSendChanges();
					}

					// not dead, register DEAD and kill
					if (handler == null) { // delay player respawn a bit to prevent "Fetching addPacket for removed entity" warning in console
						TickHandler.register(false, RespawnManager.DEAD.setEntity(entity).setTicks(entity instanceof EntityPlayerMP ? 2 : 0).setString(entity.getTeam() != null ? entity.getTeam().getName() : null).setNumber(entity instanceof EntityPlayerMP ? ((EntityPlayerMP)entity).interactionManager.getGameType().ordinal() : -1));
						entity.onKillCommand();
					}
					// already dead, respawn
					else {
						TickHandler.unregister(false, handler);
					}
				}
			}

			// kill items
			if (flags.contains(EnumFlag.KILL_ITEMS)) {
				entities = Lists.newArrayList(sender.getEntityWorld().loadedEntityList);
				for (Entity entity : entities) 
					if (entity instanceof EntityItem)
						entity.onKillCommand();
			}

			// warn about invalid flags
			if (!flagStrings.isEmpty())
				sender.sendMessage(new TextComponentString(TextFormatting.RED+Minewatch.translate("command.reset.unrecognized_flags")+": "+flagStrings));

			// notify all players of reset
			for (EntityPlayer player : sender.getEntityWorld().playerEntities)
				player.sendMessage(new TextComponentString(TextFormatting.YELLOW+Minewatch.translate("command.reset.success")));
		}
		else
			throw new WrongUsageException(this.getUsage(sender), new Object[0]);
	}

	public static void equipWithHeroArmor(EnumHero hero, EntityLivingBase entity, ICommandSender sender) {
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
			sender.sendMessage(new TextComponentTranslation(TextFormatting.GREEN+"Spawned set for "+hero.getFormattedName(false)+
					(sender != entity ? " on "+entity.getName() : "")));
			// sync inventory - needed for when called from GuiTab
			if (sender instanceof EntityPlayerMP)
				((EntityPlayerMP)sender).sendContainerToPlayer(((EntityPlayerMP)sender).inventoryContainer);
			SetManager.healToFull(entity);
		}		
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return (sender instanceof EntityPlayer && server.getPlayerList().canSendCommands(((EntityPlayer)sender).getGameProfile())) ||
				!(sender instanceof Entity);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 1) {
			ArrayList<String> list = Lists.newArrayList("hero", "teamSpawn", "reset", "ult");
			if (!server.isSinglePlayer())
				list.add("syncConfigToServer");
			return CommandBase.getListOfStringsMatchingLastWord(args, list);
		}
		// hero
		else if (args.length == 2 && args[0].equalsIgnoreCase("hero"))
			return CommandBase.getListOfStringsMatchingLastWord(args, ALL_HERO_NAMES);
		else if (args.length == 3 && args[0].equalsIgnoreCase("hero"))
			return CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		// reset
		else if (args.length > 1 && args[0].equalsIgnoreCase("reset"))
			return CommandBase.getListOfStringsMatchingLastWord(args, ALL_FLAG_NAMES);
		// teamSpawn
		else if (args.length == 2 && args[0].equalsIgnoreCase("teamSpawn"))
			return CommandBase.getListOfStringsMatchingLastWord(args, TileEntityTeamSpawn.teamSpawnPositions.values());
		else if (args.length == 3 && args[0].equalsIgnoreCase("teamSpawn"))
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[] {"activate", "deactivate"});
		
		// ult
		else if (args.length == 2 && args[0].equalsIgnoreCase("ult"))
			return CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		else if (args.length == 3 && args[0].equalsIgnoreCase("ult"))
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[] {"100", "0"});
		
		else
			return new ArrayList<String>();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return (args.length == 3 && args[0].equalsIgnoreCase("hero")) || (args.length >= 2 && args[0].equalsIgnoreCase("ult"));
	}
}