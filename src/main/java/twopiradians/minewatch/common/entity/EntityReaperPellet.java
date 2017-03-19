package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.item.weapon.ModWeapon;

public class EntityReaperPellet extends EntityThrowable
{
	private static final int LIFETIME = 5;
    private EntityLivingBase thrower;
	
	public EntityReaperPellet(World worldIn) {
		super(worldIn);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
	}
	
	//Client doesn't read here
	public EntityReaperPellet(World worldIn, EntityLivingBase throwerIn) {
		this(worldIn);
		//TODO adjust for which gun fires
		double velX = Math.cos(throwerIn.rotationPitch*Math.PI/180) * Math.cos(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.2d;
		double velY = - Math.sin(throwerIn.rotationPitch*Math.PI/180) + (Math.random() - 0.5d)*0.2d;
		double velZ = Math.cos(throwerIn.rotationPitch*Math.PI/180) * Math.sin(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.2d;
		double x = throwerIn.posX + Math.cos(throwerIn.rotationPitch*Math.PI/180)*Math.cos(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2);
		double y = throwerIn.posY + 1.5d - Math.sin(throwerIn.rotationPitch*Math.PI/180);
		double z = throwerIn.posZ + Math.cos(throwerIn.rotationPitch*Math.PI/180)*Math.sin(throwerIn.rotationYawHead*Math.PI/180 + Math.PI/2);
		this.setPositionAndRotationDirect(x, y, z, 0, 0, 0, false);
		double speed = 3.0d;
		double speedNormalize = Math.sqrt(velX*velX + velY*velY + velZ*velZ);
		velX *= speed/speedNormalize;
		velY *= speed/speedNormalize;
		velZ *= speed/speedNormalize;
		setVelocity(velX, velY, velZ);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.ticksExisted > LIFETIME)
			this.setDead();
		
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.entityHit != null && result.entityHit == this.thrower)
			return;
		else if (result.entityHit instanceof EntityLiving) {
			float damage = 7 - (7 - 2) * (this.ticksExisted / LIFETIME);
			((EntityLiving)result.entityHit).attackEntityFrom(DamageSource.MAGIC, damage/ModWeapon.DAMAGE_SCALE);
			((EntityLiving)result.entityHit).hurtResistantTime = 0;
			this.setDead();
		}
		else if (result.typeOfHit == RayTraceResult.Type.BLOCK)
			this.setDead();
	}
	
    public void writeEntityToNBT(NBTTagCompound compound) {
    	
    }
	
    public void readEntityFromNBT(NBTTagCompound compound) {
        this.thrower = this.world.getPlayerEntityByName(compound.getString("ownerName"));
    }
}
