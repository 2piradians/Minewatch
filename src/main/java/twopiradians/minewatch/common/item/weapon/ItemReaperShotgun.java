package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityReaperBullet;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemReaperShotgun extends ItemMWWeapon
{
	public ItemReaperShotgun() {
		super(30);
		this.hasOffhand = true;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (this.canUse(player, true)) {
			if (!world.isRemote) {
				for (int i=0; i<20; i++) {
					EntityReaperBullet bullet = new EntityReaperBullet(world, player, hand);
					bullet.setAim(player, player.rotationPitch, player.rotationYaw, 3.0F, 4F, hand, false);
					world.spawnEntity(bullet);
				}
				world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.reaperShoot, SoundCategory.PLAYERS, 
						world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);	

				this.subtractFromCurrentAmmo(player, 1, hand);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 11);
				if (world.rand.nextInt(25) == 0 && ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) != hero)
					player.getHeldItem(hand).damageItem(1, player);
			}
			else {
				Vec3d look = player.getLookVec().addVector(player.posX, player.posY+player.eyeHeight, player.posZ);
				Minewatch.proxy.spawnParticlesSmoke(world, look.xCoord, look.yCoord, look.zCoord, 0xD93B1A, 0x510D30, 5, 5);
			}
		}
	}

}
