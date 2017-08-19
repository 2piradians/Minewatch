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

	private EntityHitHandler handler = new EntityHitHandler();

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
				Minewatch.proxy.spawnParticlesHanzoSonic(world, x, y, z, true, false);
		else if (world.isRemote && ticks > 30 && ticks < 170 &&
				(ticks % 30 == 0 || ticks % 32 == 0))
			if (trackEntity != null)
				Minewatch.proxy.spawnParticlesHanzoSonic(world, trackEntity, false);
			else
				Minewatch.proxy.spawnParticlesHanzoSonic(world, x, y, z, false, false);
		else if (ticks > 200)
			return true;

		return false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.inGround && doEffect(world, shootingEntity, null, posX, posY, posZ, timeInGround))
			this.setDead();
		else if (!this.inGround && this.world.isRemote) {
			if (this.ticksExisted % 2 == 0)
				Minewatch.proxy.spawnParticlesHanzoSonic(world, posX, posY, posZ, false, true);

			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*5d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.world, 
						this.posX+(this.lastTickPosX-this.posX)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posY+(this.lastTickPosY-this.posY)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posZ+(this.lastTickPosZ-this.posZ)*i/numParticles+world.rand.nextDouble()*0.05d, 
						0, 0, 0, 0x5EDCE5, 0x007acc, 1, 20);
		}
	}

	@Override
	protected void onHit(RayTraceResult result) {
		super.onHit(result);	

		if (result.entityHit != null && result.entityHit != this.shootingEntity) {
			if (result.entityHit.world.isRemote)
				this.handler.clientHitEntities.put(result.entityHit, 0);
			else
				this.handler.serverHitEntities.put(result.entityHit, 0);
		}
	}

	private static class EntityHitHandler {

		public HashMap<Entity, Integer> serverHitEntities = Maps.newHashMap();
		public HashMap<Entity, Integer> clientHitEntities = Maps.newHashMap();

		public EntityHitHandler() {
			MinecraftForge.EVENT_BUS.register(this);
		}

		@SubscribeEvent
		public void clientSide(PlayerTickEvent event) {
			if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT) {
				ArrayList<Entity> toRemove = new ArrayList<Entity>();
				for (Entity entity : clientHitEntities.keySet()) {
					if (entity == null || entity.isDead || doEffect(entity.world, null, entity, entity.posX, 
							entity.posY, entity.posZ, clientHitEntities.get(entity))) 
						toRemove.add(entity);
					else
						clientHitEntities.put(entity, clientHitEntities.get(entity)+1);
				}
				for (Entity entity : toRemove)
					clientHitEntities.remove(entity);
			}
		}

		@SubscribeEvent
		public void serverSide(WorldTickEvent event) {
			if (event.phase == TickEvent.Phase.END && event.world.getTotalWorldTime() % 3 == 0) {
				ArrayList<Entity> toRemove = new ArrayList<Entity>();
				for (Entity entity : serverHitEntities.keySet()) {
					if (entity == null || entity.isDead || doEffect(entity.world, null, entity, entity.posX, 
							entity.posY, entity.posZ, serverHitEntities.get(entity))) 
						toRemove.add(entity);
					else
						serverHitEntities.put(entity, serverHitEntities.get(entity)+1);
				}
				for (Entity entity : toRemove)
					serverHitEntities.remove(entity);
			}
		}

	}

}
