package twopiradians.minewatch.common.potion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.SPacketPotionEffect;
import twopiradians.minewatch.packet.SPacketSpawnParticle;

public class PotionFrozen extends Potion {

	public static HashMap<UUID, Triple<Float, Float, Float>> rotations = Maps.newHashMap();
	public HashMap<EntityLivingBase, Integer> clientFreezes = Maps.newHashMap();
	public HashMap<EntityLivingBase, Integer> serverFreezes = Maps.newHashMap();
	public HashMap<EntityLivingBase, Integer> clientDelays = Maps.newHashMap();
	public HashMap<EntityLivingBase, Integer> serverDelays = Maps.newHashMap();

	public PotionFrozen(boolean isBadEffectIn, int liquidColorIn) {
		super(isBadEffectIn, liquidColorIn);
		MinecraftForge.EVENT_BUS.register(this);
		this.setPotionName("Frozen");
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -255D, 2);
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) { 
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Minewatch.MODID+":textures/effects/frozen.png"));
		Minecraft.getMinecraft().currentScreen.drawTexturedModalRect(x+6, y+8, 0, 0, 16, 16);
		GlStateManager.popMatrix();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) { 
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Minewatch.MODID+":textures/effects/frozen.png"));
		Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(x+3, y+4, 0, 0, 18, 18);
		GlStateManager.popMatrix();
	}

	@Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
		super.removeAttributesModifiersFromEntity(entity, map, amplifier);

		if (amplifier == 0) {
			Minewatch.network.sendToAll(new SPacketSpawnParticle(2, entity.posX, entity.posY+entity.height/2, entity.posZ, 0, 0, 0, 0));
			entity.world.playSound(null, entity.getPosition(), ModSoundEvents.meiUnfreeze, SoundCategory.NEUTRAL, 0.8f, 1.0f);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void colorEntities(RenderLivingEvent.Pre<EntityLivingBase> event) {
		if (clientFreezes.containsKey(event.getEntity()) || 
				(event.getEntity().getActivePotionEffect(ModPotions.frozen) != null && 
				event.getEntity().getActivePotionEffect(ModPotions.frozen).getDuration() > 0) &&
				event.getEntity().getActivePotionEffect(ModPotions.frozen).getAmplifier() == 0) {
			int freeze = clientFreezes.containsKey(event.getEntity()) ? clientFreezes.get(event.getEntity()) : 30;
			event.getEntity().maxHurtTime = -1;
			event.getEntity().hurtTime = -1;
			GlStateManager.color(1f-freeze/30f, 1f-freeze/120f, 1f);
			Random rand = event.getEntity().world.rand;
			if (rand.nextInt(130 - freeze*2) == 0)
				event.getEntity().world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, 
						(event.getEntity().posX+rand.nextDouble()-0.5d)*event.getEntity().width, 
						event.getEntity().posY+rand.nextDouble()-0.5d+event.getEntity().height/2, 
						(event.getEntity().posZ+rand.nextDouble()-0.5d)*event.getEntity().width, 
						(rand.nextDouble()-0.5d)*0.5d, 
						rand.nextDouble()-0.5d, 
						(rand.nextDouble()-0.5d)*0.5d, 
						new int[0]);
			if (rand.nextInt(70 - freeze*2) == 0)
				Minewatch.proxy.spawnParticlesMeiBlaster(event.getEntity().world, 
						event.getEntity().posX+rand.nextDouble()-0.5d, 
						event.getEntity().posY+rand.nextDouble()-0.5d+event.getEntity().height/2, 
						event.getEntity().posZ+rand.nextDouble()-0.5d, 
						0, (rand.nextDouble())*0.2f, 0, rand.nextFloat(), 8, 2.5f, 2f);
		}
	}

	@SubscribeEvent
	public void clientSide(PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT) {
			if (event.player.getActivePotionEffect(ModPotions.frozen) != null &&
					event.player.getActivePotionEffect(ModPotions.frozen).getDuration() == 0)
				event.player.removeActivePotionEffect(ModPotions.frozen);

			if (event.player.ticksExisted % 2 == 0) {
				ArrayList<EntityLivingBase> toRemove = new ArrayList<EntityLivingBase>();
				for (EntityLivingBase entity : clientFreezes.keySet())
					if (clientFreezes.get(entity) > 1 && !entity.isDead &&
							(entity.getActivePotionEffect(ModPotions.frozen) == null || 
							entity.getActivePotionEffect(ModPotions.frozen).getDuration() == 0)) {
						if (clientDelays.containsKey(entity)) {
							if (clientDelays.get(entity) > 1)
								clientDelays.put(entity, clientDelays.get(entity) - 1);
							else
								clientDelays.remove(entity);
						}
						else
							clientFreezes.put(entity, clientFreezes.get(entity) - 1);
					}
					else 
						toRemove.add(entity);
				for (EntityLivingBase entity : toRemove)
					clientFreezes.remove(entity);
			}
		}
	}

	@SubscribeEvent
	public void serverSide(WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.world.getTotalWorldTime() % 6 == 0) {
			ArrayList<EntityLivingBase> toRemove = new ArrayList<EntityLivingBase>();
			for (EntityLivingBase entity : serverFreezes.keySet())
				if (serverFreezes.get(entity) > 1 && !entity.isDead && 
						(entity.getActivePotionEffect(ModPotions.frozen) == null || 
						entity.getActivePotionEffect(ModPotions.frozen).getDuration() == 0)) {
					int level = serverFreezes.get(entity) / 5;
					// apply freeze/slowness effect
					if (serverFreezes.get(entity) >= 30) {
						entity.removePotionEffect(MobEffects.SLOWNESS);
						PotionEffect effect = new PotionEffect(ModPotions.frozen, 60, 0, false, true);
						entity.setRevengeTarget(null);
						if (entity instanceof EntityLiving)
							((EntityLiving)entity).setAttackTarget(null);
						entity.addPotionEffect(effect);
						Minewatch.network.sendToAll(new SPacketPotionEffect(entity, effect));
						entity.world.playSound(null, entity.getPosition(), ModSoundEvents.meiFreeze, SoundCategory.NEUTRAL, 1.0f, 1.0f);
						Minewatch.network.sendToAll(new SPacketSpawnParticle(2, entity.posX, entity.posY+entity.height/2, entity.posZ, 0, 0, 0, 0));
						serverFreezes.put(entity, serverFreezes.get(entity) - 1);
					}
					else
						entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10, level, true, true));
					if (serverDelays.containsKey(entity)) {
						if (serverDelays.get(entity) > 1)
							serverDelays.put(entity, serverDelays.get(entity) - 1);
						else
							serverDelays.remove(entity);
					}
					else
						serverFreezes.put(entity, serverFreezes.get(entity) - 1);
				}
				else
					toRemove.add(entity);
			for (EntityLivingBase entity : toRemove)
				serverFreezes.remove(entity);
		}
	}

	/**Stop player from using mouse buttons while frozen*/
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void mouseEvent(MouseEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (event.isButtonstate() && player != null && 
				(player.getActivePotionEffect(ModPotions.frozen) != null && 
				player.getActivePotionEffect(ModPotions.frozen).getDuration() > 0))
			event.setCanceled(true);
	}

	/**Stop player from moving camera while frozen*/
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void viewEvent(EntityViewRenderEvent.CameraSetup event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player != null && rotations.containsKey(player.getPersistentID()) &&
				(player.getActivePotionEffect(ModPotions.frozen) != null && 
				player.getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) {
			Triple<Float, Float, Float> triple = rotations.get(player.getPersistentID());
			player.rotationPitch = triple.getLeft();
			player.rotationYaw = triple.getMiddle();
			player.rotationYawHead = triple.getRight();
			event.setPitch(triple.getLeft());
			event.setYaw(triple.getMiddle() + 180.0F);
		}
	}

	@SubscribeEvent
	public void setEntityRotations(LivingUpdateEvent event) {
		Triple<Float, Float, Float> triple = rotations.get(event.getEntityLiving().getPersistentID());
		if (triple != null) {
			if (event.getEntityLiving().getActivePotionEffect(ModPotions.frozen) == null || 
					event.getEntityLiving().getActivePotionEffect(ModPotions.frozen).getDuration() == 0)
				rotations.remove(event.getEntityLiving().getPersistentID());
			else {
				event.getEntityLiving().rotationPitch = triple.getLeft();
				event.getEntityLiving().rotationYaw = triple.getMiddle();
				event.getEntityLiving().rotationYawHead = triple.getRight();
			}
		}
	}

	@SubscribeEvent
	public void preventAttacking(LivingAttackEvent event) {
		if (event.getSource().getEntity() instanceof EntityLivingBase &&
				((EntityLivingBase) event.getSource().getEntity()).getActivePotionEffect(ModPotions.frozen) != null && 
				((EntityLivingBase) event.getSource().getEntity()).getActivePotionEffect(ModPotions.frozen).getDuration() > 0) {
			if (event.getEntity() instanceof EntityLiving)
				((EntityLiving)event.getSource().getEntity()).setAttackTarget(null);
			((EntityLivingBase) event.getSource().getEntity()).setRevengeTarget(null);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void preventJumping(LivingJumpEvent event) {
		if (event.getEntity() instanceof EntityLivingBase &&
				((EntityLivingBase) event.getEntity()).getActivePotionEffect(ModPotions.frozen) != null && 
				((EntityLivingBase) event.getEntity()).getActivePotionEffect(ModPotions.frozen).getDuration() > 0)  {
			event.getEntity().motionY = 0;
			event.getEntity().isAirBorne = false;
		}
	}

	@SubscribeEvent
	public void preventTeleporting(EnderTeleportEvent event) {
		if (event.getEntity() instanceof EntityLivingBase &&
				((((EntityLivingBase) event.getEntity()).getActivePotionEffect(ModPotions.frozen) != null && 
				((EntityLivingBase) event.getEntity()).getActivePotionEffect(ModPotions.frozen).getDuration() > 0) || 
						event.getEntity() instanceof EntityEnderman && (clientFreezes.containsKey(event.getEntity()) ||
								serverFreezes.containsKey(event.getEntity()))))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void preventTargeting(LivingSetAttackTargetEvent event) {
		if (event.getTarget() != null && event.getEntity() instanceof EntityLivingBase &&
				((EntityLivingBase) event.getEntity()).getActivePotionEffect(ModPotions.frozen) != null && 
				((EntityLivingBase) event.getEntity()).getActivePotionEffect(ModPotions.frozen).getDuration() > 0) {
			if (event.getEntity() instanceof EntityLiving)
				((EntityLiving)event.getEntity()).setAttackTarget(null);
			((EntityLivingBase) event.getEntity()).setRevengeTarget(null);
		}
	}

}