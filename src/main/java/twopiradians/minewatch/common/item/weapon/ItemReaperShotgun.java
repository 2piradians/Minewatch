package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMWThrowable;
import twopiradians.minewatch.common.entity.EntityReaperBullet;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.SPacketSpawnParticle;

public class ItemReaperShotgun extends ItemMWWeapon {
	
	public ItemReaperShotgun() {
		super(30);
		this.hasOffhand = true;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (this.canUse(player, true) && !world.isRemote) {
			for (int i=0; i<20; i++) {
				EntityReaperBullet bullet = new EntityReaperBullet(world, player, hand);
				bullet.setAim(player, player.rotationPitch, player.rotationYaw, 3.0F, 4F, 1F, hand, false);
				world.spawnEntity(bullet);
			}
			world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.reaperShoot, SoundCategory.PLAYERS, 
					world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);	
			Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
			Minewatch.network.sendToAllAround(new SPacketSpawnParticle(0, vec.xCoord, vec.yCoord, vec.zCoord, 0xD93B1A, 0x510D30, 5, 5), 
					new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 128));

			this.subtractFromCurrentAmmo(player, 1, hand);
			if (!player.getCooldownTracker().hasCooldown(this))
				player.getCooldownTracker().setCooldown(this, 11);
			if (world.rand.nextInt(25) == 0 && ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) != hero)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, world, entity, itemSlot, isSelected);

		if (entity instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer)entity;

			if (!world.isRemote && hero.ability1.isSelected(player)) {   

				Vec3d vecPos = player.getPositionEyes(1);
				Vec3d vec = player.getLookVec().scale(35);
				Vec3d vecLook = vec.addVector(vec.xCoord * 40, vec.yCoord * 40, vec.zCoord * 40);
												
				if (world.rayTraceBlocks(vecPos, vecLook, false, false, true) != null && world.rayTraceBlocks(vecPos, vecLook, false, false, true).typeOfHit == RayTraceResult.Type.BLOCK) {
					
					BlockPos pos = world.rayTraceBlocks(vecPos, vecLook, false, false, true).getBlockPos();
					
					((WorldServer)world).spawnParticle(EnumParticleTypes.HEART, pos.getX() + 0.5d, pos.getY()+world.rand.nextDouble()*2, pos.getZ() + 0.5d, 10, 0, 0, 0, 0, new int[0]);
					
					if (Minewatch.keys.rmb(player) && player.attemptTeleport(pos.getX() + 0.5d, pos.getY()+1, pos.getZ() + 0.5d)) {//if pos found and can tp
						if (player.isRiding())
							player.dismountRidingEntity();
						/*world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
					world.playSound((EntityPlayer)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
					player.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
					for (int j = 0; j < 64; ++j) {
						((WorldServer)world).spawnParticle(EnumParticleTypes.PORTAL, player.posX+2*world.rand.nextDouble(), player.posY+world.rand.nextDouble()+1, player.posZ+2*world.rand.nextDouble(), 1, 0, 0, 0, 1, new int[0]);
						((WorldServer)world).spawnParticle(EnumParticleTypes.PORTAL, player.posX+2*world.rand.nextDouble(), player.posY+world.rand.nextDouble()+1.0D, player.posZ+2*world.rand.nextDouble(), 1, 0, 0, 0, 1, new int[0]);
					}*/
						hero.ability1.keybind.setCooldown(player, 20, false); 
					}
				}
			}
		}
	}
}
