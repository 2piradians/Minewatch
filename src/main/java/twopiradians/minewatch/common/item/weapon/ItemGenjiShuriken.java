package twopiradians.minewatch.common.item.weapon;

import java.util.List;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemGenjiShuriken extends ItemMWWeapon {

	public ItemGenjiShuriken() {
		super(40);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (!player.world.isRemote && this.canUse(player, true) && player.ticksExisted % 3 == 0) {
			EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player);
			shuriken.setAim(player, player.rotationPitch, player.rotationYaw, 3F, 1.0F, 1F, hand, false);
			player.world.spawnEntity(shuriken);
			player.world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.genjiShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
					player.world.rand.nextFloat()/2+0.75f);	
			this.subtractFromCurrentAmmo(player, 1, hand);
			if (!player.getCooldownTracker().hasCooldown(this) && this.getCurrentAmmo(player) % 3 == 0 &&
					this.getCurrentAmmo(player) != this.getMaxAmmo(player))
				player.getCooldownTracker().setCooldown(this, 15);
			if (player.world.rand.nextInt(24) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (!player.world.isRemote && this.canUse(player, true)) {
			for (int i = 0; i < Math.min(3, this.getCurrentAmmo(player)); i++) {
				EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player);
				shuriken.setAim(player, player.rotationPitch, player.rotationYaw + (1 - i)*8, 3F, 1.0F, 0F, hand, false);
				player.world.spawnEntity(shuriken);
			}
			player.world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.genjiShoot, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/2+0.75f);
			this.subtractFromCurrentAmmo(player, 3, hand);
			if (world.rand.nextInt(8) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			if (!player.getCooldownTracker().hasCooldown(this))
				player.getCooldownTracker().setCooldown(this, 15);
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entity;

			if (world.isRemote && ((EntityPlayerSP)player).movementInput.jump && !player.onGround && !player.isOnLadder() && player.motionY < 0.0d)
				player.jump();
			
			//TODO Genji
			// deflect
			if (isSelected && hero.ability1.isSelected((EntityPlayer) entity) && !world.isRemote) {
				//	world.playSound(null, entity.getPosition(), ModSoundEvents.tracerBlink, SoundCategory.PLAYERS, 1.0f, world.rand.nextFloat()/2f+0.75f);	
				//	if (entity instanceof EntityPlayerMP)
				//		Minewatch.network.sendTo(new SPacketTriggerAbility(0), (EntityPlayerMP) entity);
				//	hero.ability2.subtractUse((EntityPlayer) entity);
				//	hero.ability2.keybind.setCooldown((EntityPlayer) entity, 5, true); 

				AxisAlignedBB aabb = player.getEntityBoundingBox().expandXyz(3);
				List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, aabb);

				for (Entity entityCollided : list) {

					if (entityCollided instanceof EntityArrow && ((EntityArrow)entityCollided).shootingEntity != player
							&& player.getLookVec().dotProduct(new Vec3d(entityCollided.motionX, entityCollided.motionY, entityCollided.motionZ)) < -0.1d) { 
						System.out.println(player.getLookVec().dotProduct(new Vec3d(entityCollided.motionX, entityCollided.motionY, entityCollided.motionZ)));
						EntityArrow ent = (EntityArrow) entityCollided;
						ent.shootingEntity = player;
						double velScale = Math.sqrt(ent.motionX*ent.motionX + ent.motionY*ent.motionY + ent.motionZ*ent.motionZ)*1.2d;
						ent.setPosition(player.posX + player.getLookVec().xCoord, player.posY + player.eyeHeight, player.posZ + player.getLookVec().zCoord);
						ent.motionX = player.getLookVec().xCoord*velScale;	
						ent.motionY = player.getLookVec().yCoord*velScale;	
						ent.motionZ = player.getLookVec().zCoord*velScale;	
						ent.velocityChanged = true;
					}
					else if (entityCollided instanceof EntityThrowable && player.getLookVec().dotProduct(new Vec3d(entityCollided.motionX, entityCollided.motionY, entityCollided.motionZ)) < -0.1d) {
						EntityThrowable ent = (EntityThrowable) entityCollided;
						double velScale = Math.sqrt(ent.motionX*ent.motionX + ent.motionY*ent.motionY + ent.motionZ*ent.motionZ)*1.2d;
						ent.motionX = player.getLookVec().xCoord*velScale;	
						ent.motionY = player.getLookVec().yCoord*velScale;	
						ent.motionZ = player.getLookVec().zCoord*velScale;		
						ent.velocityChanged = true;
					}
					else if (entityCollided instanceof EntityFireball /*&& ((EntityFireball)entityCollided).shootingEntity != player*/
							&& player.getLookVec().dotProduct(new Vec3d(entityCollided.motionX, entityCollided.motionY, entityCollided.motionZ)) < -0.1d) {
						EntityFireball ent = (EntityFireball) entityCollided;
						ent.accelerationX = 0;
						ent.accelerationY = 0;
						ent.accelerationZ = 0;
						ent.shootingEntity = player;
						double velScale = Math.sqrt(ent.motionX*ent.motionX + ent.motionY*ent.motionY + ent.motionZ*ent.motionZ)*1.2d;
						ent.motionX = player.getLookVec().xCoord*velScale;	
						ent.motionY = player.getLookVec().yCoord*velScale;	
						ent.motionZ = player.getLookVec().zCoord*velScale;		
						ent.velocityChanged = true;
					}
				}
			}
		}
	}	
}
