package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.EntityMcCreeBullet;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemMcCreeGun extends ModWeapon
{
	public ItemMcCreeGun() {
		super();
		this.setMaxDamage(100);
		this.hasOffhand = false;
		this.material = ModItems.mccree;
		this.cooldown = 10;
	}

	@Override
	public void onShoot(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!worldIn.isRemote) {
			worldIn.spawnEntity(new EntityMcCreeBullet(worldIn));
			worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, 
					ModSoundEvents.mccreeGun, SoundCategory.PLAYERS, 1.0f, worldIn.rand.nextFloat()/2+0.75f);	
		}
	}
}
