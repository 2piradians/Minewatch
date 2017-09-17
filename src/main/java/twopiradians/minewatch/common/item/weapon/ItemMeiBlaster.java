package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMWThrowable;
import twopiradians.minewatch.common.entity.EntityMeiBlast;
import twopiradians.minewatch.common.entity.EntityMeiIcicle;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemMeiBlaster extends ItemMWWeapon {

	public ItemMeiBlaster() {
		super(30);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (this.canUse(player, true, hand) && !world.isRemote) {
			EntityMeiBlast bullet = new EntityMeiBlast(world, player);
			bullet.setAim(player, player.rotationPitch, player.rotationYaw, 2F, 0.3F, 2.5F, hand, false);
			world.spawnEntityInWorld(bullet);
			world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.meiShoot, SoundCategory.PLAYERS, world.rand.nextFloat()/3, 
					world.rand.nextFloat()/2+0.75f);	

			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(200) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		// shoot
		if (this.canUse(playerIn, true, hand)) {
			if (!worldIn.isRemote) {
				EntityMeiIcicle icicle = new EntityMeiIcicle(worldIn, playerIn);
				icicle.setAim(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 2F, 0.2F, 0F, hand, false);
				worldIn.spawnEntityInWorld(icicle);
				if (!playerIn.getCooldownTracker().hasCooldown(this))
					playerIn.getCooldownTracker().setCooldown(this, 24);
				worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, ModSoundEvents.meiIcicleShoot, 
						SoundCategory.PLAYERS, worldIn.rand.nextFloat()+0.5F, worldIn.rand.nextFloat()/20+0.95f);	
				if (worldIn.rand.nextInt(8) == 0)
					playerIn.getHeldItem(hand).damageItem(1, playerIn);
				this.subtractFromCurrentAmmo(playerIn, 25, hand);
			}
			else {
				Vec3d vec = EntityMWThrowable.getShootingPos(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesSpark(worldIn, vec.xCoord, vec.yCoord, vec.zCoord, 0x2B9191, 0x2B9191, 3, 3);
			}
		}

		return new ActionResult(EnumActionResult.PASS, playerIn.getHeldItem(hand));
	}

}
