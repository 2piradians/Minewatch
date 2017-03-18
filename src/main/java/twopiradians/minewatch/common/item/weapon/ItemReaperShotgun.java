package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.EntityReaperPellet;

public class ItemReaperShotgun extends ModWeapons
{
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!playerIn.getCooldownTracker().hasCooldown(playerIn.getHeldItem(hand).getItem())
				&& playerIn.getHeldItem(hand).getTagCompound().getInteger("cooldown") < 0) {	
			if (!worldIn.isRemote)
				for (int i=0; i<20; i++)
					worldIn.spawnEntity(new EntityReaperPellet(worldIn, playerIn));
			playerIn.getCooldownTracker().setCooldown(playerIn.getHeldItem(hand).getItem(), 10);
			playerIn.getHeldItem(hand).getTagCompound().setInteger("cooldown", 20);
			worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, 
					SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 0.2f, worldIn.rand.nextFloat()+0.7f);	
			return new ActionResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
		}
		else
			return new ActionResult(EnumActionResult.PASS, playerIn.getHeldItem(hand));	
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {	
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		int cooldown = stack.getTagCompound().hasKey("cooldown") ? stack.getTagCompound().getInteger("cooldown") : 0;
		if (stack.getTagCompound().getInteger("cooldown") >= 0)
			stack.getTagCompound().setInteger("cooldown", --cooldown);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return oldStack.getItem() != newStack.getItem();
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}
}
