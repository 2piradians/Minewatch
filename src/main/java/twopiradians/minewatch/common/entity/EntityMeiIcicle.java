package twopiradians.minewatch.common.entity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityMeiIcicle extends EntityMW {

	private int xTile;
	private int yTile;
	private int zTile;
	private Block inTile;
	private int inData;
	private boolean inGround;

	public EntityMeiIcicle(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityMeiIcicle(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 40;
		this.xTile = -1;
		this.yTile = -1;
		this.zTile = -1;
	}
	
	@Override
	public void spawnMuzzleParticles(EnumHand hand, EntityLivingBase shooter) {
		Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.SPARK, world, (EntityLivingBase) getThrower(), 
				0x2B9191, 0x2B9191, 0.7f, 3, 3, 2.5f, world.rand.nextFloat(), 0.01f, hand, 10, 0.55f);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.world.isRemote && !this.inGround) 
			EntityHelper.spawnTrailParticles(this, 10, 0, 0x5EDCE5, 0x007acc, 0.6f, 5, 0.1f);

		// in ground

		if (this.inGround && this.lifetime == 40) 
			this.lifetime = 1240;

		BlockPos blockpos = new BlockPos(this.xTile, this.yTile, this.zTile);
		IBlockState iblockstate = this.world.getBlockState(blockpos);
		Block block = iblockstate.getBlock();

		if (iblockstate.getMaterial() != Material.AIR) {
			AxisAlignedBB axisalignedbb = iblockstate.getCollisionBoundingBox(this.world, blockpos);

			if (axisalignedbb != Block.NULL_AABB && axisalignedbb.offset(blockpos).isVecInside(new Vec3d(this.posX, this.posY, this.posZ)))
				this.inGround = true;
		}
		if (this.inGround && (block != this.inTile || block.getMetaFromState(iblockstate) != this.inData) && 
				!this.world.collidesWithAnyBlock(this.getEntityBoundingBox().expandXyz(0.05D))) {
			this.setDead();
		}

	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos blockpos = result.getBlockPos();
			this.xTile = blockpos.getX();
			this.yTile = blockpos.getY();
			this.zTile = blockpos.getZ();
			IBlockState iblockstate = this.world.getBlockState(blockpos);
			this.inTile = iblockstate.getBlock();
			this.inData = this.inTile.getMetaFromState(iblockstate);
			this.inGround = true;
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
			// push out of block and set in position
			boolean setPos = false;
			for (Vec3d vec : new Vec3d[] {result.hitVec, result.hitVec.addVector(-this.width/2, 0, 0), 
					result.hitVec.addVector(0, -this.width/2, 0), result.hitVec.addVector(0, 0, -this.width/2)}) {
				BlockPos pos = new BlockPos(vec);
				IBlockState state = this.world.getBlockState(pos);
				if (state.getBlock().isPassable(this.world, pos) || state.getMaterial() == Material.AIR) {
					this.setPosition(vec.xCoord, vec.yCoord, vec.zCoord);
					setPos = true;
				}
			}
			if (!setPos)
				this.setPosition(result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord);

			if (iblockstate.getMaterial() != Material.AIR)
				this.inTile.onEntityCollidedWithBlock(this.world, blockpos, iblockstate, this);
		}
		else {
			if (EntityHelper.shouldHit(getThrower(), result.entityHit, false)) {
				EntityHelper.moveToEntityHit(this, result.entityHit);
				double distance = this.getPositionVector().distanceTo(new Vec3d(prevPosX, prevPosY, prevPosZ));
				EntityHelper.attemptImpact(this, result.entityHit, (float) (75-(75-22) * MathHelper.clamp((distance-26) / (55-26), 0, 1)), false); 
			}
		}
	}
}
