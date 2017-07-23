package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMcCreeBullet;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.armor.ModArmor;
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
	
	/** How long it takes to use or consume an item*/
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 20;
	}
	
	@Override
	public void onShoot(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!worldIn.isRemote) {
			EntityMcCreeBullet bullet = new EntityMcCreeBullet(worldIn, playerIn);
			bullet.setAim(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 2.0F, 0.3F);
			worldIn.spawnEntity(bullet);
			worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, 
					ModSoundEvents.MCCREE_GUN, SoundCategory.PLAYERS, 1.0f, worldIn.rand.nextFloat()/2+0.75f);	
		}
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		if (!player.world.isRemote && player instanceof EntityPlayer) {
			if (Minewatch.keyMode.isKeyDown((EntityPlayer) player) && count % 3 == 0 && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() != Items.AIR && player.getHeldItemMainhand().getItem() instanceof ItemMcCreeGun) {
				EntityMcCreeBullet bullet = new EntityMcCreeBullet(player.world, player);
				bullet.setAim(player, player.rotationPitch, player.rotationYaw, 2.0F, 0.3F);
				player.world.spawnEntity(bullet);				
				player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.MCCREE_GUN, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);	
				if (!ModArmor.isSet((EntityPlayer)player, ModItems.mccree))
					player.getHeldItemMainhand().damageItem(1, player);
			}
			else if (Minewatch.keyMode.isKeyDown((EntityPlayer) player) && count % 3 == 0 && player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() != Items.AIR && player.getHeldItemOffhand().getItem() instanceof ItemMcCreeGun) {
				EntityMcCreeBullet bullet = new EntityMcCreeBullet(player.world, player);
				bullet.setAim(player, player.rotationPitch, player.rotationYaw, 2.0F, 0.3F);
				player.world.spawnEntity(bullet);				
				player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.MCCREE_GUN, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);
				if (!ModArmor.isSet((EntityPlayer)player, ModItems.mccree))
					player.getHeldItemOffhand().damageItem(1, player);
			}

			if (count <= 1)
				doCooldown((EntityPlayer)player, player.getActiveHand());
		}
	}
}
