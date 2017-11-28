package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityJunkratMine extends EntityLivingBaseMW {

	private static final DataParameter<Integer> FACING = EntityDataManager.<Integer>createKey(EntityJunkratMine.class, DataSerializers.VARINT);
	public EnumFacing facing;
	private boolean prevOnGround;
	public int deflectTimer = -1;

	public EntityJunkratMine(World worldIn) {
		this(worldIn, null);
	}

	public EntityJunkratMine(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.5f, 0.5f);
		this.lifetime = Integer.MAX_VALUE;
		this.ignoreImpacts.add(RayTraceResult.Type.ENTITY);
		this.setNoGravity(true);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Math.max(1, 50D*Config.damageScale));
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);

		// set facing / onGround on client 
		if (key.getId() == FACING.getId()) {
			int facing = this.dataManager.get(FACING);
			if (facing >= 0 && facing < EnumFacing.VALUES.length) {
				this.facing = EnumFacing.values()[facing];
				this.onGround = true;
			}
			else {
				this.facing = null;
				this.onGround = false;
			}
		}
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(FACING, -1);
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
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
		}

		// prevOnGround and normal particle
		if (prevOnGround != onGround && onGround) 
			ModSoundEvents.JUNKRAT_MINE_LAND.playSound(this, 1, 1);
		this.prevOnGround = this.onGround;

		// check if not attached
		if (!this.world.isRemote && this.onGround && 
				this.facing != null && !world.collidesWithAnyBlock(getEntityBoundingBox().expandXyz(0.01d))) {
			this.onGround = false;
			this.facing = null;
			this.dataManager.set(FACING, -1);
		}
		else if (!this.onGround) 
			this.motionY -= 0.03D;

		// explode automatically if deflected
		if (--this.deflectTimer == 0)
			this.explode();

		super.onUpdate();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float partialTicks) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));
		
		// offset by facing
		if (this.facing == EnumFacing.SOUTH || this.facing == EnumFacing.EAST)
			pos.move(facing.getOpposite());

		if (this.world.isBlockLoaded(pos)) {
			pos.setY(MathHelper.floor(this.posY + (double)this.getEyeHeight()));
			return this.world.getCombinedLight(pos, 0);
		}
		else
			return 0;
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			this.onGround = true;
			this.facing = result.sideHit.getOpposite();
			this.setPosition(result.hitVec.xCoord, result.hitVec.yCoord-(result.sideHit == EnumFacing.DOWN ? this.height : 0), result.hitVec.zCoord);
			if (!this.world.isRemote) {
				this.dataManager.set(FACING, this.facing.ordinal());
				Minewatch.network.sendToDimension(new SPacketSimple(34, this, false, this.posX, this.posY, this.posZ), world.provider.getDimension());
			}
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
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
			ModSoundEvents.JUNKRAT_MINE_EXPLODE.playSound(this, 1, 1);
		}
		else {
			Minewatch.proxy.createExplosion(world, getThrower(), posX, posY, posZ, 2f, 0, 120, 120, null, 120, false, 2.2f, 2.2f);
			Minewatch.network.sendToDimension(new SPacketSimple(30, this, false), world.provider.getDimension());
			this.setDead();
		}
	}

}