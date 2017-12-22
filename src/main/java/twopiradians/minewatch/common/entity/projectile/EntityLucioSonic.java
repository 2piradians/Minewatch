package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityLucioSonic extends EntityMW {

	public EntityLucioSonic(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityLucioSonic(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.3f, 0.3f);
		this.lifetime = 60;
		this.setNoGravity(true);
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.CIRCLE, world, shooter, 
				0x8CAB46, 0x819765, 0.8f, 3, 3, 6, 0, 0, hand, 14, 0.65f);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return distance < 3000;
	}

	@Override
	public void onUpdate() {
		// initial particle spawn / sound start
		if (this.world.isRemote) {
			if (this.firstUpdate)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, this, 0x7EA950, 0x7EA950, 0.9f, Integer.MAX_VALUE, 2.5f, 2.5f, 0, 1);
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE, world, this.posX, this.posY+height/2f, this.posZ, motionX/10f, motionY/10f, motionZ/10f, 0xABBF85, 0xD3EAA4, 0.9f, 10, 2, 0.5f, world.rand.nextFloat(), world.rand.nextFloat()/3f);
		}

		super.onUpdate(); 
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		EntityHelper.attemptDamage(getThrower(), result.entityHit, 20, false);
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {}

}