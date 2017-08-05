package twopiradians.minewatch.common.entity;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityAnaBullet extends EntityThrowable
{
	private static final int LIFETIME = 40;
	private boolean heal = false;

	public EntityAnaBullet(World worldIn) {
		super(worldIn);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
	}

	//Client doesn't read here
	public EntityAnaBullet(World worldIn, EntityLivingBase throwerIn, boolean heal) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
		this.setPosition(throwerIn.posX, throwerIn.posY + (double)throwerIn.getEyeHeight(), throwerIn.posZ);
		this.heal = heal;
	}

	/**Copied from EntityArrow*/
	public void setAim(Entity shooter, float pitch, float yaw, float velocity, float inaccuracy) {
		float f = -MathHelper.sin(yaw * (float)Math.PI/180) * MathHelper.cos(pitch * (float)Math.PI/180);
		float f1 = -MathHelper.sin(pitch * (float)Math.PI/180);
		float f2 = MathHelper.cos(yaw * (float)Math.PI/180) * MathHelper.cos(pitch * (float)Math.PI/180);
		this.setThrowableHeading((double)f, (double)f1, (double)f2, velocity, inaccuracy);
		this.motionX += shooter.motionX;
		this.motionZ += shooter.motionZ;
		this.prevRotationPitch = pitch;
		this.prevRotationYaw = yaw;
		this.setRotation(yaw, pitch);

		if (!shooter.onGround) {
			this.motionY += shooter.motionY;
		}
	}

	@Override
	public void onUpdate() {		
		float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
		this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * (180D / Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
		super.onUpdate();

		if (this.ticksExisted > LIFETIME)
			this.setDead();
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.entityHit instanceof EntityLivingBase && this.getThrower() != null && result.entityHit != this.getThrower()) {
			if (this.heal) {
				((EntityLivingBase)result.entityHit).heal(75/ItemMWWeapon.DAMAGE_SCALE);
				((WorldServer)result.entityHit.world).spawnParticle(EnumParticleTypes.HEART, 
						result.entityHit.posX+0.5d, result.entityHit.posY+0.5d,result.entityHit.posZ+0.5d, 
						10, 0.4d, 0.4d, 0.4d, 0d, new int[0]);
				if (this.getThrower() != null)
					result.entityHit.world.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
							SoundEvents.BLOCK_NOTE_PLING, SoundCategory.PLAYERS, 0.3f, result.entityHit.world.rand.nextFloat()/2+1.5f);	
			}
			else {
				if (this.getThrower() instanceof EntityPlayer)
					((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 60F/ItemMWWeapon.DAMAGE_SCALE);
				else 
					if (this.getThrower() instanceof EntityPlayer)
						((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 60F/ItemMWWeapon.DAMAGE_SCALE);
				if (this.getThrower() != null)
					result.entityHit.world.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
							SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.3f, result.entityHit.world.rand.nextFloat()/2+0.75f);
			}
			this.setDead();
		}
		else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			Block block = this.world.getBlockState(result.getBlockPos()).getBlock();

			if (!Arrays.asList(ModEntities.ENTITY_PASSES_THROUGH).contains(block))
				this.setDead();
		}
	}
}
