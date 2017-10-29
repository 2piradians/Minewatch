package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityJunkratMine extends EntityLivingBaseMW {

	public EnumFacing facing;
	private boolean prevOnGround;
	// TODO test with genji deflect
	public int deflectTimer = -1;

	public EntityJunkratMine(World worldIn) {
		this(worldIn, null);
	}

	public EntityJunkratMine(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.5f, 0.5f);
		this.lifetime = Integer.MAX_VALUE;
		this.ignoreImpacts.add(RayTraceResult.Type.ENTITY);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Math.max(1, 1D*Config.damageScale));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return distance < 2000;
	}

	@Override
	public void onUpdate() {		
		// set rotation on ground
		if (this.onGround) {
			this.rotationPitch = 0;
			this.rotationYaw = 0;
		}

		// prevOnGround and normal particle
		if (prevOnGround != onGround && onGround) 
			this.world.playSound(null, this.getPosition(), ModSoundEvents.junkratTrapLand, SoundCategory.PLAYERS, 1.0f, 1.0f);
		this.prevOnGround = this.onGround;

		// check if not attached
		if (this.onGround && this.facing != null && !world.collidesWithAnyBlock(getEntityBoundingBox().expandXyz(0.01d))) 
			this.onGround = false;
		else if (!this.onGround)
			this.motionY -= 0.03D;

		// explode automatically if deflected
		if (--this.deflectTimer == 0)
			this.explode();

		super.onUpdate();
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			this.onGround = true;
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
			this.facing = result.sideHit.getOpposite();
			this.setPosition(result.hitVec.xCoord, result.hitVec.yCoord-(result.sideHit == EnumFacing.DOWN ? this.height : 0), result.hitVec.zCoord);
		}
		else if (result.typeOfHit == RayTraceResult.Type.ENTITY && this.deflectTimer >= 0 &&
				EntityHelper.shouldHit(getThrower(), result.entityHit, false) && !world.isRemote)
			this.explode();
	}

	/**Only call directly on server - sends packet to client*/
	public void explode() {
		if (this.world.isRemote) {
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.EXPLOSION, world, 
					this.posX, this.posY+height/2d, this.posZ, 0, 0, 0, 
					0xFFFFFF, 0xFFFFFF, 1, 35+world.rand.nextInt(10), 40, 40, 0, 0);
			this.world.playSound(this.posX, this.posY, this.posZ, ModSoundEvents.junkratGrenadeExplode, 
					SoundCategory.PLAYERS, 1.0f, 1.0f, false);
		}
		else {
			Minewatch.proxy.createExplosion(world, getThrower(), posX, posY, posZ, 2f, 0, 120, 120, null, 120, false, 2.2f, 2.2f);
			Minewatch.network.sendToDimension(new SPacketSimple(30, this, false), world.provider.getDimension());
			this.setDead();
		}
	}

}