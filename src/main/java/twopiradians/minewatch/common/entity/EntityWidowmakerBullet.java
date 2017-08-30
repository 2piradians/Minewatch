package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class EntityWidowmakerBullet extends EntityMWThrowable {

	private static final DataParameter<Boolean> SCOPED = EntityDataManager.<Boolean>createKey(EntityWidowmakerBullet.class, DataSerializers.BOOLEAN);
	private int damage;

	public EntityWidowmakerBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityWidowmakerBullet(World worldIn, EntityLivingBase throwerIn, boolean scoped, int damage) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.lifetime = 20;
		this.damage = damage;
		this.getDataManager().set(SCOPED, scoped);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(SCOPED, false);
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		if (this.getDataManager().get(SCOPED)) {
			if (this.world.isRemote && this.ticksExisted > 1) {
				int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*30d);
				for (int i=0; i<numParticles; ++i)
					Minewatch.proxy.spawnParticlesTrail(this.world, 
							this.posX+(this.prevPosX-this.posX)*i/numParticles+world.rand.nextDouble()*0.05d, 
							this.posY+(this.prevPosY-this.posY)*i/numParticles+world.rand.nextDouble()*0.05d, 
							this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+world.rand.nextDouble()*0.05d, 
							0, 0, 0, 0xFF0000, 0xB2B2B2, 0.5f, 15, 1);
			}
		}
		else {
			if (this.world.isRemote) {
				int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*10d);
				for (int i=0; i<numParticles; ++i)
					Minewatch.proxy.spawnParticlesTrail(this.world, 
							this.posX+(this.prevPosX-this.posX)*i/numParticles, 
							this.posY+this.height/2+(this.prevPosY-this.posY)*i/numParticles, 
							this.posZ+(this.prevPosZ-this.posZ)*i/numParticles, 
							0, 0, 0, 0xFF0000, 0xFF0000, 0.5f, 1, 0.01f);
			}
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (result.entityHit instanceof EntityLivingBase && this.getThrower() instanceof EntityPlayer && 
				result.entityHit != this.getThrower() && ((EntityLivingBase)result.entityHit).getHealth() > 0) {
			if (!this.world.isRemote) {
				((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), damage*ItemMWWeapon.damageScale);
				if (!this.dataManager.get(SCOPED))
					((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
			}
			else
				this.getThrower().playSound(ModSoundEvents.hurt, 0.3f, result.entityHit.world.rand.nextFloat()/2+0.75f);
			this.setDead();
		}
	}
}
