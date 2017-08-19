package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class EntityMeiBlast extends EntityMWThrowable {

	public EntityMeiBlast(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityMeiBlast(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 5;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.worldObj.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*2d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesMeiBlaster(this.worldObj, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+(worldObj.rand.nextDouble()-0.5d)*0.05d, 
						this.posY+this.height/2+(this.prevPosY-this.posY)*i/numParticles+(worldObj.rand.nextDouble()-0.5d)*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+(worldObj.rand.nextDouble()-0.5d)*0.05d,
						motionX/10d, motionY/10d, motionZ/10d, 0.8f, 3, 2.5f, 2f);
			if (this.worldObj.rand.nextInt(5) == 0)
				Minewatch.proxy.spawnParticlesTrail(this.worldObj, 
						this.posX+(this.prevPosX-this.posX)*worldObj.rand.nextDouble()*0.8d, 
						this.posY+(this.prevPosY-this.posY)*worldObj.rand.nextDouble()*0.8d, 
						this.posZ+(this.prevPosZ-this.posZ)*worldObj.rand.nextDouble()*0.8d,
						motionX, motionY, motionZ, 0xAED4FF, 0x007acc, 0.3f, 5);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (result.entityHit instanceof EntityLivingBase && this.getThrower() != null &&
				result.entityHit != this.getThrower()) {
			if (this.worldObj.isRemote && 
					(((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen) == null || 
					((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen).getDuration() == 0)) {
				int freezeCount = ModPotions.frozen.clientFreezes.containsKey(result.entityHit) ? ModPotions.frozen.clientFreezes.get(result.entityHit)+1 : 1;
				ModPotions.frozen.clientFreezes.put((EntityLivingBase) result.entityHit, Math.min(freezeCount, 30)); 
				ModPotions.frozen.clientDelays.put((EntityLivingBase) result.entityHit, 10);
			}
			if (!this.worldObj.isRemote) {
				if ((((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen) == null || 
						((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen).getDuration() == 0)) {
					int freezeCount = ModPotions.frozen.serverFreezes.containsKey(result.entityHit) ? ModPotions.frozen.serverFreezes.get(result.entityHit)+1 : 1;
					ModPotions.frozen.serverFreezes.put((EntityLivingBase) result.entityHit, Math.min(freezeCount, 30)); 
					ModPotions.frozen.serverDelays.put((EntityLivingBase) result.entityHit, 10);
				}
				double prev = ((EntityLivingBase) result.entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue();
				((EntityLivingBase) result.entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
				((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 2.25f/ItemMWWeapon.DAMAGE_SCALE);
				((EntityLivingBase) result.entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(prev);
				((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
				result.entityHit.worldObj.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
						ModSoundEvents.hurt, SoundCategory.PLAYERS, 0.3f, result.entityHit.worldObj.rand.nextFloat()/2+0.75f);
				this.setDead();
			}
		}
	}

}
