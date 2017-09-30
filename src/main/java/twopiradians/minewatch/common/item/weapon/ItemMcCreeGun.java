package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMWThrowable;
import twopiradians.minewatch.common.entity.EntityMcCreeBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemMcCreeGun extends ItemMWWeapon {

	public static final Handler ROLL = new Handler(Identifier.MCCREE_ROLL, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			player.onGround = true;
			if (player == Minecraft.getMinecraft().player)
				SPacketSimple.move(player, 1.0d, false);
			if (this.ticksLeft % 3 == 0 && this.ticksLeft > 2)
				Minewatch.proxy.spawnParticlesSmoke(player.world, 
						player.prevPosX+player.world.rand.nextDouble()-0.5d, 
						player.prevPosY+player.world.rand.nextDouble(), 
						player.prevPosZ+player.world.rand.nextDouble()-0.5d, 
						0xB4907B, 0xE6C4AC, 15+player.world.rand.nextInt(5), 10);
			return super.onClientTick();
		}

		@Override
		public Handler onRemove() {
			if (!player.world.isRemote)
				EnumHero.MCCREE.ability2.keybind.setCooldown(this.player, 160, false);
			return super.onRemove();
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
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand)) {
			if (!world.isRemote) {
				EntityMcCreeBullet bullet = new EntityMcCreeBullet(world, player);
				bullet.setAim(player, player.rotationPitch, player.rotationYaw, 5.0F, 0.3F, 0F, hand, true);
				world.spawnEntity(bullet);
				world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.mccreeShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
						world.rand.nextFloat()/2+0.75f);	

				this.subtractFromCurrentAmmo(player, 1, hand);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 9);
				if (world.rand.nextInt(6) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
			else {
				Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesSpark(world, vec.xCoord, vec.yCoord, vec.zCoord, 0xFFEF89, 0x5A575A, 5, 1);
			}
		}
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return (Minewatch.proxy.getClientPlayer() != null && 
				TickHandler.hasHandler(Minewatch.proxy.getClientPlayer(), Identifier.MCCREE_ROLL)) ? 
						true : super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
		if (entity instanceof EntityPlayer && count % 2 == 0 && this.canUse((EntityPlayer) entity, true, getHand(entity, stack))) {
			EnumHand hand = null;
			for (EnumHand hand2 : EnumHand.values())
				if (((EntityPlayer)entity).getHeldItem(hand2) == stack)
					hand = hand2;
			if (!entity.world.isRemote && hand != null) {
				EntityMcCreeBullet bullet = new EntityMcCreeBullet(entity.world, entity);
				bullet.setAim((EntityPlayer) entity, entity.rotationPitch, entity.rotationYaw, 5.0F, 1.5F, 1F, hand, true);
				entity.world.spawnEntity(bullet);				
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, ModSoundEvents.mccreeShoot, 
						SoundCategory.PLAYERS, entity.world.rand.nextFloat()+0.5F, entity.world.rand.nextFloat()/20+0.95f);	
				if (count == this.getMaxItemUseDuration(stack))
					this.subtractFromCurrentAmmo((EntityPlayer) entity, 1, hand);
				else
					this.subtractFromCurrentAmmo((EntityPlayer) entity, 1);
				if (entity.world.rand.nextInt(25) == 0)
					entity.getHeldItem(hand).damageItem(1, entity);
			} 
			else if (hand != null) {
				entity.rotationPitch--;
				Vec3d vec = EntityMWThrowable.getShootingPos(entity, entity.rotationPitch, entity.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesSpark(entity.world, vec.xCoord, vec.yCoord, vec.zCoord, 0xFFEF89, 0x5A575A, 5, 1);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		// roll
		if (isSelected && entity.onGround && entity instanceof EntityPlayer && hero.ability2.isSelected((EntityPlayer) entity) &&
				!world.isRemote && (this.canUse((EntityPlayer) entity, true, getHand((EntityPlayer) entity, stack)) || this.getCurrentAmmo((EntityPlayer) entity) == 0)) {
			world.playSound(null, entity.getPosition(), ModSoundEvents.mccreeRoll, SoundCategory.PLAYERS, 1.3f, world.rand.nextFloat()/4f+0.8f);
			if (entity instanceof EntityPlayerMP)
				Minewatch.network.sendToAll(new SPacketSimple(2, true, (EntityPlayerMP) entity));
			this.setCurrentAmmo((EntityPlayer)entity, this.getMaxAmmo((EntityPlayer) entity));
			TickHandler.register(false, ROLL.setEntity((EntityPlayer) entity).setTicks(10));
			TickHandler.register(false, Ability.ABILITY_USING.setEntity(entity).setTicks(10));
		}
	}

}