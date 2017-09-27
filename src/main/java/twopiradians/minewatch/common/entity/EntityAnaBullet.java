package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class EntityAnaBullet extends EntityMWThrowable {

	private static final DataParameter<Boolean> HEAL = EntityDataManager.<Boolean>createKey(EntityAnaBullet.class, DataSerializers.BOOLEAN);

	public EntityAnaBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityAnaBullet(World worldIn, EntityLivingBase throwerIn, boolean heal) {
		super(worldIn, throwerIn);
		this.getDataManager().set(HEAL, heal);
		this.setNoGravity(true);
		this.lifetime = 40;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(HEAL, false);
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.worldObj.isRemote && (this.ticksExisted > 1 || !(this.getThrower() instanceof EntityPlayer) || 
				!Minewatch.keys.rmb((EntityPlayer) this.getThrower()))) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*30d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.worldObj, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posY+(this.prevPosY-this.posY)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						0, 0, 0, 0xFFFCC7, 0xEAE7B9, 0.5f, 8, 1);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (this.getDataManager().get(HEAL) && this.attemptImpact(result.entityHit, -75, true) &&
				this.worldObj instanceof WorldServer) {
			((WorldServer)result.entityHit.worldObj).spawnParticle(EnumParticleTypes.HEART, 
					result.entityHit.posX+0.5d, result.entityHit.posY+0.5d,result.entityHit.posZ+0.5d, 
					10, 0.4d, 0.4d, 0.4d, 0d, new int[0]);
			this.worldObj.playSound(null, this.getThrower().getPosition(), ModSoundEvents.anaHeal, SoundCategory.PLAYERS, 
					0.3f, result.entityHit.worldObj.rand.nextFloat()/2+1.5f);
		}
		else if (!this.getDataManager().get(HEAL)) 
			this.attemptImpact(result.entityHit, 60, false);
	}
}
