package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import twopiradians.minewatch.common.item.armor.ModArmor;

public class ModWeapon extends Item
{
	/**Used to uniformly scale damage for all weapons/abilities*/
	public static final float DAMAGE_SCALE = 10f;

	/**Cooldown in ticks for MC cooldown and nbt cooldown (if hasOffhand)*/
	protected int cooldown;
	/**ArmorMaterial that determines which set this belongs to*/
	protected ArmorMaterial material;
	/**Will give shooting hand MC cooldown = cooldown/2 if true*/
	protected boolean hasOffhand;

	/**Called when right click is held and cooldown is not active*/
	protected void onShoot(World worldIn, EntityPlayer playerIn, EnumHand hand) {}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		// check that item does not have MC cooldown (and nbt cooldown if it hasOffhand)
		if (cooldown > 0 && playerIn != null && playerIn.getHeldItem(hand) != null && 
				!playerIn.getCooldownTracker().hasCooldown(playerIn.getHeldItem(hand).getItem()) && 
				(!hasOffhand || (playerIn.getHeldItem(hand).hasTagCompound() && 
						playerIn.getHeldItem(hand).getTagCompound().getInteger("cooldown") <= 0))) {	
			if (!worldIn.isRemote) {
				onShoot(worldIn, playerIn, hand);
				// set MC cooldown/2 and nbt cooldown if hasOffhand, otherwise just set MC cooldown
				if (hasOffhand) {
					playerIn.getCooldownTracker().setCooldown(playerIn.getHeldItem(hand).getItem(), cooldown/2);
					playerIn.getHeldItem(hand).getTagCompound().setInteger("cooldown", cooldown);
				}
				else 
					playerIn.getCooldownTracker().setCooldown(playerIn.getHeldItem(hand).getItem(), cooldown);
				// only damage item if 
				if (!ModArmor.isSet(playerIn, material))
					playerIn.getHeldItem(hand).damageItem(1, playerIn);
			}
			return new ActionResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
		}
		else
			return new ActionResult(EnumActionResult.PASS, playerIn.getHeldItem(hand));	
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {	
		if (cooldown > 0 && hasOffhand && !worldIn.isRemote) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			int cooldown = stack.getTagCompound().getInteger("cooldown");
			if (stack.getTagCompound().getInteger("cooldown") > 0)
				stack.getTagCompound().setInteger("cooldown", --cooldown);
		}
	}

	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return oldStack.getItem() != newStack.getItem();
	}

	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	public int getItemEnchantability() {
		return 0;
	}
}
