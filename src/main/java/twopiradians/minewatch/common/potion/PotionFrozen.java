package twopiradians.minewatch.common.potion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.packet.PacketPotionEffect;

public class PotionFrozen extends Potion {

	public static HashMap<UUID, Tuple<Float, Float>> rotations = Maps.newHashMap();
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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void colorEntities(RenderLivingEvent.Pre<EntityLivingBase> event) {
		if (clientFreezes.containsKey(event.getEntity()) || 
				(event.getEntity().getActivePotionEffect(ModPotions.frozen) != null && 
				event.getEntity().getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) {
			int freeze = clientFreezes.containsKey(event.getEntity()) ? clientFreezes.get(event.getEntity()) : 30;
			event.getEntity().maxHurtTime = -1;
			event.getEntity().hurtTime = -1;
			GlStateManager.color(1f-freeze/50f, 1f-freeze/150f, 1f);
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
						0, (rand.nextDouble())*0.2f, 0, rand.nextFloat(), 8);
		}
	}

	@SubscribeEvent
	public void clientSide(PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT) {
			if (event.player.ticksExisted % 2 == 0) {
				/*if (!clientFreezes.isEmpty())
					System.out.println(clientFreezes);*/
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
			if (!serverFreezes.isEmpty())
				System.out.println(serverFreezes);
			ArrayList<EntityLivingBase> toRemove = new ArrayList<EntityLivingBase>();
			for (EntityLivingBase entity : serverFreezes.keySet())
				if (serverFreezes.get(entity) > 1 && !entity.isDead && 
						(entity.getActivePotionEffect(ModPotions.frozen) == null || 
						entity.getActivePotionEffect(ModPotions.frozen).getDuration() == 0)) {
					if (serverDelays.containsKey(entity)) {
						if (serverDelays.get(entity) > 1)
							serverDelays.put(entity, serverDelays.get(entity) - 1);
						else
							serverDelays.remove(entity);
					}
					else {
						int level = serverFreezes.get(entity) / 10;
						// apply freeze/slowness effect
						if (level == 3) {
							entity.removePotionEffect(MobEffects.SLOWNESS);
							PotionEffect effect = new PotionEffect(ModPotions.frozen, 60, 0, true, true);
							entity.addPotionEffect(effect);
							Minewatch.network.sendToAll(new PacketPotionEffect(entity, effect));
							serverFreezes.put(entity, serverFreezes.get(entity) - 1);
						}
						else
							entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10, level, true, true));
						serverFreezes.put(entity, serverFreezes.get(entity) - 1);
					}
				}
				else
					toRemove.add(entity);
			for (EntityLivingBase entity : toRemove)
				serverFreezes.remove(entity);
		}
	}

	/**Stop player from using mouse buttons while frozen*/
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
	public void viewEvent(EntityViewRenderEvent.CameraSetup event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player != null && rotations.containsKey(player.getPersistentID()) &&
				(player.getActivePotionEffect(ModPotions.frozen) != null && 
				player.getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) {
			Tuple<Float, Float> tuple = rotations.get(player.getPersistentID());
			player.rotationPitch = tuple.getFirst();
			player.rotationYaw = tuple.getSecond();
			event.setPitch(tuple.getFirst());
			event.setYaw(tuple.getSecond() + 180.0F);
		}
	}

	@SubscribeEvent
	public void setEntityRotations(LivingUpdateEvent event) {
		Tuple<Float, Float> tuple = rotations.get(event.getEntityLiving().getPersistentID());
		if (tuple != null) {
			if (event.getEntityLiving().getActivePotionEffect(ModPotions.frozen) == null || 
					event.getEntityLiving().getActivePotionEffect(ModPotions.frozen).getDuration() == 0)
				rotations.remove(event.getEntityLiving().getPersistentID());
			else {
				event.getEntityLiving().rotationPitch = tuple.getFirst();
				event.getEntityLiving().rotationYaw = tuple.getSecond();
			}
		}
	}

}