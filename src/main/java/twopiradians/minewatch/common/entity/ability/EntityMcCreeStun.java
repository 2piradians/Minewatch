package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityMcCreeStun extends EntityMW {

	public EntityMcCreeStun(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityMcCreeStun(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.2f, 0.2f);
		this.lifetime = 10;
		this.setNoGravity(true);
	}


	@Override
	public void onUpdate() {
		// gravity
		this.motionY -= 0.02d;

		super.onUpdate();
	}

	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 7, 0, 0xFFF9E3, 0xA18458, 1, 6, 1);
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		this.setDead();
	}

	@Override
	public void setDead() {
		if (!this.isDead) {
			// entities affected
			if (!worldObj.isRemote) {
				boolean hit = false;
				for (EntityLivingBase entity : this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expandXyz(2))) {
					if (entity.isEntityAlive() && 
							EntityHelper.attemptDamage(getThrower(), entity, 25, true)) {
						TickHandler.interrupt(entity);
						Minewatch.network.sendToDimension(new SPacketSimple(52, entity, false), worldObj.provider.getDimension());
						TickHandler.register(false, Handlers.PREVENT_INPUT.setEntity(entity).setTicks(17),
								Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks(17),
								Handlers.PREVENT_ROTATION.setEntity(entity).setTicks(17));
						hit = true;
					}
				}

				if (hit)
					ModSoundEvents.MCCREE_STUN_VOICE.playFollowingSound(getThrower(), 1.0f, 1.0f, false);
			}

			// sounds and particles
			if (this.worldObj.isRemote) {
				ModSoundEvents.MCCREE_STUN_HIT.playSound(this, 1.0f, 1.0f, false);

				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, worldObj, posX, posY, posZ, 0, 0, 0, 0xFFF9E3, 0xFFF9E3, 0.3f, 5, 30, 35, 0, 0);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, worldObj, posX, posY, posZ, 0, 0, 0, 0xFFF9E3, 0xFFFBEA, 1f, 5, 15, 8, 0, 0);
				for (int i=0; i<3; ++i)
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.SMOKE, worldObj, 
							posX+(worldObj.rand.nextFloat()-0.5f)*3f, 
							posY+(worldObj.rand.nextFloat()-0.5f)*1f, 
							posZ+(worldObj.rand.nextFloat()-0.5f)*3f, 
							(worldObj.rand.nextFloat()-0.5f)*0.2f, 
							(worldObj.rand.nextFloat()-0.5f)*0.1f, 
							(worldObj.rand.nextFloat()-0.5f)*0.2f, 
							0xFEEF71, 0xC8A043, 0.2f, (int) (15+(worldObj.rand.nextFloat()-0.5f)*5f), 15, 12, 
							worldObj.rand.nextFloat(), (worldObj.rand.nextFloat()-0.5f)*0.1f);
			}
		}

		super.setDead();
	}
}
