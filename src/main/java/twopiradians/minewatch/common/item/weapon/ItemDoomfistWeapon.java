package twopiradians.minewatch.common.item.weapon;

import java.awt.Color;
import java.util.ArrayList;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.projectile.EntityDoomfistBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.HealthManager;
import twopiradians.minewatch.common.hero.HealthManager.Type;
import twopiradians.minewatch.common.hero.RenderManager;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemDoomfistWeapon extends ItemMWWeapon {

	public static final Handler PUNCH_ANIMATIONS = new Handler(Identifier.DOOMFIST_PUNCH_ANIMATIONS, true) {};
	public static final ResourceLocation OVERLAY = new ResourceLocation(Minewatch.MODID, "textures/gui/doomfist_overlay.png");
	public static final ResourceLocation PUNCH_OVERLAY = new ResourceLocation(Minewatch.MODID, "textures/gui/doomfist_punch.png");
	public static final Handler RELOAD = new Handler(Identifier.DOOMFIST_RELOAD, false) {
		@Override
		public Handler onServerRemove() {
			if (entityLiving != null && entityLiving.getHeldItemMainhand() != null && 
					entityLiving.getHeldItemMainhand().getItem() == EnumHero.DOOMFIST.weapon) {
				EnumHero.DOOMFIST.weapon.setCurrentAmmo(entityLiving, EnumHero.DOOMFIST.weapon.getCurrentAmmo(entityLiving)+1, EnumHand.MAIN_HAND);
				ModSoundEvents.DOOMFIST_RELOAD.playFollowingSound(entityLiving, 0.7f, entityLiving.world.rand.nextFloat()/2+0.75f, false);
			}
			return super.onServerRemove();
		}
	};

	public static void spawnSlamParticles(World world, float yaw, Vec3d vec) {
		Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_SLAM_1, world, vec.x, vec.y, vec.z, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 0.8f, 100, 60, 60, (yaw + 90f) / 180f, 0, EnumFacing.UP, false);
		Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_SLAM_2, world, vec.x, vec.y, vec.z, 0, 0, 0, 0x90FFF9, 0x90FFF9, 0.5f, 10, 60, 60, (yaw + 90f) / 180f, 0, EnumFacing.UP, false);
	}
	
	/**number = ticks running, number2 = initial posY, position = targetPosition, bool = used while onGround*/
	public static final Handler SLAM = new Handler(Identifier.DOOMFIST_SLAM, true) {
		public void move() {
			entity.fallDistance = 0;
			if (this.ticksLeft == this.initialTicks) { // moving up for first tick
				//if (entity.world.isRemote) // debug particle
				//	Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, entity.world, position.x, position.y+0.05d, position.z, 0, 0, 0, 0xFF0000, 0xFF0000, 1, 300, 10, 10, 0, 0, EnumFacing.UP, false);
				bool = entity.onGround;
				entity.onGround = false;
				entity.setSneaking(false);
				entity.motionY += 3d;
			}
			else if (!entity.onGround && number < 40) { // moving forward+down 
				if (this.ticksLeft == this.initialTicks-1) {
					number2 = entity.posY;
					TickHandler.register(entity.world.isRemote,	Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks(50).setBoolean(true));
				}
				Vector2f angles = EntityHelper.getDirectLookAngles(EntityHelper.getPositionEyes(entity), position);
				float prev = entity.rotationYaw;
				entity.rotationYaw = angles.x;
				entity.moveRelative(0, 0, 1, 1);
				entity.rotationYaw = prev;
				if (entityLiving != null) {
					entityLiving.moveForward = 0;
					entityLiving.moveStrafing = 0;
				}
				double horizontalDistance = entity.getDistance(position.x, entity.posY, position.z);
				double verticalDistance = Math.abs(entity.posY-position.y);
				double verticalPercent = MathHelper.clamp((verticalDistance / Math.max(0.1d, Math.abs(number2-position.y))), 0, 1d); 
				double scale = MathHelper.clamp(horizontalDistance, 0, 2.5d); // scale speed by horizontal distance
				if (entity.posY < position.y)
					scale = 0;
				Vec3d motion = new Vec3d(entity.motionX, 0, entity.motionZ).normalize().scale(scale);
				entity.motionX = motion.x;
				entity.motionZ = motion.z;
				entity.motionY = Math.max(-Math.abs(entity.posY-position.y)-0.02d, bool ? -Math.min(number/10f, 1d) : -1.5d); // scale by vertical distance
				if (horizontalDistance > 5 && verticalDistance < 5)
					entity.motionY *= Math.max(verticalPercent, 0.5d);
				this.ticksLeft = 2;
			}
			else { // on ground or time's up
				this.ticksLeft = 1;
				if (entity.onGround) {
					if (!entity.world.isRemote) { // particles
						// damage
						double yOffset = 0;
						for (int i=0; i<3; ++i) {
							IBlockState state = entity.world.getBlockState(new BlockPos(entity.getPositionVector().addVector(0, -i, 0)));
							if (state.getRenderType() != EnumBlockRenderType.INVISIBLE && state.getRenderType() != EnumBlockRenderType.LIQUID) {
								yOffset = -i;
								break;
							}
						}
						int damage = MathHelper.clamp(getSlamCharge((EntityLivingBase) entity), 11, 125);
						Vec3d look = EntityHelper.getLook(0, entity.rotationYaw).scale(4d);
						AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(4.5d, 0, 4.5d).offset(look.x, yOffset, look.z);
						int entitiesHit = 0;
						for (Entity target : entity.world.getEntitiesWithinAABBExcludingEntity(entity, aabb)) 
							if (target != entityLiving && target != entity && 
							target instanceof EntityLivingBase && 
							EntityHelper.shouldHit(entity, target, false)) {
								if (target.isEntityAlive() && EntityHelper.isInFieldOfVision(entity, target, 45, entity.rotationYaw, 0) && 
										((EntityLivingBase)entity).canEntityBeSeen(target) && 
										EntityHelper.attemptDamage(entity, target, damage, true) && !EntityHelper.shouldIgnoreEntity(target)) {
									target.onGround = false;
									Vec3d motion = new Vec3d(entity.posX-target.posX, 0, entity.posZ-target.posZ).normalize().scale(0.3d);
									target.motionX = motion.x;
									target.motionZ = motion.z;
									target.motionY = 0.4d;
									entitiesHit++;
								}
							}
						float shield = Math.min(150f-HealthManager.getCurrentHealth((EntityLivingBase)entity, EnumHero.DOOMFIST, Type.SHIELD_ABILITY), entitiesHit*30f);
						if (shield > 0) {
							HealthManager.addHealth((EntityLivingBase) entity, Type.SHIELD_ABILITY, shield);
							HealthManager.setShieldAbilityDecay((EntityLivingBase) entity, shield, 3, 20); 
						}
					}
					// particle - detect on client (server very inaccurate), send to server, which sends to other clients
					else {
						double yOffset = 0.05d;
						BlockPos pos = new BlockPos(entity.getPositionVector());
						for (int i=0; i<5; ++i) {
							IBlockState state = entity.world.getBlockState(pos);
							if (state.getRenderType() != EnumBlockRenderType.INVISIBLE && state.getRenderType() != EnumBlockRenderType.LIQUID) {
								yOffset += state.getBoundingBox(entity.world, pos).maxY;
								Vec3d vec = new Vec3d(entity.posX, pos.getY()+yOffset, entity.posZ).add(EntityHelper.getLook(0, entity.getRotationYawHead()).scale(3d));
								Minewatch.network.sendToServer(new CPacketSimple(19, entity, false, vec.x, vec.y, vec.z, entity.getRotationYawHead(), 0, 0));
								break;
							}
							else
								pos = pos.down();
						}
					}
				}
				entity.motionX = 0;
				entity.motionZ = 0;
			}
			++number;
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			move();
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			move();
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			if (entity.onGround)
				ModSoundEvents.DOOMFIST_SLAM_STOP.playFollowingSound(entity, 1, 1, false);
			TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.ABILITY_USING),
					TickHandler.getHandler(entity, Identifier.PREVENT_MOVEMENT));
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			if (entity instanceof EntityLivingBase) {
				EnumHero.DOOMFIST.ability2.keybind.setCooldown((EntityLivingBase) entity, 140, false);	
				TickHandler.unregister(false, TickHandler.getHandler(entity, Identifier.ABILITY_USING),
						TickHandler.getHandler(entity, Identifier.PREVENT_MOVEMENT));
			}	
			return super.onServerRemove();
		}
	};
	public static final Handler UPPERCUT = new Handler(Identifier.DOOMFIST_UPPERCUT, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			moveUppercut(this);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			moveUppercut(this);
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {

			return super.onServerRemove();
		}
	};
	public static final Handler UPPERCUTTING = new Handler(Identifier.DOOMFIST_UPPERCUTTING, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			moveUppercut(this);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			moveUppercut(this);
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			return super.onServerRemove();
		}
	};
	private static void moveUppercut(Handler handler) {
		if (handler.ticksLeft > 1) { // moving up
			handler.entity.setSneaking(false);
			handler.entity.onGround = false;
			handler.entity.motionY = Math.max(handler.entity.motionY, 1);
			if (handler.ticksLeft == handler.initialTicks && handler.identifier == Identifier.DOOMFIST_UPPERCUTTING)
				handler.entity.moveRelative(0, 0, 0.3f, 1);
		}
		else if (!handler.entity.onGround && handler.number < 40) { // done moving up, slowly fall down
			if (handler.entity.motionY < 0)
				handler.entity.motionY = Math.min(handler.entity.motionY+0.03d, 0);
			handler.ticksLeft = 2;
		}
		else // on ground, stop handler
			handler.ticksLeft = 1;
		handler.entity.fallDistance = 0;
		if (++handler.number == 4 && !handler.entity.world.isRemote &&
				handler.entity instanceof EntityLivingBase)
			EnumHero.DOOMFIST.ability3.keybind.setCooldown((EntityLivingBase) handler.entity, 140, false);

		// check for entities to knockback
		if (!handler.entity.world.isRemote && handler.identifier == Identifier.DOOMFIST_UPPERCUTTING &&
				handler.ticksLeft > 2) {
			Vec3d look = handler.entity.getLookVec().scale(1d);
			AxisAlignedBB aabb = handler.entity.getEntityBoundingBox().grow(1d).offset(look);
			int entitiesHit = 0;
			for (Entity target : handler.entity.world.getEntitiesWithinAABBExcludingEntity(handler.entity, aabb)) 
				if (target != handler.entityLiving && target != handler.entity && 
				target instanceof EntityLivingBase && 
				EntityHelper.shouldHit(handler.entity, target, false)) {
					if (target.isEntityAlive() && !TickHandler.hasHandler(target, Identifier.DOOMFIST_UPPERCUT) && 
							EntityHelper.attemptDamage(handler.entity, target, 50, false) && !EntityHelper.shouldIgnoreEntity(target)) {
						target.motionX += handler.entity.motionX*2f;
						target.motionZ += handler.entity.motionZ*2f;
						TickHandler.interrupt(target);
						Minewatch.network.sendToDimension(new SPacketSimple(63, handler.entity, true, target, handler.ticksLeft+1, 0, 0), handler.entity.world.provider.getDimension());
						TickHandler.register(false, UPPERCUT.setEntity(target).setEntityLiving((EntityLivingBase) handler.entity).setTicks(handler.ticksLeft+1).setAllowDead(true));
						entitiesHit++;
					}
				}
			float shield = Math.min(150f-HealthManager.getCurrentHealth((EntityLivingBase)handler.entity, EnumHero.DOOMFIST, Type.SHIELD_ABILITY), entitiesHit*30f);
			if (shield > 0) {
				HealthManager.addHealth((EntityLivingBase) handler.entity, Type.SHIELD_ABILITY, shield);
				HealthManager.setShieldAbilityDecay((EntityLivingBase) handler.entity, shield, 3, 20); 
			}
		}
	}
	/**number = punch power 0-1, number2 = yaw of punch, entity = getting punched, entityLiving = doomfist*/
	public static final Handler PUNCHED = new Handler(Identifier.DOOMFIST_PUNCHED, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			movePunch(this);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			movePunch(this);
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			if (entity instanceof EntityLivingBase) {
				// spawn particle
				RayTraceResult result = EntityHelper.getMouseOverBlock((EntityLivingBase) entity, 11, 0, (float) number2);
				if (result != null) {
					entity.motionX = 0;
					entity.motionZ = 0;
					if (!bool)
						ModSoundEvents.DOOMFIST_PUNCH_HIT_WALL.playFollowingSound(entity, 1, 1, false);
					double x = result.hitVec.x; 
					double y = result.hitVec.y;
					double z = result.hitVec.z;
					if (result.sideHit == EnumFacing.SOUTH)
						z = Math.ceil(z);
					else if (result.sideHit == EnumFacing.EAST)
						x = Math.ceil(x);
					else if (result.sideHit == EnumFacing.UP)
						y = Math.ceil(y);
					Vec3d pos = new Vec3d(x, y, z);
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_PUNCH_0, entity.world, pos.x, pos.y, pos.z, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1.0f, 300, 15, 15, entity.world.rand.nextFloat(), 0, result.sideHit, true);
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_PUNCH_1, entity.world, pos.x, pos.y, pos.z, 0, 0, 0, 0xFFFFFF, 0x62B4FE, 0.7f, 10, 15, 15, entity.world.rand.nextFloat(), 0, result.sideHit, true);
				}
			}
			if (entity instanceof EntityLivingBase) {
				entity.motionX = 0;
				entity.motionZ = 0;
				entity.velocityChanged = true;
			}
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			if (entity instanceof EntityLivingBase) {
				entity.motionX = 0;
				entity.motionZ = 0;
				entity.velocityChanged = true;
			}
			return super.onServerRemove();
		}
	};
	/**number = punch power 0-1, bool = hit wall / entity, position = starting position*/
	public static final Handler PUNCH = new Handler(Identifier.DOOMFIST_PUNCH, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			movePunch(this);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			movePunch(this);
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {			
			if (entity instanceof EntityLivingBase) {
				entity.motionX *= ((EntityLivingBase)entity).moveForward;
				entity.motionZ *= ((EntityLivingBase)entity).moveForward;
				entity.velocityChanged = true;
				entity.hurtResistantTime = 0;

				// spawn particle
				RayTraceResult result = EntityHelper.getMouseOverBlock((EntityLivingBase) entity, 5, 0, entity.rotationYaw);
				if (result != null) {
					entity.motionX = 0;
					entity.motionZ = 0;
					if (!bool) {
						ModSoundEvents.REINHARDT_CHARGE_HIT.playFollowingSound(entity, 1, 1, false);
						TickHandler.register(true, PUNCH_ANIMATIONS.setEntity(entity).setTicks(5));
					}
					double x = result.hitVec.x; 
					double y = result.hitVec.y;
					double z = result.hitVec.z;
					if (result.sideHit == EnumFacing.SOUTH)
						z = Math.ceil(z);
					else if (result.sideHit == EnumFacing.EAST)
						x = Math.ceil(x);
					else if (result.sideHit == EnumFacing.UP)
						y = Math.ceil(y);
					Vec3d pos = new Vec3d(x, y, z);
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_PUNCH_0, entity.world, pos.x, pos.y, pos.z, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1.0f, 300, 15, 15, entity.world.rand.nextFloat(), 0, result.sideHit, true);
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_PUNCH_1, entity.world, pos.x, pos.y, pos.z, 0, 0, 0, 0xFFFFFF, 0x62B4FE, 0.7f, 10, 15, 15, entity.world.rand.nextFloat(), 0, result.sideHit, true);
				}
			}
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			EnumHero.DOOMFIST.ability1.keybind.setCooldown(entityLiving, 80, false);
			if (bool)
				ModSoundEvents.DOOMFIST_PUNCH_DURING.stopFollowingSound(entity);
			entity.hurtResistantTime = 0;
			return super.onServerRemove();
		}
	};
	private static void movePunch(Handler handler) {
		if (handler.identifier == Identifier.DOOMFIST_PUNCH &&
				handler.ticksLeft == handler.initialTicks)
			handler.position = handler.entity.getPositionVector();
		handler.entity.setSneaking(false);
		float prev = handler.entity.rotationYaw;
		if (handler.identifier == Identifier.DOOMFIST_PUNCHED)
			handler.entity.rotationYaw = (float) handler.number2;
		handler.entity.moveRelative(0, 0, 1, 1);
		handler.entity.rotationYaw = prev;
		Vec3d motion = new Vec3d(handler.entity.motionX, 0, handler.entity.motionZ).normalize().scale((22d+(37d*handler.number))/20d); 	
		handler.entity.motionX = motion.x;
		handler.entity.motionZ = motion.z;
		if (handler.entity.motionY < 0)
			handler.entity.motionY *= 0.5d;
		if (handler.entity instanceof EntityLivingBase) {
			((EntityLivingBase)handler.entity).moveForward = 0;
			((EntityLivingBase)handler.entity).moveStrafing = 0;
			if (handler.identifier == Identifier.DOOMFIST_PUNCH) // immunity from vanilla attacks while punching
				handler.entity.hurtResistantTime = ((EntityLivingBase)handler.entity).maxHurtResistantTime;
		}

		// check for entities to hit
		if (handler.identifier == Identifier.DOOMFIST_PUNCH) {
			Vec3d look = handler.entity.getLookVec().scale(1d);
			AxisAlignedBB aabb = handler.entity.getEntityBoundingBox().grow(1d).offset(look);
			for (Entity target : handler.entity.world.getEntitiesWithinAABBExcludingEntity(handler.entity, aabb)) 
				if (target != handler.entityLiving && target != handler.entity && 
				target instanceof EntityLivingBase && 
				EntityHelper.shouldHit(handler.entity, target, false) && target.isEntityAlive() &&
				((EntityLivingBase)handler.entity).canEntityBeSeen(target) && !EntityHelper.shouldIgnoreEntity(target)) {
					/*if (target.isEntityAlive() && (TickHandler.hasHandler(target, Identifier.REINHARDT_CHARGE) ||
							TickHandler.hasHandler(target, Identifier.DOOMFIST_PUNCH))) {
						handler.ticksLeft = 20; // TODO (eventually?) punch + punch, charge + charge
						TickHandler.unregister(false, TickHandler.getHandler(handler.entity, Identifier.HERO_SNEAKING));
						TickHandler.register(false, Handlers.PREVENT_INPUT.setEntity(handler.entity).setTicks(handler.ticksLeft),
								Handlers.PREVENT_ROTATION.setEntity(handler.entity).setTicks(handler.ticksLeft), 
								Handlers.PREVENT_MOVEMENT.setEntity(handler.entity).setTicks(handler.ticksLeft));
						TickHandler.register(false, Handlers.PREVENT_INPUT.setEntity(target).setTicks(handler.ticksLeft),
								Handlers.PREVENT_ROTATION.setEntity(target).setTicks(handler.ticksLeft), 
								Handlers.PREVENT_MOVEMENT.setEntity(target).setTicks(handler.ticksLeft));
						TickHandler.getHandler(handler.entity, Identifier.PREVENT_MOVEMENT).setBoolean(false);
						Minewatch.network.sendToDimension(new SPacketSimple(58, handler.entity, true, target), handler.entity.world.provider.getDimension());
					}
					else */if (EntityHelper.attemptDamage(handler.entity, target, (float) (49f+(51f*handler.number)), true)) {
						ModSoundEvents.DOOMFIST_PUNCH_HIT.playFollowingSound(handler.entity, 0.5f, 1, false);
						TickHandler.interrupt(target);
						Minewatch.network.sendToDimension(new SPacketSimple(62, handler.entity, false, target, handler.initialTicks*0.7f, handler.number, handler.entity.rotationYaw), handler.entity.world.provider.getDimension());
						TickHandler.register(false, Handlers.PREVENT_INPUT.setEntity(target).setTicks((int) (handler.initialTicks*0.7f)),
								Handlers.PREVENT_ROTATION.setEntity(target).setTicks((int) (handler.initialTicks*0.7f)), 
								Handlers.PREVENT_MOVEMENT.setEntity(target).setTicks((int) (handler.initialTicks*0.7f)).setBoolean(true),
								PUNCHED.setEntity(target).setEntityLiving((EntityLivingBase) handler.entity).setTicks((int) (handler.initialTicks*0.7f)).setNumber(handler.number).setNumber2(handler.entity.rotationYaw).setAllowDead(true));
						float shield = Math.min(150f-HealthManager.getCurrentHealth((EntityLivingBase)handler.entity, EnumHero.DOOMFIST, Type.SHIELD_ABILITY), 30f);
						if (shield > 0) {
							HealthManager.addHealth((EntityLivingBase) handler.entity, Type.SHIELD_ABILITY, shield);
							HealthManager.setShieldAbilityDecay((EntityLivingBase) handler.entity, shield, 3, 20); 
						}
					}
					handler.bool = true;
					handler.ticksLeft = 1;
					handler.entity.motionX = 0;
					handler.entity.motionZ = 0;
					handler.entity.velocityChanged = true;
					break;
				}
		}

		// check for wall impact
		if (handler.entity.isCollidedHorizontally) {
			handler.ticksLeft = 1;
			handler.entity.motionX = 0;
			handler.entity.motionZ = 0;
			handler.entity.velocityChanged = true;
			if (handler.entity.world.isRemote && handler.identifier == Identifier.DOOMFIST_PUNCH) {
				handler.bool = true;
				ModSoundEvents.DOOMFIST_PUNCH_HIT_WALL.playFollowingSound(handler.entity, 1, 1, false);
				TickHandler.register(true, PUNCH_ANIMATIONS.setEntity(handler.entity).setTicks(5));
			}
			else if (!handler.entity.world.isRemote && handler.identifier == Identifier.DOOMFIST_PUNCHED && 
					handler.entity instanceof EntityLivingBase && handler.entityLiving != null) {
				EntityHelper.attemptDamage(handler.entityLiving, handler.entity, (float) (49f+(101f*handler.number)), true);
			}
		}
	}

	public ItemDoomfistWeapon() {
		super(13-1);
		this.saveEntityToNBT = true;
	}

	@Override
	public void reload(Entity player) {}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return oldStack != newStack || slotChanged;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase player) {
		player.stopActiveHand();
		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack)	{
		return EnumAction.BOW;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// stop charging punch
		if (ItemDoomfistWeapon.getCharge(player) != -1) {
			player.resetActiveHand();
			ModSoundEvents.DOOMFIST_PUNCH_CHARGE.stopFollowingSound(player);
			ModSoundEvents.DOOMFIST_PUNCH_CHARGE_VOICE.stopFollowingSound(player);
			hero.ability1.keybind.setCooldown(player, 80, false);
		}
		// shoot
		else if (this.canUse(player, true, hand, false) && !world.isRemote) {
			for (int i=0; i<11; ++i) {
				EntityDoomfistBullet projectile = new EntityDoomfistBullet(world, player, EnumHand.OFF_HAND.ordinal());
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 80, 4F, EnumHand.OFF_HAND, 18, 0.6f);
				world.spawnEntity(projectile);
			}
			ModSoundEvents.DOOMFIST_SHOOT.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);
			this.subtractFromCurrentAmmo(player, 1, hand);
			this.setCooldown(player, 7);
			if (world.rand.nextInt(6) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			Handler handler = TickHandler.getHandler(player, Identifier.DOOMFIST_RELOAD);
			if (handler != null)
				handler.ticksLeft = this.reloadTime+1;
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int timeLeft) {
		// release punch
		float charge = getCharge(player);
		if (!world.isRemote && charge > 0) {
			ModSoundEvents.DOOMFIST_PUNCH_CHARGE.stopFollowingSound(player);
			ModSoundEvents.DOOMFIST_PUNCH_CHARGE_VOICE.stopFollowingSound(player);
			ModSoundEvents.DOOMFIST_PUNCH_DURING.playFollowingSound(player, 1, 1, false);
			ModSoundEvents.DOOMFIST_PUNCH_DURING_VOICE.playFollowingSound(player, 1, 1, false);
			player.renderYawOffset = player.rotationYawHead;
			int ticks = 8;
			Minewatch.network.sendToDimension(new SPacketSimple(62, player, true, ticks, charge, 0), world.provider.getDimension());
			TickHandler.register(false, PUNCH.setEntity(player).setTicks(ticks).setNumber(charge),
					Ability.ABILITY_USING.setEntity(player).setTicks(ticks).setAbility(hero.ability1),
					Handlers.PREVENT_ROTATION.setEntity(player).setTicks(ticks));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);	

		// charge punch
		if (!world.isRemote && hand == EnumHand.MAIN_HAND && this.canUse(player, true, hand, true) && 
				hero.ability1.isSelected(player, true)) {
			ModSoundEvents.DOOMFIST_PUNCH_CHARGE.playFollowingSound(player, 1, 1, false);
			if (world.rand.nextBoolean())
				ModSoundEvents.DOOMFIST_PUNCH_CHARGE_VOICE.playFollowingSound(player, 1, 1, false);
			Minewatch.network.sendToDimension(new SPacketSimple(66, player, false), world.provider.getDimension());
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHeldItemMainhand() == stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;
			
			// slam particle
			if (player == Minewatch.proxy.getClientPlayer() && world.isRemote && hero.ability2.keybind.getCooldown(player) <= 0 && 
					player.ticksExisted % 5 == 0 && !player.onGround && !(player instanceof EntityPlayer && ((EntityPlayer)player).isSpectator()))
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.DOOMFIST_SLAM_0, world, player, 0xFFFFFF, 0xFFFFFF, 1, Integer.MAX_VALUE, 60, 60, 0, 0);

			// reload automatically
			if (!world.isRemote && this.getCurrentAmmo(player) < this.getMaxAmmo(player) && !TickHandler.hasHandler(player, Identifier.DOOMFIST_RELOAD)) 
				TickHandler.register(false, RELOAD.setEntity(player).setTicks(this.reloadTime+1));

			// seismic slam
			if (hero.ability2.isSelected(player) &&
					!world.isRemote && this.canUse(player, true, getHand(player, stack), true) && getCharge(player) == -1) {
				RayTraceResult result = EntityHelper.getMouseOverBlock(player, 30);
				if ((result != null && result.hitVec.y <= player.posY) || player.onGround) {
					TickHandler.unregister(false, TickHandler.getHandler(player, Identifier.DOOMFIST_UPPERCUTTING));
					Vec3d targetPos = player.onGround ? player.getPositionVector().add(EntityHelper.getLook(0, player.rotationYaw).scale(7)) : result.hitVec;
					ModSoundEvents.DOOMFIST_SLAM_START.playFollowingSound(player, 1, 1, false);				
					Minewatch.network.sendToDimension(new SPacketSimple(64, player, true, targetPos.x, targetPos.y, targetPos.z), world.provider.getDimension());
					TickHandler.register(false, SLAM.setEntity(player).setTicks(10).setPosition(targetPos).setNumber2(player.posY),
							Ability.ABILITY_USING.setEntity(player).setTicks(50).setAbility(hero.ability2));
				}
			}

			// uppercut
			if (hero.ability3.isSelected(player) &&
					!world.isRemote && this.canUse(player, true, getHand(player, stack), true) && getCharge(player) == -1) {
				ModSoundEvents.DOOMFIST_UPPERCUT_START.playFollowingSound(player, 1, 1, false);		
				ModSoundEvents.DOOMFIST_UPPERCUT_VOICE.playFollowingSound(player, 1, 1, false);	
				int ticks = 4;
				Minewatch.network.sendToDimension(new SPacketSimple(63, player, true, ticks, 0, 0), world.provider.getDimension());
				TickHandler.register(false, UPPERCUTTING.setEntity(player).setTicks(ticks),
						Ability.ABILITY_USING.setEntity(player).setTicks(ticks).setAbility(hero.ability3));
			}
		}
	}

	/**Gets current punch charge 0-1 or -1 if not charging*/
	public static float getCharge(EntityLivingBase entity) {
		if (entity != null && entity.getActiveItemStack() != null && entity.getActiveItemStack().getItem() == EnumHero.DOOMFIST.weapon) {
			float charge = MathHelper.clamp(2f - (float) entity.getItemInUseCount() / 20f, 0, 1);
			return charge;
		}
		return -1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		EntityLivingBase entity = getEntity(Minecraft.getMinecraft().world, stack);
		int model = getModel(entity);
		if (model > 0 && entity != null && entity.getHeldItemMainhand() == stack) {
			float percent = MathHelper.clamp(model == 1 ? ItemDoomfistWeapon.getCharge(entity) : 1, 0, 1);
			return new Color((255f-203f*percent)/255f, (255f-140f*percent)/255f, (255f-1f*percent)/255f).getRGB();
		}
		return -1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderHand(AbstractClientPlayer player, EnumHand hand) {
		return hand == EnumHand.OFF_HAND && this.renderOffHand(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height, EnumHand hand) {
		super.preRenderGameOverlay(event, player, width, height, hand);

		Minecraft mc = Minecraft.getMinecraft();

		if (hand == EnumHand.MAIN_HAND && event.getType() == ElementType.CROSSHAIRS && mc.gameSettings.thirdPersonView == 0) {
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();

			float charge = getCharge(player);

			if (charge != -1) { // charging punch
				GlStateManager.pushMatrix();
				GlStateManager.translate(width/2, height/2, 0);
				mc.getTextureManager().bindTexture(PUNCH_OVERLAY);
				GlStateManager.color(1, 1, 1, 1);
				GuiUtils.drawTexturedModalRect(-128, -128, 0, 0, 256, 256, 0);

				double scale = 3d*Config.guiScale;
				GlStateManager.scale(scale, scale, 1);
				mc.getTextureManager().bindTexture(RenderManager.ABILITY_OVERLAY);
				GlStateManager.color(0.5f, 0.8f, 0.8f, 0.5f);
				GuiUtils.drawTexturedModalRect(-9, 8, 19, 239, 19, 8, 0);
				GlStateManager.color(1, 1, 1, 1);
				GuiUtils.drawTexturedModalRect(-9, 8+(int) (8d*(1d-charge)), 19, (int) (239d+(8d*(1d-charge))), 19, (int) (8d*charge), 0);
				GlStateManager.popMatrix();
			}
			else { // normal ammo overlay
				GlStateManager.pushMatrix();
				double scale = 0.65d*Config.guiScale;
				GlStateManager.translate(width/2, height/2, 0);
				GlStateManager.scale(scale, scale, 1);
				mc.getTextureManager().bindTexture(OVERLAY);
				GlStateManager.color(1, 1, 1, 1);
				GuiUtils.drawTexturedModalRect(-128, -128, 0, 0, 256, 256, 0);
				GlStateManager.popMatrix();

				GlStateManager.pushMatrix();
				GlStateManager.translate(width/2, height/2, 0);
				scale = 2.65d*Config.guiScale;
				GlStateManager.scale(scale, scale, 1);
				mc.getTextureManager().bindTexture(RenderManager.ABILITY_OVERLAY);
				int ammo = this.getCurrentAmmo(player);
				for (int i=0; i<4; ++i) {
					if (ammo > i)
						GlStateManager.color(1, 1, 1, 1);
					else
						GlStateManager.color(0.5f, 0.8f, 0.8f, 0.6f);

					switch(i) {
					case 3:
						GuiUtils.drawTexturedModalRect(-11, 13, 12, 244, 7, 2, 0);
						break;
					case 2:
						GuiUtils.drawTexturedModalRect(-6, 12, 12, 244, 7, 2, 0);
						break;
					case 1:
						GuiUtils.drawTexturedModalRect(-1, 12, 12, 244, 7, 2, 0);
						break;
					case 0:
						GuiUtils.drawTexturedModalRect(4, 13, 12, 244, 7, 2, 0);
						break;
					}
				}
				GlStateManager.popMatrix();

				int number = getSlamCharge(player);
				if (number > 0) {
					GlStateManager.pushMatrix();
					scale = 0.9d*Config.guiScale;
					GlStateManager.translate(width/2, height/2, 0);
					GlStateManager.scale(scale, scale*1.3d, 1);
					String brackets = TextFormatting.GRAY+""+TextFormatting.ITALIC;
					String num = TextFormatting.WHITE+""+TextFormatting.ITALIC+""+TextFormatting.BOLD;
					String text = brackets+"[ "+num+number+brackets+" ]";
					mc.fontRenderer.drawString(text, -mc.fontRenderer.getStringWidth(text)/2, 17, 0xFFFFFF);
					GlStateManager.popMatrix();
				}
			}

			GlStateManager.disableBlend();
		}
	}

	/**Returns the current model
	 * 0 = normal
	 * 1 = charging punch
	 * 2 = punching
	 * 3 = uppercutting
	 * 4 = slam*/
	public int getModel(EntityLivingBase entity) {
		if (entity == null) // normal
			return 0;
		else if (getCharge(entity) > 0) // charging punch
			return 1;
		else if (TickHandler.hasHandler(entity, Identifier.DOOMFIST_PUNCH) || // punching
				TickHandler.hasHandler(entity, Identifier.DOOMFIST_PUNCH_ANIMATIONS)) {
			return 2;
		}
		else if (TickHandler.hasHandler(entity, Identifier.DOOMFIST_UPPERCUTTING) && // uppercut
				TickHandler.getHandler(entity, Identifier.DOOMFIST_UPPERCUTTING).number < 8)
			return 3;
		else if (TickHandler.hasHandler(entity, Identifier.DOOMFIST_SLAM)) // slam
			return 4;
		else // normal
			return 0;
	}

	public static int getSlamCharge(EntityLivingBase entity) {
		Handler handler = TickHandler.getHandler(entity, Identifier.DOOMFIST_SLAM);
		if (handler != null) {
			int number = (int) (handler.number*4.9d+Minewatch.proxy.getRenderPartialTicks());
			return Math.min(number, 125);
		}
		else
			return 0;
	}

	public boolean renderOffHand(EntityLivingBase entity) {
		return entity != null && (entity.getHeldItemOffhand() == null || entity.getHeldItemOffhand().isEmpty()) &&
				((getModel(entity) == 0 && this.getCurrentAmmo(entity) < this.getMaxAmmo(entity)) || getModel(entity) == 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType transform, Pair<? extends IBakedModel, Matrix4f> ret) {
		float charge = getCharge(entity);
		Handler handler = TickHandler.getHandler(entity, Identifier.DOOMFIST_PUNCH_ANIMATIONS);
		// charging punch
		if (charge > 0 && transform == TransformType.THIRD_PERSON_RIGHT_HAND) {
			GlStateManager.translate(0, 0, charge*0.2f);
		}
		// punch animation
		else if (entity != null && handler != null && transform == TransformType.FIRST_PERSON_RIGHT_HAND) {
			float percent = (5f-(handler.ticksLeft - Minewatch.proxy.getRenderPartialTicks())) / 5f;
			GlStateManager.rotate(percent*80f, -1, 20, 0.5f);
			GlStateManager.translate(-percent*1.0f, -percent*0.8f, percent*0.1f);
		}

		return ret;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean preRenderArmor(EntityLivingBase entity, ModelMWArmor model) { 
		switch (getModel(entity)) {
		case 0:
			if (this.renderOffHand(entity)) {
				model.bipedLeftArmwear.rotateAngleX = -1.5f;
				model.bipedLeftArm.rotateAngleX = -1.5f;
				model.bipedLeftArmwear.rotateAngleZ = 0f;
				model.bipedLeftArm.rotateAngleZ = 0f;
				model.bipedLeftArmwear.rotateAngleY = -0.1f;
				model.bipedLeftArm.rotateAngleY = -0.1f;
			}
			break;
		case 1:

			break;
		case 2:
			model.bipedRightArmwear.rotateAngleX = -1.5f;
			model.bipedRightArm.rotateAngleX = -1.5f;
			model.bipedRightArmwear.rotateAngleZ = 0f;
			model.bipedRightArm.rotateAngleZ = 0f;
			model.bipedRightArmwear.rotateAngleY = 0.1f;
			model.bipedRightArm.rotateAngleY = 0.1f;
			entity.limbSwingAmount = 0;
			break;
		case 3:
			model.bipedRightArmwear.rotateAngleX = -2.9f;
			model.bipedRightArm.rotateAngleX = -2.9f;
			model.bipedRightArmwear.rotateAngleZ = -0.3f;
			model.bipedRightArm.rotateAngleZ = -0.3f;
			model.bipedRightArmwear.rotateAngleY = 0.4f;
			model.bipedRightArm.rotateAngleY = 0.4f;
			entity.limbSwingAmount = 0;
			break;
		case 4:
			model.bipedRightArmwear.rotateAngleX = -2.5f;
			model.bipedRightArm.rotateAngleX = -2.5f;
			model.bipedRightArmwear.rotateAngleZ = -0.4f;
			model.bipedRightArm.rotateAngleZ = -0.4f;
			model.bipedRightArmwear.rotateAngleY = 0.1f;
			model.bipedRightArm.rotateAngleY = 0.1f;
			entity.limbSwingAmount = 0;
			break;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getAllModelLocations(ArrayList<String> locs) {
		for (int i=0; i<5; ++i)
			locs.add("_"+i);
		return locs;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getModelLocation(ItemStack stack, @Nullable EntityLivingBase entity) {
		return "_"+getModel(entity);
	}

}