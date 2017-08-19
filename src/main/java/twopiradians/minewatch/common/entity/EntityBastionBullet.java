package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class EntityBastionBullet extends EntityMWThrowable {

	public EntityBastionBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityBastionBullet(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 15;
	}
	
	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*10d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.world, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posY+(this.prevPosY-this.posY)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+world.rand.nextDouble()*0.05d, 
						0, 0, 0, 0xFFFCC7, 0xEAE7B9, 1, 1);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (result.entityHit instanceof EntityLivingBase && this.getThrower() != null &&
				result.entityHit != this.getThrower() && !this.world.isRemote) {
			float damage = 20 - (20 - 6) * ((float)this.ticksExisted / lifetime);
			((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), damage/ItemMWWeapon.DAMAGE_SCALE);
			((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
			result.entityHit.world.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
					ModSoundEvents.hurt, SoundCategory.PLAYERS, 0.3f, result.entityHit.world.rand.nextFloat()/2+0.75f);
			this.setDead();
		}
	}
}
