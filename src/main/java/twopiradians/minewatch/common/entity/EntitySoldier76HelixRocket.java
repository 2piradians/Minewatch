package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntitySoldier76HelixRocket extends EntityMW {

	private static final DataParameter<Integer> NUMBER = EntityDataManager.<Integer>createKey(EntitySoldier76HelixRocket.class, DataSerializers.VARINT);
	/**non-spiral position*/
	private Vec3d vec;
	
	public EntitySoldier76HelixRocket(World worldIn) {
		this(worldIn, null, -1, -1);
	}

	public EntitySoldier76HelixRocket(World worldIn, EntityLivingBase throwerIn, int hand, int number) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		if (!worldIn.isRemote && number != -1)
			this.getDataManager().set(NUMBER, number);
		this.setNoGravity(true);
		this.lifetime = 60;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(NUMBER, Integer.valueOf(0));
	}
	
	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, world, shooter, 
				0x2B9191, 0x2B9191, 0.7f, 3, 8, 7.5f, 0, 0, hand, 12, 0.45f);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		// spiral rotation
		if (vec == null)
			vec = this.getPositionVector();
		else
			vec = vec.addVector(this.motionX, this.motionY, this.motionZ);

		float verticalAdjust = this.ticksExisted*30f + this.getDataManager().get(NUMBER)*120f;
		Vec3d vec2 = vec.add(EntityHelper.getLook(this.rotationPitch+verticalAdjust, this.rotationYaw+90).scale(0.2d));
		this.setPosition(vec2.x, vec2.y, vec2.z);
		
		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0x5EDCE5, 0x007acc, 1, 4, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (!(result.entityHit instanceof EntitySoldier76HelixRocket)) {
			// direct hit damage (explosions do plenty of damage - direct can't be much)
			if (EntityHelper.attemptImpact(this, result.entityHit, 1, false)) 
				result.entityHit.hurtResistantTime = 10;

			if (!TickHandler.hasHandler(result.entityHit, Identifier.GENJI_DEFLECT)) {
				// explosion
				if (this.world.isRemote) 
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.SMOKE, world, posX, posY, posZ, 
							0, 0, 0, 0x62E2FC, 0x203B7E, 1, 10, 25, 20, 0, 0);
				else {
					Minewatch.proxy.createExplosion(world, this.getThrower(), posX, posY, posZ, 
							1.6f, 40f, 80/3, 80/3, result.entityHit, 120/3, true);
					this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
				}
				this.setDead();
			}
		}
	}
}
