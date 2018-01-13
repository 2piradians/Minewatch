package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
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

public class EntityAnaGrenade extends EntityMW {

	public EntityAnaGrenade(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityAnaGrenade(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.15f, 0.15f);
		this.lifetime = 1200;
		this.setNoGravity(true);
	}


	@Override
	public void onUpdate() {
		// gravity
		this.motionY -= 0.05d;

		super.onUpdate();
	}

	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 10, 0, 0xE4D2A3, 0x6646AB, 1, 9, 1);
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		// entities affected
		if (!world.isRemote) {
			boolean hit = false;
			for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expandXyz(2))) {
				if (entity.isEntityAlive() && 
						EntityHelper.attemptDamage(getThrower(), entity, 25, true)) { // TODO
					Minewatch.network.sendToDimension(new SPacketSimple(52, entity, false), world.provider.getDimension());
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
		if (this.world.isRemote) {
			ModSoundEvents.MCCREE_STUN_HIT.playSound(this, 1.0f, 1.0f, false);

			for (int i=0; i<4; ++i)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.ANA_DAMAGE, world, posX, posY, posZ, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1f, 9, 5, 40, world.rand.nextFloat(), 0);
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, posX, posY, posZ, 0, 0, 0, 0xB4A779, 0xB4A779, 0.8f, 12, 7, 37, 0, 0);
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE, world, posX, posY, posZ, 0, 0, 0, 0x6646AB, 0x6646AB, 1f, 7, 5, 39, world.rand.nextFloat(), 0.05f);
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, posX, posY, posZ, 0, 0, 0, 0x6646AB, 0x6646AB, 1f, 4, 30, 35, 0, 0);
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, posX, posY, posZ, 0, 0, 0, 0xFCF8E1, 0xFDF7DF, 1f, 4, 20, 25, 0, 0);
		}

		this.setDead();
	}

}