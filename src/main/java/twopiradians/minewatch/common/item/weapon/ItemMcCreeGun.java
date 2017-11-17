package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityMcCreeBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemMcCreeGun extends ItemMWWeapon {

	public static final Handler ROLL = new Handler(Identifier.MCCREE_ROLL, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (this.entityLiving != null) {
				entityLiving.onGround = true;
				if (entityLiving == Minecraft.getMinecraft().player)
					SPacketSimple.move(entityLiving, 0.6d, false);
				if (this.ticksLeft % 3 == 0 && this.ticksLeft > 2)
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.SMOKE, entityLiving.world, 
							entityLiving.prevPosX+entityLiving.world.rand.nextDouble()-0.5d, 
							entityLiving.prevPosY+entityLiving.world.rand.nextDouble(), 
							entityLiving.prevPosZ+entityLiving.world.rand.nextDouble()-0.5d, 
							0, 0, 0, 0xB4907B, 0xE6C4AC, 0.7f, 10, 15+entityLiving.world.rand.nextInt(5), 15+entityLiving.world.rand.nextInt(5), 0, 0);
			}
			return super.onClientTick();
		}
		
		@Override
		public boolean onServerTick() {
			if (this.entityLiving instanceof EntityHero) {
				((EntityHero) this.entityLiving).getMoveHelper().action = EntityMoveHelper.Action.WAIT;
				SPacketSimple.move(entityLiving, 0.6d, false);
			}
			return super.onServerTick();
		}

		@Override
		public Handler onServerRemove() {
			EnumHero.MCCREE.ability2.keybind.setCooldown(this.entityLiving, 160, false);
			return super.onServerRemove();
		}
	};

	public ItemMcCreeGun() {
		super(30);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 20;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && !world.isRemote) {
			EntityMcCreeBullet bullet = new EntityMcCreeBullet(world, player, hand.ordinal(), false);
			EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYaw, -1, 0.6F, hand, 10, 0.5f);
			world.spawnEntity(bullet);
			world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.mccreeShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
					world.rand.nextFloat()/2+0.75f);	

			this.subtractFromCurrentAmmo(player, 1, hand);
			this.setCooldown(player, 9);
			if (world.rand.nextInt(6) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return (Minewatch.proxy.getClientPlayer() != null && 
				TickHandler.hasHandler(Minewatch.proxy.getClientPlayer(), Identifier.MCCREE_ROLL)) ? 
						true : super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityLivingBase player, EnumHand hand) {
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
		// Fan the Hammer
		if (entity != null && count % 2 == 0 && this.canUse(entity, true, getHand(entity, stack), false)) {
			EnumHand hand = null;
			for (EnumHand hand2 : EnumHand.values())
				if (entity.getHeldItem(hand2) == stack)
					hand = hand2;
			if (!entity.world.isRemote && hand != null) {
				EntityMcCreeBullet bullet = new EntityMcCreeBullet(entity.world, entity, hand.ordinal(), true);
				EntityHelper.setAim(bullet, entity, entity.rotationPitch, entity.rotationYawHead, -1, 3F, hand, 10, 0.5f);
				entity.world.spawnEntity(bullet);				
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, ModSoundEvents.mccreeShoot, 
						SoundCategory.PLAYERS, entity.world.rand.nextFloat()+0.5F, entity.world.rand.nextFloat()/20+0.95f);	
				if (count == this.getMaxItemUseDuration(stack))
					this.subtractFromCurrentAmmo(entity, 1, hand);
				else
					this.subtractFromCurrentAmmo(entity, 1);
				if (entity.world.rand.nextInt(25) == 0)
					entity.getHeldItem(hand).damageItem(1, entity);
			} 
			else 
				entity.rotationPitch--;
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		// roll
		if (isSelected && entity.onGround && entity instanceof EntityLivingBase && hero.ability2.isSelected((EntityLivingBase) entity) &&
				!world.isRemote && this.canUse((EntityLivingBase) entity, true, getHand((EntityLivingBase) entity, stack), true)) {
			world.playSound(null, entity.getPosition(), ModSoundEvents.mccreeRoll, SoundCategory.PLAYERS, 1.3f, world.rand.nextFloat()/4f+0.8f);
			Minewatch.network.sendToDimension(new SPacketSimple(2, entity, true), world.provider.getDimension());
			if (entity instanceof EntityHero)
				SPacketSimple.move((EntityLivingBase) entity, 0.6d, false);
			this.setCurrentAmmo((EntityLivingBase)entity, this.getMaxAmmo((EntityLivingBase) entity));
			TickHandler.register(false, ROLL.setEntity(entity).setTicks(10));
			TickHandler.register(false, Ability.ABILITY_USING.setEntity(entity).setTicks(10).setAbility(hero.ability2));
		}
	}

}