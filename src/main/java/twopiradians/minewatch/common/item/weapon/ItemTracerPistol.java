package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
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
		if (!player.worldObj.isRemote && this.canUse(player, true)) {
			for (int i=0; i<2; i++)
				player.worldObj.spawnEntityInWorld(new EntityTracerBullet(player.worldObj, player, hand));
			player.worldObj.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.tracerShoot, SoundCategory.PLAYERS, 1.0f, player.worldObj.rand.nextFloat()/20+0.95f);	
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(25) == 0 && ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) != hero)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

}