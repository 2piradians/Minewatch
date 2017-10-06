package twopiradians.minewatch.common.item.weapon;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMWThrowable;
import twopiradians.minewatch.common.entity.EntityMercyBeam;
import twopiradians.minewatch.common.entity.EntityMercyBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemMercyWeapon extends ItemMWWeapon {

	public static final Handler NOT_REGENING_SERVER = new Handler(Identifier.MERCY_NOT_REGENING, false) {};
	public static HashMap<EntityPlayer, EntityMercyBeam> beams = Maps.newHashMap();
	public static final Handler VOICE_COOLDOWN_SERVER = new Handler(Identifier.MERCY_VOICE_COOLDOWN, false) {};

	public static final Handler ANGEL = new Handler(Identifier.MERCY_ANGEL, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (entity == null || position == null)
				return true;
			
			entity.fallDistance = 0;
			entity.motionX = (position.xCoord - entity.posX)/10;
			entity.motionY = (position.yCoord - entity.posY)/10;
			entity.motionZ = (position.zCoord - entity.posZ)/10;
			entity.velocityChanged = true;

			return super.onClientTick() || Minewatch.keys.jump(player) ||
					Math.sqrt(entity.getDistanceSq(position.xCoord, position.yCoord , position.zCoord)) <= 2; 	
		}
		@Override
		public boolean onServerTick() {
			if (entity == null || position == null)
				return true;
			
			entity.fallDistance = 0;
			entity.motionX = (position.xCoord - entity.posX)/10;
			entity.motionY = (position.yCoord - entity.posY)/10;
			entity.motionZ = (position.zCoord - entity.posZ)/10;

			return super.onServerTick() || Minewatch.keys.jump(player) ||
					Math.sqrt(entity.getDistanceSq(position.xCoord, position.yCoord , position.zCoord)) <= 2;
		}
		@Override
		public Handler onRemove() {
			if (this.player != null) {
				if (!this.player.world.isRemote)
					EnumHero.MERCY.ability3.keybind.setCooldown(this.player, 30, false);
				else
					Minewatch.proxy.stopSound(player, ModSoundEvents.mercyAngel, SoundCategory.PLAYERS);
				TickHandler.unregister(this.player.world.isRemote, 
						TickHandler.getHandler(this.player, Identifier.ABILITY_USING));
			}
			return super.onRemove();
		}
	};

	public ItemMercyWeapon() {
		super(20);
		this.savePlayerToNBT = true;
		MinecraftForge.EVENT_BUS.register(this);
		this.addPropertyOverride(new ResourceLocation("gun"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return !ItemMercyWeapon.isStaff(stack) ? 1.0F : 0.0F;
			}
		});
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		boolean battleMercy = false;
		if (stack.hasTagCompound()) {
			UUID uuid = stack.getTagCompound().getUniqueId("player");
			battleMercy = uuid != null && hero.playersUsingAlt.containsKey(uuid) && hero.playersUsingAlt.get(uuid);		
		}
		return battleMercy ? "Caduceus Blaster" : "Caduceus Staff";
	}

	public static boolean isStaff(ItemStack stack) {
		ItemStack copy = stack.copy();
		copy.clearCustomName();
		return copy.getDisplayName().equals("Caduceus Staff");
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && hero.playersUsingAlt.containsKey(player.getPersistentID()) && 
				hero.playersUsingAlt.get(player.getPersistentID())) {
			if (!world.isRemote) {
				EntityMercyBullet bullet = new EntityMercyBullet(world, player);
				bullet.setAim(player, player.rotationPitch, player.rotationYaw, 2.0F, 0.3F, 0F, hand, true);
				world.spawnEntity(bullet);
				world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.mercyShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
						world.rand.nextFloat()/2+0.75f);	

				this.subtractFromCurrentAmmo(player, 1, hand);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 5);
				if (world.rand.nextInt(20) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
			else {
				Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, vec.xCoord, vec.yCoord, vec.zCoord, 
						0, 0, 0, 0xEF5D1F, 0xEF5D1F, 0.7f, 3, 4, 3, world.rand.nextFloat(), 0.01f);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		// health particles
		if (isSelected && entity instanceof EntityPlayer && this.canUse((EntityPlayer) entity, false, EnumHand.MAIN_HAND, true) &&
				world.isRemote && entity.ticksExisted % 5 == 0) {
			AxisAlignedBB aabb = entity.getEntityBoundingBox().expandXyz(30);
			List<Entity> list = entity.world.getEntitiesWithinAABBExcludingEntity(entity, aabb);
			for (Entity entity2 : list) 
				if (entity2 instanceof EntityLivingBase && ((EntityLivingBase)entity2).getHealth() > 0 &&
						((EntityLivingBase)entity2).getHealth() < ((EntityLivingBase)entity2).getMaxHealth()/2f) {
					float size = Math.min(entity2.height, entity2.width)*9f;
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.HEALTH, world, entity2, 0xFFFFFF, 0xFFFFFF, 0.7f, Integer.MAX_VALUE, size, size, 0, 0);
				}
		}

		if (isSelected && !world.isRemote && entity instanceof EntityPlayer) {
			// remove beams that are dead or too far away (unloaded - where they can't kill themselves)
			if (beams.containsKey(entity) && (beams.get(entity).isDead || 
					Math.sqrt(entity.getDistanceSqToEntity(beams.get(entity))) > 16)) {
				beams.get(entity).setDead();
				beams.remove(entity);
				// stop sound
				if (entity instanceof EntityPlayerMP) 
					Minewatch.proxy.stopSound((EntityPlayer) entity, ModSoundEvents.mercyBeamDuring, SoundCategory.PLAYERS);
				world.playSound(null, entity.posX, entity.posY, entity.posZ, 
						ModSoundEvents.mercyBeamStop, SoundCategory.PLAYERS, 2.0f, 1.0f);
			}
			// spawn beam
			if (isStaff(stack) && 
					(Minewatch.keys.rmb((EntityPlayer) entity) || Minewatch.keys.lmb((EntityPlayer) entity)) &&
					!ItemMercyWeapon.beams.containsKey(entity)) {
				EntityLivingBase target = this.getMouseOver((EntityPlayer) entity, 15);
				if (target != null && ((EntityPlayer) entity).canEntityBeSeen(target) && !(target instanceof EntityArmorStand)) {				
					EntityMercyBeam beam = new EntityMercyBeam(world, (EntityPlayer) entity, target);
					world.spawnEntity(beam);
					beams.put((EntityPlayer) entity, beam);
					world.playSound(null, entity.posX, entity.posY, entity.posZ, 
							ModSoundEvents.mercyBeamStart, SoundCategory.PLAYERS, 2.0f,	1.0f);
					world.playSound(null, entity.posX, entity.posY, entity.posZ, 
							ModSoundEvents.mercyBeamDuring, SoundCategory.PLAYERS, 2.0f, 1.0f);
					if (!TickHandler.hasHandler(entity, Identifier.MERCY_VOICE_COOLDOWN)) {
						world.playSound(null, entity.posX, entity.posY, entity.posZ, 
								beam.isHealing() ? ModSoundEvents.mercyHeal : ModSoundEvents.mercyDamage, SoundCategory.PLAYERS, 2.0f, 1.0f);
						TickHandler.register(false, VOICE_COOLDOWN_SERVER.setEntity((EntityPlayer) entity).setTicks(200));
					}
				}
			}
			if (beams.containsKey(entity) && beams.get(entity).target != null) {
				// heal
				if (beams.get(entity).isHealing() && beams.get(entity).target.getHealth() < beams.get(entity).target.getMaxHealth()) {
					beams.get(entity).target.heal(0.3f);
				}
				// during sound
				if (entity.ticksExisted % 20 == 0)
					world.playSound(null, entity.posX, entity.posY, entity.posZ, 
							ModSoundEvents.mercyBeamDuring, SoundCategory.PLAYERS, 2.0f, 1.0f);
				// switch sound
				if (beams.get(entity).prevHeal != beams.get(entity).isHealing()) {
					world.playSound(null, entity.posX, entity.posY, entity.posZ, 
							ModSoundEvents.mercyBeamStart, SoundCategory.PLAYERS, 2.0f,	1.0f);
					beams.get(entity).prevHeal = beams.get(entity).isHealing();
				}
			}

			// angel
			if (hero.ability3.isSelected((EntityPlayer) entity) && !TickHandler.hasHandler(entity, Identifier.MERCY_ANGEL)) {
				EntityLivingBase target = this.getMouseOver((EntityPlayer) entity, 30);
				if (target != null && ((EntityPlayer) entity).canEntityBeSeen(target) && !(target instanceof EntityArmorStand)) {	
					Vec3d vec = target.getPositionVector().addVector(0, target.height, 0);
					TickHandler.register(false, ANGEL.setPosition(vec).setTicks(75).setEntity(entity),
							Ability.ABILITY_USING.setTicks(75).setEntity(entity).setAbility(hero.ability3));
					boolean playSound = !TickHandler.hasHandler(entity, Identifier.MERCY_VOICE_COOLDOWN) &&
							world.rand.nextInt(3) == 0;
					if (playSound)
						TickHandler.register(false, VOICE_COOLDOWN_SERVER.setEntity((EntityPlayer) entity).setTicks(200));
					Minewatch.network.sendToAll(new SPacketSimple(19, playSound, (EntityPlayer) entity, vec.xCoord, vec.yCoord, vec.zCoord));
				}
			}

		}
	}

	@SubscribeEvent
	public void addToNotRegening(LivingHurtEvent event) {
		EntityLivingBase target = event.getEntityLiving();
		Entity source = event.getSource().getEntity();

		// add to notRegening if hurt
		if (target instanceof EntityPlayer && !target.world.isRemote &&
				ItemMWArmor.SetManager.playersWearingSets.get(target.getPersistentID()) == EnumHero.MERCY) {
			TickHandler.register(false, NOT_REGENING_SERVER.setEntity((EntityPlayer) target).setTicks(40));
			target.removePotionEffect(MobEffects.REGENERATION);
		}

		// increase damage
		for (EntityMercyBeam beam : beams.values()) {
			if (beam.target == source && beam.player instanceof EntityPlayerMP && !beam.player.world.isRemote &&
					ItemMWArmor.SetManager.playersWearingSets.get(beam.player.getPersistentID()) == EnumHero.MERCY) {
				if (!beam.isHealing())
					event.setAmount(event.getAmount()*1.3f);
				((EntityPlayerMP)beam.player).connection.sendPacket((new SPacketSoundEffect
						(ModSoundEvents.hurt, SoundCategory.PLAYERS, source.posX, source.posY, source.posZ, 
								0.3f, source.world.rand.nextFloat()/2+0.75f)));
				break;
			}
		}
	}

	// get entity that player is looking at within 15 blocks - modified from EntityRenderer#getMouseOver
	public EntityLivingBase getMouseOver(EntityPlayer player, int distance) {
		Entity entity = null;
		if (player != null) {
			double d0 = distance - 1;
			Vec3d vec3d = player.getPositionEyes(1);

			double d1 = d0;

			Vec3d vec3d1 = player.getLook(1.0F);
			Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0);
			List<Entity> list = player.world.getEntitiesInAABBexcluding(player, player.getEntityBoundingBox().addCoord(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0).expand(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
				public boolean apply(@Nullable Entity p_apply_1_) {
					return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
				}
			}));
			double d2 = d1;

			for (int j = 0; j < list.size(); ++j) {
				Entity player1 = (Entity)list.get(j);
				AxisAlignedBB axisalignedbb = player1.getEntityBoundingBox().expandXyz((double)player1.getCollisionBorderSize());
				RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

				if (axisalignedbb.isVecInside(vec3d)) {
					if (d2 >= 0.0D) {
						entity = player1;
						d2 = 0.0D;
					}
				}
				else if (raytraceresult != null) {
					double d3 = vec3d.distanceTo(raytraceresult.hitVec);

					if (d3 < d2 || d2 == 0.0D) {
						if (player1.getLowestRidingEntity() == player.getLowestRidingEntity() && !player.canRiderInteract()) {
							if (d2 == 0.0D) 
								entity = player1;
						}
						else {
							entity = player1;
							d2 = d3;
						}
					}
				}
			}
		}

		if (entity instanceof EntityLivingBase)
			return (EntityLivingBase) entity;
		else
			return null;
	}

}