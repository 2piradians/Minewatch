package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.potion.PotionFrozen;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

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

		if (this.world.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*2d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesCircle(this.world, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+(world.rand.nextDouble()-0.5d)*0.05d, 
						this.posY+this.height/2+(this.prevPosY-this.posY)*i/numParticles+(world.rand.nextDouble()-0.5d)*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+(world.rand.nextDouble()-0.5d)*0.05d,
						motionX/10d, motionY/10d, motionZ/10d, 0x5BC8E0, 0xAED4FF, 0.8f, 3, 2.5f, 2f);
			if (this.world.rand.nextInt(5) == 0)
				Minewatch.proxy.spawnParticlesTrail(this.world, 
						this.posX+(this.prevPosX-this.posX)*world.rand.nextDouble()*0.8d, 
						this.posY+(this.prevPosY-this.posY)*world.rand.nextDouble()*0.8d, 
						this.posZ+(this.prevPosZ-this.posZ)*world.rand.nextDouble()*0.8d,
						motionX, motionY, motionZ, 0xAED4FF, 0x007acc, 0.3f, 5, 1);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (result.entityHit instanceof EntityLivingBase && this.getThrower() != null &&
				result.entityHit != this.getThrower() && ((EntityLivingBase)result.entityHit).getHealth() > 0) {
			if (this.world.isRemote && 
					(((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen) == null || 
					((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen).getDuration() == 0)) {
				Handler handler = TickHandler.getHandler(result.entityHit, Identifier.POTION_FROZEN);
				if (handler != null) 
					handler.ticksLeft = Math.min(handler.ticksLeft+1, 30);
				else
					TickHandler.register(true, PotionFrozen.FROZEN_CLIENT.setEntity(result.entityHit).setTicks(1));
				TickHandler.register(true, PotionFrozen.DELAYS.setEntity(result.entityHit).setTicks(10));
			}
			if (!this.world.isRemote && 
					!(result.entityHit instanceof EntityPlayer && ((EntityPlayer)result.entityHit).isCreative())) {
				if ((((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen) == null || 
						((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen).getDuration() == 0)) {
					Handler handler = TickHandler.getHandler(result.entityHit, Identifier.POTION_FROZEN);
					if (handler != null) 
						handler.ticksLeft = Math.min(handler.ticksLeft+1, 30);
					else
						TickHandler.register(false, PotionFrozen.FROZEN_SERVER.setEntity(result.entityHit).setTicks(1));
					TickHandler.register(false, PotionFrozen.DELAYS.setEntity(result.entityHit).setTicks(10));
				}
				double prev = ((EntityLivingBase) result.entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue();
				((EntityLivingBase) result.entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
				((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 2.25f*ItemMWWeapon.damageScale);
				((EntityLivingBase) result.entityHit).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(prev);
				((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
			}
			else
				this.getThrower().playSound(ModSoundEvents.hurt, 0.3f, result.entityHit.world.rand.nextFloat()/2+0.75f);
			this.setDead();
		}
	}

}
