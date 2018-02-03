package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

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
import twopiradians.minewatch.common.entity.ability.EntitySoldier76Heal;
import twopiradians.minewatch.common.entity.ability.EntitySoldier76HelixRocket;
import twopiradians.minewatch.common.entity.projectile.EntitySoldier76Bullet;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class ItemSoldier76Gun extends ItemMWWeapon {

	/**Map of player uuids and the number of bullets they shot recently*/
	private static HashMap<UUID, Integer> spreads = Maps.newHashMap();

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
	public ActionResult<ItemStack> onItemRightClick(World worldObj, EntityLivingBase player, EnumHand hand) {
		// helix rockets
		if (this.canUse(player, true, hand, true) && hero.ability1.isSelected(player, true)) {
			if (!worldObj.isRemote) {
				for (int i=1; i<=3; ++i) {
					EntitySoldier76HelixRocket rocket = new EntitySoldier76HelixRocket(worldObj, player, hand.ordinal(), i);
					EntityHelper.setAim(rocket, player, player.rotationPitch, player.rotationYawHead, 50, 0, hand, 12, 0.45f);
					worldObj.spawnEntityInWorld(rocket);
				}
				hero.ability1.keybind.setCooldown(player, 160, false);
				ModSoundEvents.SOLDIER76_HELIX.playSound(player, worldObj.rand.nextFloat()+0.5F, worldObj.rand.nextFloat()/20+0.95f);	
				player.getHeldItem(hand).damageItem(1, player);
			}
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World worldObj, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, worldObj, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHeldItemMainhand() == stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// heal 
			if (!worldObj.isRemote && hero.ability2.isSelected(player, player instanceof EntityPlayer) && 
					this.canUse((EntityLivingBase) entity, true, EnumHand.MAIN_HAND, true)) {
				EntitySoldier76Heal projectile = new EntitySoldier76Heal(worldObj, player);
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 0, 0F, EnumHand.OFF_HAND, 40, 0.15f, 0.5f);
				worldObj.spawnEntityInWorld(projectile);
				ModSoundEvents.SOLDIER76_HEAL_THROW.playFollowingSound(player, worldObj.rand.nextFloat()+0.5F, worldObj.rand.nextFloat()/2+0.75f, false);
				ModSoundEvents.SOLDIER76_HEAL_VOICE.playFollowingSound(player, 1, 1, false);
				hero.ability2.keybind.setCooldown(player, 300, false); 
			}
		}

		// decrease spread
		if (!worldObj.isRemote && spreads.containsKey(entity.getPersistentID()) && entity instanceof EntityLivingBase && 
				(!KeyBind.LMB.isKeyDown((EntityLivingBase) entity) || this.getCurrentAmmo(entity) == 0)) {
			if (spreads.get(entity.getPersistentID()) > 1)
				spreads.put(entity.getPersistentID(), spreads.get(entity.getPersistentID())-1);
			else
				spreads.remove(entity.getPersistentID());
		}

		// stop sprinting if right clicking (since onItemRightClick isn't called while blocking)
		if (isSelected && entity instanceof EntityLivingBase && KeyBind.RMB.isKeyDown((EntityLivingBase) entity)) {
			if (entity.isSprinting())
				entity.setSprinting(false);
			this.onItemRightClick(worldObj, (EntityLivingBase) entity, EnumHand.MAIN_HAND);
		}

		// block while running
		if (isSelected && entity instanceof EntityLivingBase && entity.isSprinting() &&
				((EntityLivingBase)entity).getActiveItemStack() != stack) 
			((EntityLivingBase)entity).setActiveHand(EnumHand.MAIN_HAND);

		// faster sprint
		if (isSelected && entity.isSprinting() && entity instanceof EntityLivingBase && 
				SetManager.getWornSet((EntityLivingBase) entity) == hero) {
			if (!worldObj.isRemote)
				((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.SPEED, 3, entity instanceof EntityPlayer ? 2 : 0, false, false));
			hero.ability3.toggle(entity, true);
		}
		else if (isSelected)
			hero.ability3.toggle(entity, false);
	}	

	@Override
	public void onItemLeftClick(ItemStack stack, World worldObj, EntityLivingBase player, EnumHand hand) { 
		if (player.isSprinting())
			player.setSprinting(false);

		// shoot
		if (this.canUse(player, true, hand, false) && !worldObj.isRemote) {
			spreads.put(player.getPersistentID(), spreads.containsKey(player.getPersistentID()) ? spreads.get(player.getPersistentID())+1 : 1);
			EntitySoldier76Bullet bullet = new EntitySoldier76Bullet(worldObj, player, hand.ordinal());
			int spread = spreads.get(player.getPersistentID());
			EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, -1, spread < 5 ? 0.8f : 2.4F, hand, 12, 0.45f);
			worldObj.spawnEntityInWorld(bullet);
			ModSoundEvents.SOLDIER76_SHOOT.playSound(player, worldObj.rand.nextFloat()+0.5F, worldObj.rand.nextFloat()/20+0.95f);
			this.subtractFromCurrentAmmo(player, 1);
			if (worldObj.rand.nextInt(25) == 0)
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