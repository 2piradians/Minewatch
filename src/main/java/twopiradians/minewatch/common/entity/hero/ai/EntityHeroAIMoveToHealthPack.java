package twopiradians.minewatch.common.entity.hero.ai;

import java.util.HashSet;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.tileentity.TileEntityHealthPack;

public class EntityHeroAIMoveToHealthPack extends EntityAIBase {

	private EntityHero entity;
	private BlockPos healthPack;

	public EntityHeroAIMoveToHealthPack(EntityHero entity) {
		this.entity = entity;
		this.setMutexBits(4);
	}

	@Override
	public void startExecuting() {
		entity.movingToHealthPack = true;
		entity.moveStrafing = 0;
	}

	@Override
	public void resetTask() {
		entity.movingToHealthPack = false;
		entity.onPack = false;
		this.healthPack = null;
		if (entity.getNavigator().noPath()) {
			entity.moveForward = 0;
			entity.moveStrafing = 0;
		}
	}

	@Override
	public boolean shouldExecute() {
		boolean needsHealth = entity.getHealth() < entity.getMaxHealth()/2f || 
				(entity.getHealth() < entity.getMaxHealth() && entity.getAttackTarget() == null &&
				entity.healTarget == null);
		// find health pack
		if (needsHealth && this.healthPack == null && entity.ticksExisted % 50 == 0) {
			HashSet<BlockPos> posToRemove = new HashSet<BlockPos>();
			for (BlockPos pos : TileEntityHealthPack.healthPackPositions)
				if (pos == null || !(entity.worldObj.getTileEntity(pos) instanceof TileEntityHealthPack))
					posToRemove.add(pos);
				else if ((this.healthPack == null || entity.getDistanceSq(pos) < 50) && 
						(((TileEntityHealthPack)entity.worldObj.getTileEntity(pos)).getCooldown() <= 100))
					this.healthPack = pos;
			TileEntityHealthPack.healthPackPositions.removeAll(posToRemove);
		}
		return needsHealth && this.healthPack != null && (entity.movingToHealthPack || 
				entity.getNavigator().tryMoveToXYZ(this.healthPack.getX(), this.healthPack.up().getY(), this.healthPack.getZ(), 1.1d));
	}

	@Override
	public boolean continueExecuting() {
		// stop moving while on pack
		if (this.shouldExecute() && entity.getPosition().equals(this.healthPack) && 
				entity.worldObj.getTileEntity(this.healthPack) instanceof TileEntityHealthPack && 
				(((TileEntityHealthPack)entity.worldObj.getTileEntity(healthPack)).getCooldown() <= 100)) {
			entity.moveForward = 0;
			entity.moveStrafing = 0;
			entity.getNavigator().clearPathEntity();
			entity.onPack = true;
			return true;
		}
		entity.onPack = false;
		return healthPack != null && !entity.getNavigator().noPath() && this.shouldExecute() && 
				entity.worldObj.getTileEntity(this.healthPack) instanceof TileEntityHealthPack;
	}

}
