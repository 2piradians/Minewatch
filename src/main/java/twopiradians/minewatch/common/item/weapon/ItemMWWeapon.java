package twopiradians.minewatch.common.item.weapon;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.packet.PacketSyncAmmo;

public abstract class ItemMWWeapon extends Item
{
	/**Used to uniformly scale damage for all weapons/abilities*/
	public static final float DAMAGE_SCALE = 10f;

	public EnumHero hero;

	public boolean hasOffhand;
	protected ResourceLocation scope;

	/**Cooldown in ticks for warning player about misusing weapons (main weapon in offhand, no offhand, etc.) */
	private HashMap<UUID, Integer> warningCooldown = Maps.newHashMap();
	/**Do not interact with directly - use the getter / setter*/
	private HashMap<UUID, Integer> currentAmmo = Maps.newHashMap();
	private int reloadTime;

	public ItemMWWeapon(int reloadTime) {
		this.setMaxDamage(100);
		this.reloadTime = reloadTime;
	}

	public int getMaxAmmo(EntityPlayer player) {
		if (player != null && hero.hasAltWeapon && hero.playersUsingAlt.containsKey(player.getPersistentID()) && 
				hero.playersUsingAlt.get(player.getPersistentID()))
			return hero.altAmmo;
		else
			return hero.mainAmmo;
	}

	public int getCurrentAmmo(EntityPlayer player) {
		if (player != null && currentAmmo.containsKey(player.getPersistentID()))
			return currentAmmo.get(player.getPersistentID());
		else 
			return getMaxAmmo(player);
	}
	
	public void setCurrentAmmo(EntityPlayer player, int amount) {
		if (player != null && player instanceof EntityPlayerMP)
			Minewatch.network.sendTo(new PacketSyncAmmo(player.getPersistentID(), amount), (EntityPlayerMP) player);
		currentAmmo.put(player.getPersistentID(), amount);
	}

	public void subtractFromCurrentAmmo(EntityPlayer player, int amount) {
		int ammo = getCurrentAmmo(player);
		if (ammo - amount > 0)
			this.setCurrentAmmo(player, ammo-amount);
		else {
			this.setCurrentAmmo(player, 0);
			reload(player);
		}
	}

	public void reload(EntityPlayer player) {
		if (player != null && !player.world.isRemote && getCurrentAmmo(player) < getMaxAmmo(player)) {
			player.getCooldownTracker().setCooldown(this, reloadTime);
			this.setCurrentAmmo(player, 0);
			if (hero.reloadSound != null)
				player.world.playSound(null, player.posX, player.posY, player.posZ, 
						hero.reloadSound, SoundCategory.PLAYERS, 1.0f, 
						player.world.rand.nextFloat()/2+0.75f);
		}
	}

	/**Check that weapon is in correct hand and that offhand weapon is held if hasOffhand.
	 * Also checks that weapon is not on cooldown.
	 * Warns player if something is incorrect.*/
	public boolean canUse(EntityPlayer player, boolean shouldWarn) {
		if (player == null || player.getCooldownTracker().hasCooldown(this) || 
				(this.getMaxAmmo(player) > 0 && this.getCurrentAmmo(player) == 0))
			return false;

		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();

		if (this.hasOffhand && ((off == null || off.getItem() != this) || (main == null || main.getItem() != this))) {
			if (shouldWarn && (!this.warningCooldown.containsKey(player.getPersistentID()) || 
					this.warningCooldown.get(player.getPersistentID()) == 0))
				player.sendMessage(new TextComponentString(TextFormatting.RED+
						new ItemStack(this).getDisplayName()+" must be held in the main-hand and off-hand to work."));
		} 
		else if (main == null || main.getItem() != this) {
			if (shouldWarn && (!this.warningCooldown.containsKey(player.getPersistentID()) || 
					this.warningCooldown.get(player.getPersistentID()) == 0))
				player.sendMessage(new TextComponentString(TextFormatting.RED+
						new ItemStack(this).getDisplayName()+" must be held in the main-hand to work."));
		}
		else
			return true;

		if (shouldWarn && (!this.warningCooldown.containsKey(player.getPersistentID()) || 
				this.warningCooldown.get(player.getPersistentID()) == 0))
			this.warningCooldown.put(player.getPersistentID(), 60);

		return false;
	}

	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { }

	/**Cancel swing animation when left clicking*/
	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
		return true;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {	
		// reloading
		if (!world.isRemote && entity instanceof EntityPlayer && isSelected && 
				!((EntityPlayer)entity).getCooldownTracker().hasCooldown(this))
			// automatic reload
			if (this.getCurrentAmmo((EntityPlayer) entity) == 0 && this.getMaxAmmo((EntityPlayer) entity) > 0) 
				this.setCurrentAmmo((EntityPlayer) entity, this.getMaxAmmo((EntityPlayer) entity));
		// manual reload
			else if (Minewatch.keys.reload((EntityPlayer) entity))
				this.reload((EntityPlayer) entity);
		
		// left click 
		// note: this alternates stopping hands for weapons with hasOffhand, 
		// so make sure weapons with hasOffhand use an odd numbered cooldown
		if (entity instanceof EntityPlayer && Minewatch.keys.lmb((EntityPlayer) entity))
			for (EnumHand hand : EnumHand.values())
				if (((EntityPlayer)entity).getHeldItem(hand) == stack && 
				(!hasOffhand || !(hand == EnumHand.MAIN_HAND && entity.ticksExisted % 2 == 0)))
					onItemLeftClick(stack, world, (EntityPlayer) entity, hand);

		// warning cooldown
		for (UUID uuid : warningCooldown.keySet())
			if (warningCooldown.get(uuid) != 0)
				warningCooldown.put(uuid, Math.max(warningCooldown.get(uuid)-1, 0));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return oldStack.getItem() != newStack.getItem();
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

}