package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.Particle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.Handlers;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;
import twopiradians.minewatch.packet.SPacketSpawnParticle;

public class EntityMeiBlast extends EntityMWThrowable {

	public static final Handler FROZEN = new Handler(Identifier.POTION_FROZEN, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (TickHandler.getHandler(entityLiving, Identifier.POTION_DELAY) != null)
				return false;

			return entityLiving.isDead ||
					(entityLiving.getActivePotionEffect(ModPotions.frozen) != null && 
					entityLiving.getActivePotionEffect(ModPotions.frozen).getDuration() > 0) || super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (entityLiving == null || entityLiving.isDead || 
					(entityLiving.getActivePotionEffect(ModPotions.frozen) != null && 
					entityLiving.getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) 
				return true;

			int level = this.ticksLeft / 5;
			// apply freeze/slowness effect
			if (this.ticksLeft >= 30) {
				entityLiving.removePotionEffect(MobEffects.SLOWNESS);
				entityLiving.setRevengeTarget(null);
				if (entityLiving instanceof EntityLiving)
					((EntityLiving)entityLiving).setAttackTarget(null);
				entityLiving.addPotionEffect(new PotionEffect(ModPotions.frozen, 60, 0, false, true));
				TickHandler.interrupt(entityLiving);
				TickHandler.register(false, Handlers.PREVENT_INPUT.setEntity(entityLiving).setTicks(60),
						Handlers.PREVENT_MOVEMENT.setEntity(entityLiving).setTicks(60),
						Handlers.PREVENT_ROTATION.setEntity(entityLiving).setTicks(60));
				Minewatch.network.sendToAll(new SPacketSimple(9, entityLiving, true, 60, 0, 0));
				entityLiving.world.playSound(null, entityLiving.getPosition(), ModSoundEvents.meiFreeze, SoundCategory.NEUTRAL, 1.0f, 1.0f);
				Minewatch.network.sendToAll(new SPacketSpawnParticle(2, entityLiving.posX, entityLiving.posY+entityLiving.height/2, entityLiving.posZ, 0, 0, 0, 0));
			}
			else
				entityLiving.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10, level, true, true));

			if (TickHandler.hasHandler(entityLiving, Identifier.POTION_DELAY))
				return false;

			return super.onServerTick();
		}
	};
	public static final Handler DELAYS = new Handler(Identifier.POTION_DELAY, false) {};
	
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
				Minewatch.proxy.spawnParticlesCustom(Particle.CIRCLE, this.world, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+(world.rand.nextDouble()-0.5d)*0.05d, 
						this.posY+this.height/2+(this.prevPosY-this.posY)*i/numParticles+(world.rand.nextDouble()-0.5d)*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+(world.rand.nextDouble()-0.5d)*0.05d,
						motionX/10d, motionY/10d, motionZ/10d, 0x5BC8E0, 0xAED4FF, 0.8f, 3, 2.5f, 2f, 0, 0);
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
					TickHandler.register(true, FROZEN.setEntity(result.entityHit).setTicks(1));
				TickHandler.register(true, DELAYS.setEntity(result.entityHit).setTicks(10));
			}
			if (!this.world.isRemote && 
					!(result.entityHit instanceof EntityPlayer && ((EntityPlayer)result.entityHit).isCreative())) {
				if ((((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen) == null || 
						((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen).getDuration() == 0)) {
					Handler handler = TickHandler.getHandler(result.entityHit, Identifier.POTION_FROZEN);
					if (handler != null) 
						handler.ticksLeft = Math.min(handler.ticksLeft+1, 30);
					else
						TickHandler.register(false, FROZEN.setEntity(result.entityHit).setTicks(1));
					TickHandler.register(false, DELAYS.setEntity(result.entityHit).setTicks(10));
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
