package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityAnaGrenade extends EntityMW {

	public static final Handler DAMAGE = new Handler(Identifier.ANA_GRENADE_DAMAGE, false) {
		@Override
		public boolean onServerTick() {
			if (entityLiving != null && this.number > 0 && entityLiving.getHealth() > this.number)
				entityLiving.setHealth((float) this.number);
			return super.onServerTick();
		}
	};

	public static final Handler HEAL = new Handler(Identifier.ANA_GRENADE_HEAL, false) {};

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
			boolean damage = false;
			boolean heal = false;
			for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(4))) {
				if (entity.isEntityAlive()) { 
					// heal
					if (EntityHelper.attemptDamage(this, entity, -100, true)) {
						TickHandler.register(false, HEAL.setEntity(entity).setTicks(80));
						Minewatch.network.sendToDimension(new SPacketSimple(53, entity, true), world.provider.getDimension());
						if (getThrower() != entity)
							heal = true;
					}
					// damage
					else if (EntityHelper.attemptDamage(this, entity, 60, true)) {
						TickHandler.register(false, DAMAGE.setEntity(entity).setTicks(80).setNumber(entity.getHealth()));
						Minewatch.network.sendToDimension(new SPacketSimple(53, entity, false, entity.getHealth(), 0, 0), world.provider.getDimension());
						damage = true;
					}

				}
			}

			if (damage)
				ModSoundEvents.ANA_GRENADE_DAMAGE_VOICE.playFollowingSound(getThrower(), 1.0f, 1.0f, false);
			else if (heal)
				ModSoundEvents.ANA_GRENADE_HEAL_VOICE.playFollowingSound(getThrower(), 1.0f, 1.0f, false);
		}

		// sounds and particles
		if (this.world.isRemote) {
			ModSoundEvents.ANA_GRENADE_HIT.playSound(this, 1.0f, 1.0f, false);

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