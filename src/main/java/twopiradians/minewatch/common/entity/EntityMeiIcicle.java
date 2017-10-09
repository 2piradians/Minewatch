package twopiradians.minewatch.common.entity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSyncSpawningEntity;

public class EntityMeiIcicle extends EntityMW {

	private int xTile;
	private int yTile;
	private int zTile;
	private Block inTile;
	private int inData;
	private boolean inGround;

	public EntityMeiIcicle(World worldIn) {
		this(worldIn, null);
	}

	public EntityMeiIcicle(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.1f, 0.1f);
		this.setNoGravity(true);
		this.lifetime = 40;
		this.xTile = -1;
		this.yTile = -1;
		this.zTile = -1;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.world.isRemote && !this.inGround) 
			EntityHelper.spawnTrailParticles(this, 10, 0, 0x5EDCE5, 0x007acc, 0.6f, 5, 0.1f);

		// in ground

		if (this.inGround && this.lifetime == 40) {
			this.lifetime = 1240;
			if (this.world.isRemote && this.getPersistentID().equals(ModEntities.spawningEntityUUID)) 
				EntityHelper.updateFromPacket(this);
		}

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

			if (!this.world.isRemote && this.world instanceof WorldServer) {
				Minewatch.network.sendToAll(new SPacketSyncSpawningEntity(this.getPersistentID(), 
						this.rotationPitch, this.rotationYaw, this.motionX, this.motionY, this.motionZ, 
						this.posX, this.posY, this.posZ));
			}
		}
		else
			EntityHelper.attemptImpact(this, result.entityHit, 75 - (75 - 22) * ((float)this.ticksExisted / lifetime), false);
	}
}
