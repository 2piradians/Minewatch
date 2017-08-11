package twopiradians.minewatch.common.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import twopiradians.minewatch.common.Minewatch;

public class EntityHanzoSonicArrow extends EntityHanzoArrow {

	public EntityHanzoSonicArrow(World worldIn) {
		super(worldIn);
	}

	public EntityHanzoSonicArrow(World worldIn, EntityLivingBase shooter) {
		super(worldIn, shooter);
	}

	public static boolean doEffect(World world, Entity ignoreEntity, Entity trackEntity, double x, double y, double z, int ticks) {
		if (!world.isRemote && (ticks+1) % 10 == 0) {
			AxisAlignedBB aabb = new AxisAlignedBB(x-10d, y-10d, z-10d, x+10d, y+10d, z+10d);
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(ignoreEntity, aabb);
			for (Entity entity2 : list) 
				if (entity2 instanceof EntityLivingBase) 
					((EntityLivingBase) entity2).addPotionEffect(new PotionEffect(MobEffects.GLOWING, 30, 0, true, false));
		}

		if (world.isRemote && 
				(ticks == 1 || ticks == 7 || ticks == 10 || ticks == 13))
			if (trackEntity != null)
				Minewatch.proxy.spawnParticlesHanzoSonic(world, trackEntity, true);
			else
				Minewatch.proxy.spawnParticlesHanzoSonic(world, x, y, z, true);
		else if (world.isRemote && ticks > 30 && ticks < 170 &&
				(ticks % 30 == 0 || ticks % 32 == 0))
			if (trackEntity != null)
				Minewatch.proxy.spawnParticlesHanzoSonic(world, trackEntity, false);
			else
				Minewatch.proxy.spawnParticlesHanzoSonic(world, x, y, z, false);
		else if (ticks > 200)
			return true;

		return false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.inGround && doEffect(world, shootingEntity, null, posX, posY, posZ, timeInGround))
			this.setDead();
	}

	@Override
	protected void onHit(RayTraceResult result) {
		super.onHit(result);	

		if (result.entityHit != null && result.entityHit != this.shootingEntity) 
			new EntityHitHandler(result.entityHit);
	}

	private class EntityHitHandler {

		private HashMap<Entity, Integer> hitEntities = Maps.newHashMap();

		public EntityHitHandler(Entity entity) {
			hitEntities.put(entity, 0);
			try { // sometimes has some weird registration exception
				MinecraftForge.EVENT_BUS.register(this);
			}
			catch (Exception e) {
				System.out.println("Caught exception: ");
				e.printStackTrace();
			}
		}

		@SubscribeEvent
		public void clientSide(PlayerTickEvent event) {
			if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT) {
				ArrayList<Entity> toRemove = new ArrayList<Entity>();
				for (Entity entity : hitEntities.keySet()) {
					if (entity == null || doEffect(entity.world, null, entity, entity.posX, 
							entity.posY, entity.posZ, hitEntities.get(entity)))
						toRemove.add(entity);
					else
						hitEntities.put(entity, hitEntities.get(entity)+1);
				}
				for (Entity entity : toRemove)
					hitEntities.remove(entity);
			}
		}

		@SubscribeEvent
		public void serverSide(WorldTickEvent event) {
			if (event.phase == TickEvent.Phase.END && event.world.rand.nextBoolean()) {
				ArrayList<Entity> toRemove = new ArrayList<Entity>();
				for (Entity entity : hitEntities.keySet()) {
					if (entity == null || doEffect(entity.world, null, entity, entity.posX, 
							entity.posY, entity.posZ, hitEntities.get(entity)))
						toRemove.add(entity);
					else
						hitEntities.put(entity, hitEntities.get(entity)+1);
				}
				for (Entity entity : toRemove)
					hitEntities.remove(entity);
			}
		}

	}

}
