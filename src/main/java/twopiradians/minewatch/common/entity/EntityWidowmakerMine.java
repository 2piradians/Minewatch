package twopiradians.minewatch.common.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityWidowmakerMine extends EntityLivingBaseMW {

	public EnumFacing facing;
	private boolean prevOnGround;
	public static final Handler TRAPPED = new Handler(Identifier.JUNKRAT_TRAP, false) {};

	public EntityWidowmakerMine(World worldIn) {
		this(worldIn, null);
	}

	public EntityWidowmakerMine(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.4f, 0.4f);
		this.lifetime = Integer.MAX_VALUE;
		this.ignoreImpacts.add(RayTraceResult.Type.ENTITY);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Math.max(1, 1.0D*Config.damageScale));
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
		if (prevOnGround != onGround && onGround) {
			//this.world.playSound(null, this.getPosition(), ModSoundEvents.junkratTrapLand, SoundCategory.PLAYERS, 1.0f, 1.0f);
			if (world.isRemote && this.getThrower() instanceof EntityPlayer && 
					this.getThrower().getPersistentID().equals(Minewatch.proxy.getClientUUID()))
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.JUNKRAT_TRAP, world, this, 0xFFFFFF, 0xFFFFFF, 1, Integer.MAX_VALUE, 1, 1, 0, 0);
		}
		this.prevOnGround = this.onGround;
		
		// gravity
		if (!this.onGround)
			this.motionY -= 0.03D;

		// check for entities
		if (!this.world.isRemote  && this.onGround && this.getThrower() instanceof EntityLivingBase) {
			List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expandXyz(3d));
			for (Entity entity : entities) 
				if (!(entity instanceof EntityLivingBaseMW) && entity instanceof EntityLivingBase && 
						EntityHelper.shouldHit(this.getThrower(), entity, false)) {
					/*TickHandler.register(false, Handlers.PREVENT_MOVEMENT.setTicks(70).setEntity(entity),
							TRAPPED.setTicks(70).setEntity(entity));
					Minewatch.network.sendToAll(new SPacketSimple(25, true, 
							this.getThrower() instanceof EntityPlayer ? (EntityPlayer)this.getThrower() : null, 
									0, 0, 0, this, this.trappedEntity));*/
					//world.playSound(null, this.getPosition(), ModSoundEvents.junkratTrapTrigger, SoundCategory.PLAYERS, 1.0f, 1.0f);
					//this.setDead();
				}
		}

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
	}

}
