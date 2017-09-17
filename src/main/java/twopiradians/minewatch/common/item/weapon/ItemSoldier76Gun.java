package twopiradians.minewatch.common.item.weapon;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMWThrowable;
import twopiradians.minewatch.common.entity.EntitySoldier76Bullet;
import twopiradians.minewatch.common.entity.EntitySoldier76HelixRocket;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemSoldier76Gun extends ItemMWWeapon {

	public ItemSoldier76Gun() {
		super(30);
		this.savePlayerToNBT = true;
		this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
			}
		});
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return Integer.MAX_VALUE;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		// shoot
		if (this.canUse(playerIn, true, hand) && hero.ability1.isSelected(playerIn)) {
			if (!worldIn.isRemote) {
				for (int i=1; i<=3; ++i) {
					EntitySoldier76HelixRocket rocket = new EntitySoldier76HelixRocket(worldIn, playerIn, i);
					rocket.setAim(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 2.0F, 0F, 1F, hand, false);
					worldIn.spawnEntityInWorld(rocket);
				}
				hero.ability1.keybind.setCooldown(playerIn, 160, false); 
				worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, ModSoundEvents.soldier76Helix, 
						SoundCategory.PLAYERS, worldIn.rand.nextFloat()+0.5F, worldIn.rand.nextFloat()/20+0.95f);	
				playerIn.getHeldItem(hand).damageItem(1, playerIn);
			}
			else {
				Vec3d vec = EntityMWThrowable.getShootingPos(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesSpark(worldIn, vec.xCoord, vec.yCoord, vec.zCoord, 0x2B9191, 0x2B9191, 8, 3);
			}
		}

		return new ActionResult(EnumActionResult.PASS, playerIn.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		// stop sprinting if right clicking (since onItemRightClick isn't called while blocking)
		if (isSelected && entity instanceof EntityPlayer && Minewatch.keys.rmb((EntityPlayer) entity)) {
			if (entity.isSprinting())
				entity.setSprinting(false);
			this.onItemRightClick(stack, world, (EntityPlayer) entity, EnumHand.MAIN_HAND);
		}

		// block while running
		if (isSelected && entity instanceof EntityPlayer && entity.isSprinting() &&
				((EntityPlayer)entity).getActiveItemStack() != stack) 
			((EntityPlayer)entity).setActiveHand(EnumHand.MAIN_HAND);

		// faster sprint
		if (isSelected && entity.isSprinting() && entity instanceof EntityPlayer) {
			if (!world.isRemote)
				((EntityPlayer)entity).addPotionEffect(new PotionEffect(MobEffects.SPEED, 3, 2, false, false));
			hero.ability3.toggle(entity, true);
		}
		else if (isSelected)
			hero.ability3.toggle(entity, false);
	}	

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (player.isSprinting())
			player.setSprinting(false);

		// shoot
		if (player.ticksExisted % 2 == 0 && this.canUse(player, true, hand)) {
			if (!world.isRemote) {
				EntitySoldier76Bullet bullet = new EntitySoldier76Bullet(world, player);
				bullet.setAim(player, player.rotationPitch, player.rotationYaw, 5.0F, 1.2F, 1F, hand, true);
				world.spawnEntityInWorld(bullet);
				world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.soldier76Shoot, 
						SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/20+0.95f);	
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
			else {
				Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesSpark(world, vec.xCoord, vec.yCoord, vec.zCoord, 0x4AFDFD, 0x4AFDFD, 5, 1);
			}
		}
	}

}