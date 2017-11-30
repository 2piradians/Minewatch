package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityBastionBullet;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemBastionGun extends ItemMWWeapon {

	public static final Handler TURRET = new Handler(Identifier.BASTION_TURRET, true) {
		@SideOnly(Side.CLIENT)
		@Override
		public boolean onClientTick() {
			if (entityLiving != null && entityLiving.getHeldItemMainhand() != null && 
					entityLiving.getHeldItemMainhand().getItem() == EnumHero.BASTION.weapon && 
					isAlternate(entityLiving.getHeldItemMainhand())) {
				// prevent movement
				Handler handler = TickHandler.getHandler(entityLiving, Identifier.PREVENT_MOVEMENT);
				if (handler == null)
					TickHandler.register(true, Handlers.PREVENT_MOVEMENT.setEntity(entityLiving).setTicks(ticksLeft));
				else
					handler.ticksLeft = this.ticksLeft;

				return false;
			}
			else if (isAlternate(entityLiving.getHeldItemMainhand())) {
				EnumHero.BASTION.reloadSound = ModSoundEvents.BASTION_RELOAD_0;
			}

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (entityLiving != null && entityLiving.getHeldItemMainhand() != null && 
					entityLiving.getHeldItemMainhand().getItem() == EnumHero.BASTION.weapon && 
					isAlternate(entityLiving.getHeldItemMainhand())) {
				// prevent movement
				Handler handler = TickHandler.getHandler(entityLiving, Identifier.PREVENT_MOVEMENT);
				if (handler == null)
					TickHandler.register(false, Handlers.PREVENT_MOVEMENT.setEntity(entityLiving).setTicks(ticksLeft));
				else
					handler.ticksLeft = this.ticksLeft;

				return false;
			}
			else if (isAlternate(entityLiving.getHeldItemMainhand())) {
				setAlternate(entityLiving.getHeldItemMainhand(), false);
				EnumHero.BASTION.reloadSound = ModSoundEvents.BASTION_RELOAD_0;
			}

			return super.onServerTick();
		}
	};

	public ItemBastionGun() {
		super(40);
		this.saveEntityToNBT = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false)) {
			boolean turret = isAlternate(stack);
			if (!world.isRemote) {
				EntityBastionBullet bullet = new EntityBastionBullet(world, player, turret ? 2 : hand.ordinal());
				if (turret)
					EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, -1, 1.5F, null, 20, 0);
				else
					EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, -1, 0.6F, hand, 12, 0.43f);
				world.spawnEntity(bullet);
				if (turret)
					ModSoundEvents.BASTION_SHOOT_1.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
				else
					ModSoundEvents.BASTION_SHOOT_0.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0)
					player.getHeldItem(hand).damageItem(1, player);
				if (!turret)
					this.setCooldown(player, 3);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// stop turret if doesn't have handler (i.e. dies in turret form)
			if (!world.isRemote && isAlternate(stack) &&
					!TickHandler.hasHandler(player, Identifier.BASTION_TURRET)) {
				setAlternate(stack, false);
				Minewatch.network.sendToAll(new SPacketSimple(31, player, false));//TEST other player dying in turret -> still looks like turret to me
				hero.reloadSound = ModSoundEvents.BASTION_RELOAD_0;
				this.setCurrentAmmo(player, this.getMaxAmmo(player), EnumHand.MAIN_HAND);
			}

			// reconfigure
			if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) { 
				boolean turret = false;
				setAlternate(stack, !isAlternate(stack));
				if (!isAlternate(stack)) 
					hero.reloadSound = ModSoundEvents.BASTION_RELOAD_0;
				else {
					hero.reloadSound = ModSoundEvents.BASTION_RELOAD_1;
					turret = true;
				}
				if (turret) 
					TickHandler.register(false, TURRET.setEntity(player).setTicks(10));
				Minewatch.network.sendToAll(new SPacketSimple(31, player, turret));
				this.setCooldown(player, turret ? 20 : 10);
				hero.ability2.keybind.setCooldown(player, turret ? 20 : 10, true);
				this.setCurrentAmmo(player, this.getMaxAmmo(player), EnumHand.MAIN_HAND);
			}

		}
	}	

	@SubscribeEvent
	public void reduceDamage(LivingHurtEvent event) {
		// reduce damage
		if (TickHandler.hasHandler(event.getEntityLiving(), Identifier.BASTION_TURRET) && event.getEntityLiving() != null) 
			event.setAmount(event.getAmount()*0.8f);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void hideOffhand(RenderSpecificHandEvent event) {
		if (event.getHand() == EnumHand.OFF_HAND && 
				isAlternate(Minecraft.getMinecraft().player.getHeldItemMainhand()) &&
				Minecraft.getMinecraft().player.getHeldItemMainhand() != null && 
				Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() == EnumHero.BASTION.weapon)
			event.setCanceled(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getAllModelLocations(ArrayList<String> locs) {
		locs.add("_0");
		locs.add("_1");
		return locs;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getModelLocation(ItemStack stack, @Nullable EntityLivingBase entity) {
		boolean turret = isAlternate(stack);
		return turret ? "_1" : "_0";
	}

}