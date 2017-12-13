package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityZenyattaOrb;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class ItemZenyattaWeapon extends ItemMWWeapon {

	private static final int VOLLEY_CHARGE_DELAY = 8;

	public static final Handler VOLLEY = new Handler(Identifier.ZENYATTA_VOLLEY, true) {
		@Override
		public boolean onServerTick() {
			if (entityLiving != null && this.ticksLeft % 2 == 0) {
				EntityZenyattaOrb orb = new EntityZenyattaOrb(entityLiving.world, entityLiving, 2);
				EntityHelper.setAim(orb, entityLiving, entityLiving.rotationPitch, entityLiving.rotationYawHead, 80, 0, null, 22, 0.78f*(this.ticksLeft%4 == 0 ? -1 : 1));
				entityLiving.world.spawnEntity(orb);
				ModSoundEvents.ZENYATTA_VOLLEY_SHOOT.playFollowingSound(player, entityLiving.world.rand.nextFloat()+0.5F, entityLiving.world.rand.nextFloat()/3+0.8f, false);
			}
			return super.onServerTick();
		}
		@Override
		public Handler onServerRemove() {
			EnumHero.ZENYATTA.weapon.setCooldown(entity, 12);
			return super.onServerRemove();
		}
	};

	public ItemZenyattaWeapon() {
		super(40);
		this.hasOffhand = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && !player.isHandActive()) {
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

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 70;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack)	{
		return EnumAction.BLOCK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		if (hand == EnumHand.MAIN_HAND && this.canUse(player, true, hand, false) && 
				!TickHandler.hasHandler(player, Identifier.ZENYATTA_VOLLEY)) {
			player.setActiveHand(hand);
			this.reequipAnimation(player.getHeldItem(EnumHand.MAIN_HAND), this.getMaxItemUseDuration(player.getHeldItem(hand)));
			this.reequipAnimation(player.getHeldItem(EnumHand.OFF_HAND), this.getMaxItemUseDuration(player.getHeldItem(hand)));
			if (!world.isRemote)
				ModSoundEvents.ZENYATTA_VOLLEY_CHARGE.playFollowingSound(player, 1.0f, 1.0f, false);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
		}
		else
			return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		// prevent speed reduction
		if (!player.isRiding()) 
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 0, 8, false, false));
		// secondary shaking screen
		if (player.world.isRemote) {
			player.rotationPitch += (player.world.rand.nextFloat()-0.5f)*(count-this.getMaxItemUseDuration(stack))*0.008f;
			player.rotationYaw += (player.world.rand.nextFloat()-0.5f)*(count-this.getMaxItemUseDuration(stack))*0.008f;
		}
		// secondary particles
		if (count > 0 && count % VOLLEY_CHARGE_DELAY == 0) {
			int num = (this.getMaxItemUseDuration(stack)-count) / VOLLEY_CHARGE_DELAY;
			float x = 0;
			float y = 0;
			switch (num) {
			case 1:
				x = 0.6f;
				y = 20;
				break;
			case 2:
				x = -0.6f;
				y = 20;
				break;
			case 3:
				x = 0.8f;
				y = -13;
				break;
			case 4:
				x = -0.8f;
				y = -13;
				break;
			case 5:
				x = 0;
				y = -28;
				break;
			}
			if (num <= 5 && num > 0) {
				if (player.world.isRemote)
					Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.ZENYATTA, player.world, player, 0xFFFFFF, 0xFFFFFF, 1, count, 2.5f, 2.9f, player.world.rand.nextFloat(), 0.001f, null, y, x);
				else {
					this.subtractFromCurrentAmmo(player, 1);
					if (player.world.rand.nextInt(25) == 0)
						stack.damageItem(1, player);
				}
			}
		}
	}

	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
		this.shootVolley(entity, 5);
		return super.onItemUseFinish(stack, world, entity);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int timeLeft) {
		this.shootVolley(player, (this.getMaxItemUseDuration(stack)-timeLeft)/(this.getMaxItemUseDuration(stack)/5)+1);
	}

	private void shootVolley(EntityLivingBase entity, int orbs) {
		if (!entity.world.isRemote)
			ModSoundEvents.ZENYATTA_VOLLEY_CHARGE.stopSound(entity.world);
		this.reequipAnimation(entity.getHeldItem(EnumHand.MAIN_HAND), 0);
		this.reequipAnimation(entity.getHeldItem(EnumHand.OFF_HAND), 0);
		TickHandler.register(entity.world.isRemote, VOLLEY.setEntity(entity).setTicks(orbs*2));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void fixFOV(FOVUpdateEvent event) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player != null && ((player.getActiveItemStack() != null && 
				player.getActiveItemStack().getItem() == this)) || TickHandler.hasHandler(player, Identifier.ZENYATTA_VOLLEY)) {
			event.setNewfov(1);
		}
	}

}