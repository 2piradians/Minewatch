package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.projectile.EntityZenyattaOrb;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class ItemZenyattaWeapon extends ItemMWWeapon {

	public ItemZenyattaWeapon() {
		super(40);
		this.hasOffhand = true;
		//MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false)) {
			if (!world.isRemote) {
				EntityZenyattaOrb orb = new EntityZenyattaOrb(world, player, hand.ordinal());
				EntityHelper.setAim(orb, player, player.rotationPitch, player.rotationYawHead, 80, 0.2F, hand, 22, 0.78f);
				world.spawnEntity(orb);
				ModSoundEvents.ZENYATTA_SHOOT.playFollowingSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f, false);
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0)
					player.getHeldItem(hand).damageItem(1, player);
				this.setCooldown(player, 9);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase) {	
			EntityLivingBase player = (EntityLivingBase) entity;

		}
	}	

}