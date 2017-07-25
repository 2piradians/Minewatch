package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.hero.Hero;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemGenjiShuriken extends ModWeapon
{
	int multiShot = 0;

	public ItemGenjiShuriken() {
		super(Hero.GENJI, 20);
		this.setMaxDamage(100);
		this.hasOffhand = true;
		this.cooldown = 20;
	}

	@Override
	public void onShoot(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!playerIn.world.isRemote) {
			for (int i = 0; i < 3; i++) {
				EntityGenjiShuriken shuriken = new EntityGenjiShuriken(playerIn.world, playerIn);
				shuriken.setAim(playerIn, playerIn.rotationPitch, playerIn.rotationYaw + (1 - i)*15, 3F, 1.0F);
				playerIn.world.spawnEntity(shuriken);
			}
			playerIn.world.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, 
					ModSoundEvents.genjiShuriken, SoundCategory.PLAYERS, 1.0f, playerIn.world.rand.nextFloat()/2+0.75f);	
		}
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		if (!player.world.isRemote && count < 4) {
			EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player);
			shuriken.setAim(player, player.rotationPitch, player.rotationYaw, 3F, 1.0F);
			player.world.spawnEntity(shuriken);
			player.world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.genjiShuriken, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/2+0.75f);	
		}
		else if (count > 2)
			this.multiShot = 0;
	}
}
