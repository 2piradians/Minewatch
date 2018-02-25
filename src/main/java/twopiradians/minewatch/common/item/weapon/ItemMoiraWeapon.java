package twopiradians.minewatch.common.item.weapon;

import java.util.HashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.entity.ability.EntityMoiraOrb;
import twopiradians.minewatch.common.entity.projectile.EntityMoiraHealEnergy;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemMoiraWeapon extends ItemMWWeapon {

	private static final ResourceLocation DAMAGE_BEAM_MISS = new ResourceLocation(Minewatch.MODID, "textures/entity/moira_damage_beam_miss.png");
	private static final ResourceLocation DAMAGE_BEAM_HIT = new ResourceLocation(Minewatch.MODID, "textures/entity/moira_damage_beam_hit.png");

	public static Handler ORB_SELECT = new Handler(Identifier.MOIRA_ORB_SELECT, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// stop handler
			if (entity == Minecraft.getMinecraft().player && entityLiving.getHeldItemMainhand() == null || 
					entityLiving.getHeldItemMainhand().getItem() != EnumHero.MOIRA.weapon ||
					!EnumHero.MOIRA.ability2.isSelected(entityLiving) || 
					!EnumHero.MOIRA.weapon.canUse(entityLiving, true, EnumHand.MAIN_HAND, true)) 
				return true;
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			// stop handler
			if (entityLiving == null || entityLiving.getHeldItemMainhand() == null || 
					entityLiving.getHeldItemMainhand().getItem() != EnumHero.MOIRA.weapon ||
					!EnumHero.MOIRA.ability2.isSelected(entityLiving) || 
					!EnumHero.MOIRA.weapon.canUse(entityLiving, true, EnumHand.MAIN_HAND, true)) {
				ModSoundEvents.MOIRA_ORB_DESELECT.playFollowingSound(entityLiving, 1, 1, false);
				return true;
			}
			// spawn orb
			else if (KeyBind.RMB.isKeyDown(entityLiving) || KeyBind.LMB.isKeyDown(entityLiving)) {
				boolean heal = KeyBind.LMB.isKeyDown(entityLiving);
				EnumHand hand = heal ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
				EntityMoiraOrb orb = new EntityMoiraOrb(entityLiving.world, entityLiving, hand.ordinal(), heal);
				EntityHelper.setAim(orb, entityLiving, entityLiving.rotationPitch, entityLiving.rotationYawHead, 20, 0, hand, 30, 0);
				entityLiving.world.spawnEntity(orb);
				EnumHero.MOIRA.ability2.keybind.setCooldown(entityLiving, 200, false); 
				EnumHero.MOIRA.weapon.setCooldown(entityLiving, 20);
				if (heal) {
					ModSoundEvents.MOIRA_ORB_HEAL_THROW.playFollowingSound(entityLiving, 1, 1, false);
					ModSoundEvents.MOIRA_ORB_HEAL_VOICE.playFollowingSound(entityLiving, 1, 1, false);
				}
				else {
					ModSoundEvents.MOIRA_ORB_DAMAGE_THROW.playFollowingSound(entityLiving, 1, 1, false);
					ModSoundEvents.MOIRA_ORB_DAMAGE_VOICE.playFollowingSound(entityLiving, 1, 1, false);
				}
				return true;
			}
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			Minewatch.network.sendToDimension(new SPacketSimple(49, entityLiving, false), entityLiving.world.provider.getDimension());
			return super.onServerRemove();
		}
	};

	public static Handler DAMAGE = new Handler(Identifier.MOIRA_DAMAGE, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// basic checks
			if (entity == Minecraft.getMinecraft().player && 
					(!(entity instanceof EntityLivingBase) || !entity.isEntityAlive() ||
							((EntityLivingBase)entity).getHeldItemMainhand() == null || 
							((EntityLivingBase)entity).getHeldItemMainhand().getItem() != EnumHero.MOIRA.weapon ||
							!KeyBind.RMB.isKeyDown((EntityLivingBase) entity))) 
				return true;
			// find new target / clear target
			else if (entity.ticksExisted % 5 == 0) {
				if (entityLiving == null || !entityLiving.isEntityAlive()) 
					entityLiving = EntityHelper.getTargetInFieldOfVision((EntityLivingBase) entity, 21, 10, false);
				else if (!EntityHelper.isInFieldOfVision(entity, entityLiving, 10) || entityLiving.getDistanceToEntity(entity) > 21)
					entityLiving = null;
				if (!checkTargetInShootingView((EntityLivingBase) entity, entityLiving))
					entityLiving = null;
				Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.MOIRA_DAMAGE, entity.world, (EntityLivingBase) entity, 0xFFFFFF, 0xFFFFFF, 1, 8, 1.1f, 1.1f, entity.world.rand.nextFloat(), 0.1f, EnumHand.MAIN_HAND, 20, 0.6f);
				if (entityLiving != null)
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.MOIRA_DAMAGE, entity.world, entityLiving, 0xFFFFFF, 0xFFFFFF, 1, 8, 3, 3, entity.world.rand.nextFloat(), 0.1f);
			}
			else 
				this.ticksLeft = 10;
			// sounds
			if (entity.ticksExisted % 14 == 0) 
				if (entityLiving == null)
					ModSoundEvents.MOIRA_DAMAGE_DURING_MISS.playFollowingSound(entity, 1, 1, false);
				else
					ModSoundEvents.MOIRA_DAMAGE_DURING_HIT.playFollowingSound(entity, 2, 1, false);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			// basic checks
			if (!(entity instanceof EntityLivingBase) || !entity.isEntityAlive() ||
					((EntityLivingBase)entity).getHeldItemMainhand() == null || 
					((EntityLivingBase)entity).getHeldItemMainhand().getItem() != EnumHero.MOIRA.weapon ||
					!KeyBind.RMB.isKeyDown((EntityLivingBase) entity) || 
					EnumHero.MOIRA.weapon.hasCooldown(entity)) 
				return true;
			// find new target / clear target
			else if (entity.ticksExisted % 5 == 0) {
				if (entityLiving == null || !entityLiving.isEntityAlive()) 
					entityLiving = EntityHelper.getTargetInFieldOfVision((EntityLivingBase) entity, 21, 10, false);
				else if (!EntityHelper.isInFieldOfVision(entity, entityLiving, 10) || entityLiving.getDistanceToEntity(entity) > 21)
					entityLiving = null;
				if (!checkTargetInShootingView((EntityLivingBase) entity, entityLiving))
					entityLiving = null;
			}
			else 
				this.ticksLeft = 10;
			return super.onServerTick();
		}
		@Override
		public Handler onServerRemove() {
			Minewatch.network.sendToDimension(new SPacketSimple(48, entity, false),  entity.world.provider.getDimension());
			return super.onServerRemove();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			ModSoundEvents.MOIRA_DAMAGE_DURING_MISS.stopSound(Minecraft.getMinecraft().player);
			ModSoundEvents.MOIRA_DAMAGE_DURING_HIT.stopSound(Minecraft.getMinecraft().player);
			return super.onClientRemove();
		}
	};

	public static HashMap<EntityPlayer, Boolean> fadeViewBobbing = Maps.newHashMap();
	public static Handler FADE = new Handler(Identifier.MOIRA_FADE, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (player == Minecraft.getMinecraft().player) {
				if (!player.capabilities.isFlying)
					entityLiving.motionY += 0.038f;
				if (this.ticksLeft > 1)
					Minecraft.getMinecraft().gameSettings.viewBobbing = false;
				else if (fadeViewBobbing.containsKey(player)) {
					Minecraft.getMinecraft().gameSettings.viewBobbing = fadeViewBobbing.get(player);
					fadeViewBobbing.remove(player);
				}
			}
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			// set resistance high to prevent hurt sound/animations 
			// (entityLiving.hurtResistantTime > entityLiving.maxHurtResistantTime / 2.0F)
			entityLiving.hurtResistantTime = (int) (entityLiving.maxHurtResistantTime*2.1f); 
			entityLiving.motionY += 0.038f;
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.REAPER_TELEPORT_BASE_0, entityLiving.world, 
					entityLiving.posX, entityLiving.posY+entityLiving.height/2f, entityLiving.posZ, 0, 0, 0, 0xFFFFFF, 0xAAAAAA, 1, 5, 5, 17, entityLiving.world.rand.nextFloat(), 0.3f);
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			EnumHero.MOIRA.ability3.keybind.setCooldown(entityLiving, 120, false); 
			entityLiving.hurtResistantTime = 0;
			return super.onServerRemove();
		}
	};

	public ItemMoiraWeapon() {
		super(0); 
		this.maxCharge = 180;
		this.rechargeRate = 1/5f;
		this.hasOffhand = true;
		this.showHealthParticles = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 	
		// heal
		if (hand == EnumHand.OFF_HAND && this.canUse(player, true, hand, false) && this.getCurrentCharge(player) >= 1 && 
				!KeyBind.RMB.isKeyDown(player) && !TickHandler.hasHandler(player, Identifier.MOIRA_ORB_SELECT)) {
			this.subtractFromCurrentCharge(player, 1, player.ticksExisted % 10 == 0);
			if (!world.isRemote) {
				EntityMoiraHealEnergy energy = new EntityMoiraHealEnergy(world, player, hand.ordinal());
				EntityHelper.setAim(energy, player, player.rotationPitch, player.rotationYawHead, 30, 0,  
						hand, 20, 0.6f);
				world.spawnEntity(energy);
				if (KeyBind.LMB.isKeyPressed(player)) 
					ModSoundEvents.MOIRA_HEAL_START.playFollowingSound(player, world.rand.nextFloat()+0.5F, 1, false);
				else if (player.ticksExisted % 24 == 0)
					ModSoundEvents.MOIRA_HEAL_DURING.playFollowingSound(player, world.rand.nextFloat()+0.5F, 1, false);
				if (world.rand.nextInt(100) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHeldItemMainhand() == stack &&
				((EntityLivingBase)entity).getActiveItemStack() != stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// fade
			if (hero.ability3.isSelected(player, true) && !world.isRemote &&
					this.canUse((EntityLivingBase) entity, true, EnumHand.MAIN_HAND, true)) {
				TickHandler.unregister(false, TickHandler.getHandler(player, Identifier.MOIRA_DAMAGE));
				TickHandler.register(false, Ability.ABILITY_USING.setEntity(player).setTicks(16).setAbility(hero.ability3),
						FADE.setEntity(player).setTicks(16), Handlers.INVULNERABLE.setEntity(player).setTicks(16)); 
				Minewatch.network.sendToDimension(new SPacketSimple(47, player, false), world.provider.getDimension());
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 16, 16, true, false));
				player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 16, 0, true, false));
				player.extinguish();
				ModSoundEvents.MOIRA_FADE.playFollowingSound(player, 1, 1, false);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.REAPER_TELEPORT_BASE_0, world, 
						player.posX, player.posY+player.height/2f, player.posZ, 0, 0, 0, 0xFFFFFF, 0xAAAAAA, 1, 5, 17, 5, world.rand.nextFloat(), 0.3f);
			}

			// orb select
			if (hero.ability2.isSelected(player, player instanceof EntityPlayer) && !world.isRemote && 
					this.canUse((EntityLivingBase) entity, true, EnumHand.MAIN_HAND, true)) {
				Handler handler =  TickHandler.getHandler(player, Identifier.MOIRA_ORB_SELECT);
				if (handler == null) {
					TickHandler.register(false, ORB_SELECT.setEntity(player).setTicks(12000)); 
					Minewatch.network.sendToDimension(new SPacketSimple(49, player, true), world.provider.getDimension());
					ModSoundEvents.MOIRA_ORB_SELECT.playFollowingSound(player, 1, 1, false);
				}
			}
		}
	}	

	public static boolean checkTargetInShootingView(EntityLivingBase entity, Entity target) {
		Vector2f rotations = EntityHelper.getEntityPartialRotations(entity);
		Vec3d shooting = EntityHelper.getShootingPos(entity, rotations.x, rotations.y, EnumHand.MAIN_HAND, 20, 0.6f);
		return EntityHelper.canEntityBeSeen(shooting, target);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		// find new damage target
		if (!world.isRemote && hand == EnumHand.MAIN_HAND && this.canUse(player, true, hand, false) &&
				!TickHandler.hasHandler(player, Identifier.MOIRA_ORB_SELECT)) {
			// start damage
			if (!TickHandler.hasHandler(player, Identifier.MOIRA_DAMAGE)) {
				EntityLivingBase target = EntityHelper.getTargetInFieldOfVision(player, 21, 10, false);
				if (!checkTargetInShootingView(player, target))
					target = null;
				TickHandler.register(false, DAMAGE.setEntity(player).setEntityLiving(target).setTicks(10));
				Minewatch.network.sendToDimension(new SPacketSimple(48, player, true, target), world.provider.getDimension());
				ModSoundEvents.MOIRA_DAMAGE_START.playFollowingSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f, false);
				if (target != null)
					ModSoundEvents.MOIRA_DAMAGE_VOICE.playFollowingSound(player, 1, 1, false);
			}

			// do effects
			Handler handler = TickHandler.getHandler(player, Identifier.MOIRA_DAMAGE);
			if (handler != null && handler.entityLiving != null && 
					EntityHelper.attemptDamage(player, handler.entityLiving, 2.5f, true, true)) {
				if (!(handler.entityLiving instanceof EntityLivingBaseMW))
					EntityHelper.heal(player, 1.5f);
				this.setCurrentCharge(player, this.getCurrentCharge(player)+1f, true);
			}
		}

		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return (Minewatch.proxy.getClientPlayer() != null && 
				TickHandler.hasHandler(Minewatch.proxy.getClientPlayer(), Identifier.MOIRA_FADE)) ? 
						true : super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderDamageBeam(RenderWorldLastEvent event) {
		// damage
		for (Handler handler : TickHandler.getHandlers(true, null, Identifier.MOIRA_DAMAGE, null)) {
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.depthMask(false);
			GlStateManager.color(1, 1, 1, (float) (0.4f+Math.abs(Math.sin(handler.entity.ticksExisted/5d))/3d));
			vertexbuffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);

			Vec3d entityLivingPos = EntityHelper.getEntityPartialPos(handler.entityLiving);
			Vector2f rotations = EntityHelper.getEntityPartialRotations(handler.entity);
			Vec3d shooting = EntityHelper.getShootingPos((EntityLivingBase) handler.entity, rotations.x, rotations.y, EnumHand.MAIN_HAND, 20, 0.6f);
			Vec3d vec = shooting.subtract(EntityHelper.getEntityPartialPos(Minewatch.proxy.getRenderViewEntity()));
			if (handler.entityLiving != null) {
				rotations = EntityHelper.getDirectLookAngles(shooting, entityLivingPos.addVector(0, handler.entityLiving.height/2f, 0));
				rotations = new Vector2f(rotations.y, rotations.x);
			}
			double x = vec.x; 
			double y = vec.y;
			double z = vec.z;

			double pan = ((handler.entity.ticksExisted+Minewatch.proxy.getRenderPartialTicks()))/12d;
			if (handler.entityLiving != null)
				pan *= 2d;
			vertexbuffer.pos(x, y, z).tex(0.5d, pan).endVertex();

			Minecraft.getMinecraft().getTextureManager().bindTexture(handler.entityLiving == null ? DAMAGE_BEAM_MISS : DAMAGE_BEAM_HIT);

			double distance = handler.entityLiving == null ? 5 : handler.entityLiving.getDistanceToEntity(handler.entity)-handler.entity.width/2f-handler.entityLiving.width/2f;
			double size = handler.entityLiving == null ? 5 : 2;
			double deg_to_rad = 0.0174532925d;
			double precision = 0.05d;
			double degrees = 360d;
			double steps = Math.round(degrees*precision);
			degrees += 21.2d;
			double angle = 0;

			for (int j=0; j<2; j++) {
				pan += j;
				size += j;

				for (int i=1; i<=steps; i++) {
					angle = degrees/steps*i;
					Vec3d target = EntityHelper.getLook((float) (rotations.x+size*Math.cos(angle*deg_to_rad)), (float) (rotations.y+size*Math.sin(angle*deg_to_rad))).scale(distance);
					vertexbuffer.pos(x+target.x, y+target.y, z+target.z).tex((i-1)/(steps-1), pan+0.5d).endVertex();
				}

				for (int i=(int) steps; i>0; i--) {
					angle = degrees/steps*i;
					Vec3d target = EntityHelper.getLook((float) (rotations.x+size*Math.cos(angle*deg_to_rad)), (float) (rotations.y+size*Math.sin(angle*deg_to_rad))).scale(distance);
					vertexbuffer.pos(x+target.x, y+target.y, z+target.z).tex((i-1)/(steps-1), pan+0.5d).endVertex();
				}
			}

			tessellator.draw();
			GlStateManager.depthMask(true);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}

	enum Render {
		NONE, INACTIVE, ACTIVE
	}
	
	public Render getRenderHandType(EntityLivingBase entity, EnumHand hand) {
		// select
		if (TickHandler.hasHandler(entity, Identifier.MOIRA_ORB_SELECT))
			return Render.ACTIVE;
		// don't render heal if damaging
		else if (KeyBind.RMB.isKeyDown(entity) && 
				!this.hasCooldown(entity) && EntityHelper.isHoldingItem(entity, this, EnumHand.MAIN_HAND))
			return hand == EnumHand.MAIN_HAND ? Render.ACTIVE : Render.NONE;
		// don't render damage if healing
		else if (KeyBind.LMB.isKeyDown(entity) && 
				!this.hasCooldown(entity) && EntityHelper.isHoldingItem(entity, this, EnumHand.OFF_HAND))
			return hand == EnumHand.OFF_HAND ? Render.ACTIVE : Render.NONE;
		else if (EntityHelper.isHoldingItem(entity, this, hand))
			return Render.INACTIVE;
		else
			return Render.NONE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean preRenderArmor(EntityLivingBase entity, ModelMWArmor model) { 
		// damage
		if (this.getRenderHandType(entity, EnumHand.MAIN_HAND) == Render.ACTIVE) {
			model.bipedRightArmwear.rotateAngleX = 5;
			model.bipedRightArm.rotateAngleX = 5;
			model.bipedRightArmwear.rotateAngleY = 0.2f;
			model.bipedRightArm.rotateAngleY = 0.2f;
		}
		// heal
		if (this.getRenderHandType(entity, EnumHand.OFF_HAND) == Render.ACTIVE) {
			model.bipedLeftArmwear.rotateAngleX = 5;
			model.bipedLeftArm.rotateAngleX = 5;
			model.bipedLeftArmwear.rotateAngleY = -0.2f;
			model.bipedLeftArm.rotateAngleY = -0.2f;
		}

		// fade
		if (TickHandler.hasHandler(entity, Identifier.MOIRA_FADE)) {
			GlStateManager.enableCull();
			GlStateManager.color(1, 1, 1, 0);
			return true;
		}
		else
			return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType transform, Pair<? extends IBakedModel, Matrix4f> ret) {
		//if (SetManager.getWornSet(entity) == EnumHero.MOIRA) {
			boolean select = TickHandler.hasHandler(entity, Identifier.MOIRA_ORB_SELECT);
			// damage
			if (this.getRenderHandType(entity, EnumHand.MAIN_HAND) == Render.ACTIVE) {
				if (transform == TransformType.THIRD_PERSON_RIGHT_HAND && entity.getHeldItemMainhand() == stack && 
						entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST) != null && 
						entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemMWArmor) {
					GlStateManager.rotate(50, 29f, -10f, -1.2f);
					GlStateManager.translate(0.15f, 0.5f, -0.11f);
				}
				else if (!select && transform == TransformType.FIRST_PERSON_LEFT_HAND && entity.getHeldItemOffhand() == stack) 
					ret.getRight().setScale(0);
			}
			// heal
			if (this.getRenderHandType(entity, EnumHand.OFF_HAND) == Render.ACTIVE) {
				if (transform == TransformType.THIRD_PERSON_LEFT_HAND && entity.getHeldItemOffhand() == stack && 
						entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST) != null && 
						entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemMWArmor) {
					GlStateManager.rotate(50, 29f, 10f, 1.2f);
					GlStateManager.translate(-0.15f, 0.5f, -0.11f);
				}
				else if (!select && transform == TransformType.FIRST_PERSON_RIGHT_HAND && entity.getHeldItemMainhand() == stack) 
					ret.getRight().setScale(0);
			}
		//}

		// fade
		if (transform != TransformType.GUI && TickHandler.hasHandler(entity, Identifier.MOIRA_FADE)) 
			ret.getRight().setScale(0);
		return ret;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height, EnumHand hand) {
		super.preRenderGameOverlay(event, player, width, height, hand);

		// heal/damage orb overlay
		if (hand == EnumHand.MAIN_HAND && event.getType() == ElementType.CROSSHAIRS && 
				TickHandler.getHandler(player, Identifier.MOIRA_ORB_SELECT) != null) {
			GlStateManager.enableBlend();

			double scale = 0.8d*Config.guiScale;
			GlStateManager.scale(scale, scale, 1);
			GlStateManager.translate((int) ((width - 256*scale)/2d / scale), (int) ((height - 256*scale)/2d / scale), 0);
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/moira_orb.png"));
			GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);

			GlStateManager.disableBlend();
		}
		// moira fade
		else if (event.getType() == ElementType.ALL && TickHandler.hasHandler(player, Identifier.MOIRA_FADE)) {
			Handler handler = TickHandler.getHandler(player, Identifier.MOIRA_FADE);
			float ticks = handler.initialTicks - handler.ticksLeft+Minecraft.getMinecraft().getRenderPartialTicks();

			GlStateManager.pushMatrix();
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
			GlStateManager.enableBlend();
			//PORT scale x event.getResolution().getScaleFactor()
			GlStateManager.scale(width/256d, height/256d, 1);
			int firstImage = (int) (ticks / 10);
			int secondImage = firstImage + 1;
			if (firstImage < 6) {
				GlStateManager.color(1, 1, 1, 1.1f-((ticks) % 10)/10f);
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/reaper_wraith_"+firstImage+".png"));
				GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);
			}
			if (secondImage < 6) {
				GlStateManager.color(1, 1, 1, (ticks % 10)/10f);
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/reaper_wraith_"+secondImage+".png"));
				GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);
			}
			GlStateManager.popMatrix();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderHand(AbstractClientPlayer player, EnumHand hand) {
		return !TickHandler.hasHandler(player, Identifier.MOIRA_FADE) && 
				((hand == EnumHand.MAIN_HAND && this.getRenderHandType(player, EnumHand.MAIN_HAND) != Render.NONE) || 
				(hand == EnumHand.OFF_HAND && this.getRenderHandType(player, EnumHand.OFF_HAND) != Render.NONE));
	}

}