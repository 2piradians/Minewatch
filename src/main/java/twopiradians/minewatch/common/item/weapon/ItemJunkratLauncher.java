package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityJunkratGrenade;
import twopiradians.minewatch.common.entity.EntityJunkratTrap;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemJunkratLauncher extends ItemMWWeapon {

	public ItemJunkratLauncher() {
		super(30);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false)) {
			if (!world.isRemote) {
				EntityJunkratGrenade grenade = new EntityJunkratGrenade(world, player);
				EntityHelper.setAim(grenade, player, player.rotationPitch, player.rotationYaw, 17.5f, 0.6F, hand);
				world.spawnEntity(grenade);
				world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.junkratShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
						world.rand.nextFloat()/3+0.8f);	
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0)
					player.getHeldItem(hand).damageItem(1, player);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 12);
				if (world.rand.nextInt(20) == 0)
					Minewatch.proxy.playFollowingSound(player, ModSoundEvents.junkratLaugh, SoundCategory.PLAYERS, 1f, 1.0f, false);
			}
			else {
				Vec3d vec = EntityHelper.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, world, vec.xCoord, vec.yCoord, vec.zCoord,
						0, 0, 0, 0xFF9D1A, 0x964D21, 0.7f, 5, 5, 4.5f, world.rand.nextFloat(), 0.01f);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityPlayer) {	
			EntityPlayer player = (EntityPlayer) entity;

			// steel trap
			if (!world.isRemote && hero.ability1.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				for (EntityPlayer player2 : world.playerEntities) 
					Minewatch.proxy.stopSound(player2, ModSoundEvents.junkratTrapTrigger, SoundCategory.PLAYERS);
				hero.ability1.keybind.setCooldown(player, 240, false); 
				EntityJunkratTrap trap = new EntityJunkratTrap(world, player);
				EntityHelper.setAim(trap, player, player.rotationPitch, player.rotationYaw, 15, 0, null);
				world.playSound(null, player.getPosition(), ModSoundEvents.junkratTrapThrow, SoundCategory.PLAYERS, 1.0f, 1.0f);
				world.spawnEntity(trap);
				player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
				hero.ability1.keybind.setCooldown(player, 20, false); //TODO 200
				if (hero.ability1.entity instanceof EntityJunkratTrap && hero.ability1.entity.isEntityAlive()) {
					TickHandler.unregister(false, 
							TickHandler.getHandler(((EntityJunkratTrap)hero.ability1.entity).trappedEntity, Identifier.PREVENT_MOVEMENT),
							TickHandler.getHandler(((EntityJunkratTrap)hero.ability1.entity).trappedEntity, Identifier.JUNKRAT_TRAP));
					Minewatch.network.sendToDimension(new SPacketSimple(26, hero.ability1.entity, false), world.provider.getDimension());
					hero.ability1.entity.isDead = true;
				}
				hero.ability1.entity = trap;
			}
		}
	}	

}
