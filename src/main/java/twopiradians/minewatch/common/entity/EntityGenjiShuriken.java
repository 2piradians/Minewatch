package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityGenjiShuriken extends EntityMWThrowable {

	public EntityGenjiShuriken(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityGenjiShuriken(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.ignoreEntity = this;
		this.lifetime = 40;
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);
		
		if (result.entityHit instanceof EntityLivingBase && this.getThrower() instanceof EntityPlayer && 
				result.entityHit != this.getThrower()) {
			((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 28F/ItemMWWeapon.DAMAGE_SCALE);
			result.entityHit.world.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
					SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.3f, result.entityHit.world.rand.nextFloat()/2+0.75f);
			((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
			this.setDead();
		}
	}
}
