package twopiradians.minewatch.common.item.weapon;

import java.util.List;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntitySombraTranslocator;
import twopiradians.minewatch.common.entity.projectile.EntitySombraBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemSombraMachinePistol extends ItemMWWeapon {

	public static final Handler OPPORTUNIST = new Handler(Identifier.SOMBRA_OPPORTUNIST, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			if (this.entity != null && this.entity.isEntityAlive() && this.entityLiving != null && 
					ItemMWArmor.SetManager.entitiesWearingSets.get(this.entityLiving.getPersistentID()) == EnumHero.SOMBRA &&
					!this.entityLiving.canEntityBeSeen(entity) && entity instanceof EntityLivingBase && 
					((EntityLivingBase)entity).getHealth() > 0 &&
					((EntityLivingBase)entity).getHealth() < ((EntityLivingBase)entity).getMaxHealth()/2f) {
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
			if (this.entityLiving != null && !TickHandler.hasHandler(this.entityLiving, Identifier.SOMBRA_INVISIBLE)) {
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
				entityLiving.setPositionAndUpdate(this.position.xCoord, this.position.yCoord, this.position.zCoord);
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
					Minewatch.proxy.stopSound(player, ModSoundEvents.sombraInvisibleStart, SoundCategory.PLAYERS);
				Minewatch.proxy.playFollowingSound(entity, ModSoundEvents.sombraInvisibleStop, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
				Minewatch.proxy.playFollowingSound(entity, ModSoundEvents.sombraInvisibleVoice, SoundCategory.PLAYERS, 0.7f, 1.0f, false);
			}

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (this.entityLiving != null) {
				this.entityLiving.addPotionEffect(new PotionEffect(MobEffects.SPEED, 5, 2, true, false));
				this.entityLiving.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 5, 2, true, false));
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
			EnumHero.SOMBRA.ability3.keybind.setCooldown(player, 120, false); 
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
		if (this.canUse(player, true, hand, false)) {
			if (!world.isRemote) {
				EntitySombraBullet bullet = new EntitySombraBullet(world, player, hand.ordinal());
				EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYaw, -1, 1.5F, hand, 12, 0.43f);
				world.spawnEntity(bullet);
				world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.sombraShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
						world.rand.nextFloat()/3+0.8f);	
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityPlayer) {	
			EntityPlayer player = (EntityPlayer) entity;

			// cancel invisibility
			Handler handler = TickHandler.getHandler(player, Identifier.SOMBRA_INVISIBLE);
			if (hero.ability3.keybind.isKeyDown(player) || KeyBind.RMB.isKeyDown(player)) {
				if (handler != null && handler.initialTicks-handler.ticksLeft > 30) 
					cancelInvisibility(player); 
			}

			// invisibility
			if (handler == null && !world.isRemote && hero.ability3.isSelected(player) && hero.ability3.keybind.isKeyDown(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				TickHandler.register(false, INVISIBLE.setEntity(player).setTicks(130),
						Ability.ABILITY_USING.setEntity(player).setTicks(130).setAbility(hero.ability3).setBoolean(true));
				Minewatch.network.sendToDimension(new SPacketSimple(27, player, true), world.provider.getDimension());
			}

			// translocator
			if (!world.isRemote && hero.ability2.isSelected(player) && hero.ability2.keybind.isKeyDown(player) &&
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				// teleport
				Entity translocator = hero.ability2.entities.get(player);
				if (translocator instanceof EntitySombraTranslocator &&	translocator.isEntityAlive()) {
					if (translocator.ticksExisted > 10) {
						Minewatch.proxy.stopSound(player, ModSoundEvents.sombraTranslocatorDuring, SoundCategory.PLAYERS);
						Minewatch.proxy.playFollowingSound(player, ModSoundEvents.sombraTranslocatorTeleport, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
						TickHandler.register(false, TELEPORT.setEntity(player).setTicks(10).
								setPosition(new Vec3d(translocator.posX, translocator.posY, translocator.posZ)));
						Minewatch.network.sendToDimension(new SPacketSimple(29, false, player, 
								translocator.posX, translocator.posY, translocator.posZ), world.provider.getDimension());
						translocator.setDead();
						hero.ability2.keybind.setCooldown(player, 80, false);
					}
				}
				// throw new translocator
				else {
					translocator = new EntitySombraTranslocator(world, player);
					EntityHelper.setAim(translocator, player, player.rotationPitch, player.rotationYaw, 25, 0, null, 0, 0);
					world.playSound(null, player.getPosition(), ModSoundEvents.sombraTranslocatorThrow, SoundCategory.PLAYERS, 1.0f, 1.0f);
					world.spawnEntity(translocator);
					player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
					hero.ability2.entities.put(player, translocator);
					TickHandler.register(false, Ability.ABILITY_USING.setAbility(hero.ability2).setTicks(10).setEntity(player).setBoolean(true));
					Minewatch.network.sendToDimension(new SPacketSimple(35, false, player, 0, 0, 0, translocator), world.provider.getDimension());
				}
			}

			// passive
			if (isSelected && entity instanceof EntityPlayer && this.canUse((EntityPlayer) entity, false, EnumHand.MAIN_HAND, true) &&
					world.isRemote && entity.ticksExisted % 5 == 0) {
				AxisAlignedBB aabb = entity.getEntityBoundingBox().expandXyz(30);
				List<Entity> list = entity.world.getEntitiesWithinAABBExcludingEntity(entity, aabb);
				for (Entity entity2 : list) 
					if (!TickHandler.hasHandler(entity2, Identifier.SOMBRA_OPPORTUNIST) && 
							entity2 instanceof EntityLivingBase && ((EntityLivingBase)entity2).getHealth() > 0 &&
							((EntityLivingBase)entity2).getHealth() < ((EntityLivingBase)entity2).getMaxHealth()/2f &&
							!entity2.isGlowing() && !((EntityPlayer)entity).canEntityBeSeen(entity2)) {
						entity2.setGlowing(true);
						TickHandler.register(true, OPPORTUNIST.setEntity(entity2).setEntityLiving(player).setTicks(5));
					}
			}
		}
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

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderArmor(EntityLivingBase entity, ModelMWArmor model) {
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
			float alpha = EntityHelper.shouldHit(entity, Minecraft.getMinecraft().player, false) ? 1 : 0.5f;
			GlStateManager.color((255f-20f*percent)/255f, (255f-109f*percent)/255f, (255f-3f*percent)/255f, 1f-percent*alpha);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType cameraTransformType, Pair<? extends IBakedModel, Matrix4f> ret) {
		// hide gun if not friendly
		if (TickHandler.hasHandler(entity, Identifier.SOMBRA_INVISIBLE) && 
				EntityHelper.shouldHit(entity, Minecraft.getMinecraft().player, false)) 
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

}