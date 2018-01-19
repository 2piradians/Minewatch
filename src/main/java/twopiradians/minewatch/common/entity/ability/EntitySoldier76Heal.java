package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntitySoldier76Heal extends EntityMW {

	public EntitySoldier76Heal(World worldIn) {
		this(worldIn, null);
	}

	public EntitySoldier76Heal(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn, -1);
		this.setSize(0.2f, 0.25f);
		this.lifetime = 1200;
		this.impactOnClient = true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return distance < 2000;
	}

	@Override
	public void onUpdate() {
		// motion
		this.motionX = 0;
		this.motionZ = 0;
		this.motionY = Math.max(-2, motionY-0.5d);

		// effect
		if (!world.isRemote && this.ticksExisted % 2 == 0 && this.onGround)
			for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().expandXyz(5)))
				if (entity.isEntityAlive() && this.getDistanceToEntity(entity) <= 5) 
					EntityHelper.attemptDamage(getThrower(), entity, -4.08f, true);

		boolean firstOnGround = this.onGround;
		super.onUpdate();
		firstOnGround = this.onGround && !firstOnGround;
		
		// onGround lifetime
		if (firstOnGround)
			this.lifetime = this.ticksExisted + 100;
		
		// particles
		if (this.world.isRemote && onGround) {
			if (firstOnGround) {
				ModSoundEvents.SOLDIER76_HEAL_PASSIVE.playSound(this, 0.7f, 1);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.BEAM, world, posX, posY+3.1d, posZ, 0, 0, 0, 0xFDFA75, 0xFDFA75, 0.5f, 110, 30, 30, 0, 0, EnumFacing.WEST, false);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.BEAM, world, posX, posY+3.1d, posZ, 0, 0, 0, 0xFDFA75, 0xFDFA75, 0.5f, 110, 30, 30, 0, 0, EnumFacing.NORTH, false);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_2, world, posX, posY+0.01d, posZ, 0, 0, 0, 0xFDFA75, 0xFDFA75, 0.5f, 110, 54, 54, 0, 0, EnumFacing.UP, true);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_3, world, posX, posY+0.01d, posZ, 0, 0, 0, 0xFDFA75, 0xFDFA75, 1f, 120, 54, 54, 0, 0, EnumFacing.UP, true);
			}
			if ((this.lifetime-this.ticksExisted) % 22 == 0 && this.ticksExisted+40 < this.lifetime)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_3, world, posX, posY+0.01d, posZ, 0, 0, 0, 0xFDFA75, 0xFDFA75, 0.8f, 50, 50, 10, 0, 0, EnumFacing.UP, true);
			if (this.ticksExisted % 2 == 0)
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.HEALTH_PLUS, world, 
					posX+(world.rand.nextFloat()-0.5f)*8f, posY+world.rand.nextFloat()*0.5f, posZ+(world.rand.nextFloat()-0.5f)*8f, 0, world.rand.nextFloat()*0.2f, 0, 0xFDFA75, 0xFDFA75, 0.9f, 20, 1.2f, 0.1f, 0, 0);
			if (world.rand.nextInt(2) == 0)
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, 
					posX+(world.rand.nextFloat()-0.5f)*8f, posY+world.rand.nextFloat()*0.5f, posZ+(world.rand.nextFloat()-0.5f)*8f, (world.rand.nextFloat()-0.5f)*0.1f, world.rand.nextFloat()*0.1f, (world.rand.nextFloat()-0.5f)*0.1f, 0xFDFA75, 0xFDFA75, 0.9f, 30, world.rand.nextFloat()*0.8f+0.1f, 0.1f, 0, 0);
		}
	}

	@Override
	public void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK)
			this.onGround = true;
	}

	@Override
	protected boolean isValidImpact(RayTraceResult result, boolean nearest) {
		return result.typeOfHit == RayTraceResult.Type.BLOCK && result.sideHit == EnumFacing.DOWN;
	}

}