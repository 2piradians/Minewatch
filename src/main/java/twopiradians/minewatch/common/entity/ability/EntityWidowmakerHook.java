package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityWidowmakerHook extends EntityMW {

	public EnumFacing facing;

	public EntityWidowmakerHook(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityWidowmakerHook(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.25f, 0.25f);
		this.lifetime = 14;
		this.setNoGravity(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float partialTicks) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(MathHelper.floor_double(this.posX), 0, MathHelper.floor_double(this.posZ));

		// offset by facing
		if (this.facing == EnumFacing.SOUTH || this.facing == EnumFacing.EAST)
			pos.move(facing.getOpposite());

		if (this.worldObj.isBlockLoaded(pos)) {
			pos.setY(MathHelper.floor_double(this.posY + (double)this.getEyeHeight()));
			return this.worldObj.getCombinedLight(pos, 0);
		}
		else
			return 0;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	protected boolean isValidImpact(RayTraceResult result, boolean nearest) {
		return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && nearest;
	}

	@Override
	protected void onImpactMoveToHitPosition(RayTraceResult result) {
		if (result != null) 
			if (!worldObj.isRemote && result.getBlockPos() != null) {
				EntityHelper.moveToHitPosition(this, result, false);
				// move to top of block (if not already at top / bottom)
				if (this.posY % 1 != 0)
					this.posY = MathHelper.floor_double(posY) + worldObj.getBlockState(result.getBlockPos()).getBoundingBox(worldObj, result.getBlockPos()).maxY - this.height/2f;
			}
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		this.lifetime = this.ticksExisted + 100;
		this.facing = result.sideHit.getOpposite();

		if (!worldObj.isRemote) {
			ModSoundEvents.WIDOWMAKER_HOOK_THROW.stopSound(worldObj);
			ModSoundEvents.WIDOWMAKER_HOOK_HIT.playFollowingSound(getThrower(), 1, 1, false);
		}

	}

}