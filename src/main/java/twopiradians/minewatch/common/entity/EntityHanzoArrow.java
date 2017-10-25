package twopiradians.minewatch.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityHanzoArrow extends EntityArrow implements IThrowableEntity {

	public static final DataParameter<Rotations> VELOCITY = EntityDataManager.<Rotations>createKey(EntityHanzoArrow.class, DataSerializers.ROTATIONS);
	
	public EntityHanzoArrow(World worldIn) {
		super(worldIn);
	}

	public EntityHanzoArrow(World worldIn, EntityLivingBase shooter) {
		super(worldIn, shooter);
		if (shooter instanceof EntityPlayer 
				&& (ItemMWArmor.SetManager.entitiesWearingSets.get(shooter.getPersistentID()) == EnumHero.HANZO || 
				((EntityPlayer)shooter).capabilities.isCreativeMode))
			this.pickupStatus = EntityTippedArrow.PickupStatus.DISALLOWED;
		else
			this.pickupStatus = EntityTippedArrow.PickupStatus.ALLOWED;
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (key.getId() == VELOCITY.getId()) {
			this.motionX = this.dataManager.get(VELOCITY).getX();
			this.motionY = this.dataManager.get(VELOCITY).getY();
			this.motionZ = this.dataManager.get(VELOCITY).getZ();
			EntityHelper.setRotations(this);
		}
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(VELOCITY, new Rotations(0, 0, 0));
	}

	@Override
	protected void onHit(RayTraceResult result) {
		if (result.entityHit != null) {
			if (EntityHelper.attemptImpact(this, result.entityHit, (float) this.getDamage(), false)) {
				if (result.entityHit instanceof EntityLivingBase)
					((EntityLivingBase) result.entityHit).setArrowCountInEntity(((EntityLivingBase) result.entityHit).getArrowCountInEntity() + 1);
				this.setDead();
			}
			
			// stop moving so particles don't keep going
			if (EntityHelper.shouldHit(this.getThrower(), result.entityHit, false) &&
					!TickHandler.hasHandler(result.entityHit, Identifier.GENJI_DEFLECT)) {
				this.motionX = 0;
				this.motionY = 0;
				this.motionZ = 0;
			}
		}
		else if (result.entityHit == null)
			super.onHit(result);
	}

	@Override
	protected ItemStack getArrowStack() {
		return new ItemStack(Items.ARROW);
	}

	@Override
	public EntityLivingBase getThrower() {
		if (this.shootingEntity instanceof EntityLivingBase)
			return (EntityLivingBase) this.shootingEntity;
		else
			return null;
	}

	@Override
	public void setThrower(Entity entity) {
		if (entity instanceof EntityLivingBase)
			this.shootingEntity = (EntityLivingBase) entity;
	}

}