package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class EntitySoldier76HelixRocket extends EntityMWThrowable {

	private static final DataParameter<Integer> NUMBER = EntityDataManager.<Integer>createKey(EntitySoldier76HelixRocket.class, DataSerializers.VARINT);

	public EntitySoldier76HelixRocket(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntitySoldier76HelixRocket(World worldIn, EntityLivingBase throwerIn, int number) {
		super(worldIn, throwerIn);
		this.getDataManager().set(NUMBER, number);
		this.setNoGravity(true);
		this.lifetime = 60;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(NUMBER, Integer.valueOf(0));
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		int num = this.getDataManager().get(NUMBER);
		double speed = 100d;
		double size = 0.3d;

		double separateThree = (num - 2) * 360 / 3;
		double toRadians = Math.PI / 180;
		double ticks = this.ticksExisted * speed;
		double yaw = this.rotationYaw;
		double pitch = this.rotationPitch;

		if (this.ticksExisted == 1) {
			if (Math.abs(pitch) >= 30 && Math.abs(pitch) <= 70) {
				this.posX += size * (Math.sin(Math.abs(pitch) * toRadians + Math.PI/4) * Math.cos((yaw + separateThree) * toRadians));                
				this.posY += size * Math.cos((pitch + separateThree) * toRadians) * Math.cos(pitch * toRadians);
				this.posZ += size * (Math.sin(Math.abs(pitch) * toRadians + Math.PI/4) * Math.sin((yaw + separateThree) * toRadians));
			}
			else {
				this.posX += size * (Math.sin(pitch * toRadians) * Math.sin((yaw + separateThree) * toRadians) + Math.cos(pitch * toRadians) * Math.cos(yaw * toRadians) * Math.signum(num - 2));                
				this.posY += size * Math.cos((pitch + separateThree) * toRadians) * Math.cos(pitch * toRadians);
				this.posZ += size * (Math.sin(pitch * toRadians) * Math.cos((yaw + separateThree) * toRadians) - Math.cos(pitch * toRadians) * Math.sin(yaw * toRadians) * Math.signum(num - 2));
			}
		}

		this.posX += this.motionX + Math.cos((yaw + separateThree + ticks) * toRadians) * Math.cos((pitch + separateThree + ticks) * toRadians) * size - Math.cos(yaw * toRadians) * size/2;                
		this.posY += this.motionY + Math.sin((yaw + separateThree + this.ticksExisted * speed) * Math.PI / 180) * size;
		this.posZ += this.motionZ + Math.cos((yaw + separateThree + ticks) * toRadians) * Math.sin((pitch + separateThree + ticks) * toRadians) * size + Math.sin(yaw * toRadians) * size/2;

		if (this.worldObj.isRemote) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*30d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.worldObj, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posY+(this.prevPosY-this.posY)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+worldObj.rand.nextDouble()*0.05d, 
						0, 0, 0, 0x5EDCE5, 0x007acc, 1, 4, 1);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (this.getThrower() != null && result.entityHit != this.getThrower() && 
				!(result.entityHit instanceof EntitySoldier76HelixRocket) && 
				(!(result.entityHit instanceof EntityLivingBase) || ((EntityLivingBase)result.entityHit).getHealth() > 0)) {
			// direct hit damage (explosions do plenty of damage - direct can't be much)
			if (result.entityHit instanceof EntityLivingBase) {
				if (!this.worldObj.isRemote) {
					((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 1f*ItemMWWeapon.damageScale);
					((EntityLivingBase)result.entityHit).hurtResistantTime = 10;
				}
				else
					this.getThrower().playSound(ModSoundEvents.hurt, 0.3f, result.entityHit.worldObj.rand.nextFloat()/2+0.75f);
			}

			// explosion
			Explosion explosion = new Explosion(worldObj, this.getThrower(), posX, posY, posZ, 1.8f, false, true);
			explosion.doExplosionA();
			explosion.clearAffectedBlockPositions();
			explosion.doExplosionB(true);
			if (this.worldObj.isRemote)
				Minewatch.proxy.spawnParticlesSmoke(worldObj, posX, posY, posZ, 0x62E2FC, 0x203B7E, 25, 10);
			this.setDead();
		}
	}
}
