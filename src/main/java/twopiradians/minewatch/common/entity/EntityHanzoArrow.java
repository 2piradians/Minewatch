package twopiradians.minewatch.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.packet.PacketSyncSpawningEntity;

public class EntityHanzoArrow extends EntityArrow {

	public EntityHanzoArrow(World worldIn) {
		super(worldIn);
	}

	public EntityHanzoArrow(World worldIn, EntityLivingBase shooter) {
		super(worldIn, shooter);
		if (shooter instanceof EntityPlayer 
				&& (ItemMWArmor.SetManager.playersWearingSets.get(shooter.getPersistentID()) == EnumHero.HANZO || 
				((EntityPlayer)shooter).capabilities.isCreativeMode))
			this.pickupStatus = EntityTippedArrow.PickupStatus.DISALLOWED;
		else
			this.pickupStatus = EntityTippedArrow.PickupStatus.ALLOWED;
	}

	@Override
	public void setAim(Entity shooter, float pitch, float yaw, float p_184547_4_, float velocity, float inaccuracy) {
		super.setAim(shooter, pitch, yaw, p_184547_4_, velocity, inaccuracy);

		// correct trajectory of fast entities (received in render class)
		if (!this.world.isRemote && this.ticksExisted == 0)
			Minewatch.network.sendToAllAround(
					new PacketSyncSpawningEntity(this.getPersistentID(), this.rotationPitch, this.rotationYaw, this.motionX, this.motionY, this.motionZ), 
					new TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 1024));
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!this.hasNoGravity() && this.rotationPitch > -50) 
            this.motionY += 0.04D;
	}

	@Override
	protected ItemStack getArrowStack() {
		return new ItemStack(Items.ARROW);
	}

}
