package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityMercyBeam;
import twopiradians.minewatch.common.entity.projectile.EntityMercyBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemMercyWeapon extends ItemMWWeapon {

	public static final Handler NOT_REGENING_SERVER = new Handler(Identifier.MERCY_NOT_REGENING, false) {};
	public static HashMap<EntityLivingBase, EntityMercyBeam> beams = Maps.newHashMap();

	public static final Handler ANGEL = new Handler(Identifier.MERCY_ANGEL, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (entity == null || position == null)
				return true;

			entity.fallDistance = 0;
			entity.motionX = (position.x - entity.posX)/10;
			entity.motionY = (position.y - entity.posY)/10;
			entity.motionZ = (position.z - entity.posZ)/10;
			entity.velocityChanged = true;

			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, entity.world, 
					entity.posX+entity.world.rand.nextFloat()-0.5f, entity.posY+(entity.getEyeHeight()-entity.height/2f), entity.posZ+entity.world.rand.nextFloat()-0.5f, 
					0, 0, 0, 0xFFFAC3, 0xC1C090, 0.8f, 20, 1f+entity.world.rand.nextFloat(), 0.3f, entity.world.rand.nextFloat(), entity.world.rand.nextFloat());

			return super.onClientTick() || KeyBind.JUMP.isKeyDown(entityLiving) ||
					Math.sqrt(entity.getDistanceSq(position.x, position.y , position.z)) <= 2; 	
		}
		@Override
		public boolean onServerTick() {
			if (entity == null || position == null)
				return true;

			entity.fallDistance = 0;
			entity.motionX = (position.x - entity.posX)/10;
			entity.motionY = (position.y - entity.posY)/10;
			entity.motionZ = (position.z - entity.posZ)/10;

			return super.onServerTick() || KeyBind.JUMP.isKeyDown(entityLiving) ||
					Math.sqrt(entity.getDistanceSq(position.x, position.y , position.z)) <= 2;
		}
		@SideOnly(Side.CLIENT)
		@Override
		public Handler onClientRemove() {
			if (this.entityLiving != null) {
				ModSoundEvents.MERCY_ANGEL.stopSound(Minecraft.getMinecraft().player);
				TickHandler.unregister(entityLiving.world.isRemote, 
						TickHandler.getHandler(entityLiving, Identifier.ABILITY_USING));
			}
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			if (this.entityLiving != null) {
				EnumHero.MERCY.ability3.keybind.setCooldown(this.entityLiving, 30, false);
				TickHandler.unregister(this.entityLiving.world.isRemote, 
						TickHandler.getHandler(this.entityLiving, Identifier.ABILITY_USING));
			}
			return super.onServerRemove();
		}
	};

	public ItemMercyWeapon() {
		super(20);
		this.saveEntityToNBT = true;
		this.showHealthParticles = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return isAlternate(stack) ? "Caduceus Blaster" : "Caduceus Staff";
	}

	public static boolean isStaff(ItemStack stack) {
		ItemStack copy = stack.copy();
		copy.clearCustomName();
		return copy.getDisplayName().equals("Caduceus Staff");
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && isAlternate(stack)) {
			if (!world.isRemote) {
				EntityMercyBullet bullet = new EntityMercyBullet(world, player, hand.ordinal());
				EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, 45, 0.6F, hand, 8.5f, 0.6f);
				world.spawnEntity(bullet);
				ModSoundEvents.MERCY_SHOOT.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);
				this.subtractFromCurrentAmmo(player, 1, hand);
				this.setCooldown(player, 5);
				if (world.rand.nextInt(20) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && !world.isRemote && entity instanceof EntityLivingBase) {
			// remove beams that are dead or too far away (unloaded - where they can't kill themselves)
			if (beams.containsKey(entity) && (!beams.get(entity).isEntityAlive()|| 
					Math.sqrt(entity.getDistanceSqToEntity(beams.get(entity))) > 16)) {
				beams.get(entity).setDead();
				beams.remove(entity);
				// stop sound
				if (entity instanceof EntityPlayerMP) 
					ModSoundEvents.MERCY_BEAM_DURING.stopSound((EntityPlayerMP) entity);
				ModSoundEvents.MERCY_BEAM_STOP.playSound(entity, 2.0f, 1.0f);
			}
			// spawn beam
			if (isStaff(stack) && 
					(KeyBind.RMB.isKeyDown((EntityLivingBase) entity) || KeyBind.LMB.isKeyDown((EntityLivingBase) entity)) &&
					!ItemMercyWeapon.beams.containsKey(entity)) {
				RayTraceResult result = EntityHelper.getMouseOverEntity((EntityLivingBase) entity, 15, true);
				EntityLivingBase target = result == null ? null : (EntityLivingBase)result.entityHit;
				if (target != null && ((EntityLivingBase) entity).canEntityBeSeen(target) && !(target instanceof EntityArmorStand)) {				
					EntityMercyBeam beam = new EntityMercyBeam(world, (EntityLivingBase) entity, target);
					world.spawnEntity(beam);
					beams.put((EntityLivingBase) entity, beam);
					ModSoundEvents.MERCY_BEAM_START.playSound(entity, 0.8f, 1f);
					ModSoundEvents.MERCY_BEAM_DURING.playSound(entity, 0.8f, 1f);
					if (beam.isHealing())
						ModSoundEvents.MERCY_HEAL_VOICE.playFollowingSound(entity, 1, 1, false);
					else
						ModSoundEvents.MERCY_DAMAGE_VOICE.playFollowingSound(entity, 1, 1, false);
				}
			}
			if (beams.containsKey(entity) && beams.get(entity).target != null) {
				// heal
				if (beams.get(entity).isHealing() && beams.get(entity).target.getHealth() < beams.get(entity).target.getMaxHealth()) {
					EntityHelper.attemptDamage(entity, beams.get(entity).target, -3, false);
				}
				// during sound
				if (entity.ticksExisted % 20 == 0)
					ModSoundEvents.MERCY_BEAM_DURING.playSound(entity, 0.8f, 1);
				// switch sound
				if (beams.get(entity).prevHeal != beams.get(entity).isHealing()) {
					ModSoundEvents.MERCY_BEAM_START.playSound(entity, 0.8f, 1);
					beams.get(entity).prevHeal = beams.get(entity).isHealing();
				}
			}

			// angel
			if (hero.ability3.isSelected((EntityLivingBase) entity) && !TickHandler.hasHandler(entity, Identifier.MERCY_ANGEL)) {
				RayTraceResult result = EntityHelper.getMouseOverEntity((EntityLivingBase) entity, 30, true);
				EntityLivingBase target = result == null ? null : (EntityLivingBase)result.entityHit;
				if (target != null && ((EntityLivingBase) entity).canEntityBeSeen(target) && !(target instanceof EntityArmorStand)) {	
					Vec3d vec = target.getPositionVector().addVector(0, target.height, 0);
					TickHandler.register(false, ANGEL.setPosition(vec).setTicks(75).setEntity(entity),
							Ability.ABILITY_USING.setTicks(75).setEntity(entity).setAbility(hero.ability3));
					Minewatch.network.sendToAll(new SPacketSimple(19, (EntityLivingBase) entity, world.rand.nextInt(3) == 0, vec.x, vec.y, vec.z));
				}
			}

		}
	}

	@SubscribeEvent
	public void addToNotRegening(LivingHurtEvent event) {
		EntityLivingBase target = event.getEntityLiving();
		Entity source = event.getSource().getTrueSource();

		// add to notRegening if hurt
		if (target instanceof EntityLivingBase && !target.world.isRemote &&
				ItemMWArmor.SetManager.getWornSet(target) == EnumHero.MERCY) {
			TickHandler.register(false, NOT_REGENING_SERVER.setEntity(target).setTicks(40));
			target.removePotionEffect(MobEffects.REGENERATION);
		}
		
		source = EntityHelper.getThrower(source);
		// increase damage
		for (EntityMercyBeam beam : beams.values()) {
			if (beam.target == source && beam.player instanceof EntityLivingBase && !beam.player.world.isRemote) {
				if (!beam.isHealing())
					event.setAmount(event.getAmount()*1.3f);
				if (beam.player instanceof EntityPlayerMP)
					Minewatch.network.sendTo(new SPacketSimple(40, beam.player, false), (EntityPlayerMP)beam.player);
				break;
			}
		}
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
		return !ItemMercyWeapon.isStaff(stack) ? "_1" : "_0";
	}

}