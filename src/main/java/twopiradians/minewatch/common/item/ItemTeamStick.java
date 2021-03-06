package twopiradians.minewatch.common.item;

import java.awt.Color;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumGui;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.block.ModBlocks;
import twopiradians.minewatch.common.tileentity.TileEntityTeam;
import twopiradians.minewatch.common.util.ColorHelper;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemTeamStick extends Item {

	public ItemTeamStick() {
		super();
		this.setMaxStackSize(1);
		MinecraftForge.EVENT_BUS.register(this);
		this.addPropertyOverride(new ResourceLocation("hasTeam"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
				return getTeamName(stack, false) != null ? 1 : 0;
			}
		});
	}

	/**Get a team's display name / registry name*/
	public static String getTeamName(Team team) {
		if (team instanceof ScorePlayerTeam)
			return ((ScorePlayerTeam)team).getDisplayName();
		else if (team != null)
			return team.getName();
		else
			return "";
	}

	/**Get the stack's team registry name*/
	public static String getTeamName(ItemStack stack, boolean displayName) {
		if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("teamRegistryName")) {
			if (displayName && stack.getTagCompound().hasKey("teamDisplayName"))
				return getTeamFormat(stack)+stack.getTagCompound().getString("teamDisplayName");
			else
				return getTeamFormat(stack)+stack.getTagCompound().getString("teamRegistryName");
		}
		else
			return null;
	}

	/**Get the stack's team chat format*/
	public static TextFormatting getTeamFormat(ItemStack stack) {
		if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("teamFormat")) 
			return TextFormatting.fromColorIndex(stack.getTagCompound().getInteger("teamFormat"));
		else
			return TextFormatting.RESET;
	}

	/**Get the stack's team*/
	@Nullable
	public static ScorePlayerTeam getTeam(World world, ItemStack stack) {
		String name = getTeamName(stack, false);
		if (name != null) 
			return world.getScoreboard().getTeam(TextFormatting.getTextWithoutFormattingCodes(name));
		else
			return null;
	}

	/**Set the stack's team*/
	public static void setTeam(ItemStack stack, @Nullable Team team) {
		if (stack != null) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());

			NBTTagCompound nbt = stack.getTagCompound();

			if (team == null) {// remove team
				nbt.removeTag("teamRegistryName");
				nbt.removeTag("teamDisplayName");
				nbt.removeTag("teamFormat");
			}
			else {// set team
				nbt.setString("teamRegistryName", team.getName());
				if (team instanceof ScorePlayerTeam)
					nbt.setString("teamDisplayName", ((ScorePlayerTeam)team).getDisplayName());
				else
					nbt.removeTag("teamDisplayName");
				nbt.setInteger("teamFormat", team.getColor().getColorIndex());
			}

			stack.setTagCompound(nbt);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void  addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		String format1 = TextFormatting.GRAY+""+TextFormatting.UNDERLINE;
		String format2 = TextFormatting.BLUE+"";
		tooltip.add(TextFormatting.GOLD+""+TextFormatting.ITALIC+"Teams made easy - works with entities and Team blocks");
		tooltip.add(format1+"RMB:"+format2+" Open Team Stick GUI");
		tooltip.add(format1+"RMB+Sneak:"+format2+" Clear selected team");
		tooltip.add(format1+"RMB+Entity:"+format2+" Remove team");
		tooltip.add(format1+"LMB+Entity:"+format2+" Assign team");
		tooltip.add(format1+"LMB+Entity+Sneak:"+format2+" Copy team");
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String name = getTeamName(stack, true);
		return super.getItemStackDisplayName(stack)+(name == null ? "" : ": "+name);
	}

	/**Send a message to the player - only call on server*/
	public static void sendChatMessage(EntityPlayer player, String string) {
		if (player != null && !player.world.isRemote && player instanceof EntityPlayerMP)
			Minewatch.network.sendTo(new SPacketSimple(45, player, string), (EntityPlayerMP) player);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		// right click air - open gui
		if (!player.isSneaking()) {
			if (world.isRemote)
				Minewatch.proxy.openGui(EnumGui.TEAM_STICK);
			return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
		}
		// sneak right click - remove selected
		else if (player.isSneaking()) {
			if (!world.isRemote) {
				setTeam(player.getHeldItem(hand), null);
				sendChatMessage(player, "Cleared selected team");
			}
			return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.world.isRemote && entity instanceof EntityLivingBase && !EntityHelper.shouldIgnoreEntity(entity, true, true)) {
			Team team = getTeam(player.world, stack);
			// copy team
			if (player.isSneaking()) {
				if (entity.getTeam() != null) {
					setTeam(stack, entity.getTeam());
					sendChatMessage(player, "Copied "+entity.getName()+"'s team: "+getTeamName(stack, true));
				}
				else 
					sendChatMessage(player, entity.getName()+" isn't on a team");
			}
			// add to team
			else if (team != null && !team.isSameTeam(entity.getTeam())) {
				player.world.getScoreboard().addPlayerToTeam(entity instanceof EntityPlayer ? entity.getName() : entity.getCachedUniqueIdString(), team.getName());
				sendChatMessage(player, "Set "+entity.getName()+"'s team to: "+getTeamName(stack, true));
				player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.5f, 1.8f);
			}
			// add to team already on
			else if (team != null && team.isSameTeam(entity.getTeam()))
				sendChatMessage(player, entity.getName()+" is already on team "+getTeamName(stack, true));
			// no team selected
			else if (team == null)
				sendChatMessage(player, "No team selected; select a team by right-click the air first");
		}

		return true;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
		if (!player.world.isRemote && !player.isSneaking() && !EntityHelper.shouldIgnoreEntity(entity, true, true)) {
			// remove from team
			if (entity.getTeam() != null) {
				try {
					player.world.getScoreboard().removePlayerFromTeams(entity instanceof EntityPlayer ? entity.getName() : entity.getCachedUniqueIdString());
				}
				catch (Exception e) {}
				sendChatMessage(player, "Removed "+entity.getName()+"'s team");
				player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.PLAYERS, 0.8f, 1);
				return true;
			}
			// remove from team when not on team
			else if (entity.getTeam() == null)
				sendChatMessage(player, entity.getName()+" is not on a team");
		}

		return false;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		// update team - mainly color
		if (!world.isRemote && entity.ticksExisted % 20 == 0) 
			setTeam(stack, getTeam(world, stack));
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
		if (!player.world.isRemote && player.world.getTileEntity(pos) instanceof TileEntityTeam) {
			TileEntityTeam te = (TileEntityTeam) player.world.getTileEntity(pos);
			String name = te.getBlockType().getLocalizedName();
			Team team = getTeam(player.world, stack);
			// copy team
			if (player.isSneaking()) {
				if (te.getTeam() != null) {
					setTeam(stack, te.getTeam());
					sendChatMessage(player, "Copied "+name+"'s team: "+getTeamName(stack, true));
				}
				else 
					sendChatMessage(player, name+" doesn't have a team");
			}
			// add to team
			else if (team != null && !team.isSameTeam(te.getTeam())) {
				te.setTeam(team);
				sendChatMessage(player, "Set "+name+"'s team to: "+getTeamName(stack, true));
				player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.5f, 1.8f);
			}
			// add to team already on
			else if (team != null && team.isSameTeam(te.getTeam()))
				sendChatMessage(player, name+" is already assigned to team "+getTeamName(stack, true));
			// no team selected
			else if (team == null)
				sendChatMessage(player, "No team selected; select a team by right-click the air first");
		}
		return true;
	}

	@Override
	public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
		return world.getBlockState(pos).getBlock() == ModBlocks.teamSpawn;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void glowTeams(RenderLivingEvent.Pre<EntityLivingBase> event) {
		boolean glow = false;
		for (ItemStack stack : Minecraft.getMinecraft().player.getHeldEquipment())
			if (stack != null && stack.getItem() == this && event.getEntity().getTeam() != null)
				glow = true;

		if (glow) {
			Handler handler = TickHandler.getHandler(event.getEntity(), Identifier.GLOWING);
			if (handler == null)
				TickHandler.register(true, Handlers.CLIENT_GLOWING.setEntity(event.getEntity()).setTicks(2));
			else
				handler.ticksLeft = 5;
		}
	}

	@SideOnly(Side.CLIENT)
	public static int getColorFromItemStack(ItemStack stack, int tintIndex) {
		if (tintIndex == 1 || Minecraft.getMinecraft().player == null)
			return -1;

		TextFormatting format = getTeamFormat(stack);
		int rate = 20;
		float glow = (Minecraft.getMinecraft().player.ticksExisted % rate);
		if (glow > rate/2)
			glow = rate-glow;
		glow *= 2f;
		if (format.isColor()) {
			Color color = new Color(ColorHelper.getForegroundColor(format));
			return new Color((int) MathHelper.clamp(color.getRed()+glow, 0, 255), 
					(int) MathHelper.clamp(color.getGreen()+glow, 0, 255), 
					(int) MathHelper.clamp(color.getBlue()+glow, 0, 255)).getRGB();
		}
		return -1;
	}

}