package twopiradians.minewatch.common.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
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
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityJunkratTrap extends EntityLivingBaseMW {

	public EntityLivingBase trappedEntity;
	public int trappedTicks;
	private boolean prevOnGround;
	public static final Handler TRAPPED = new Handler(Identifier.JUNKRAT_TRAP, false) {};

	public EntityJunkratTrap(World worldIn) {
		this(worldIn, null);
	}

	public EntityJunkratTrap(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(1.3f, 0.3f);
		this.lifetime = Integer.MAX_VALUE;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Math.max(1, 100.0D*Config.damageScale));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return distance < 2000;
	}

	@Override
	public void onUpdate() {
		if (this.onGround)
			this.rotationPitch = 0;

		// prevOnGround and normal particle
		if (prevOnGround != onGround && onGround) {
			this.worldObj.playSound(null, this.getPosition(), ModSoundEvents.junkratTrapLand, SoundCategory.PLAYERS, 1.0f, 1.0f);
			if (worldObj.isRemote && this.getThrower() instanceof EntityPlayer && 
					this.getThrower().getPersistentID().equals(Minewatch.proxy.getClientUUID()))
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.JUNKRAT_TRAP, worldObj, this, 0xFFFFFF, 0xFFFFFF, 1, Integer.MAX_VALUE, 1, 1, 0, 0);
		}
		this.prevOnGround = this.onGround;

		// don't impact when not on ground (bc it jumps around a bit)
		this.skipImpact = !this.onGround;

		// gravity
		this.motionY -= 0.05D;

		// gradual slowdown
		double d1 = this.onGround ? 0.1d : this.inWater ? 0.6d : 0.97d;
		this.motionX *= d1;
		this.motionY *= d1;
		this.motionZ *= d1;

		// check for entities to trap
		if (!this.worldObj.isRemote && this.trappedEntity == null && 
				this.onGround && this.getThrower() instanceof EntityLivingBase) {
			List<Entity> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expandXyz(0.5d));
			for (Entity entity : entities) 
				if (!(entity instanceof EntityLivingBaseMW) && entity instanceof EntityLivingBase && EntityHelper.shouldHit(this.getThrower(), entity, false) && 
						!TickHandler.hasHandler(entity, Identifier.JUNKRAT_TRAP) && EntityHelper.attemptDamage(this.getThrower(), entity, 80, true)) {
					if (((EntityLivingBase)entity).getHealth() > 0) {
						this.trappedEntity = (EntityLivingBase) entity;
						this.lifetime = this.ticksExisted + 70;
						TickHandler.register(false, Handlers.PREVENT_MOVEMENT.setTicks(70).setEntity(entity),
								TRAPPED.setTicks(70).setEntity(entity));
						Minewatch.network.sendToAll(new SPacketSimple(25, true, 
								this.getThrower() instanceof EntityPlayer ? (EntityPlayer)this.getThrower() : null, 
										0, 0, 0, this, this.trappedEntity));
						worldObj.playSound(null, this.getPosition(), ModSoundEvents.junkratTrapTrigger, SoundCategory.PLAYERS, 1.0f, 1.0f);
					}
					else {
						this.setDead();
						Minewatch.network.sendToAll(new SPacketSimple(25, false, 
								this.getThrower() instanceof EntityPlayer ? (EntityPlayer)this.getThrower() : null, 
										0, 0, 0, this, this.trappedEntity));
					}
					break;
				}
		}

		// set position of trapped entity
		if (this.trappedEntity != null) {
			this.trappedTicks++;
			this.trappedEntity.setPosition(this.posX, this.posY, this.posZ);
		}

		// check to set dead
		if (!this.worldObj.isRemote && !(this.getThrower() instanceof EntityLivingBase))
			this.setDead();
		else if (!this.worldObj.isRemote && this.trappedEntity != null && (this.trappedEntity.getHealth() <= 0 || !this.onGround)) 
			this.setDead();

		super.onUpdate();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.trappedEntity == null)
			return super.attackEntityFrom(source, amount);
		else 
			return false;
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);

		if (this.worldObj.isRemote && this.getThrower() instanceof EntityPlayer && 
				this.getThrower().getPersistentID().equals(Minewatch.proxy.getClientUUID()))
			Minewatch.proxy.spawnParticlesCustom(EnumParticle.JUNKRAT_TRAP_DESTROYED, worldObj, posX, posY+1.5d, posZ, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1, 80, 5, 5, 0, 0);
	}

	@Override
	protected void onImpact(RayTraceResult result) {}

	@Override
	public void setDead() {
		this.isDead = true;
		if (!this.worldObj.isRemote) {
			for (EntityPlayer player : worldObj.playerEntities) 
				Minewatch.proxy.stopSound(player, ModSoundEvents.junkratTrapTrigger, SoundCategory.PLAYERS);
			this.worldObj.playSound(null, this.getPosition(), ModSoundEvents.junkratTrapBreak, SoundCategory.PLAYERS, 1.0f, 1.0f);
			Minewatch.network.sendToDimension(new SPacketSimple(26, this, true, posX, posY, posZ), this.worldObj.provider.getDimension());
		}
	}

}
