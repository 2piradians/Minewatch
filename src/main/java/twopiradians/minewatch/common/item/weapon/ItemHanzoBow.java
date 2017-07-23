package twopiradians.minewatch.common.item.weapon;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.EntityHanzoArrow;
import twopiradians.minewatch.common.hero.Hero;
import twopiradians.minewatch.common.item.armor.ModArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemHanzoBow extends ModWeapon
{
	public ItemHanzoBow() {
		super(Hero.HANZO);
		this.setMaxDamage(100);
		this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return entityIn == null ? 0.0F : (!(entityIn.getActiveItemStack().getItem() instanceof ItemHanzoBow) ? 0.0F :
					(float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 10.0F);
			}
		});
		this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return  entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
			}
		});
	}

	private ItemStack findAmmo(EntityPlayer player) {
		if (ModArmor.SetManager.playersWearingSets.get(player.getPersistentID()) == hero)
			return new ItemStack(Items.ARROW);
		else if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND)))
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
		}
	}

	private boolean isArrow(ItemStack stack) {
		return stack.getItem() instanceof ItemArrow;
	}

	/**Called when the player stops using an Item (stops holding the right mouse button).*/
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
		if (entityLiving instanceof EntityPlayer) {
			EntityPlayer entityplayer = (EntityPlayer)entityLiving;
			boolean flag = entityplayer.capabilities.isCreativeMode || 
					ModArmor.SetManager.playersWearingSets.get(entityplayer.getPersistentID()) == hero;
			ItemStack itemstack = this.findAmmo(entityplayer);

			int i = Math.min(this.getMaxItemUseDuration(stack) - timeLeft,20);
			i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, entityplayer, i, !itemstack.isEmpty() || flag);
			if (i < 0) return;

			if (!itemstack.isEmpty() || flag) {
				if (itemstack.isEmpty())
					itemstack = new ItemStack(Items.ARROW);

				float f = (float)i/10;

				if (f >= 0.1f) {
					boolean flag1 = flag || (itemstack.getItem() instanceof ItemArrow 
							&& ((ItemArrow) itemstack.getItem()).isInfinite(itemstack, stack, entityplayer));

					if (!worldIn.isRemote) {
						EntityHanzoArrow entityarrow = new EntityHanzoArrow(worldIn, entityplayer);
						if (itemstack.getItem() instanceof ItemArrow)
							entityarrow.setPotionEffect(itemstack);
						entityarrow.setAim(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 0.0F, f * 2.0F, 1.0F);
						entityarrow.setDamage(125*((double)i/80/DAMAGE_SCALE));
						if (ModArmor.SetManager.playersWearingSets.get(entityplayer.getPersistentID()) != hero)
							stack.damageItem(1, entityplayer);
						worldIn.spawnEntity(entityarrow);
					}

					worldIn.playSound((EntityPlayer)null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, 
							ModSoundEvents.hanzoBowShoot, SoundCategory.PLAYERS, 1.0F, worldIn.rand.nextFloat()/2+0.75f);

					if (!flag1 && !entityplayer.capabilities.isCreativeMode) {
						itemstack.shrink(1);

						if (itemstack.isEmpty())
							entityplayer.inventory.deleteStack(itemstack);
					}
				}
			}
		}
	}

	/** How long it takes to use or consume an item*/
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 100;
	}

	/** returns the action that specifies what animation to play when the items is being used */
	@Override
	public EnumAction getItemUseAction(ItemStack stack)	{
		return EnumAction.BOW;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {	
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
				
		// set player in nbt for model changer (in ModItems) to reference
		if (entityIn instanceof EntityPlayer && !entityIn.world.isRemote && 
				stack != null && stack.getItem() instanceof ItemHanzoBow) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			NBTTagCompound nbt = stack.getTagCompound();
			
			if (!nbt.hasKey("player") || !nbt.getUniqueId("player").equals(entityIn.getPersistentID())) {
				nbt.setUniqueId("player", entityIn.getPersistentID());
				stack.setTagCompound(nbt);
			}
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);	
		boolean flag = !this.findAmmo(playerIn).isEmpty();

		ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
		if (ret != null) return ret;

		if (!playerIn.capabilities.isCreativeMode && !flag) {
			return flag ? new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack) : new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
		}
		else {
			playerIn.setActiveHand(handIn);
			worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, 
					ModSoundEvents.hanzoBowDraw, SoundCategory.PLAYERS, 1.0f, worldIn.rand.nextFloat()/2+0.75f);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
		}
	}
}
