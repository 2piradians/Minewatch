package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
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
		// shoot
		if (this.canUse(player, true, hand, false) && !world.isRemote) {
			EntityMeiBlast bullet = new EntityMeiBlast(world, player);
			bullet.setAim(player, player.rotationPitch, player.rotationYaw, 2F, 0.3F, 2.5F, hand, false);
			world.spawnEntity(bullet);
			world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.meiShoot, SoundCategory.PLAYERS, world.rand.nextFloat()/3, 
					world.rand.nextFloat()/2+0.75f);	

			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(200) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		// shoot
		if (this.canUse(player, true, hand, false)) {
			if (!world.isRemote) {
				EntityMeiIcicle icicle = new EntityMeiIcicle(world, player);
				icicle.setAim(player, player.rotationPitch, player.rotationYaw, 2F, 0.2F, 0F, hand, false);
				world.spawnEntity(icicle);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 24);
				world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.meiIcicleShoot, 
						SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/20+0.95f);	
				if (world.rand.nextInt(8) == 0)
					player.getHeldItem(hand).damageItem(1, player);
				this.subtractFromCurrentAmmo(player, 25, hand);
			}
			else {
				Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, vec.xCoord, vec.yCoord, vec.zCoord, 
						0, 0, 0, 0x2B9191, 0x2B9191, 0.7f, 3, 3, 2.5f, world.rand.nextFloat(), 0.01f);
			}
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

}
