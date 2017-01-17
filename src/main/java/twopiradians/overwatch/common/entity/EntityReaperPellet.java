package twopiradians.overwatch.common.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityReaperPellet extends EntityThrowable
{
	private static final int LIFETIME = 5;
	
	public EntityReaperPellet(World worldIn) {
		super(worldIn);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
	}
	
	public EntityReaperPellet(World worldIn, EntityPlayer shooter) {
		this(worldIn);
		//TODO adjust for which gun fires
		double velX = Math.cos(shooter.rotationPitch*Math.PI/180) * Math.cos(shooter.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.2d;
		double velY = - Math.sin(shooter.rotationPitch*Math.PI/180) + (Math.random() - 0.5d)*0.2d;
		double velZ = Math.cos(shooter.rotationPitch*Math.PI/180) * Math.sin(shooter.rotationYawHead*Math.PI/180 + Math.PI/2) + (Math.random() - 0.5d)*0.2d;
		double x = shooter.posX + Math.cos(shooter.rotationPitch*Math.PI/180)*Math.cos(shooter.rotationYawHead*Math.PI/180 + Math.PI/2)*2;
		double y = shooter.posY + 1 - Math.sin(shooter.rotationPitch*Math.PI/180)*2;
		double z = shooter.posZ + Math.cos(shooter.rotationPitch*Math.PI/180)*Math.sin(shooter.rotationYawHead*Math.PI/180 + Math.PI/2)*2;
		setPositionAndRotationDirect(x, y, z, shooter.rotationYawHead, shooter.rotationPitch, 0, false);
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
		if (result.entityHit instanceof EntityLiving) {
			float damage = 7 - (7 - 2) * (this.ticksExisted / LIFETIME);
			((EntityLiving)result.entityHit).attackEntityFrom(DamageSource.magic, damage);
			((EntityLiving)result.entityHit).hurtResistantTime = 0;
		}
		this.setDead();
	}
}
