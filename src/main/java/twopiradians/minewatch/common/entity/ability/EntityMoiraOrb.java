package twopiradians.minewatch.common.entity.ability;

import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.base.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityMoiraOrb extends EntityMW {

	private static final DataParameter<Boolean> HEAL = EntityDataManager.<Boolean>createKey(EntityMoiraOrb.class, DataSerializers.BOOLEAN);

	public CopyOnWriteArrayList<EntityLivingBase> tethered = new CopyOnWriteArrayList<EntityLivingBase>();

	public EntityMoiraOrb(World worldIn) {
		this(worldIn, null, -1, false);
	}

	public EntityMoiraOrb(World worldIn, EntityLivingBase throwerIn, int hand, boolean heal) {
		super(worldIn, Minecraft.getMinecraft().player/*throwerIn*/, hand); // TODO
		this.setSize(1f, 1f);
		this.lifetime = Integer.MAX_VALUE;// TODO 200;
		if (!worldIn.isRemote)
			this.getDataManager().set(HEAL, heal);
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, world, (EntityLivingBase) getThrower(), 
				0xFF9D1A, 0x964D21, 0.7f, 5, 5, 4.5f, world.rand.nextFloat(), 0.01f, hand, 10, 0.5f);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(HEAL, false);
	}

	@Override
	public void onUpdate() {
		this.isFriendly = !this.getDataManager().get(HEAL);

		// check for entities to tether to
		if (this.ticksExisted % 2 == 0) {
			this.tethered.clear();
			for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expandXyz(5)))
				if (entity instanceof EntityLivingBase/* && EntityHelper.shouldHit(this, entity, this.isFriendly)*/) // TODO
					this.tethered.add((EntityLivingBase) entity);
		}

		//super.onUpdate();
	}

	@Override
	public void spawnTrailParticles() {
		// trail/spark particles
		//EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0xFFD387, 0x423D37, 0.4f, 20, 0.3f);
	}

	@Override
	public void onImpact(RayTraceResult result) {	
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			// bounce
			if (result.sideHit == EnumFacing.DOWN || result.sideHit == EnumFacing.UP) 
				this.motionY *= -1.1d;
			else if (result.sideHit == EnumFacing.NORTH || result.sideHit == EnumFacing.SOUTH) 
				this.motionZ *= -0.7d;
			else 
				this.motionX *= -0.7d;
		}
	}

}
