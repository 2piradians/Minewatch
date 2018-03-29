package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityPharahRocket;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemPharahWeapon extends ItemMWWeapon {

	public static final Handler CONCUSSIVE = new Handler(Identifier.PHARAH_CONCUSSIVE, true) {};
	
	public ItemPharahWeapon() {
		super(20);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// primary fire
		if (!world.isRemote && this.canUse(player, true, hand, false)) {
			EntityPharahRocket projectile = new EntityPharahRocket(world, player, hand.ordinal(), false);
			EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 35, 0, hand, 10, 0.31f, true);
			world.spawnEntity(projectile);
			ModSoundEvents.PHARAH_ROCKET_SHOOT.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
			this.subtractFromCurrentAmmo(player, 1, hand);
			if (world.rand.nextInt(25) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			this.setCooldown(player, 18);
		}
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHeldItemMainhand() == stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// jump jet
			if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				player.onGround = false;
				player.motionY = 2;
				// TODO forward
				ModSoundEvents.PHARAH_JET.playFollowingSound(player, 1, 1, false);
				hero.ability2.keybind.setCooldown(player, 200, false);
				Minewatch.network.sendToDimension(new SPacketSimple(82, player, false, player.motionY, 0, 0), world.provider.getDimension());
			}
			// concussive
			else if (!world.isRemote && hero.ability1.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				EntityPharahRocket projectile = new EntityPharahRocket(world, player, EnumHand.OFF_HAND.ordinal(), true);
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 35, 0, EnumHand.OFF_HAND, 10, 0.31f, true);
				world.spawnEntity(projectile);
				ModSoundEvents.PHARAH_ROCKET_SHOOT.playFollowingSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f, false);
				ModSoundEvents.PHARAH_CONCUSSION_VOICE.playFollowingSound(player, 1, 1, false);
				hero.ability1.keybind.setCooldown(player, 240, false);
				Minewatch.network.sendToDimension(new SPacketSimple(81, player, false), world.provider.getDimension());
			}

		}
	}	

	@Override
	@SideOnly(Side.CLIENT)
	public boolean preRenderArmor(EntityLivingBase entity, ModelMWArmor model) {
		// concussive
		if (TickHandler.hasHandler(entity, Identifier.PHARAH_CONCUSSIVE)) {
			model.bipedLeftArmwear.rotateAngleX = 5;
			model.bipedLeftArm.rotateAngleX = 5;
			model.bipedLeftArmwear.rotateAngleY = -0.2f;
			model.bipedLeftArm.rotateAngleY = -0.2f;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderHand(AbstractClientPlayer player, EnumHand hand) {
		return hand == EnumHand.OFF_HAND && TickHandler.hasHandler(player, Identifier.PHARAH_CONCUSSIVE);
	}

}