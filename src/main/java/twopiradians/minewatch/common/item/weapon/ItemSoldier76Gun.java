package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.EntitySoldierBullet;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemSoldier76Gun extends ItemMWWeapon
{	
	public ItemSoldier76Gun() {
		super(30);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (!world.isRemote && player.ticksExisted % 2 == 0 && this.canUse(player, true)) {
			world.spawnEntity(new EntitySoldierBullet(world, player, EnumHand.MAIN_HAND));
			world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.soldierGun, 
					SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/20+0.95f);	
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(25) == 0 && !(ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) == hero))
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

}