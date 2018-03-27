package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityBastionBullet;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemBastionGun extends ItemMWWeapon {

	public static final Handler TURRET = new Handler(Identifier.BASTION_TURRET, true) {
		@SideOnly(Side.CLIENT)
		@Override
		public boolean onClientTick() {
			if (entityLiving != null && entityLiving.isEntityAlive() && entityLiving.getHeldItemMainhand() != null && 
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
			if (entityLiving != null && entityLiving.isEntityAlive() && entityLiving.getHeldItemMainhand() != null && 
					entityLiving.getHeldItemMainhand().getItem() == EnumHero.BASTION.weapon && 
					isAlternate(entityLiving.getHeldItemMainhand())) {
				// prevent movement
				Handler handler = TickHandler.getHandler(entityLiving, Identifier.PREVENT_MOVEMENT);
				if (handler == null)
					TickHandler.register(false, Handlers.PREVENT_MOVEMENT.setEntity(entityLiving).setTicks(30));
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
		@Override
		public Handler onServerRemove() {
			TickHandler.unregister(false, TickHandler.getHandler(entityLiving, Identifier.PREVENT_MOVEMENT));
			return super.onServerRemove();
		}
	};

	public ItemBastionGun() {
		super(40);
		this.saveEntityToNBT = true;
		MinecraftForge.EVENT_BUS.register(this);
		this.maxCharge = 80;
		this.rechargeRate = 80f/140f;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && hero.ability2.getCooldown(player) == 0 && 
				!KeyBind.RMB.isKeyDown(player)) {
			boolean turret = isAlternate(stack); // TODO prob only way to do is on render tick, get rid of hitscan entitiies
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
	public int getMaxItemUseDuration(ItemStack stack) {
		return 90;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		// start heal
		if (hand == EnumHand.MAIN_HAND && this.canUse(player, true, hand, true) && 
				this.getCurrentCharge(player) >= this.maxCharge*0.2f && hero.ability1.isSelected(player)) {
			player.setActiveHand(hand);
		}

		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		// heal
		if (this.canUse(player, true, EnumHand.MAIN_HAND, true) && this.getCurrentCharge(player) >= 1 &&
				hero.ability1.isSelected(player) && !this.hasCooldown(player)) {
			if (count == this.getMaxItemUseDuration(stack) - 10 && !player.world.isRemote)
				ModSoundEvents.BASTION_HEAL.playFollowingSound(player, 1.0f, 1.0f, false);
			if (count <= this.getMaxItemUseDuration(stack) - 10) { 
				this.subtractFromCurrentCharge(player, 1, true);
				if (!player.world.isRemote) 
					EntityHelper.attemptDamage(player, player, -3.75f, true);
			}
			else if (this.getCurrentCharge(player) <= 0) 
				player.stopActiveHand();
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
		if (!world.isRemote)
			ModSoundEvents.BASTION_HEAL.stopSound(world);
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
				Minewatch.network.sendToAll(new SPacketSimple(31, player, false));
				hero.reloadSound = ModSoundEvents.BASTION_RELOAD_0;
				this.setCurrentAmmo(player, this.getMaxAmmo(player), EnumHand.MAIN_HAND);
			}

			// reconfigure
			if (!world.isRemote && hero.ability2.isSelected(player, true) && 
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