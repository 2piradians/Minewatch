package twopiradians.minewatch.common.entity.projectile;

import java.util.ArrayList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityMoiraHealEnergy extends EntityMW {

	private ArrayList<EntityLivingBase> affectedEntities = new ArrayList<EntityLivingBase>();

	public static final Handler HEAL = new Handler(Identifier.MOIRA_HEAL, false) {
		@Override
		public boolean onServerTick() {
			// heal
			if (this.entity != null && this.entityLiving != null && this.ticksLeft < this.initialTicks)
				EntityHelper.attemptDamage(this.entity, this.entityLiving, -0.8333f, true);
			return super.onServerTick();
		}
	};

	public EntityMoiraHealEnergy(World worldIn) {
		this(worldIn, null, -1);

	}

	public EntityMoiraHealEnergy(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
		this.lifetime = 10; 
		this.isFriendly = true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.CIRCLE, world, shooter, 
				0xFFF555, 0xC2B55F, 0.2f, 5, 2, 2, 0, 0, hand, 20, 0.6f);
		Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, 
				posX+(world.rand.nextFloat()-0.5f)*0.1f, 
				posY+(world.rand.nextFloat()-0.5f)*0.1f, 
				posZ+(world.rand.nextFloat()-0.5f)*0.1f, 
				motionX/2f+(world.rand.nextFloat()-0.5f)*0.01f, 
				motionY/2f+(world.rand.nextFloat()-0.5f)*0.01f, 
				motionZ/2f+(world.rand.nextFloat()-0.5f)*0.01f, 
				0xFFF555, 0xE2D171, 0.4f, 20, 0.5f, 5f, world.rand.nextFloat(), world.rand.nextFloat()/10f);
		for (int i=0; i<5; ++i)
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, 
					posX+(world.rand.nextFloat()-0.5f)*0.1f, 
					posY+(world.rand.nextFloat()-0.5f)*0.1f, 
					posZ+(world.rand.nextFloat()-0.5f)*0.1f, 
					motionX/2f+(world.rand.nextFloat()-0.5f)*0.1f, 
					motionY/2f+(world.rand.nextFloat()-0.5f)*0.1f, 
					motionZ/2f+(world.rand.nextFloat()-0.5f)*0.1f, 
					0xFFF555, 0xC2B55F, 0.4f, 20, 1, 1.3f, world.rand.nextFloat(), world.rand.nextFloat()/10f);
		for (int i=0; i<1; ++i)
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, 
					posX+(world.rand.nextFloat()-0.5f)*0.1f, 
					posY+(world.rand.nextFloat()-0.5f)*0.1f, 
					posZ+(world.rand.nextFloat()-0.5f)*0.1f, 
					motionX/2f+(world.rand.nextFloat()-0.5f)*0.1f, 
					motionY/2f+(world.rand.nextFloat()-0.5f)*0.1f, 
					motionZ/2f+(world.rand.nextFloat()-0.5f)*0.1f, 
					0xFFFCE0, 0xFFFFFF, 0.4f, 20, 0.2f, 0.5f, world.rand.nextFloat(), world.rand.nextFloat()/10f);
	}

	@Override
	public void spawnTrailParticles() {
		//EntityHelper.spawnTrailParticles(this, 10, 0.05d, this.isFriendly ? 0xFFFCC7 : 0x9361D4, 0xEAE7B9, 0.5f, 8, 1); 
	}

	/**Should this move to the hit position of the RayTraceResult*/
	protected void onImpactMoveToHitPosition(RayTraceResult result) {}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		// heal
		if (!this.world.isRemote && !affectedEntities.contains(result.entityHit) && 
				EntityHelper.attemptDamage(this, result.entityHit, -4f+0.8333f, true) && 
				result.entityHit instanceof EntityLivingBase) {
			affectedEntities.add((EntityLivingBase) result.entityHit);
			TickHandler.register(false, HEAL.setEntity(getThrower()).setEntityLiving((EntityLivingBase) result.entityHit).setTicks(61));
		}
	}
}
