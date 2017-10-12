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
import twopiradians.minewatch.common.entity.EntityBastionBullet;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class ItemBastionGun extends ItemMWWeapon {
	
	public ItemBastionGun() {
		super(40);
		this.savePlayerToNBT = true;
		/*this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return entityIn == null ? 0.0F : (!(entityIn.getActiveItemStack().getItem() instanceof ItemHanzoBow) ? 0.0F :
					(float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 10.0F);
			}
		});*/
	}
	
	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false)) {
			if (!world.isRemote) {
				EntityBastionBullet bullet = new EntityBastionBullet(world, player);
				EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYaw, -1, 0.6F, hand);
				world.spawnEntity(bullet);
				world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.bastionShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
						world.rand.nextFloat()/3+0.8f);	
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0)
					player.getHeldItem(hand).damageItem(1, player);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 3);
			}
			else {
				Vec3d vec = EntityHelper.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, vec.xCoord, vec.yCoord, vec.zCoord, 
						0, 0, 0, 0xFFEF89, 0x5A575A, 0.7f, 1, 5, 4.5f, 0, 0);
			}
		}
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

	}	
	
}
