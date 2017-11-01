package twopiradians.minewatch.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityReinhardtStrike extends EntityMW {

	public EntityReinhardtStrike(World worldIn) {
		this(worldIn, null);
	}

	public EntityReinhardtStrike(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn, -1);
		this.setSize(1.5f, 2f);
		this.lifetime = Integer.MAX_VALUE;
		this.ignoreMoveToEntity = true;
		this.setNoGravity(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return distance < 5000;
	}

	@Override
	public void onUpdate() {
		if (world.isRemote) {
			for (int i=0; i<3; ++i) {
				world.spawnParticle(EnumParticleTypes.FLAME, 
						posX+(world.rand.nextDouble()-0.5f)*1.5d, 
						posY+height/2d+(world.rand.nextDouble()-0.5f)*1.5d, 
						posZ+(world.rand.nextDouble()-0.5f)*1.5d, 
						0, 0, 0, new int[0]);
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, 
						posX+(world.rand.nextDouble()-0.5f)*1.5d, 
						posY+height/2d+(world.rand.nextDouble()-0.5f)*1.5d, 
						posZ+(world.rand.nextDouble()-0.5f)*1.5d, 
						0, 0, 0, new int[0]);
			}
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, prevPosX, prevPosY+height/2d, prevPosZ, 0, 0, 0,
					0xFFFFF5, 0xF2DEA2, 0.2f, 10, 14, 12, 0, 0);
		}
		
		super.onUpdate();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float partialTicks) {
		return 15728880;
    }

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);
		
		EntityHelper.attemptDamage(getThrower(), result.entityHit, 100, true);
	}
	
	@Override
	public void applyEntityCollision(Entity entityIn) {}
	
	@Override
	protected boolean isValidImpact(RayTraceResult result) {
		return super.isValidImpact(result) && 
				!(result.entityHit != null && result.entityHit.hurtResistantTime > 0);
	}

}