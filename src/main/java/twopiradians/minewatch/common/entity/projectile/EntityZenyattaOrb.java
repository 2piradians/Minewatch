package twopiradians.minewatch.common.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityZenyattaOrb extends EntityMW {

	public EntityZenyattaOrb(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityZenyattaOrb(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.15f, 0.15f);
		this.setNoGravity(true);
		this.lifetime = 40;
	}

	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		EnumHero.ZENYATTA.weapon.reequipAnimation(shooter.getHeldItem(hand));
	}

	@Override
	public void spawnTrailParticles() {
		EntityHelper.spawnTrailParticles(this, 8, 0, 0x86F3FF, 0x929EC8, 1, 4, 1);
		if (this.ticksExisted % 2 == 0)
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, posX, posY+height/2d, posZ, 0, 0, 0, 0x90E3FF, 0x91C3ED, 1f, 7, 2, 2.2f, 0, 0);
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		EntityHelper.attemptDamage(this, result.entityHit, 46, false);

		if (this.world.isRemote)
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, result.entityHit == null ? result.hitVec.xCoord : posX, 
					result.entityHit == null ? result.hitVec.yCoord : posY, 
							result.entityHit == null ? result.hitVec.zCoord : posZ, 
									0, 0, 0, 0x86F3FF, 0xCFDFF9, 0.7f, 10, 5, 4.5f, world.rand.nextFloat(), 0.01f);
	}
}
