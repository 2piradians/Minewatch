package twopiradians.minewatch.common.entity.ability;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.projectile.EntityHanzoArrow;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityHanzoSonicArrow extends EntityHanzoArrow {

	public static final Handler SONIC = new Handler(Identifier.HANZO_SONIC, false) {
		@Override
		public boolean onServerTick() {
			return entity == null || entity.isDead || doEffect(entity.world, null, entity, entity.posX, 
					entity.posY, entity.posZ, this.ticksLeft++);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			return onServerTick();
		}
	};

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
		if (this.inGround && doEffect(world, shootingEntity, null, posX, posY, posZ, timeInGround))
			this.setDead();
		else if (!this.inGround && this.world.isRemote) {
			if (this.ticksExisted % 2 == 0)
				Minewatch.proxy.spawnParticlesHanzoSonic(world, posX, posY, posZ, false, true);

			EntityHelper.spawnTrailParticles(this, 5, 0.05d, 0x5EDCE5, 0x007acc, 1, 20, 1);
		}

		super.onUpdate();
	}

	@Override
	protected void onHit(RayTraceResult result) {
		super.onHit(result);	

		if (EntityHelper.shouldHit(this.getThrower(), result.entityHit, false)) 
			TickHandler.register(result.entityHit.world.isRemote, SONIC.setEntity(result.entityHit).setTicks(0));
	}

}