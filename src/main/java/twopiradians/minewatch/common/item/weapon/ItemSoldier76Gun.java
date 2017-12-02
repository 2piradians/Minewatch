package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.projectile.EntitySoldier76Bullet;
import twopiradians.minewatch.common.entity.projectile.EntitySoldier76HelixRocket;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class ItemSoldier76Gun extends ItemMWWeapon {

	public ItemSoldier76Gun() {
		super(30);
		this.saveEntityToNBT = true;
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
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		// helix rockets
		if (this.canUse(player, true, hand, true) && hero.ability1.isSelected(player)) {
			if (!world.isRemote) {
				for (int i=1; i<=3; ++i) {
					EntitySoldier76HelixRocket rocket = new EntitySoldier76HelixRocket(world, player, hand.ordinal(), i);
					EntityHelper.setAim(rocket, player, player.rotationPitch, player.rotationYawHead, 50, 0, hand, 12, 0.45f);
					world.spawnEntity(rocket);
				}
				hero.ability1.keybind.setCooldown(player, 160, false);
				ModSoundEvents.SOLDIER76_HELIX.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/20+0.95f);	
				player.getHeldItem(hand).damageItem(1, player);
			}
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		// stop sprinting if right clicking (since onItemRightClick isn't called while blocking)
		if (isSelected && entity instanceof EntityLivingBase && KeyBind.RMB.isKeyDown((EntityLivingBase) entity)) {
			if (entity.isSprinting())
				entity.setSprinting(false);
			this.onItemRightClick(world, (EntityLivingBase) entity, EnumHand.MAIN_HAND);
		}

		// block while running
		if (isSelected && entity instanceof EntityLivingBase && entity.isSprinting() &&
				((EntityLivingBase)entity).getActiveItemStack() != stack) 
			((EntityLivingBase)entity).setActiveHand(EnumHand.MAIN_HAND);

		// faster sprint
		if (isSelected && entity.isSprinting() && entity instanceof EntityLivingBase && 
				ItemMWArmor.SetManager.getWornSet((EntityLivingBase) entity) == hero) {
			if (!world.isRemote)
				((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.SPEED, 3, entity instanceof EntityPlayer ? 2 : 0, false, false));
			hero.ability3.toggle(entity, true);
		}
		else if (isSelected)
			hero.ability3.toggle(entity, false);
	}	

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		if (player.isSprinting())
			player.setSprinting(false);

		// shoot TODO add continual usage spread
		if (this.canUse(player, true, hand, false) && !world.isRemote) {
			EntitySoldier76Bullet bullet = new EntitySoldier76Bullet(world, player, hand.ordinal());
			EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, -1, 2.4F, hand, 12, 0.45f);
			world.spawnEntity(bullet);
			ModSoundEvents.SOLDIER76_SHOOT.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/20+0.95f);
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(25) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			this.setCooldown(player, 1);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getAllModelLocations(ArrayList<String> locs) {
		locs.add("_blocking");
		return super.getAllModelLocations(locs);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getModelLocation(ItemStack stack, @Nullable EntityLivingBase entity) {
		boolean blocking = entity != null ? entity.isSprinting() : false;
		return blocking ? "_blocking" : "";
	}

}