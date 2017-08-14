package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityTracerBullet;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemTracerPistol extends ItemMWWeapon {

	public ItemTracerPistol() {
		super(20);
		this.hasOffhand = true;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (this.canUse(player, true)) {
			if (!world.isRemote) {
				for (int i=0; i<2; i++) {
					EntityTracerBullet bullet = new EntityTracerBullet(player.world, player, hand);
					bullet.setAim(player, player.rotationPitch, player.rotationYaw, 2F, 1.0F, hand, false);
					player.world.spawnEntity(bullet);
				}
				player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.tracerShoot, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);	
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0 && ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) != hero)
					player.getHeldItem(hand).damageItem(1, player);
			}
			else {
				Vec3d look = player.getLookVec().scale(1.2d).addVector(player.posX, player.posY+player.eyeHeight, player.posZ);
				Minewatch.proxy.spawnParticlesSpark(world, look.xCoord, look.yCoord, look.zCoord, 0x4AFDFD, 0x4AFDFD, 3, 1);
			}
		}
	}

}