package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.Handlers;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityAnaSleepDart extends EntityMWThrowable {


	public EntityAnaSleepDart(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityAnaSleepDart(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 40;
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.world.isRemote && (this.ticksExisted > 1 || !(this.getThrower() instanceof EntityPlayer) || 
				!Minewatch.keys.rmb((EntityPlayer) this.getThrower()))) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*30d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.world, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posY+(this.prevPosY-this.posY)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+world.rand.nextDouble()*0.05d, 
						0, 0, 0, 0x6FE8E6, 0xECFDFE, 0.5f, 8, 1);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (result.entityHit instanceof EntityLivingBase && this.getThrower() instanceof EntityPlayer && 
				result.entityHit != this.getThrower() && ((EntityLivingBase)result.entityHit).getHealth() > 0) {
			if (!this.world.isRemote && this.getThrower() instanceof EntityPlayerMP &&
					((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causeIndirectDamage(this, this.getThrower()), 5F*ItemMWWeapon.damageScale)) {
				((EntityPlayerMP)this.getThrower()).connection.sendPacket((new SPacketSoundEffect
						(ModSoundEvents.hurt, SoundCategory.PLAYERS, this.getThrower().posX, this.getThrower().posY, 
								this.getThrower().posZ, 0.3f, this.world.rand.nextFloat()/2+0.75f)));
				if (!(result.entityHit instanceof EntityPlayer))
					result.entityHit.setRotationYawHead(0);
				result.entityHit.rotationPitch = 0;
				TickHandler.interrupt(result.entityHit);//TODO switch all entities to impact on server and clean up (methods)
				TickHandler.register(this.world.isRemote, ItemAnaRifle.SLEEP.setEntity(result.entityHit).setTicks(120),
						Handlers.PREVENT_INPUT.setEntity(result.entityHit).setTicks(120),
						Handlers.PREVENT_MOVEMENT.setEntity(result.entityHit).setTicks(120),
						Handlers.PREVENT_ROTATION.setEntity(result.entityHit).setTicks(120));
				Minewatch.network.sendTo(new SPacketSimple(12, result.entityHit, false), (EntityPlayerMP) this.getThrower());
				Minewatch.proxy.playFollowingSound(result.entityHit, ModSoundEvents.anaSleepHit, SoundCategory.PLAYERS, 1.0f, 1.0f);
				Minewatch.proxy.playFollowingSound(this.getThrower(), ModSoundEvents.anaSleepVoice, SoundCategory.PLAYERS, 0.5f, 1.0f);
			}
			this.setDead();
		}
	}
}
