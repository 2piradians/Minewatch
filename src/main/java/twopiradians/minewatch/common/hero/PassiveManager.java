package twopiradians.minewatch.common.hero;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityJunkratGrenade;
import twopiradians.minewatch.common.item.weapon.ItemPharahWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;
import twopiradians.minewatch.packet.SPacketSimple;

@Mod.EventBusSubscriber
public class PassiveManager {

	public static ArrayList<EntityLivingBase> playersJumped = new ArrayList<EntityLivingBase>(); // Genji double jump
	public static ArrayList<EntityLivingBase> playersHovering = new ArrayList<EntityLivingBase>(); // Mercy hover
	public static HashMap<EntityLivingBase, Integer> playersClimbing = Maps.newHashMap(); // Genji/Hanzo climb
	public static ArrayList<EntityLivingBase> playersFlying = new ArrayList<EntityLivingBase>(); // Pharah hover

	/**Called once per tick for (all) players and heroes wearing a full set*/
	public static void onUpdate(World world, EntityLivingBase entity, EnumHero hero) {
		if (!entity.isEntityAlive()) // TODO hurt sound effects and overlay and health animation, prevent kick for flying
			return;

		boolean hacked = TickHandler.hasHandler(entity, Identifier.SOMBRA_HACKED);

		// genji jump boost/double jump
		if (!hacked && hero == EnumHero.GENJI) {
			// jump boost
			if (!world.isRemote && (entity.getActivePotionEffect(MobEffects.JUMP_BOOST) == null || 
					entity.getActivePotionEffect(MobEffects.JUMP_BOOST).getDuration() == 0))
				entity.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 10, 0, true, false));
			// double jump
			else if (world.isRemote && (entity.onGround || entity.isInWater() || entity.isInLava()))
				playersJumped.remove(entity);
			else if (KeyBind.JUMP.isKeyPressed(entity) && !entity.onGround && !entity.isOnLadder() && 
					entity.motionY < 0.2d && !playersJumped.contains(entity)) {
				if (world.isRemote) {
					if (entity instanceof EntityPlayer)
						((EntityPlayer)entity).jump();
					else if (entity instanceof EntityHero)
						((EntityHero)entity).jump();
					entity.motionY += 0.2d;
					playersJumped.add(entity);
					ModSoundEvents.GENJI_JUMP.playSound(entity, 0.8f, world.rand.nextFloat()/6f+0.9f);
				}
				entity.fallDistance = 0;
			}
		}

		// genji/hanzo wall climb
		if (!hacked && (hero == EnumHero.GENJI || hero == EnumHero.HANZO) && 
				world.isRemote == entity instanceof EntityPlayer) {
			// reset climbing
			boolean badBlock = false;
			for (BlockPos pos : BlockPos.getAllInBox(entity.getPosition().up().east().north(), entity.getPosition().up().west().south()))
				if (EntityHelper.shouldIgnoreBlock(world.getBlockState(pos).getBlock())) {
					badBlock = true;
					break;
				}
			if (badBlock || (entity instanceof EntityPlayer && entity.onGround) ||  entity.isInWater() || entity.isInLava()) {
				playersClimbing.remove(entity);
			}
			else if (entity.isCollidedHorizontally && entity.moveForward > 0 && 
					!(entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isFlying) && 
					KeyBind.JUMP.isKeyDown(entity)) {
				int ticks = playersClimbing.containsKey(entity) ? playersClimbing.get(entity)+1 : 1;
				if (ticks <= 17) {
					if (ticks % 4 == 0 || ticks == 1) { // reset fall distance and play sound
						if (world.isRemote)
							Minewatch.network.sendToServer(new CPacketSimple(0, entity, false));
						else 
							ModSoundEvents.WALL_CLIMB.playSound(entity, 0.9f, 1);
						entity.fallDistance = 0.0F;
					}
					entity.motionX = MathHelper.clamp(entity.motionX, -0.15D, 0.15D);
					entity.motionZ = MathHelper.clamp(entity.motionZ, -0.15D, 0.15D);
					entity.motionY = Math.max(0.2d, entity.motionY);
					entity.move(MoverType.SELF, entity.motionX, entity.motionY, entity.motionZ);
					if (entity instanceof EntityHero) {
						Vec3d vec = entity.getPositionVector().add(new Vec3d(entity.getHorizontalFacing().getDirectionVec()));
						((EntityHero) entity).getLookHelper().setLookPosition(vec.x, vec.y, vec.z, 30, 30);
					}
					playersClimbing.put(entity, ticks);
				}
			}
		}
		// mercy's regen/slow fall
		else if (hero == EnumHero.MERCY) {
			if (TickHandler.getHandler(entity, Identifier.MERCY_NOT_REGENING) == null &&
					!world.isRemote && (entity.getActivePotionEffect(MobEffects.REGENERATION) == null || 
					entity.getActivePotionEffect(MobEffects.REGENERATION).getDuration() == 0))
				entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 0, true, false));
			else if (!hacked && KeyBind.JUMP.isKeyDown(entity) && entity.motionY <= -0.09d && !entity.isInWater() && !entity.isInLava()) {
				entity.motionY = Math.min(entity.motionY*0.75f, -0.1f);
				entity.fallDistance = Math.max(entity.fallDistance*0.75f, 1);
				if (!playersHovering.contains(entity) && !world.isRemote) {
					ModSoundEvents.MERCY_HOVER.playSound(entity, 0.2f, 1);
					playersHovering.add(entity);
				}
				if (world.isRemote)
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, 
							entity.posX+world.rand.nextFloat()-0.5f, entity.posY+(entity.getEyeHeight()-entity.height/2f), entity.posZ+world.rand.nextFloat()-0.5f, 
							0, 0, 0, 0xFFFAC3, 0xC1C090, 0.8f, 10, 1f+world.rand.nextFloat(), 0.3f, world.rand.nextFloat(), world.rand.nextFloat());
			}
			else if (playersHovering.contains(entity)) 
				playersHovering.remove(entity);
		}
		// pharah's jet pack
		else if (!hacked && hero == EnumHero.PHARAH && !(entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isFlying) &&
				(KeyBind.JUMP.isKeyDown(entity) || KeyBind.RMB.isKeyDown(entity)) && ChargeManager.canUseCharge(entity)) {
			ChargeManager.subtractFromCurrentCharge(entity, 1, false);
			entity.motionY = Math.min(entity.motionY+0.22f, Math.max(entity.motionY, 5.5f/20f));
			if (entity.world.isRemote) {
				ItemPharahWeapon.spawnJetPackParticles(entity, false);
				// start flying sounds
				if (!playersFlying.contains(entity)) {
					ModSoundEvents.PHARAH_FLY_0.playFollowingSound(entity, 0.2f, 1, true);
					ModSoundEvents.PHARAH_FLY_1.playFollowingSound(entity, 0.3f, 1, true);
					playersFlying.add(entity);
				}
			}
		}
		// stop flying sounds
		else if (world.isRemote && playersFlying.contains(entity)) {
			ModSoundEvents.PHARAH_FLY_0.stopFollowingSound(entity);
			ModSoundEvents.PHARAH_FLY_1.stopFollowingSound(entity);
			playersFlying.remove(entity);
		}

		// reduced gravity
		if (Config.lowerGravity && !entity.hasNoGravity() && !entity.isElytraFlying() && 
				!(entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isFlying))
			entity.motionY += 0.02f;

	}

	@SubscribeEvent
	public static void preventFallDamage(LivingFallEvent event) {
		// prevent fall damage if enabled in config and wearing set
		if (Config.preventFallDamage && event.getEntity() != null &&
				SetManager.getWornSet(event.getEntity()) != null)
			event.setCanceled(true);
		// prevent fall damage for pharah
		else if (event.getEntity() != null && 
				SetManager.getWornSet(event.getEntity()) == EnumHero.PHARAH)
			event.setCanceled(true);
		// genji fall
		else if (event.getEntity() != null && 
				SetManager.getWornSet(event.getEntity()) == EnumHero.GENJI) 
			event.setDistance(event.getDistance()*0.8f);
	}

	@SubscribeEvent
	public static void junkratDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof EntityLivingBase && !event.getEntity().world.isRemote &&
				SetManager.getWornSet(event.getEntity()) == EnumHero.JUNKRAT) {
			ModSoundEvents.JUNKRAT_DEATH.playSound(event.getEntity(), 1, 1);
			for (int i=0; i<6; ++i) {
				EntityJunkratGrenade grenade = new EntityJunkratGrenade(event.getEntity().world, 
						(EntityLivingBase) event.getEntity(), -1);
				grenade.explodeTimer = 20+i*2;
				grenade.setPosition(event.getEntity().posX, event.getEntity().posY+event.getEntity().height/2d, event.getEntity().posZ);
				grenade.motionX = (event.getEntity().world.rand.nextDouble()-0.5d)*0.1d;
				grenade.motionY = (event.getEntity().world.rand.nextDouble()-0.5d)*0.1d;
				grenade.motionZ = (event.getEntity().world.rand.nextDouble()-0.5d)*0.1d;
				event.getEntity().world.spawnEntity(grenade);
				grenade.isDeathGrenade = true;
				Minewatch.network.sendToAll(new SPacketSimple(24, grenade, false, grenade.explodeTimer, 0, 0));
			}
		}
	}

}