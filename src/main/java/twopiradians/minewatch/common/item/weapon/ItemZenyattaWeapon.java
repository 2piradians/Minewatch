package twopiradians.minewatch.common.item.weapon;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.render.entity.RenderZenyattaOrb;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityZenyattaOrb;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemZenyattaWeapon extends ItemMWWeapon {

	private static final int VOLLEY_CHARGE_DELAY = 8;
	public static final int ANIMATION_TIME = 40;
	/**Client: ItemStacks and the ticksExisted that their animations stop - only for client player*/
	public static ItemStack animatingDiscord;
	public static ItemStack animatingHarmony;
	public static int animatingTime = -1;

	public static final Handler HARMONY = new Handler(Identifier.ZENYATTA_HARMONY, false) {
		@Override
		public boolean onServerTick() {
			// send keep-alive packet to client
			if (this.entityLiving != null && this.entity instanceof EntityLivingBase 
					&& (this.ticksLeft-1) % 20 == 0 && ((EntityLivingBase)this.entity).canEntityBeSeen(this.entityLiving) &&
					ItemMWArmor.SetManager.getWornSet(this.entity) == EnumHero.ZENYATTA) {
				this.ticksLeft = 60;
				Minewatch.network.sendToDimension(new SPacketSimple(42, this.entity, true, this.entityLiving), this.entity.world.provider.getDimension());
			}
			// heal
			if (this.entityLiving != null)
				EntityHelper.attemptDamage(this.entity, this.entityLiving, -1.5f, true);
			return super.onServerTick() || (entityLiving != null && !entityLiving.isEntityAlive());
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// particles
			if (this.entityLiving != null && this.ticksLeft % 30 == 0) {
				float size = Math.min(entityLiving.height, entityLiving.width)*8f;
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, this.entityLiving.world, this.entityLiving, 0xCFC77F, 0xCFC77F, 0.3f, 
						40, size, size/1.1f, 0, 0);
			}
			return super.onClientTick() || (entityLiving != null && !entityLiving.isEntityAlive());
		}
		@Override
		public Handler onServerRemove() {
			// stop on client
			Minewatch.network.sendToDimension(new SPacketSimple(42, false, null, this.ticksLeft, 0, 0, this.entity, this.entityLiving), this.entity.world.provider.getDimension());
			return super.onServerRemove();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
	};
	public static final Handler DISCORD = new Handler(Identifier.ZENYATTA_DISCORD, false) {
		@Override
		public boolean onServerTick() {
			// send keep-alive packet to client
			if (this.entityLiving != null && this.entity instanceof EntityLivingBase 
					&& (this.ticksLeft-1) % 20 == 0 && ((EntityLivingBase)this.entity).canEntityBeSeen(this.entityLiving) &&
					ItemMWArmor.SetManager.getWornSet(this.entity) == EnumHero.ZENYATTA) {
				this.ticksLeft = 60;
				Minewatch.network.sendToDimension(new SPacketSimple(43, this.entity, true, this.entityLiving), this.entity.world.provider.getDimension());
			}
			return super.onServerTick() || (entityLiving != null && !entityLiving.isEntityAlive());
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// particles
			if (this.entityLiving != null && this.ticksLeft % 30 == 0) {
				float size = Math.min(entityLiving.height, entityLiving.width)*8f;
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.ANA_HEAL, this.entityLiving.world, this.entityLiving, 0x8D4BF3, 0x8D4BF3, 0.4f, 
						40, size, size, this.entityLiving.world.rand.nextFloat(), this.entityLiving.world.rand.nextFloat()/20f);
			}
			return super.onClientTick() || (entityLiving != null && !entityLiving.isEntityAlive());
		}
		@Override
		public Handler onServerRemove() {
			// stop on client
			Minewatch.network.sendToDimension(new SPacketSimple(43, false, null, this.ticksLeft, 0, 0, this.entity, this.entityLiving), this.entity.world.provider.getDimension());
			return super.onServerRemove();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
	};
	public static final Handler VOLLEY = new Handler(Identifier.ZENYATTA_VOLLEY, true) {
		@Override
		public boolean onServerTick() {
			if (entityLiving != null && this.ticksLeft % 2 == 0) {
				EntityZenyattaOrb orb = new EntityZenyattaOrb(entityLiving.world, entityLiving, 2, 0);
				EntityHelper.setAim(orb, entityLiving, entityLiving.rotationPitch, entityLiving.rotationYawHead, 80, 0, null, 22, 0.78f*(this.ticksLeft%4 == 0 ? -1 : 1));
				entityLiving.world.spawnEntity(orb);
				ModSoundEvents.ZENYATTA_VOLLEY_SHOOT.playFollowingSound(entityLiving, entityLiving.world.rand.nextFloat()+0.5F, entityLiving.world.rand.nextFloat()/3+0.8f, false);
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
		this.showHealthParticles = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 		
		// shoot
		if (this.canUse(player, true, hand, false) && !player.isHandActive() && !TickHandler.hasHandler(player, Identifier.ZENYATTA_VOLLEY)) {
			if (!world.isRemote) {
				EntityZenyattaOrb orb = new EntityZenyattaOrb(world, player, hand.ordinal(), 0);
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

		// check if done animating
		if (animatingTime != -1 && animatingTime < Minewatch.proxy.getClientPlayer().ticksExisted) {
			animatingTime = -1;
			animatingHarmony = null;
			animatingDiscord = null;
		}
		
		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHeldItemMainhand() == stack &&
				((EntityLivingBase)entity).getActiveItemStack() != stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// harmony
			if (!world.isRemote && hero.ability1.isSelected(player, player instanceof EntityPlayer) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				EntityLivingBase target = EntityHelper.getTargetInFieldOfVision(player, 40, 10, true, 
						// ignore if harmony from anyone
						input -> !TickHandler.hasHandler(handler -> handler.identifier == Identifier.ZENYATTA_HARMONY && handler.entityLiving == input, false));
				if (target != null) {
					// remove discord by same player
					Handler discord = TickHandler.getHandler(player, Identifier.ZENYATTA_DISCORD);
					if (discord != null && discord.entityLiving == target)
						TickHandler.unregister(false, discord);
					// apply harmony
					TickHandler.register(false, HARMONY.setEntity(player).setEntityLiving(target).setTicks(60));
					Minewatch.network.sendToDimension(new SPacketSimple(42, player, true, target), world.provider.getDimension());
					ModSoundEvents.ZENYATTA_HEAL.playFollowingSound(player, 0.5f, 1.0f, false);
					ModSoundEvents.ZENYATTA_HEAL_VOICE.playFollowingSound(player, 1.0f, 1.0f, false);
					// shoot orb
					EntityZenyattaOrb orb = new EntityZenyattaOrb(world, player, EnumHand.OFF_HAND.ordinal(), 2);
					EntityHelper.setAim(orb, player, target, 120, EnumHand.OFF_HAND, 22, 0.78f);
					world.spawnEntity(orb);
				}
			}

			// discord
			if (!world.isRemote && hero.ability2.isSelected(player, true) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				EntityLivingBase target = EntityHelper.getTargetInFieldOfVision(player, 40, 10, false, 
						// ignore if discord from anyone
						input -> !TickHandler.hasHandler(handler -> handler.identifier == Identifier.ZENYATTA_DISCORD && handler.entityLiving == input, false));	
				if (target != null) {
					// remove harmony by same player
					Handler harmony = TickHandler.getHandler(player, Identifier.ZENYATTA_HARMONY);
					if (harmony != null && harmony.entityLiving == target)
						TickHandler.unregister(false, harmony);
					// apply discord
					TickHandler.register(false, DISCORD.setEntity(player).setEntityLiving(target).setTicks(60));
					Minewatch.network.sendToDimension(new SPacketSimple(43, player, true, target), world.provider.getDimension());
					ModSoundEvents.ZENYATTA_DAMAGE.playFollowingSound(player, 0.5f, 1.0f, false);
					ModSoundEvents.ZENYATTA_DAMAGE_VOICE.playFollowingSound(player, 1.0f, 1.0f, false);
					// shoot orb
					EntityZenyattaOrb orb = new EntityZenyattaOrb(world, player, EnumHand.MAIN_HAND.ordinal(), 1);
					EntityHelper.setAim(orb, player, target, 120, EnumHand.MAIN_HAND, 22, 0.78f);
					world.spawnEntity(orb);
				}
			}
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
			else {
				animatingTime = -1;
				animatingDiscord = null;
				animatingHarmony = null;
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
		}
		else
			return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		// prevent speed reduction
		if (!player.isRiding() && player instanceof EntityPlayer) 
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

	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void discord(LivingHurtEvent event) {
		EntityLivingBase target = event.getEntityLiving();

		// discord boosted damage
		if (target != null && !target.world.isRemote && 
				TickHandler.hasHandler(handler -> handler.identifier == Identifier.ZENYATTA_DISCORD && handler.entityLiving == target, false)) 
			event.setAmount(event.getAmount() * 1.3f);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		// prevent reequip while animating
		if (animatingTime != -1 && (newStack == animatingHarmony || newStack == animatingDiscord))
			return false;
		else
			return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType cameraTransformType, Pair<? extends IBakedModel, Matrix4f> ret) {
		if (animatingTime != -1 && entity != null && Minewatch.proxy.getClientPlayer() != null && 
				(animatingHarmony == stack && entity.getHeldItemMainhand() == stack && 
				(cameraTransformType.equals(TransformType.FIRST_PERSON_RIGHT_HAND) || cameraTransformType.equals(TransformType.THIRD_PERSON_RIGHT_HAND))) ||
				(animatingDiscord == stack && entity.getHeldItemOffhand() == stack && 
				(cameraTransformType.equals(TransformType.FIRST_PERSON_LEFT_HAND) || cameraTransformType.equals(TransformType.THIRD_PERSON_LEFT_HAND)))) {

			float percent = 1f - ((((float)animatingTime) - Minewatch.proxy.getClientPlayer().ticksExisted-Minewatch.proxy.getRenderPartialTicks()) / ANIMATION_TIME);
			float upTime = 0.1f;
			float downTime = 0.05f;

			if (percent < upTime) // up
				percent /= upTime;
			else if (percent > 1f-downTime) // down
				percent = (1f - percent) / downTime;
			else // top
				percent = 1f;
			percent = MathHelper.clamp(percent, 0, 1);

			GlStateManager.translate(0, percent*0.25f, 0);
		}
		return ret;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		if (stack == RenderZenyattaOrb.NORMAL)
			return 0x82ECFE;
		else if (stack == RenderZenyattaOrb.DISCORD)
			return 0x000000;
		else if (stack == RenderZenyattaOrb.HARMONY)
			return 0xFFD800;
		else if (stack == animatingDiscord)
			return 0x630063;
		else if (stack == animatingHarmony)
			return 0xFFD800;
		else 
			return -1;
	}

}