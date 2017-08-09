package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityReaperBullet extends EntityMWThrowable {

	public EntityReaperBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityReaperBullet(World worldIn, EntityLivingBase throwerIn, EnumHand hand) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.ignoreEntity = this;
		this.lifetime = 5;
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);
		
		if (result.entityHit instanceof EntityLivingBase && this.getThrower() instanceof EntityPlayer &&
				result.entityHit != this.getThrower()) {
			float damage = 7 - (7 - 2) * (this.ticksExisted / lifetime);
			((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), damage/ItemMWWeapon.DAMAGE_SCALE);
			((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
			this.setDead();
		}
	}
}
