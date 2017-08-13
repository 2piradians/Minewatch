package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntitySoldier76HelixRocket extends EntityMWThrowable {

    private static final DataParameter<Integer> NUMBER = EntityDataManager.<Integer>createKey(EntitySoldier76HelixRocket.class, DataSerializers.VARINT);
	
	public EntitySoldier76HelixRocket(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntitySoldier76HelixRocket(World worldIn, EntityLivingBase throwerIn, int number) {
		super(worldIn, throwerIn);
		this.getDataManager().set(NUMBER, number);
		this.setNoGravity(true);
		this.lifetime = 100;
	}
	
	@Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(NUMBER, Integer.valueOf(0));
    }
	
	@Override
	public void onUpdate() {		
		int num = this.getDataManager().get(NUMBER);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		if (result.entityHit instanceof EntityLivingBase && this.getThrower() != null &&
				result.entityHit != this.getThrower()) {
			float damage = 19 - (19 - 5.7f) * (this.ticksExisted / lifetime);
			((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), damage/ItemMWWeapon.DAMAGE_SCALE);
			((EntityLivingBase)result.entityHit).hurtResistantTime = 0;
			this.setDead();
		}
	}
}
