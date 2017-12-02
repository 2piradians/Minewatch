package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityJunkratMine;
import twopiradians.minewatch.common.entity.ability.EntityJunkratTrap;
import twopiradians.minewatch.common.entity.projectile.EntityJunkratGrenade;
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
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && !world.isRemote) {
			EntityJunkratGrenade grenade = new EntityJunkratGrenade(world, player, hand.ordinal());
			EntityHelper.setAim(grenade, player, player.rotationPitch, player.rotationYawHead, 35f, 0.6F, hand, 10, 0.55f);
			world.spawnEntityInWorld(grenade);
			ModSoundEvents.JUNKRAT_SHOOT.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(25) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			this.setCooldown(player, 12);
			if (world.rand.nextInt(20) == 0)
				ModSoundEvents.JUNKRAT_LAUGH_VOICE.playFollowingSound(player, 1, 1, false);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// give trigger if mine active and offhand is empty
			if (!world.isRemote && (player.getHeldItemOffhand() == null) && 
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
				ModSoundEvents.JUNKRAT_TRAP_TRIGGER.stopSound(world);
				EntityJunkratTrap trap = new EntityJunkratTrap(world, player);
				EntityHelper.setAim(trap, player, player.rotationPitch, player.rotationYawHead, 15, 0, null, 0, 0);
				ModSoundEvents.JUNKRAT_TRAP_THROW.playSound(player, 1, 1);
				world.spawnEntityInWorld(trap);
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
				ModSoundEvents.JUNKRAT_TRAP_PLACED_VOICE.playFollowingSound(player, 1, 1, false);
			}
			// mine
			else if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				hero.ability2.subtractUse(player); 
				hero.ability2.keybind.setCooldown(player, 10, true); 
				EntityJunkratMine mine = new EntityJunkratMine(world, player);
				EntityHelper.setAim(mine, player, player.rotationPitch, player.rotationYawHead, 25, 0, null, 0, 0);
				ModSoundEvents.JUNKRAT_MINE_THROW.playSound(player, 1, 1);
				world.spawnEntityInWorld(mine);
				player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
				Entity entity2 = hero.ability2.entities.get(player);
				if (entity2 instanceof EntityJunkratMine && entity2.isEntityAlive()) 
					entity2.isDead = true;
				hero.ability2.entities.put(player, mine);
				if (world.rand.nextBoolean())
					ModSoundEvents.JUNKRAT_TRAP_PLACED_VOICE.playFollowingSound(player, 1, 1, false);
			}
		}
	}	

}
