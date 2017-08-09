package twopiradians.minewatch.common.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;

public class EntityHanzoSonicArrow extends EntityHanzoArrow {

	public EntityHanzoSonicArrow(World worldIn) {
		super(worldIn);
	}

	public EntityHanzoSonicArrow(World worldIn, EntityLivingBase shooter) {
		super(worldIn, shooter);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (!this.world.isRemote && (this.timeInGround+1) % 10 == 0) {
			AxisAlignedBB aabb = this.getEntityBoundingBox().expandXyz(10);
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this.shootingEntity, aabb);
			for (Entity entity2 : list) 
				if (entity2 instanceof EntityLivingBase) 
					((EntityLivingBase) entity2).addPotionEffect(new PotionEffect(MobEffects.GLOWING, 30, 0, true, false));
		}
		
		if (this.world.isRemote && 
				(this.timeInGround == 1 || this.timeInGround == 7 || this.timeInGround == 10 || this.timeInGround == 13))
			Minewatch.proxy.spawnParticlesHanzoSonic(this.world, this.posX, this.posY, this.posZ, true);
		else if (this.world.isRemote && this.timeInGround > 30 && this.timeInGround < 170 &&
				(this.timeInGround % 30 == 0 || this.timeInGround % 32 == 0))
			Minewatch.proxy.spawnParticlesHanzoSonic(this.world, this.posX, this.posY, this.posZ, false);
		else if (this.timeInGround > 200)
			this.setDead();
		
	}

}
