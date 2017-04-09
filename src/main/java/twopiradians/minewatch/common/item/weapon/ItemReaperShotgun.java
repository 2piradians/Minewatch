package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.EntityReaperBullet;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemReaperShotgun extends ModWeapon
{
	public ItemReaperShotgun() {
		super();
		this.setMaxDamage(100);
		this.hasOffhand = true;
		this.material = ModItems.reaper;
		this.cooldown = 20;
	}

	@Override
	public void onShoot(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		for (int i=0; i<20; i++)
			worldIn.spawnEntity(new EntityReaperBullet(worldIn, playerIn));
		worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, 
				ModSoundEvents.reaperShotgun, SoundCategory.PLAYERS, 1.0f, worldIn.rand.nextFloat()/2+0.75f);	
	}
}
