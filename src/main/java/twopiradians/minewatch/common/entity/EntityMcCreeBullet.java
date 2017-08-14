package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityMcCreeBullet extends EntityMWThrowable {

	public EntityMcCreeBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityMcCreeBullet(World worldIn, EntityLivingBase throwerIn) {
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
						this.posX+(this.prevPosX-this.posX)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posY+(this.prevPosY-this.posY)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+world.rand.nextDouble()*0.05d, 
						0x5AD8E8, 0x5A575A, 0.8f, 7);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);
		
		if (result.entityHit instanceof EntityLivingBase && this.getThrower() instanceof EntityPlayer &&
				result.entityHit != this.getThrower() && !this.world.isRemote) {
			float damage = 70 - (70 - 21) * (this.ticksExisted / lifetime);
			((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), damage/ItemMWWeapon.DAMAGE_SCALE);
			((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
			this.setDead();
		}
	}
}
