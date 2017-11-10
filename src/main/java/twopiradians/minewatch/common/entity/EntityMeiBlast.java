package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityMeiBlast extends EntityMW {

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
				entityLiving.addPotionEffect(new PotionEffect(ModPotions.frozen, 60, 0, false, true));
				TickHandler.interrupt(entityLiving);
				TickHandler.register(false, Handlers.PREVENT_INPUT.setEntity(entityLiving).setTicks(60),
						Handlers.PREVENT_MOVEMENT.setEntity(entityLiving).setTicks(60),
						Handlers.PREVENT_ROTATION.setEntity(entityLiving).setTicks(60));
				Minewatch.network.sendToAll(new SPacketSimple(9, entityLiving, true, 60, 0, 0));
				entityLiving.world.playSound(null, entityLiving.getPosition(), ModSoundEvents.meiFreeze, SoundCategory.NEUTRAL, 1.0f, 1.0f);
				Minewatch.network.sendToAll(new SPacketSimple(23, entityLiving, false, entityLiving.posX, entityLiving.posY+entityLiving.height/2, entityLiving.posZ));
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
		this(worldIn, null, -1);
	}

	public EntityMeiBlast(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.notDeflectible = true;
		this.setNoGravity(true);
		this.lifetime = 8;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*2d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, this.world, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+(world.rand.nextDouble()-0.5d)*0.05d, 
						this.posY+this.height/2+(this.prevPosY-this.posY)*i/numParticles+(world.rand.nextDouble()-0.5d)*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+(world.rand.nextDouble()-0.5d)*0.05d,
						motionX/10d, motionY/10d, motionZ/10d, 0x5BC8E0, 0xAED4FF, 0.8f, 3, 2.5f, 2f, 0, 0);
			if (this.world.rand.nextInt(5) == 0)
				EntityHelper.spawnTrailParticles(this, 1, 0.8d, motionX, motionY, motionZ, 0xAED4FF, 0x007acc, 0.3f, 5, 1);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (result.entityHit != null) {
			/*if (result.entityHit instanceof EntityDragonPart && ((EntityDragonPart)result.entityHit).entityDragonObj instanceof EntityDragon)
				result.entityHit = (Entity) ((EntityDragonPart)result.entityHit).entityDragonObj;*/
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
			if (EntityHelper.attemptDamage(this, result.entityHit, 2.25f, true)) {
				if ((((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen) == null || 
						((EntityLivingBase) result.entityHit).getActivePotionEffect(ModPotions.frozen).getDuration() == 0)) {
					Handler handler = TickHandler.getHandler(result.entityHit, Identifier.POTION_FROZEN);
					if (handler != null) 
						handler.ticksLeft = Math.min(handler.ticksLeft+1, 30);
					else
						TickHandler.register(false, FROZEN.setEntity(result.entityHit).setTicks(1));
					TickHandler.register(false, DELAYS.setEntity(result.entityHit).setTicks(10));
				}			
				((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
			}
		}
	}

}
