package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.EntityMcCreeBullet;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemMcCreeGun extends ItemMWWeapon
{
	public ItemMcCreeGun() {
		super(30);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 20;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (!world.isRemote && this.canUse(player, true)) {
			EntityMcCreeBullet bullet = new EntityMcCreeBullet(world, player);
			bullet.setAim(player, player.rotationPitch, player.rotationYaw, 2.0F, 0.3F);
			world.spawnEntityInWorld(bullet);
			world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.mccreeShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
					world.rand.nextFloat()/2+0.75f);	

			this.subtractFromCurrentAmmo(player, 1, hand);
			if (!player.getCooldownTracker().hasCooldown(this))
				player.getCooldownTracker().setCooldown(this, 10);
			if (world.rand.nextInt(25) == 0 && ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) != hero)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player, EnumHand hand) {
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
		if (!entity.worldObj.isRemote && entity instanceof EntityPlayer && count % 2 == 0 && 
				this.canUse((EntityPlayer) entity, true)) {
			EntityMcCreeBullet bullet = new EntityMcCreeBullet(entity.worldObj, entity);
			bullet.setAim(entity, entity.rotationPitch, entity.rotationYaw, 2.0F, 0.3F);
			entity.worldObj.spawnEntityInWorld(bullet);				
			entity.worldObj.playSound(null, entity.posX, entity.posY, entity.posZ, ModSoundEvents.mccreeShoot, 
					SoundCategory.PLAYERS, entity.worldObj.rand.nextFloat()+0.5F, entity.worldObj.rand.nextFloat()/20+0.95f);	
			if (count == this.getMaxItemUseDuration(stack))
				this.subtractFromCurrentAmmo((EntityPlayer) entity, 1, EnumHand.MAIN_HAND);
			else
				this.subtractFromCurrentAmmo((EntityPlayer) entity, 1);			
			if (entity.worldObj.rand.nextInt(25) == 0 && ItemMWArmor.SetManager.playersWearingSets.get(entity.getPersistentID()) != hero)
					entity.getHeldItemMainhand().damageItem(1, entity);
		}
	}
}
