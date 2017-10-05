package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityJunkratGrenade;
import twopiradians.minewatch.common.entity.EntityMWThrowable;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemJunkratLauncher extends ItemMWWeapon {
	
	public ItemJunkratLauncher() {
		super(30);
	}
	
	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false)) {
			if (!world.isRemote) {
				EntityJunkratGrenade grenade = new EntityJunkratGrenade(world, player);
				grenade.setAim(player, player.rotationPitch, player.rotationYaw, 1.5F, 0.3F, 2F, hand, false);
				world.spawnEntity(grenade);
				world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.junkratShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
						world.rand.nextFloat()/3+0.8f);	
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0)
					player.getHeldItem(hand).damageItem(1, player);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 12);
				if (world.rand.nextInt(20) == 0)
					Minewatch.proxy.playFollowingSound(player, ModSoundEvents.junkratLaugh, SoundCategory.PLAYERS, 1f, 1.0f, false);
			}
			else {
				Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, vec.xCoord, vec.yCoord, vec.zCoord,
						0, 0, 0, 0xFF9D1A, 0x964D21, 0.7f, 5, 5, 4.5f, world.rand.nextFloat(), 0.01f);
			}
		}
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

	}	
	
}
