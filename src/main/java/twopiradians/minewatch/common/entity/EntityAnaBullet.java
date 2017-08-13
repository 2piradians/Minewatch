package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityAnaBullet extends EntityMWThrowable
{
	private boolean heal = false;

	public EntityAnaBullet(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
	}

	public EntityAnaBullet(World worldIn, EntityLivingBase throwerIn, boolean heal) {
		super(worldIn, throwerIn);
		this.setNoGravity(true);
		this.heal = heal;
		this.lifetime = 40;
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);
		
		if (result.entityHit instanceof EntityLivingBase && this.getThrower() instanceof EntityPlayer && 
				result.entityHit != this.getThrower()) {
			if (this.heal) {
				((EntityLivingBase)result.entityHit).heal(75/ItemMWWeapon.DAMAGE_SCALE);
				((WorldServer)result.entityHit.world).spawnParticle(EnumParticleTypes.HEART, 
						result.entityHit.posX+0.5d, result.entityHit.posY+0.5d,result.entityHit.posZ+0.5d, 
						10, 0.4d, 0.4d, 0.4d, 0d, new int[0]);
				result.entityHit.world.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
						SoundEvents.BLOCK_NOTE_PLING, SoundCategory.PLAYERS, 0.3f, result.entityHit.world.rand.nextFloat()/2+1.5f);	
			}
			else {
				((EntityLivingBase)result.entityHit).attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getThrower()), 60F/ItemMWWeapon.DAMAGE_SCALE);
				result.entityHit.world.playSound(null, this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ, 
						SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.3f, result.entityHit.world.rand.nextFloat()/2+0.75f);
			}
			this.setDead();
		}
	}
}
