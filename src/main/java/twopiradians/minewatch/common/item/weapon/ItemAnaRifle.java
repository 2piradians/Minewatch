package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityAnaBullet;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemAnaRifle extends ModWeapon 
{
	public ItemAnaRifle() {
		super();
		this.setMaxDamage(100);
		this.material = ModItems.ana;
		this.scope = new ResourceLocation(Minewatch.MODID + ":textures/gui/ana_scope.png");
	}

	@Override
	public void onShoot(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		EntityAnaBullet bullet = new EntityAnaBullet(worldIn, playerIn);
		bullet.setAim(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 5.0F, 1.0F);
		worldIn.spawnEntity(bullet);
		worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, 
				ModSoundEvents.reaperShotgun, SoundCategory.PLAYERS, 1.0f, worldIn.rand.nextFloat()/2+0.75f);	
	}
}
