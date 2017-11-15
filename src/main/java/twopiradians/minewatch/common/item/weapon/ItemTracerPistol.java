package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityTracerBullet;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemTracerPistol extends ItemMWWeapon {

	public ItemTracerPistol() {
		super(20);
		this.hasOffhand = true;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && !world.isRemote) {
			for (int i=0; i<2; i++) {
				EntityTracerBullet bullet = new EntityTracerBullet(player.world, player, hand.ordinal());
				EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYaw, -1, 2, hand, 7, 0.58f);
				player.world.spawnEntity(bullet);
			}
			player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.tracerShoot, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);	
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(40) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);
		
		// dash
		if (isSelected && entity instanceof EntityPlayer && (hero.ability2.isSelected((EntityPlayer) entity) || hero.ability2.isSelected((EntityPlayer) entity, Keys.KeyBind.RMB)) &&
				!world.isRemote && this.canUse((EntityPlayer) entity, true, EnumHand.MAIN_HAND, true)) {
			entity.setSneaking(false);
			world.playSound(null, entity.getPosition(), ModSoundEvents.tracerBlink, SoundCategory.PLAYERS, 1.0f, world.rand.nextFloat()/2f+0.75f);
			if (entity instanceof EntityPlayerMP)
				Minewatch.network.sendTo(new SPacketSimple(0), (EntityPlayerMP) entity);
			hero.ability2.subtractUse((EntityPlayer) entity);
			hero.ability2.keybind.setCooldown((EntityPlayer) entity, 3, true); 
		}
	}

}