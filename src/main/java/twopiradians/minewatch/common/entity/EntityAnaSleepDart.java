package twopiradians.minewatch.common.entity;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityAnaSleepDart extends EntityMW {

	public EntityAnaSleepDart(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityAnaSleepDart(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 10;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 10, 0.05d, 0x6FE8E6, 0xECFDFE, 0.5f, 8, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (EntityHelper.attemptImpact(this, result.entityHit, 5, true, DamageSource.causeIndirectDamage(this, this.getThrower())) &&
				result.entityHit.isNonBoss() && !(result.entityHit instanceof EntityLivingBaseMW)) {
			TickHandler.interrupt(result.entityHit);
			TickHandler.register(this.world.isRemote, ItemAnaRifle.SLEEP.setEntity(result.entityHit).setTicks(120),
					Handlers.PREVENT_INPUT.setEntity(result.entityHit).setTicks(120),
					Handlers.PREVENT_MOVEMENT.setEntity(result.entityHit).setTicks(120),
					Handlers.PREVENT_ROTATION.setEntity(result.entityHit).setTicks(120));
			if (result.entityHit instanceof EntityLivingBase) 
				Handlers.rotations.put((EntityLivingBase) result.entityHit, Triple.of(0f, 0f, 0f));
			Minewatch.network.sendToDimension(new SPacketSimple(12, result.entityHit, false), this.world.provider.getDimension());
			Minewatch.proxy.playFollowingSound(result.entityHit, ModSoundEvents.anaSleepHit, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
			Minewatch.proxy.playFollowingSound(this.getThrower(), ModSoundEvents.anaSleepVoice, SoundCategory.PLAYERS, 0.5f, 1.0f, false);
		}
	}
}
