package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;

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

		if (this.world.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*30d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.world, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posY+this.height/2+(this.prevPosY-this.posY)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+world.rand.nextDouble()*0.05d, 
						0, 0, 0, 0xC8E682, 0x709233, 0.5f, 4, 1);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (this.getThrower() instanceof EntityPlayer && result.entityHit != this.getThrower()) {
			if (result.entityHit instanceof EntityLivingBase && ((EntityLivingBase)result.entityHit).getHealth() > 0) {
				if (!this.world.isRemote) {
					((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 28F*ItemMWWeapon.damageScale);
					((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
				}
				else 
					this.getThrower().playSound(ModSoundEvents.hurt, 0.3f, result.entityHit.world.rand.nextFloat()/2+0.75f);
			}

			Minewatch.proxy.spawnParticlesSpark(world, 
					result.entityHit == null ? result.hitVec.x : posX, 
							result.entityHit == null ? result.hitVec.y : posY, 
									result.entityHit == null ? result.hitVec.z : posZ, 
											0xC8E682, 0x709233, 5, 5);
			
			this.setDead();
		}
	}
}
