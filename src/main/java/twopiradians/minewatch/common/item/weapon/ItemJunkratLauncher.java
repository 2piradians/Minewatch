package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityJunkratGrenade;
import twopiradians.minewatch.common.entity.EntityJunkratMine;
import twopiradians.minewatch.common.entity.EntityJunkratTrap;
import twopiradians.minewatch.common.item.ModItems;
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
		if (this.canUse(player, true, hand, false) && !world.isRemote) {
			EntityJunkratGrenade grenade = new EntityJunkratGrenade(world, player, hand.ordinal());
			EntityHelper.setAim(grenade, player, player.rotationPitch, player.rotationYaw, 35f, 0.6F, hand, 10, 0.55f);
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
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityPlayer) {	
			EntityPlayer player = (EntityPlayer) entity;

			// give trigger if mine active and offhand is empty
			if (!world.isRemote && (player.getHeldItemOffhand() == null || player.getHeldItemOffhand() == ItemStack.EMPTY) && 
					hero.ability2.entities.containsKey(entity) && 
					hero.ability2.entities.get(entity).isEntityAlive())
				player.setHeldItem(EnumHand.OFF_HAND, new ItemStack(ModItems.junkrat_trigger));

			// trigger mine
			if (!world.isRemote && Keys.KeyBind.RMB.isKeyDown(player) &&
					hero.ability2.entities.containsKey(entity) && 
					hero.ability2.entities.get(entity).isEntityAlive() && 
					hero.ability2.entities.get(entity) instanceof EntityJunkratMine)
				((EntityJunkratMine)hero.ability2.entities.get(entity)).explode();

			// steel trap
			if (!world.isRemote && hero.ability1.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				for (EntityPlayer player2 : world.playerEntities) 
					Minewatch.proxy.stopSound(player2, ModSoundEvents.junkratTrapTrigger, SoundCategory.PLAYERS);
				EntityJunkratTrap trap = new EntityJunkratTrap(world, player);
				EntityHelper.setAim(trap, player, player.rotationPitch, player.rotationYaw, 15, 0, null, 0, 0);
				world.playSound(null, player.getPosition(), ModSoundEvents.junkratTrapThrow, SoundCategory.PLAYERS, 1.0f, 1.0f);
				world.spawnEntity(trap);
				player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
				hero.ability1.keybind.setCooldown(player, 200, false);
				if (hero.ability1.entities.get(player) instanceof EntityJunkratTrap && hero.ability1.entities.get(player).isEntityAlive()) {
					TickHandler.unregister(false, 
							TickHandler.getHandler(((EntityJunkratTrap)hero.ability1.entities.get(player)).trappedEntity, Identifier.PREVENT_MOVEMENT),
							TickHandler.getHandler(((EntityJunkratTrap)hero.ability1.entities.get(player)).trappedEntity, Identifier.JUNKRAT_TRAP));
					Minewatch.network.sendToDimension(new SPacketSimple(26, hero.ability1.entities.get(player), false), world.provider.getDimension());
					hero.ability1.entities.get(player).isDead = true;
				}
				hero.ability1.entities.put(player, trap);
				Minewatch.proxy.playFollowingSound(player, ModSoundEvents.junkratTrapPlacedVoice, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
			}
			// mine
			else if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				hero.ability2.subtractUse(player); 
				hero.ability2.keybind.setCooldown(player, 10, true); 
				EntityJunkratMine mine = new EntityJunkratMine(world, player);
				EntityHelper.setAim(mine, player, player.rotationPitch, player.rotationYaw, 25, 0, null, 0, 0);
				world.playSound(null, player.getPosition(), ModSoundEvents.junkratMineThrow, SoundCategory.PLAYERS, 1.0f, 1.0f);
				world.spawnEntity(mine);
				player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
				Entity entity2 = hero.ability2.entities.get(player);
				if (entity2 instanceof EntityJunkratMine && entity2.isEntityAlive()) 
					entity2.isDead = true;
				hero.ability2.entities.put(player, mine);
				if (world.rand.nextBoolean())
					Minewatch.proxy.playFollowingSound(player, ModSoundEvents.junkratTrapPlacedVoice, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
			}
		}
	}	

}
