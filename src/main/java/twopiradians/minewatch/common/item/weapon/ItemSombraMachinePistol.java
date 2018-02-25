package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntitySombraTranslocator;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntitySombraBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tileentity.TileEntityHealthPack;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemSombraMachinePistol extends ItemMWWeapon {

	public static final Handler HACKED = new Handler(Identifier.SOMBRA_HACKED, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			float scale = Math.max(0.8f, Math.min(entity.height, entity.width));
			if (this.ticksLeft == this.initialTicks)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SOMBRA_HACK, entity.world, entity, 
						0x8F40F7, 0x8F40F7, 0.8f, 20, 8*scale, 12*scale, 0, 0);
			if (this.ticksLeft % 5 == 0)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SOMBRA_HACK_NUMBERS, entity.world, 
						entity.posX+(entity.world.rand.nextFloat()-0.5f)*scale, 
						entity.posY+entity.height/2f+(entity.world.rand.nextFloat()-0.5f)*scale, 
						entity.posZ+(entity.world.rand.nextFloat()-0.5f)*scale, 
						0, -0.02f, 0, 
						0x8F40F7, 0x8F40F7, 1f, 20, 8*scale, 7*scale, 0, 0);
			if (this.ticksLeft % 10 == 0 && this.ticksLeft > 20)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SOMBRA_HACK_MESH, entity.world, 
						entity.posX+(entity.world.rand.nextFloat()-0.5f)*scale, 
						entity.posY+entity.height/2f+(entity.world.rand.nextFloat()-0.5f)*scale, 
						entity.posZ+(entity.world.rand.nextFloat()-0.5f)*scale, 
						(entity.world.rand.nextFloat()-0.5f)*0.05f, 
						(entity.world.rand.nextFloat()-0.5f)*0.05f, 
						(entity.world.rand.nextFloat()-0.5f)*0.05f, 
						0x8F40F7, 0x8F40F7, 1, 100, 12*scale, 8*scale, 1f+(entity.world.rand.nextFloat()-0.5f)*0.1f, (entity.world.rand.nextFloat()-0.5f)*0.03f);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (entityLiving instanceof EntityLiving && 
					!(entityLiving instanceof EntityPlayer || entityLiving instanceof EntityHero) &&
					((EntityLiving)entityLiving).getAttackTarget() != null) 
				((EntityLiving)entityLiving).setAttackTarget((EntityLivingBase)null);
			return super.onServerTick();
		}
	};

	public static Handler HACK = new Handler(Identifier.SOMBRA_HACK, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// basic checks
			if ((entity == Minecraft.getMinecraft().player /*|| entity instanceof EntityHero*/) && 
					(!(entity instanceof EntityLivingBase) || !entity.isEntityAlive() ||
							((EntityLivingBase)entity).getHeldItemMainhand() == null || 
							((EntityLivingBase)entity).getHeldItemMainhand().getItem() != EnumHero.SOMBRA.weapon ||
							!EnumHero.SOMBRA.ability1.isSelected((EntityLivingBase) entity) || 
							KeyBind.LMB.isKeyDown((EntityLivingBase) entity) || 
							!EnumHero.SOMBRA.weapon.canUse((EntityLivingBase) entity, false, EnumHand.MAIN_HAND, false))) 
				return true;
			// find new target / clear target
			else if (entity.ticksExisted % 5 == 0) {
				// both invalid targets
				if ((entityLiving == null || !entityLiving.isEntityAlive()) && 
						(this.position == null || !(entity.world.getTileEntity(new BlockPos(this.position)) instanceof TileEntityHealthPack))) { 
					entityLiving = EntityHelper.getTargetInFieldOfVision((EntityLivingBase) entity, 15, 10, false);
					if (entityLiving == null)
						position = EntityHelper.getHealthPackInFieldOfVision((EntityLivingBase) entity, 15, entity instanceof EntityHero ? 20 : 10);
				}

				// check entity
				if (entityLiving != null && (!EntityHelper.isInFieldOfVision(entity, entityLiving, 10) || 
						entityLiving.getDistanceToEntity(entity) > 15 || 
						!checkTargetInShootingView((EntityLivingBase) entity, entityLiving.getPositionVector().addVector(0, entityLiving.height/2f, 0))))
					entityLiving = null;
				// check health pack
				else if (position != null && (!EntityHelper.isInFieldOfVision(entity, position.addVector(0.5d, 0, 0.5d), entity instanceof EntityHero ? 20 : 10) || 
						Math.sqrt(entity.getDistanceSqToCenter(new BlockPos(position))) > 15 ||
						!checkTargetInShootingView((EntityLivingBase) entity, position.addVector(0.5d, 0.5d, 0.5d)))) {
					RayTraceResult result = EntityHelper.getMouseOverBlock((EntityLivingBase) entity, 15);
					if (result == null || !(entity.world.getTileEntity(result.getBlockPos()) instanceof TileEntityHealthPack) ||
							!checkTargetInShootingView((EntityLivingBase) entity, position.addVector(0.5d, 0.5d, 0.5d)))
						position = null;
				}
			}
			else 
				this.ticksLeft = 10;

			// ticks hacking
			if ((entityLiving != null || position != null))
				this.number = Math.min(number+1, 16);
			else
				this.number = 0;

			// particles
			ArrayList<Vec3d> targetVecs = getTargetVecs(this, false);
			for (int i=0; i<targetVecs.size(); ++i) {
				Vec3d target = targetVecs.get(i);

				if (entityLiving != null)
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, entity.world, target.x, target.y, target.z, 0, 0, 0, 0xB36BF7, 0xB36BF7, 0.7f, 1, 1, 5, 0, 0);

				if ((number-1) % 4 == 0 && i == targetVecs.size()-1) {
					Vector2f offsets = getOffsets(i);
					Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.CIRCLE, entity.world, (EntityLivingBase) entity, 0xF685F8, 0xC985F6, 1f, 5, 1.2f, 0.5f, 0, 0, EnumHand.OFF_HAND, offsets.x, offsets.y);
					Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.CIRCLE, entity.world, (EntityLivingBase) entity, 0xFFFFFF, 0xFFFFFF, 0.6f, 5, 0.6f, 0.2f, 0, 0, EnumHand.OFF_HAND, offsets.x, offsets.y);
				}
			}

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			// basic checks
			if (!(entity instanceof EntityLivingBase) || !entity.isEntityAlive() ||
					((EntityLivingBase)entity).getHeldItemMainhand() == null || 
					((EntityLivingBase)entity).getHeldItemMainhand().getItem() != EnumHero.SOMBRA.weapon ||
					!EnumHero.SOMBRA.ability1.isSelected((EntityLivingBase) entity) || 
					KeyBind.LMB.isKeyDown((EntityLivingBase) entity) || 
					EnumHero.SOMBRA.weapon.hasCooldown(entity) ||
					!EnumHero.SOMBRA.weapon.canUse((EntityLivingBase) entity, false, EnumHand.MAIN_HAND, false)) 
				return true;
			// find new target / clear target
			else if (entity.ticksExisted % 5 == 0) {
				// both invalid targets
				if ((entityLiving == null || !entityLiving.isEntityAlive()) && 
						(this.position == null || !(entity.world.getTileEntity(new BlockPos(this.position)) instanceof TileEntityHealthPack))) { 
					entityLiving = EntityHelper.getTargetInFieldOfVision((EntityLivingBase) entity, 15, 10, false);
					if (entityLiving == null)
						position = EntityHelper.getHealthPackInFieldOfVision((EntityLivingBase) entity, 15, entity instanceof EntityHero ? 20 : 10);
				}

				// check entity
				if (entityLiving != null && (!EntityHelper.isInFieldOfVision(entity, entityLiving, 10) || 
						entityLiving.getDistanceToEntity(entity) > 15 || 
						!checkTargetInShootingView((EntityLivingBase) entity, entityLiving.getPositionVector().addVector(0, entityLiving.height/2f, 0))))
					entityLiving = null;
				// check health pack
				else if (position != null && (!EntityHelper.isInFieldOfVision(entity, position.addVector(0.5d, 0, 0.5d), entity instanceof EntityHero ? 20 : 10) || 
						Math.sqrt(entity.getDistanceSqToCenter(new BlockPos(position))) > 15 ||
						!checkTargetInShootingView((EntityLivingBase) entity, position.addVector(0.5d, 0.5d, 0.5d)))) {
					RayTraceResult result = EntityHelper.getMouseOverBlock((EntityLivingBase) entity, 15);
					if (result == null || !(entity.world.getTileEntity(result.getBlockPos()) instanceof TileEntityHealthPack) ||
							!checkTargetInShootingView((EntityLivingBase) entity, position.addVector(0.5d, 0.5d, 0.5d)))
						position = null;
				}
			}
			else 
				this.ticksLeft = 10;

			// ticks hacking
			if ((entityLiving != null || position != null)) {
				if (number == 0) {
					ModSoundEvents.SOMBRA_HACK_DURING.playFollowingSound(entity, 1, 1, false);
					ModSoundEvents.SOMBRA_HACK_VOICE.playFollowingSound(entity, 1, 1, false);
				}

				// hacked
				if (++number > 16) {
					if (position != null) {
						TileEntity te = entity.world.getTileEntity(new BlockPos(position));
						if (te instanceof TileEntityHealthPack)
							((TileEntityHealthPack)te).hack(entity.getTeam());
					}
					EnumHero.SOMBRA.ability1.keybind.setCooldown((EntityLivingBase) entity, 160, false);
					//Minewatch.network.sendToDimension(new SPacketSimple(61, entity, false, entityLiving),  entity.world.provider.getDimension());
					if (entityLiving != null) {
						TickHandler.interrupt(entityLiving);
						TickHandler.register(false, HACKED.setEntity(entityLiving).setTicks(120)); 
						ModSoundEvents.SOMBRA_HACK_COMPLETE.playFollowingSound(entityLiving, 3, 1, false);
					}
					else if (position != null)
						ModSoundEvents.SOMBRA_HACK_COMPLETE.playSound(entity.world, position.x, position.y, position.z, 1, 1);
					return true;
				}
			}
			else if (this.number > 0) {
				this.number = 0;
				ModSoundEvents.SOMBRA_HACK_DURING.stopFollowingSound(entity);
			}

			return super.onServerTick();
		}
		@Override
		public Handler onServerRemove() {
			if (this.number > 0)
				ModSoundEvents.SOMBRA_HACK_DURING.stopFollowingSound(entity);
			Minewatch.network.sendToDimension(new SPacketSimple(61, entity, false, this.number >= 16 ? entityLiving : null), entity.dimension);
			return super.onServerRemove();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			return super.onClientRemove();
		}
	};

	public static final Handler OPPORTUNIST = new Handler(Identifier.SOMBRA_OPPORTUNIST, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			if (this.entity != null && this.entity.isEntityAlive() && this.entityLiving != null && 
					SetManager.getWornSet(this.entityLiving) == EnumHero.SOMBRA &&
					!this.entityLiving.canEntityBeSeen(entity) && entity instanceof EntityLivingBase && 
					((EntityLivingBase)entity).getHealth() > 0 &&
					((EntityLivingBase)entity).getHealth() < ((EntityLivingBase)entity).getMaxHealth()/2f &&
					!TickHandler.hasHandler(entity, Identifier.SOMBRA_INVISIBLE)) {
				this.ticksLeft = this.initialTicks;
				return null;
			}
			else {
				this.entity.setGlowing(false);
				return super.onClientRemove();
			}
		}
	};

	public static final Handler TELEPORT = new Handler(Identifier.SOMBRA_TELEPORT, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (this.entityLiving == Minecraft.getMinecraft().player && 
					this.entityLiving != null && !TickHandler.hasHandler(this.entityLiving, Identifier.SOMBRA_INVISIBLE)) {
				if (this.ticksLeft == 8) 
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, this.entityLiving.world, 
							this.entityLiving.posX, this.entityLiving.posY+this.entityLiving.height/2d, this.entityLiving.posZ, 0, 0, 0, 0x9F62E5, 0x8E77BC, 1f, 15, 25, 1, 0, 0);
				else if (this.ticksLeft == 2)
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, this.entityLiving.world, this.entityLiving, 
							0x9F62E5, 0x8E77BC, 1f, 10, 5, 40, 0, 0);
			}

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (this.ticksLeft == 5 && this.entityLiving != null) {
				entityLiving.fallDistance = 0;
				// attempt tp - if it fails, reset cooldown
				if (!EntityHelper.attemptTeleport(entityLiving, this.position.x, this.position.y, this.position.z))
					EnumHero.SOMBRA.ability2.keybind.setCooldown(player, 1, false);
			}

			return super.onServerTick();
		}
	};
	public static final Handler INVISIBLE = new Handler(Identifier.SOMBRA_INVISIBLE, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (this.ticksLeft == 14) {
				if (this.player == Minewatch.proxy.getClientPlayer() && player.world.isRemote)
					ModSoundEvents.SOMBRA_INVISIBLE_START.stopSound(player);
				ModSoundEvents.SOMBRA_INVISIBLE_STOP.playFollowingSound(entity, 1, 1, false);
				ModSoundEvents.SOMBRA_INVISIBLE_VOICE.playFollowingSound(entity, 0.7f, 1, false);
			}

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (this.entityLiving != null) {
				this.entityLiving.addPotionEffect(new PotionEffect(MobEffects.SPEED, 5, 2, true, false));
			}

			return super.onServerTick();
		}
		@SideOnly(Side.CLIENT)
		@Override
		public Handler onClientRemove() {
			Handler handler = TickHandler.getHandler(entity, Identifier.ABILITY_USING);
			if (handler != null && handler.ability == EnumHero.SOMBRA.ability3 && entity != null)
				TickHandler.unregister(entity.world.isRemote, handler);
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			EnumHero.SOMBRA.ability3.keybind.setCooldown(entityLiving, 120, false); 
			Handler handler = TickHandler.getHandler(entity, Identifier.ABILITY_USING);
			if (handler != null && handler.ability == EnumHero.SOMBRA.ability3 && entity != null)
				TickHandler.unregister(entity.world.isRemote, handler);
			return super.onServerRemove();
		}
	};

	public ItemSombraMachinePistol() {
		super(30);
		this.saveEntityToNBT = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		cancelInvisibility(player);

		// shoot
		if (this.canUse(player, true, hand, false) && !TickHandler.hasHandler(player, Identifier.SOMBRA_INVISIBLE)) {
			if (!world.isRemote) {
				EntitySombraBullet bullet = new EntitySombraBullet(world, player, hand.ordinal());
				EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, -1, 1.5F, hand, 12, 0.43f);
				world.spawnEntity(bullet);
				ModSoundEvents.SOMBRA_SHOOT.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHeldItemMainhand() == stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// cancel invisibility
			Handler handler = TickHandler.getHandler(player, Identifier.SOMBRA_INVISIBLE);
			if (hero.ability3.keybind.isKeyDown(player) || KeyBind.RMB.isKeyDown(player)) {
				if (handler != null && handler.initialTicks-handler.ticksLeft > 30) 
					cancelInvisibility(player); 
			}

			// invisibility
			if (handler == null && !world.isRemote && hero.ability3.isSelected(player, false, hero.ability2) && hero.ability3.keybind.isKeyDown(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				TickHandler.register(false, INVISIBLE.setEntity(player).setTicks(130),
						Ability.ABILITY_USING.setEntity(player).setTicks(130).setAbility(hero.ability3).setBoolean(true));
				Minewatch.network.sendToDimension(new SPacketSimple(27, player, true), world.provider.getDimension());
			}

			// translocator
			if (!world.isRemote && hero.ability2.isSelected(player, true, hero.ability3) && hero.ability2.keybind.isKeyDown(player) &&
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				// teleport
				Entity translocator = hero.ability2.entities.get(player);
				if (translocator instanceof EntitySombraTranslocator &&	translocator.isEntityAlive()) {
					if (player instanceof EntityPlayer)
						ModSoundEvents.SOMBRA_TRANSLOCATOR_DURING.stopSound((EntityPlayer) player);
					ModSoundEvents.SOMBRA_TRANSLOCATOR_TELEPORT.playFollowingSound(player, 1, 1, false);
					TickHandler.register(false, TELEPORT.setEntity(player).setTicks(10).
							setPosition(new Vec3d(translocator.posX, translocator.posY, translocator.posZ)));
					Minewatch.network.sendToDimension(new SPacketSimple(29, player, false, 
							translocator.posX, translocator.posY, translocator.posZ), world.provider.getDimension());
					translocator.setDead();
					hero.ability2.keybind.setCooldown(player, 80, false);
				}
				// throw new translocator
				else {
					translocator = new EntitySombraTranslocator(world, player);
					EntityHelper.setAim(translocator, player, player.rotationPitch, player.rotationYawHead, 30, 0, null, 0, 0, -0.5f);
					ModSoundEvents.SOMBRA_TRANSLOCATOR_THROW.playSound(player, 1, 1);
					world.spawnEntity(translocator);
					player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
					hero.ability2.entities.put(player, translocator);
					TickHandler.register(false, Ability.ABILITY_USING.setAbility(hero.ability2).setTicks(10).setEntity(player));
					Minewatch.network.sendToDimension(new SPacketSimple(35, false, null, 0, 0, 0, player, translocator), world.provider.getDimension());
				}
			}

			// give hack if right clicking and offhand is empty
			if (!world.isRemote && (player.getHeldItemOffhand() == null || player.getHeldItemOffhand().isEmpty()) && 
					hero.ability1.isSelected(player) && !KeyBind.LMB.isKeyDown(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, false) && 
					!TickHandler.hasHandler(player, Identifier.SOMBRA_INVISIBLE)) {
				// start hacking
				if (!TickHandler.hasHandler(player, Identifier.SOMBRA_HACK)) {
					ModSoundEvents.SOMBRA_HACK_START.playFollowingSound(player, 1, 1, false);
					TickHandler.register(false, HACK.setEntity(player).setEntityLiving(null).setTicks(10));
					Minewatch.network.sendToDimension(new SPacketSimple(61, player, true), world.provider.getDimension());
				}
				player.setHeldItem(EnumHand.OFF_HAND, new ItemStack(ModItems.sombra_hack));
			}

			// passive
			if (player == Minewatch.proxy.getClientPlayer() && isSelected && 
					this.canUse(player, false, EnumHand.MAIN_HAND, true) &&
					world.isRemote && player.ticksExisted % 5 == 0) {
				AxisAlignedBB aabb = player.getEntityBoundingBox().grow(30);
				List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, aabb);
				for (Entity entity2 : list) 
					if (!TickHandler.hasHandler(entity2, Identifier.SOMBRA_OPPORTUNIST) && 
							entity2 instanceof EntityLivingBase && ((EntityLivingBase)entity2).getHealth() > 0 &&
							((EntityLivingBase)entity2).getHealth() < ((EntityLivingBase)entity2).getMaxHealth()/2f &&
							!entity2.isGlowing() && !player.canEntityBeSeen(entity2) && !TickHandler.hasHandler(entity2, Identifier.SOMBRA_INVISIBLE)) {
						entity2.setGlowing(true);
						TickHandler.register(true, OPPORTUNIST.setEntity(entity2).setEntityLiving(player).setTicks(5));
					}
			}
		}
	}

	public static boolean checkTargetInShootingView(EntityLivingBase entity, Vec3d target) {
		Vector2f rotations = EntityHelper.getEntityPartialRotations(entity);
		Vec3d shooting = EntityHelper.getShootingPos(entity, rotations.x, rotations.y, EnumHand.OFF_HAND, 20, 0.6f);
		return EntityHelper.canBeSeen(entity.world, shooting, target);
	}

	/**Set Invisibility handler to 14 ticks, if active*/
	public static void cancelInvisibility(EntityLivingBase entity) {
		Handler handler = TickHandler.getHandler(entity, Identifier.SOMBRA_INVISIBLE);
		if (handler != null && handler.ticksLeft > 14) {
			handler.ticksLeft = 14;
			if (!entity.world.isRemote)
				Minewatch.network.sendToDimension(new SPacketSimple(27, entity, false), entity.world.provider.getDimension());
		}
	}

	@SubscribeEvent
	public void cancelInvisWhenAttacked(LivingHurtEvent event) {
		if (TickHandler.hasHandler(event.getEntityLiving(), Identifier.SOMBRA_INVISIBLE)) 
			cancelInvisibility(event.getEntityLiving());
	}

	@SideOnly(Side.CLIENT)
	public static Vector2f getOffsets(int index) {
		switch (index) {
		case 0:
			return new Vector2f(20, 0.61f); // middle
		case 1:
			return new Vector2f(18, 0.46f); // mid-bottom right
		case 2:
			return new Vector2f(11, 0.72f); // top left
		default:
			return new Vector2f(27, 0.33f); // bottom right
		}
	}

	@SideOnly(Side.CLIENT)
	private static ArrayList<Vec3d> getShootingVecs(Handler handler, boolean offset) {
		ArrayList<Vec3d> list = new ArrayList<Vec3d>();
		if (handler != null && handler.identifier == Identifier.SOMBRA_HACK &&
				(handler.entityLiving != null || handler.position != null)) {
			Vector2f rotations = EntityHelper.getEntityPartialRotations(handler.entity);

			for (int i=0; i<Math.min(4, handler.number/4); ++i) {
				Vector2f offsets = getOffsets(i);
				Vec3d shooting = EntityHelper.getShootingPos((EntityLivingBase) handler.entity, rotations.x, rotations.y, EnumHand.OFF_HAND, offsets.x, offsets.y);
				if (offset) {
					Vec3d viewerPos = EntityHelper.getEntityPartialPos(Minewatch.proxy.getRenderViewEntity());
					shooting = shooting.subtract(viewerPos);
				}
				list.add(shooting);
			}
		}
		return list;
	}

	@SideOnly(Side.CLIENT)
	private static ArrayList<Vec3d> getTargetVecs(Handler handler, boolean offset) {
		ArrayList<Vec3d> list = new ArrayList<Vec3d>();
		if (handler != null && handler.identifier == Identifier.SOMBRA_HACK &&
				(handler.entityLiving != null || handler.position != null)) {
			Vec3d targetBase = handler.entityLiving != null ? EntityHelper.getEntityPartialPos(handler.entityLiving).addVector(0, handler.entityLiving.height/2f, 0) : handler.position.addVector(0.5d, 0.06d, 0.5d);
			if (offset) {
				Vec3d viewerPos = EntityHelper.getEntityPartialPos(Minewatch.proxy.getRenderViewEntity());
				targetBase = targetBase.subtract(viewerPos);
			}

			for (int i=0; i<Math.min(4, handler.number/4); ++i) {
				if (handler.entityLiving != null) {
					switch (i) {
					case 0:
						list.add(targetBase);
						break;
					case 1:
						list.add(targetBase.addVector(-handler.entityLiving.width/4f, handler.entityLiving.height/6f, -handler.entityLiving.width/4f));
						break;
					case 2:
						list.add(targetBase.addVector(handler.entityLiving.width/5f, handler.entityLiving.height/5f, handler.entityLiving.width/4f));
						break;
					case 3:
						list.add(targetBase.addVector(handler.entityLiving.width/6f, -handler.entityLiving.height/4f, handler.entityLiving.width/5f));
						break;
					}
				}
				else
					list.add(targetBase);
			}
		}
		return list;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderDamageBeam(RenderWorldLastEvent event) {
		// hack
		for (Handler handler : TickHandler.getHandlers(true, null, Identifier.SOMBRA_HACK, null)) {
			if (handler.entityLiving != null || handler.position != null) {
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder vertexbuffer = tessellator.getBuffer();
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.depthMask(false);
				GlStateManager.color(1, 1, 1, (float) (0.4f+Math.abs(Math.sin(handler.entity.ticksExisted/5d))/3d));
				vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

				double targetWidth = 0.08d;
				double shootingWidth = 0.01d;
				ArrayList<Vec3d> shootingVecs = getShootingVecs(handler, true);
				ArrayList<Vec3d> targetVecs = getTargetVecs(handler, true);

				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/entity/sombra_hack.png"));

				for (int i=0; i<shootingVecs.size(); ++i) {
					Vec3d shooting = shootingVecs.get(i);
					Vec3d target = targetVecs.get(i);

					// xz axis
					// top
					vertexbuffer.pos(shooting.x-shootingWidth/2f, shooting.y, shooting.z-shootingWidth/2f).tex(0, 0).endVertex();
					vertexbuffer.pos(shooting.x+shootingWidth/2f, shooting.y, shooting.z+shootingWidth/2f).tex(1, 0).endVertex();
					vertexbuffer.pos(target.x+targetWidth/2f, target.y, target.z+targetWidth/2f).tex(1, 1).endVertex();
					vertexbuffer.pos(target.x-targetWidth/2f, target.y, target.z-targetWidth/2f).tex(0, 1).endVertex();
					// bottom
					vertexbuffer.pos(target.x-targetWidth/2f, target.y, target.z-targetWidth/2f).tex(0, 1).endVertex();
					vertexbuffer.pos(target.x+targetWidth/2f, target.y, target.z+targetWidth/2f).tex(1, 1).endVertex();
					vertexbuffer.pos(shooting.x+shootingWidth/2f, shooting.y, shooting.z+shootingWidth/2f).tex(1, 0).endVertex();
					vertexbuffer.pos(shooting.x-shootingWidth/2f, shooting.y, shooting.z-shootingWidth/2f).tex(0, 0).endVertex();

					// y axis
					// left
					vertexbuffer.pos(shooting.x, shooting.y-shootingWidth/2f, shooting.z).tex(0, 0).endVertex();
					vertexbuffer.pos(shooting.x, shooting.y+shootingWidth/2f, shooting.z).tex(1, 0).endVertex();
					vertexbuffer.pos(target.x, target.y+targetWidth/2f, target.z).tex(1, 1).endVertex();
					vertexbuffer.pos(target.x, target.y-targetWidth/2f, target.z).tex(0, 1).endVertex();
					// right
					vertexbuffer.pos(target.x, target.y-targetWidth/2f, target.z).tex(0, 1).endVertex();
					vertexbuffer.pos(target.x, target.y+targetWidth/2f, target.z).tex(1, 1).endVertex();
					vertexbuffer.pos(shooting.x, shooting.y+shootingWidth/2f, shooting.z).tex(1, 0).endVertex();
					vertexbuffer.pos(shooting.x, shooting.y-shootingWidth/2f, shooting.z).tex(0, 0).endVertex();
				}
				tessellator.draw();

				GlStateManager.depthMask(true);
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean preRenderArmor(EntityLivingBase entity, ModelMWArmor model) {
		// hack
		if (entity.getHeldItemOffhand() != null && entity.getHeldItemOffhand().getItem() == ModItems.sombra_hack) {
			model.bipedLeftArmwear.rotateAngleX = 5;
			model.bipedLeftArm.rotateAngleX = 5;
			model.bipedLeftArmwear.rotateAngleY = -0.2f;
			model.bipedLeftArm.rotateAngleY = -0.2f;
		}

		// invisibility / teleport
		float time = 14;
		Handler handler = TickHandler.getHandler(entity, Identifier.SOMBRA_INVISIBLE);
		if (handler == null) {
			handler = TickHandler.getHandler(entity, Identifier.SOMBRA_TELEPORT);
			time = 5;
		}
		if (handler != null) {
			GlStateManager.enableCull();
			float percent = 1f;
			if (handler.ticksLeft > handler.initialTicks-time)
				percent = (handler.initialTicks-handler.ticksLeft) / time;
			else if (handler.ticksLeft < time)
				percent = handler.ticksLeft / time;
			// full alpha if not friendly
			float alpha = EntityHelper.shouldTarget(entity, Minecraft.getMinecraft().player, false) ? 1 : 0.5f;
			GlStateManager.color((255f-20f*percent)/255f, (255f-109f*percent)/255f, (255f-3f*percent)/255f, 1f-percent*alpha);
			return true;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderHand(AbstractClientPlayer player, EnumHand hand) {
		return hand == EnumHand.OFF_HAND && player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() == ModItems.sombra_hack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType cameraTransformType, Pair<? extends IBakedModel, Matrix4f> ret) {
		// hide gun if not friendly and invisible
		if (TickHandler.hasHandler(entity, Identifier.SOMBRA_INVISIBLE) && 
				EntityHelper.shouldTarget(entity, Minecraft.getMinecraft().player, false)) 
			ret.getRight().setScale(0);

		return ret;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		Entity entity = ItemMWWeapon.getEntity(Minecraft.getMinecraft().world, stack);
		return TickHandler.hasHandler(entity, Identifier.SOMBRA_INVISIBLE) ||
				TickHandler.hasHandler(entity, Identifier.SOMBRA_TELEPORT) ? 0xFB8AFE : -1;
	}

	@SubscribeEvent
	public void clearAttackTarget(LivingSetAttackTargetEvent event) {
		if (!event.getEntity().world.isRemote && event.getTarget() != null && 
				event.getEntity() instanceof EntityLiving && 
				!(event.getEntity() instanceof EntityPlayer || event.getEntity() instanceof EntityHero) && 
				TickHandler.hasHandler(event.getEntity(), Identifier.SOMBRA_HACKED))
			((EntityLiving)event.getEntity()).setAttackTarget((EntityLivingBase)null);
	}

	@SubscribeEvent
	public void stopHacking(LivingHurtEvent event) {
		if (!event.getEntity().world.isRemote && TickHandler.hasHandler(event.getEntity(), Identifier.SOMBRA_HACK)) {
			TickHandler.unregister(false, TickHandler.getHandler(event.getEntity(), Identifier.SOMBRA_HACK));
			Minewatch.network.sendToDimension(new SPacketSimple(61, event.getEntity(), false), event.getEntity().world.provider.getDimension());
		}
	}

}