package twopiradians.minewatch.common.entity;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityWidowmakerMine extends EntityLivingBaseMW {

	private static final DataParameter<Integer> FACING = EntityDataManager.<Integer>createKey(EntityWidowmakerMine.class, DataSerializers.VARINT);
	public EnumFacing facing;
	private boolean prevOnGround;
	public static final Handler POISONED = new Handler(Identifier.WIDOWMAKER_POISON, false) {
		@SideOnly(Side.CLIENT)
		@Override
		public boolean onClientTick() {
			// particles on entity
			if (this.entity != null && this.entityLiving != null) {
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, entity.worldObj, 
						entity.posX+(entity.worldObj.rand.nextFloat()-0.5f)*entity.width, 
						entity.posY+(entity.worldObj.rand.nextFloat()-0.5f)*entity.height+entity.height/2f, 
						entity.posZ+(entity.worldObj.rand.nextFloat()-0.5f)*entity.width, 
						0, 0.01f, 0, 0xBE8FC5, 0xB589BC, 0.5f, 10, 4, 4, 0, 0);
				if (this.entityLiving == Minewatch.proxy.getClientPlayer() && 
						entity.isGlowing() == this.entityLiving.canEntityBeSeen(entity))
					entity.setGlowing(!entity.isGlowing());
			}
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			// damage
			if (this.ticksLeft % 4 == 0 && this.entityLiving != null && this.entity != null && 
					EntityHelper.attemptDamage(this.entityLiving, this.entity, 3f, true)) 
				this.entity.hurtResistantTime = 0;
			return super.onServerTick();
		}
		@SideOnly(Side.CLIENT)
		@Override
		public Handler onClientRemove() {
			if (this.entity != null && this.entityLiving != null &&
					this.entityLiving == Minecraft.getMinecraft().thePlayer)
				entity.setGlowing(false);
			return super.onClientRemove();
		}
	};

	public EntityWidowmakerMine(World worldIn) {
		this(worldIn, null);
	}

	public EntityWidowmakerMine(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.4f, 0.4f);
		this.lifetime = Integer.MAX_VALUE;
		this.ignoreImpacts.add(RayTraceResult.Type.ENTITY);
		this.setNoGravity(true);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Math.max(1, 1.0D*Config.damageScale));
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
		else
			EntityHelper.spawnTrailParticles(this, 5, 0, 0x873BCF, 0x52308F, 1, 8, 0.4f);

		// prevOnGround and normal particle
		if (prevOnGround != onGround && onGround) {
			this.worldObj.playSound(null, this.getPosition(), ModSoundEvents.widowmakerMineLand, SoundCategory.PLAYERS, 1.0f, 1.0f);
			if (worldObj.isRemote && this.getThrower() instanceof EntityPlayer && 
					this.getThrower().getPersistentID().equals(Minewatch.proxy.getClientUUID()))
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.WIDOWMAKER_MINE, worldObj, this, 0xFFFFFF, 0xFFFFFF, 1, Integer.MAX_VALUE, 1, 1, 0, 0);
		}
		this.prevOnGround = this.onGround;

		// check if not attached
		if (!this.worldObj.isRemote && this.onGround && 
				this.facing != null && !worldObj.collidesWithAnyBlock(getEntityBoundingBox().expandXyz(0.01d))) {
			this.onGround = false;
			this.facing = null;
			this.dataManager.set(FACING, -1);		
		}
		else if (!this.onGround)
			this.motionY -= 0.03D;

		// check for entities
		if (!this.worldObj.isRemote  && this.onGround && this.getThrower() instanceof EntityLivingBase) {
			List<Entity> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expandXyz(3d));
			for (Entity entity : entities) 
				if (!(entity instanceof EntityLivingBaseMW) && entity instanceof EntityLivingBase && 
						EntityHelper.shouldHit(this.getThrower(), entity, false) &&
						this.canEntityBeSeen(entity)) {
					TickHandler.register(false, POISONED.setTicks(100).setEntity(entity).setEntityLiving(getThrower()));
					Minewatch.network.sendToDimension(new SPacketSimple(28, false, null, 
							posX, posY, posZ, entity, getThrower()), worldObj.provider.getDimension());
					worldObj.playSound(null, this.getPosition(), ModSoundEvents.widowmakerMineTrigger, SoundCategory.PLAYERS, 1.0f, 1.0f);
					this.setDead();
				}
		}
		super.onUpdate();
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
	public boolean canEntityBeSeen(Entity entityIn) {
		if (this.facing == null)
			return super.canEntityBeSeen(entityIn);
		else {
			AxisAlignedBB aabb = this.getEntityBoundingBox();
			Vec3d vec = new Vec3d(aabb.minX + (aabb.maxX - aabb.minX) * 0.5D, aabb.minY + (aabb.maxY - aabb.minY) * 0.5D, aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5D);
			return this.worldObj.rayTraceBlocks(vec.add(new Vec3d(facing.getOpposite().getDirectionVec()).scale(0.3d)), new Vec3d(entityIn.posX, entityIn.posY + (double)entityIn.getEyeHeight(), entityIn.posZ), false, true, false) == null;
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			this.onGround = true;
			this.facing = result.sideHit.getOpposite();
			this.setPosition(result.hitVec.xCoord, result.hitVec.yCoord-(result.sideHit == EnumFacing.DOWN ? this.height : 0), result.hitVec.zCoord);
			if (!this.worldObj.isRemote) {
				this.dataManager.set(FACING, this.facing.ordinal());
				Minewatch.network.sendToDimension(new SPacketSimple(34, this, false, this.posX, this.posY, this.posZ), worldObj.provider.getDimension());
			}
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
		}
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);

		if (this.worldObj.isRemote && this.getThrower() instanceof EntityPlayer && 
				this.getThrower().getPersistentID().equals(Minewatch.proxy.getClientUUID())) {
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.WIDOWMAKER_MINE_DESTROYED, worldObj, posX, posY+1d, posZ, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1, 80, 5, 5, 0, 0);
			Minewatch.proxy.playFollowingSound(this.getThrower(), ModSoundEvents.widowmakerMineDestroyed, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
		}
	}

}