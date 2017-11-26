package twopiradians.minewatch.common.item.weapon;

import java.util.HashSet;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityLucioSonic;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class ItemLucioSoundAmplifier extends ItemMWWeapon {
	
	public static final Handler SONIC = new Handler(Identifier.LUCIO_SONIC, true) {
		@Override
		public boolean onServerTick() {
			if (this.ticksLeft < this.initialTicks && this.ticksLeft % 2 == 0 && entityLiving != null && entityLiving.getHeldItemMainhand() != null && 
					entityLiving.getHeldItemMainhand().getItem() == EnumHero.LUCIO.weapon && 
					EnumHero.LUCIO.weapon.canUse(entityLiving, true, EnumHand.MAIN_HAND, false)) {
				EntityLucioSonic sonic = new EntityLucioSonic(entityLiving.world, entityLiving, EnumHand.MAIN_HAND.ordinal());
				EntityHelper.setAim(sonic, entityLiving, entityLiving.rotationPitch, entityLiving.rotationYawHead, 50f, 0, EnumHand.MAIN_HAND, 12, 0.15f);
				entityLiving.world.spawnEntity(sonic);
				if (this.ticksLeft >= this.initialTicks - 2)
					Minewatch.proxy.playFollowingSound(entityLiving, ModSoundEvents.lucioShoot, SoundCategory.PLAYERS, entityLiving.world.rand.nextFloat()+0.5F, entityLiving.world.rand.nextFloat()/20+0.95f, false);
				EnumHero.LUCIO.weapon.subtractFromCurrentAmmo(entityLiving, 1);
				if (entityLiving.world.rand.nextInt(25) == 0)
					entityLiving.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, entityLiving);
			}
			return super.onServerTick();
		}

		@Override
		public Handler onServerRemove() {
			EnumHero.LUCIO.weapon.setCooldown(entityLiving, 10);
			return super.onServerRemove();
		}
	};

	public ItemLucioSoundAmplifier() {
		super(30);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		/*// helix rockets
		if (this.canUse(player, true, hand, true) && hero.ability1.isSelected(player)) {
			if (!world.isRemote) {
				for (int i=1; i<=3; ++i) {
					EntitySoldier76HelixRocket rocket = new EntitySoldier76HelixRocket(world, player, hand.ordinal(), i);
					EntityHelper.setAim(rocket, player, player.rotationPitch, player.rotationYawHead, 50, 0, hand, 12, 0.45f);
					world.spawnEntity(rocket);
				}
				hero.ability1.keybind.setCooldown(player, 160, false);
				world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.soldier76Helix, 
						SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/20+0.95f);	
				player.getHeldItem(hand).damageItem(1, player);
			}
		}*/

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);
		
		if (isSelected && entity instanceof EntityLivingBase && 
				KeyBind.ABILITY_1.isKeyPressed((EntityLivingBase) entity) && 
				ItemMWArmor.SetManager.getWornSet((EntityLivingBase) entity) == hero) {
			
		}
		
		/*
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
			hero.ability3.toggle(entity, false);*/
	}	

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		if (this.canUse(player, true, hand, false) && !world.isRemote && !TickHandler.hasHandler(player, Identifier.LUCIO_SONIC)) {
			TickHandler.register(false, SONIC.setEntity(player).setTicks(10));
		}
	}

}