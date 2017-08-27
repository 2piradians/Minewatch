package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityMeiIcicle extends EntityMWThrowable {
	
	public EntityMeiIcicle(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityMeiIcicle(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 999999;//40;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		/*if (result.entityHit instanceof EntityLivingBase && this.getThrower() instanceof EntityPlayer && 
				result.entityHit != this.getThrower() && ((EntityLivingBase)result.entityHit).getHealth() > 0) {
			if (this.getDataManager().get(HEAL)) {
				if (!this.world.isRemote) {
					((EntityLivingBase)result.entityHit).heal(75*ItemMWWeapon.damageScale);
					((WorldServer)result.entityHit.world).spawnParticle(EnumParticleTypes.HEART, 
							result.entityHit.posX+0.5d, result.entityHit.posY+0.5d,result.entityHit.posZ+0.5d, 
							10, 0.4d, 0.4d, 0.4d, 0d, new int[0]);
				}
				else 
					this.getThrower().playSound(ModSoundEvents.anaHeal, 0.3f, result.entityHit.world.rand.nextFloat()/2+1.5f);
			}
			else {
				if (!this.world.isRemote)
					((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 60F*ItemMWWeapon.damageScale);
				else
					this.getThrower().playSound(ModSoundEvents.hurt, 0.3f, result.entityHit.world.rand.nextFloat()/2+0.75f);
			}
			this.setDead();
		}*/
	}
}
