package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.armor.ModArmor;

public class EntityHanzoArrow extends EntityTippedArrow {

	public EntityHanzoArrow(World worldIn) {
		super(worldIn);
	}

	public EntityHanzoArrow(World worldIn, double x, double y, double z) {
		this(worldIn);
		this.setPosition(x, y, z);
	}

	public EntityHanzoArrow(World worldIn, EntityLivingBase shooter) {
		this(worldIn, shooter.posX, shooter.posY + (double)shooter.getEyeHeight() - 0.10000000149011612D, shooter.posZ);
		this.shootingEntity = shooter;
		if (shooter instanceof EntityPlayer && ModArmor.isSet((EntityPlayer)shooter, ModItems.hanzo))
			this.pickupStatus = EntityTippedArrow.PickupStatus.DISALLOWED;
		else
			this.pickupStatus = EntityTippedArrow.PickupStatus.ALLOWED;
	}

	@Override
	protected ItemStack getArrowStack() {
        return new ItemStack(Items.ARROW);
	}
}
