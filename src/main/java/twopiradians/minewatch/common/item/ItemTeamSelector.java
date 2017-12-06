package twopiradians.minewatch.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumGui;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

public class ItemTeamSelector extends Item {

	public ItemTeamSelector() {
		super();
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**Get the stack's team*/
	@Nullable
	public static String getTeamName(ItemStack stack) {
		if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("team")) 
			return stack.getTagCompound().getString("team");
		else
			return null;
	}

	/**Get the stack's team*/
	@Nullable
	public static Team getTeam(World world, ItemStack stack) {
		String name = getTeamName(stack);
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

			if (team == null) // remove team
				nbt.removeTag("team");
			else // set team
				nbt.setString("team", team.getChatFormat()+team.getRegisteredName());

			stack.setTagCompound(nbt);
		}
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

	}

	public String getItemStackDisplayName(ItemStack stack) {
		String name = getTeamName(stack);
		return super.getItemStackDisplayName(stack)+(name == null ? "" : ": "+name);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		/*if (!world.isRemote) {
			Team team = getTeam(world, player.getHeldItem(hand));
			if ((team != null && !team.isSameTeam(player.getTeam())) || 
					(player.getTeam() != null && !player.getTeam().isSameTeam(team))) {
				setTeam(player.getHeldItem(hand), player.getTeam());
				player.sendMessage(new TextComponentString("Set selector's team to: "+(player.getTeam() == null ? "null" : player.getTeam().getRegisteredName())));
			}
		}*/
		if (world.isRemote && !player.isSneaking()) {
			Minewatch.proxy.openGui(EnumGui.TARGET_SELECTOR);
			return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
		}
		else if (!world.isRemote && player.isSneaking()) {
			setTeam(player.getHeldItem(hand), null);
			return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.world.isRemote) {
			Team team = getTeam(player.world, stack);
			if (team == null && entity.getTeam() != null) {
				try {
					player.world.getScoreboard().removePlayerFromTeams(entity.getCachedUniqueIdString());
				}
				catch (Exception e) {}
				player.sendMessage(new TextComponentString("Cleared "+entity.getName()+"'s team"));
			}
			else if (team != null && !team.isSameTeam(entity.getTeam())) {
				player.world.getScoreboard().addPlayerToTeam(entity.getCachedUniqueIdString(), team.getRegisteredName());
				player.sendMessage(new TextComponentString("Set "+entity.getName()+"'s team to: "+getTeamName(stack)));
			}
			else if (team == null && entity.getTeam() == null)
				player.sendMessage(new TextComponentString(entity.getName()+" is not on a team"));
			else if (team != null && team.isSameTeam(entity.getTeam()))
				player.sendMessage(new TextComponentString(entity.getName()+" is already on team "+getTeamName(stack)));
		}

		return true;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void glowTeams(RenderLivingEvent.Pre<EntityLivingBase> event) {
		boolean glow = false;
		for (ItemStack stack : Minecraft.getMinecraft().player.getHeldEquipment())
			if (stack != null && stack.getItem() == this && event.getEntity().getTeam() != null/* && 
				Minecraft.getMinecraft().player.canEntityBeSeen(event.getEntity())*/)
				glow = true;

		if (glow)
			event.getEntity().setGlowing(true);
		else if (!TickHandler.hasHandler(event.getEntity(), Identifier.SOMBRA_OPPORTUNIST))
			event.getEntity().setGlowing(false);
	}

}