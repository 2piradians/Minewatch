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

		if (this.world.isRemote && this.ticksExisted > 1) {
			int numParticles = (int) ((Math.abs(motionX)+Math.abs(motionY)+Math.abs(motionZ))*30d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(this.world, 
						this.posX+(this.prevPosX-this.posX)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posY+(this.prevPosY-this.posY)*i/numParticles+world.rand.nextDouble()*0.05d, 
						this.posZ+(this.prevPosZ-this.posZ)*i/numParticles+world.rand.nextDouble()*0.05d, 
						0x5EDCE5, 0x007acc, 1, 4);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (this.getThrower() != null && result.entityHit != this.getThrower() && 
				!(result.entityHit instanceof EntitySoldier76HelixRocket)) {
			// direct hit damage (explosions do plenty of damage - direct can't be much)
			if (result.entityHit instanceof EntityLivingBase && !this.world.isRemote) {
				((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 5f/ItemMWWeapon.DAMAGE_SCALE);
				((EntityLivingBase)result.entityHit).hurtResistantTime = 10;
			}

			// explosion
			Explosion explosion = new Explosion(world, this.getThrower(), posX, posY, posZ, 1.5f, false, true);
			explosion.doExplosionA();
			explosion.clearAffectedBlockPositions();
			explosion.doExplosionB(true);
			if (this.world.isRemote)
				for (int i=0; i<1; ++i)
					Minewatch.proxy.spawnParticlesSmoke(world, posX, posY, posZ, 0x62E2FC, 0x203B7E, 25, 10);
		}
	}
}
