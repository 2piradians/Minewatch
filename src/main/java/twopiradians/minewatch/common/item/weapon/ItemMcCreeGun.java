package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityMcCreeBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemMcCreeGun extends ItemMWWeapon {

	public static final Handler FAN = new Handler(Identifier.MCCREE_FAN, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// basic checks
			if (entityLiving == null || !entityLiving.isEntityAlive() ||
					entityLiving.getHeldItemMainhand() == null || 
					entityLiving.getHeldItemMainhand().getItem() != EnumHero.MCCREE.weapon ||
					(EnumHero.MCCREE.weapon.getCurrentAmmo(entityLiving) <= 0 && EnumHero.MCCREE.weapon.getMaxAmmo(entityLiving) != 0)) 
				return true;
			else {
				this.ticksLeft = 5;
				entityLiving.rotationPitch = Math.max(entityLiving.rotationPitch-1, -90);
				entityLiving.rotationYaw += entityLiving.world.rand.nextFloat()-0.5f;
			}				
			return super.onClientTick();
		}

		@Override
		public boolean onServerTick() {
			// basic checks
			if (entityLiving == null || !entityLiving.isEntityAlive() ||
					entityLiving.getHeldItemMainhand() == null || 
					entityLiving.getHeldItemMainhand().getItem() != EnumHero.MCCREE.weapon ||
					(EnumHero.MCCREE.weapon.getCurrentAmmo(entityLiving) <= 0 && EnumHero.MCCREE.weapon.getMaxAmmo(entityLiving) != 0)) 
				return true;
			else if (entityLiving.ticksExisted % 2 == 0 && EnumHero.MCCREE.weapon.canUse(entityLiving, true, EnumHand.MAIN_HAND, false)) {
				if (!entityLiving.world.isRemote) {
					EntityMcCreeBullet bullet = new EntityMcCreeBullet(entityLiving.world, entityLiving, EnumHand.MAIN_HAND.ordinal(), true);
					EntityHelper.setAim(bullet, entityLiving, entityLiving.rotationPitch, entityLiving.rotationYawHead, -1, 8F, EnumHand.MAIN_HAND, 10, 0.5f);
					entityLiving.world.spawnEntity(bullet);	
					ModSoundEvents.MCCREE_SHOOT.playSound(entityLiving, entityLiving.world.rand.nextFloat()+0.5F, entityLiving.world.rand.nextFloat()/20+0.95f);
					EnumHero.MCCREE.weapon.subtractFromCurrentAmmo(entityLiving, 1);
					if (entityLiving.world.rand.nextInt(25) == 0)
						entityLiving.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, entityLiving);
				} 
				this.ticksLeft = 5;
			}
			entityLiving.rotationPitch = Math.max(entityLiving.rotationPitch-1, -90);
			entityLiving.rotationYaw += entityLiving.world.rand.nextFloat()-0.5f;

			return super.onServerTick();
		}
		
		@Override
		public Handler onServerRemove() {
			Minewatch.network.sendToDimension(new SPacketSimple(51, entityLiving, false), entityLiving.world.provider.getDimension());
			return super.onServerRemove();
		}
	};

	public static final Handler ROLL = new Handler(Identifier.MCCREE_ROLL, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (this.entityLiving != null) {
				entityLiving.onGround = true;
				if (entityLiving == Minecraft.getMinecraft().player)
					SPacketSimple.move(entityLiving, 0.6d, false, false);
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
				SPacketSimple.move(entityLiving, 0.6d, false, false);
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
		if (this.canUse(player, true, hand, false) && !world.isRemote && !TickHandler.hasHandler(player, Identifier.MCCREE_FAN)) {
			EntityMcCreeBullet bullet = new EntityMcCreeBullet(world, player, hand.ordinal(), false);
			EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, -1, 0.6F, hand, 10, 0.5f);
			world.spawnEntity(bullet);
			ModSoundEvents.MCCREE_SHOOT.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);
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
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		// fan
		if (this.canUse(player, true, hand, false) && !world.isRemote && !TickHandler.hasHandler(player, Identifier.MCCREE_FAN)) {
			TickHandler.register(false, FAN.setEntity(player).setTicks(5));
			Minewatch.network.sendToDimension(new SPacketSimple(51, player, true), world.provider.getDimension());
		}
		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {

	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		// roll
		if (isSelected && entity.onGround && entity instanceof EntityLivingBase && hero.ability2.isSelected((EntityLivingBase) entity) &&
				!world.isRemote && this.canUse((EntityLivingBase) entity, true, getHand((EntityLivingBase) entity, stack), true)) {
			ModSoundEvents.MCCREE_ROLL.playFollowingSound(entity, 1.3f, world.rand.nextFloat()/4f+0.8f, false);
			Minewatch.network.sendToDimension(new SPacketSimple(2, entity, true), world.provider.getDimension());
			if (entity instanceof EntityHero)
				SPacketSimple.move((EntityLivingBase) entity, 0.6d, false, false);
			this.setCurrentAmmo((EntityLivingBase)entity, this.getMaxAmmo((EntityLivingBase) entity));
			TickHandler.register(false, ROLL.setEntity(entity).setTicks(10));
			TickHandler.register(false, Ability.ABILITY_USING.setEntity(entity).setTicks(10).setAbility(hero.ability2));
		}
	}

}