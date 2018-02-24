package twopiradians.minewatch.common.entity.ability;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityHanzoArrow;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityHanzoSonicArrow extends EntityHanzoArrow {

	public static final Handler SONIC = new Handler(Identifier.HANZO_SONIC, false) {
		@Override
		public boolean onServerTick() {
			return entity == null || doEffect(entity.worldObj, entityLiving, entity, entity.posX, 
					entity.posY, entity.posZ, this.ticksLeft++);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// glowing
			if ((this.ticksLeft+1) % 10 == 0) { 
				AxisAlignedBB aabb = entity.getEntityBoundingBox().expandXyz(10);
				List<Entity> list = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entityLiving, aabb);
				for (Entity entity2 : list) 
					if (entity2 instanceof EntityLivingBase && EntityHelper.shouldHit(entityLiving, entity2, false) &&
							EntityHelper.shouldTarget(entityLiving, Minewatch.proxy.getRenderViewEntity(), true)) {
						Handler handler = TickHandler.getHandler(entity2, Identifier.GLOWING);
						if (handler == null)
							TickHandler.register(true, Handlers.CLIENT_GLOWING.setEntity(entity2).setTicks(30));
						else
							handler.ticksLeft = 30;
					}
			}

			return onServerTick(); // calls onServerTick to handle effect
		}
	};

	public EntityHanzoSonicArrow(World worldIn) {
		super(worldIn);
	}

	public EntityHanzoSonicArrow(World worldIn, EntityLivingBase shooter) {
		super(worldIn, shooter);
	}

	public static boolean doEffect(World worldObj, EntityLivingBase ignoreEntity, Entity trackEntity, double x, double y, double z, int ticks) {
		if (worldObj.isRemote && 
				(ticks == 1 || ticks == 7 || ticks == 10 || ticks == 13))
			if (trackEntity != null)
				Minewatch.proxy.spawnParticlesHanzoSonic(worldObj, trackEntity, true);
			else
				Minewatch.proxy.spawnParticlesHanzoSonic(worldObj, x, y, z, true, false);
		else if (worldObj.isRemote && ticks > 30 && ticks < 170 &&
				(ticks % 30 == 0 || ticks % 32 == 0))
			if (trackEntity != null)
				Minewatch.proxy.spawnParticlesHanzoSonic(worldObj, trackEntity, false);
			else
				Minewatch.proxy.spawnParticlesHanzoSonic(worldObj, x, y, z, false, false);
		else if (ticks > 200)
			return true;

		return false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.inGround && doEffect(worldObj, this.getThrower(), null, posX, posY, posZ, timeInGround))
			this.setDead();
		else if (!this.inGround && this.worldObj.isRemote) {
			if (this.ticksExisted % 2 == 0)
				Minewatch.proxy.spawnParticlesHanzoSonic(worldObj, posX, posY, posZ, false, true);
		}

		// glowing
		if (this.inGround && worldObj.isRemote && this.ticksExisted % 10 == 0) {
			AxisAlignedBB aabb = this.getEntityBoundingBox().expandXyz(10);
			List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(getThrower(), aabb);
			for (Entity entity2 : list) 
				if (entity2 instanceof EntityLivingBase && EntityHelper.shouldHit(getThrower(), entity2, false) &&
						EntityHelper.shouldTarget(getThrower(), Minewatch.proxy.getRenderViewEntity(), true)) {
					Handler handler = TickHandler.getHandler(entity2, Identifier.GLOWING);
					if (handler == null)
						TickHandler.register(true, Handlers.CLIENT_GLOWING.setEntity(entity2).setTicks(30));
					else
						handler.ticksLeft = 30;
				}
		}
	}

	@Override
	public void spawnTrailParticles() {
		if (!this.inGround)
			EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0x5EDCE5, 0x007acc, 1, 20, 1);
	}

	@Override
	protected void onHit(RayTraceResult result) {
		if (EntityHelper.shouldHit(this.getThrower(), result.entityHit, false)) 
			TickHandler.register(result.entityHit.worldObj.isRemote, SONIC.setEntity(result.entityHit).setEntityLiving(this.getThrower()).setTicks(0).setAllowDead(true));

		super.onHit(result);	
	}

}