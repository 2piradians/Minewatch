package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class EntityAnaBullet extends EntityMWThrowable
{
	private boolean heal = false;

	public EntityAnaBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityAnaBullet(World worldIn, EntityLivingBase throwerIn, boolean heal) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.heal = heal;
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
						0xFFFCC7, 0xEAE7B9, 1, 8);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);
		
		if (result.entityHit instanceof EntityLivingBase && this.getThrower() instanceof EntityPlayer && 
				result.entityHit != this.getThrower() && !this.world.isRemote) {
			if (this.heal) {
				((EntityLivingBase)result.entityHit).heal(75/ItemMWWeapon.DAMAGE_SCALE);
				((WorldServer)result.entityHit.world).spawnParticle(EnumParticleTypes.HEART, 
						result.entityHit.posX+0.5d, result.entityHit.posY+0.5d,result.entityHit.posZ+0.5d, 
						10, 0.4d, 0.4d, 0.4d, 0d, new int[0]);
				result.entityHit.world.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
						ModSoundEvents.anaHeal, SoundCategory.PLAYERS, 0.3f, result.entityHit.world.rand.nextFloat()/2+1.5f);	
			}
			else {
				((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 60F/ItemMWWeapon.DAMAGE_SCALE);
				result.entityHit.world.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
						ModSoundEvents.hurt, SoundCategory.PLAYERS, 0.3f, result.entityHit.world.rand.nextFloat()/2+0.75f);
			}
			this.setDead();
		}
	}
}
