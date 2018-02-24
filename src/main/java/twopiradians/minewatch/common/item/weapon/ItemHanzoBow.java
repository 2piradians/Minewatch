package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.ability.EntityHanzoScatterArrow;
import twopiradians.minewatch.common.entity.ability.EntityHanzoSonicArrow;
import twopiradians.minewatch.common.entity.projectile.EntityHanzoArrow;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class ItemHanzoBow extends ItemMWWeapon {

	public ItemHanzoBow() {
		super(0);
		this.saveEntityToNBT = true;
		this.noVerticalAimAssist = true;
	}

	private ItemStack findAmmo(EntityLivingBase player) {
		//if (SetManager.entitiesWearingSets.get(player.getPersistentID()) == hero)
		return new ItemStack(Items.ARROW);
		/*else if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND)))
			return player.getHeldItem(EnumHand.OFF_HAND);
		else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND)))
			return player.getHeldItem(EnumHand.MAIN_HAND);
		else {
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
				ItemStack itemstack = player.inventory.getStackInSlot(i);
				if (this.isArrow(itemstack))
					return itemstack;
			}
			return null;
		}*/
	}

	/*private boolean isArrow(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemArrow;
	}*/

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !oldStack.equals(newStack);
	}

	/**Called when the player stops using an Item (stops holding the right mouse button).*/
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase player, int timeLeft) {
		boolean flag = (player instanceof EntityPlayer && ((EntityPlayer)player).capabilities.isCreativeMode) || 
				SetManager.getWornSet(player) == hero;
		ItemStack itemstack = this.findAmmo(player);

		int i = this.getMaxItemUseDuration(stack) - timeLeft;
		if (player instanceof EntityPlayer)
			i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, (EntityPlayer) player, i, itemstack != null || flag);
		if (i < 0) return;

		if (itemstack != null || flag) {
			if (itemstack == null)
				itemstack = new ItemStack(Items.ARROW);

			float f = (float)i / 20.0F;
			f = (f * f + f * 2.0F) / 3.0F;
			if (f > 1.0F)
				f = 1.0F;

			if (f >= 0.05f) {
				boolean flag1 = flag || (itemstack.getItem() instanceof ItemArrow 
						&& player instanceof EntityPlayer && 
						((ItemArrow) itemstack.getItem()).isInfinite(itemstack, stack, (EntityPlayer) player));

				if (!worldIn.isRemote) {
					EntityHanzoArrow entityarrow = null;
					// sonic arrow
					if (hero.ability2.isSelected(player)) {
						entityarrow = new EntityHanzoSonicArrow(worldIn, player);
						entityarrow.setDamage(125 - (125 - 29) * (1f-f));
						EntityHelper.setAim(entityarrow, player, player.rotationPitch, player.rotationYawHead, 100 - (100 - 26) * (1f-f), 0, null, 0, 0);
						hero.ability2.keybind.setCooldown(player, 400, false); 

						ModSoundEvents.HANZO_SONIC_VOICE.playSound(player, 1.0f, 1.0f);
					}
					// scatter arrow
					else if (hero.ability1.isSelected(player)) {
						entityarrow = new EntityHanzoScatterArrow(worldIn, player, true);
						entityarrow.setDamage(75 - (75 - 22) * (1f-f));
						EntityHelper.setAim(entityarrow, player, player.rotationPitch, player.rotationYawHead, 100 - (100 - 26) * (1f-f), 0, null, 0, 0);
						hero.ability1.keybind.setCooldown(player, 200, false); 

						if (worldIn.rand.nextBoolean())
							ModSoundEvents.HANZO_SCATTER_VOICE.playSound(player, 1.0f, 1.0f);
					}
					// regular arrow
					else { 
						entityarrow = new EntityHanzoArrow(worldIn, player);
						entityarrow.setDamage(125 - (125 - 29) * (1f-f));
						EntityHelper.setAim(entityarrow, player, player.rotationPitch, player.rotationYawHead, 100 - (100 - 26) * (1f-f), 0, null, 0, 0);
					}
					stack.damageItem(1, player);
					worldIn.spawnEntityInWorld(entityarrow);
					ModSoundEvents.HANZO_SHOOT.playSound(player, worldIn.rand.nextFloat()+0.5F, worldIn.rand.nextFloat()/2+0.75f);
				}

				if (!flag1 && player instanceof EntityPlayer && !((EntityPlayer)player).capabilities.isCreativeMode) {
					--itemstack.stackSize;

					if (itemstack.stackSize == 0)
						((EntityPlayer)player).inventory.deleteStack(itemstack);
				}
			}
		}
	}

	/** How long it takes to use or consume an item*/
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	/** returns the action that specifies what animation to play when the items is being used */
	@Override
	public EnumAction getItemUseAction(ItemStack stack)	{
		return EnumAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldObj, EntityLivingBase player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);	
		boolean flag = this.findAmmo(player) != null;

		if (player instanceof EntityPlayer) {
			ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(stack, worldObj, (EntityPlayer) player, hand, flag);
			if (ret != null) return ret;
		}

		if (player instanceof EntityPlayer && !((EntityPlayer)player).capabilities.isCreativeMode && !flag) {
			return flag ? new ActionResult<ItemStack>(EnumActionResult.PASS, stack) : new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}
		else if (this.canUse(player, true, hand, false)) {
			player.setActiveHand(hand);
			if (!worldObj.isRemote)
				ModSoundEvents.HANZO_DRAW.playSound(player, 1.0f, worldObj.rand.nextFloat()/2+0.75f);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}
		else
			return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getAllModelLocations(ArrayList<String> locs) {
		for (int i=0; i<5; ++i)
			locs.add("_"+String.valueOf(i));
		return locs;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getModelLocation(ItemStack stack, @Nullable EntityLivingBase entity) {
		int model = 0;
		if (entity != null) {
			model = (int) ((float) (stack.getMaxItemUseDuration() - entity.getItemInUseCount()) / 4.0F) + 1;
			if (entity.getActiveItemStack() == null || !entity.getActiveItemStack().equals(stack))
				model = 0;
			else if (model > 4)
				model = 4;
		}
		return "_"+String.valueOf(model);
	}

}