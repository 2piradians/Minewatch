package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityGenjiShuriken extends EntityMWThrowable {

	public EntityGenjiShuriken(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityGenjiShuriken(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 40;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote && this.ticksExisted > 1) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*30d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.world, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+world.rand.nextDouble()*0.09d, 
						this.posY+(this.prevPosY-this.posY)*i/numParticles+world.rand.nextDouble()*0.09d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+world.rand.nextDouble()*0.09d, 
						0xC8E682, 0x709233, 0.5f, 4);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (this.getThrower() instanceof EntityPlayer && result.entityHit != this.getThrower()) {
			if (result.entityHit instanceof EntityLivingBase && !this.world.isRemote) {
				((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 28F/ItemMWWeapon.DAMAGE_SCALE);
				result.entityHit.world.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
						SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.3f, result.entityHit.world.rand.nextFloat()/2+0.75f);
				((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
				this.setDead();
			}

			Minewatch.proxy.spawnParticlesSpark(world, 
					result.entityHit == null ? result.hitVec.xCoord : posX, 
							result.entityHit == null ? result.hitVec.yCoord : posY, 
									result.entityHit == null ? result.hitVec.zCoord : posZ, 
											0xC8E682, 0x709233, 5, 5);
		}
	}
}
