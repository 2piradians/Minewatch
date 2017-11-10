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
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.EntityHanzoArrow;
import twopiradians.minewatch.common.entity.EntityHanzoScatterArrow;
import twopiradians.minewatch.common.entity.EntityHanzoSonicArrow;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class ItemHanzoBow extends ItemMWWeapon {

	public ItemHanzoBow() {
		super(0);
		this.saveEntityToNBT = true;
	}

	private ItemStack findAmmo(EntityPlayer player) {
		//if (ItemMWArmor.SetManager.entitiesWearingSets.get(player.getPersistentID()) == hero)
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
			return ItemStack.EMPTY;
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
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
		if (entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entityLiving;
			boolean flag = player.capabilities.isCreativeMode || 
					ItemMWArmor.SetManager.entitiesWearingSets.get(player.getPersistentID()) == hero;
			ItemStack itemstack = this.findAmmo(player);

			int i = this.getMaxItemUseDuration(stack) - timeLeft;
			i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, player, i, itemstack != null || flag);
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
							&& ((ItemArrow) itemstack.getItem()).isInfinite(itemstack, stack, player));

					if (!worldIn.isRemote) {
						EntityHanzoArrow entityarrow = null;
						// sonic arrow
						if (hero.ability2.isSelected(player)) {
							entityarrow = new EntityHanzoSonicArrow(worldIn, player);
							entityarrow.setDamage(125 - (125 - 29) * (1f-f));
							EntityHelper.setAim(entityarrow, player, player.rotationPitch, player.rotationYaw, 100 - (100 - 26) * (1f-f), 0, null, 0, 0);
							hero.ability2.keybind.setCooldown(player, 400, false); 

							worldIn.playSound(null, player.getPosition(), ModSoundEvents.hanzoSonicArrow, 
									SoundCategory.PLAYERS, 1.0f, 1.0f);
						}
						// scatter arrow
						else if (hero.ability1.isSelected(player)) {
							entityarrow = new EntityHanzoScatterArrow(worldIn, player, true);
							entityarrow.setDamage(75 - (75 - 22) * (1f-f));
							EntityHelper.setAim(entityarrow, player, player.rotationPitch, player.rotationYaw, 100 - (100 - 26) * (1f-f), 0, null, 0, 0);
							hero.ability1.keybind.setCooldown(player, 200, false); 

							if (worldIn.rand.nextBoolean())
								worldIn.playSound(null, player.getPosition(), ModSoundEvents.hanzoScatterArrow, 
										SoundCategory.PLAYERS, 1.0f, 1.0f);
						}
						// regular arrow
						else { 
							entityarrow = new EntityHanzoArrow(worldIn, player);
							entityarrow.setDamage(125 - (125 - 29) * (1f-f));
							EntityHelper.setAim(entityarrow, player, player.rotationPitch, player.rotationYaw, 100 - (100 - 26) * (1f-f), 0, null, 0, 0);
						}
						stack.damageItem(1, player);
						worldIn.spawnEntityInWorld(entityarrow);
					}

					worldIn.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, 
							ModSoundEvents.hanzoShoot, SoundCategory.PLAYERS, 
							worldIn.rand.nextFloat()+0.5F, worldIn.rand.nextFloat()/2+0.75f);

					if (!flag1 && !player.capabilities.isCreativeMode) {
						--itemstack.stackSize;

						if (itemstack.stackSize == 0)
							player.inventory.deleteStack(itemstack); 
					}
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
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand handIn) {
		ItemStack itemstack = player.getHeldItem(handIn);	
		boolean flag = this.findAmmo(player) != null;

		ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, world, player, handIn, flag);
		if (ret != null) return ret;

		if (!player.capabilities.isCreativeMode && !flag) {
			return flag ? new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack) : new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
		}
		else if (this.canUse(player, true, handIn, false)) {
			player.setActiveHand(handIn);
			world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.hanzoDraw, SoundCategory.PLAYERS, 1.0f, world.rand.nextFloat()/2+0.75f);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
		}
		else
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
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