package twopiradians.minewatch.common.item.weapon;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.projectile.EntityPharahRocket;
import twopiradians.minewatch.common.entity.projectile.EntityPharahRocket.Type;
import twopiradians.minewatch.common.hero.UltimateManager;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemPharahWeapon extends ItemMWWeapon {

	public static final Handler CONCUSSIVE = new Handler(Identifier.PHARAH_CONCUSSIVE, true) {};
	public static final Handler ULTIMATE = new Handler(Identifier.PHARAH_ULTIMATE, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			entity.motionY = 0;
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			entity.motionY = 0;
			for (int i=0; i<(this.ticksLeft % 2 == 0 ? 2 : 1); ++i) {
				EntityPharahRocket projectile = new EntityPharahRocket(entity.world, entityLiving, -1, Type.ULTIMATE);
				EntityHelper.setAim(projectile, entityLiving, entityLiving.rotationPitch, entityLiving.rotationYawHead, 28.5f, 16, null, (entityLiving.world.rand.nextFloat()-0.5f)*100, (entityLiving.world.rand.nextFloat()-0.5f)*1.5f, 0.2f, true, true);
				entity.world.spawnEntity(projectile);
			}
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			ModSoundEvents.PHARAH_ULTIMATE_0.stopFollowingSound(entity);
			ModSoundEvents.PHARAH_ULTIMATE_1.stopFollowingSound(entity);
			ModSoundEvents.PHARAH_ULT.stopFollowingSound(entity);
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {

			return super.onServerRemove();
		}
	};

	public static final Handler JET = new Handler(Identifier.PHARAH_JET, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			ItemPharahWeapon.spawnJetPackParticles(entityLiving, true);
			return super.onClientTick();
		}
	};

	public static void spawnJetPackParticles(EntityLivingBase entity, boolean big) {
		if (entity != null && entity.world.isRemote) {
			float offset = 50;
			double amountPerBlock = big ? 5 : 4;
			float scale = big ? 1.5f : 1f;
			int ageBig = big ? 10 : 7;
			int ageSmall = big ? 2 : 2;
			Vec3d vec = entity.getPositionVector().subtract(EntityHelper.getLook(10, entity.rotationYaw-offset).scale(0.5d));
			Vec3d prevVec = EntityHelper.getPrevPositionVector(entity).subtract(EntityHelper.getLook(10, entity.prevRotationYaw-offset).scale(0.5d));
			EntityHelper.spawnTrailParticles(entity, amountPerBlock, 0.2d, 0, 0, 0, 0xCF9B4A, 0x33271B, scale, ageBig, 0.7f, vec, prevVec);
			EntityHelper.spawnTrailParticles(entity, amountPerBlock, 0.05d, 0, 0, 0, 0xFAED5C, 0xE2C457, scale, ageSmall, 0.7f, vec, prevVec);

			vec = entity.getPositionVector().subtract(EntityHelper.getLook(10, entity.rotationYaw+offset).scale(0.5d));
			prevVec = EntityHelper.getPrevPositionVector(entity).subtract(EntityHelper.getLook(10, entity.prevRotationYaw+offset).scale(0.5d));
			EntityHelper.spawnTrailParticles(entity, amountPerBlock, 0.2d, 0, 0, 0, 0xCF9B4A, 0x33271B, scale, ageBig, 0.7f, vec, prevVec);
			EntityHelper.spawnTrailParticles(entity, amountPerBlock, 0.05d, 0, 0, 0, 0xFAED5C, 0xE2C457, scale, ageSmall, 0.7f, vec, prevVec);
		}
	}

	public ItemPharahWeapon() {
		super(20);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// primary fire
		if (!world.isRemote && this.canUse(player, true, hand, false) && 
				!TickHandler.hasHandler(player, Identifier.PHARAH_ULTIMATE)) {
			EntityPharahRocket projectile = new EntityPharahRocket(world, player, hand.ordinal(), Type.NORMAL);
			EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 35, 0, hand, 14, 0.15f);
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
					this.canUse(player, true, EnumHand.MAIN_HAND, true) && 
					!TickHandler.hasHandler(player, Identifier.PHARAH_ULTIMATE)) {
				EntityHelper.resetFloatTime(player);
				player.onGround = false;
				entity.motionY = Math.max(Config.lowerGravity ? 1.7f : 2, entity.motionY);
				Vec3d look = EntityHelper.getLook(0, player.rotationYawHead).scale(0.9d);
				player.motionX += look.x;
				player.motionZ += look.z;
				ModSoundEvents.PHARAH_JET.playFollowingSound(player, 1, 1, false);
				hero.ability2.keybind.setCooldown(player, 200, false);
				Minewatch.network.sendToDimension(new SPacketSimple(82, player, false, player.motionY, 0, 0), world.provider.getDimension());
			}
			// concussive
			else if (!world.isRemote && hero.ability1.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true) && 
					!TickHandler.hasHandler(player, Identifier.PHARAH_ULTIMATE)) {
				EntityPharahRocket projectile = new EntityPharahRocket(world, player, EnumHand.OFF_HAND.ordinal(), Type.CONCUSSIVE);
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 35, 0, EnumHand.OFF_HAND, 15, 0.6f);
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
		
		// ultimate
		if (TickHandler.hasHandler(entity, Identifier.PHARAH_ULTIMATE)) {
			float shake = (float) Math.sin(entity.ticksExisted*3f)*0.1f;
			float x = 5;
			float y = -1f + shake;
			model.bipedLeftArmwear.rotateAngleX = x;
			model.bipedLeftArm.rotateAngleX = x;
			model.bipedRightArmwear.rotateAngleX = x;
			model.bipedRightArm.rotateAngleX = x;
			model.bipedLeftArmwear.rotateAngleY = y;
			model.bipedLeftArm.rotateAngleY = y;
			model.bipedRightArmwear.rotateAngleY = -y;
			model.bipedRightArm.rotateAngleY = -y;
			
			x = 0.2f + shake;
			y = 0.2f;
			float z = -0.5f;
			model.bipedLeftLegwear.rotateAngleX = x;
			model.bipedLeftLeg.rotateAngleX = x;
			model.bipedRightLegwear.rotateAngleX = x;
			model.bipedRightLeg.rotateAngleX = x;
			model.bipedLeftLegwear.rotateAngleY = y;
			model.bipedLeftLeg.rotateAngleY = y;
			model.bipedRightLegwear.rotateAngleY = -y;
			model.bipedRightLeg.rotateAngleY = -y;
			model.bipedLeftLegwear.rotateAngleZ = z;
			model.bipedLeftLeg.rotateAngleZ = z;
			model.bipedRightLegwear.rotateAngleZ = -z;
			model.bipedRightLeg.rotateAngleZ = -z;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType transform, Pair<? extends IBakedModel, Matrix4f> ret) {
		// hide weapon while ulting
		if ((transform == TransformType.FIRST_PERSON_LEFT_HAND ||
				transform == TransformType.FIRST_PERSON_RIGHT_HAND ||
				transform == TransformType.THIRD_PERSON_LEFT_HAND ||
				transform == TransformType.THIRD_PERSON_RIGHT_HAND) &&
				TickHandler.hasHandler(entity, Identifier.PHARAH_ULTIMATE))
			ret.getRight().setScale(0);
		
		return super.preRenderWeapon(entity, stack, transform, ret);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void hideOffhand(RenderSpecificHandEvent event) {
		if (event.getHand() == EnumHand.OFF_HAND &&
				TickHandler.hasHandler(Minewatch.proxy.getClientPlayer(), Identifier.PHARAH_ULTIMATE))
			event.setCanceled(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderHand(AbstractClientPlayer player, EnumHand hand) {
		return hand == EnumHand.OFF_HAND && TickHandler.hasHandler(player, Identifier.PHARAH_CONCUSSIVE);
	}

	@Override
	public void onUltimate(ItemStack stack, World world, EntityLivingBase player) {
		ModSoundEvents.PHARAH_ULTIMATE_0.playFollowingSound(player, 1, 1, false, true, false, true);
		ModSoundEvents.PHARAH_ULTIMATE_1.playFollowingSound(player, 1, 1, false, false, true, false);
		ModSoundEvents.PHARAH_ULT.playFollowingSound(player, 1, 1);
		
		TickHandler.register(false, ULTIMATE.setEntity(player).setTicks(60),
				Handlers.PREVENT_MOVEMENT.setEntity(player).setTicks(60),
				UltimateManager.PREVENT_CHARGE.setEntity(player).setTicks(60));
		Minewatch.network.sendToDimension(new SPacketSimple(84, player, false, 60, 0, 0), player.world.provider.getDimension());

		super.onUltimate(stack, world, player);
	}

}