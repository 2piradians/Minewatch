package twopiradians.minewatch.common.item.weapon;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.packet.PacketSyncAmmo;

public abstract class ItemMWWeapon extends Item
{
	/**Used to uniformly scale damage for all weapons/abilities*/
	public static final float DAMAGE_SCALE = 10f;

	public EnumHero hero;

	public boolean hasOffhand;
	protected ResourceLocation scope;

	private HashMap<ItemStack, Integer> reequipAnimation = Maps.newHashMap();
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

	public void setCurrentAmmo(EntityPlayer player, int amount, EnumHand... hands) {
		if (player != null) {
			if (player instanceof EntityPlayerMP)
				Minewatch.network.sendTo(new PacketSyncAmmo(player.getPersistentID(), amount, hands), (EntityPlayerMP) player);
			if (player.world.isRemote)
				for (EnumHand hand : hands)
					if (player.getHeldItem(hand) != null && player.getHeldItem(hand).getItem() == this)
						this.reequipAnimation.put(player.getHeldItem(hand), 2);
			currentAmmo.put(player.getPersistentID(), amount);
		}
	}

	public void subtractFromCurrentAmmo(EntityPlayer player, int amount, EnumHand... hands) {
		int ammo = getCurrentAmmo(player);
		if (ammo - amount > 0)
			this.setCurrentAmmo(player, ammo-amount, hands);
		else {
			this.setCurrentAmmo(player, 0, hands);
			reload(player);
		}
	}

	public void reload(EntityPlayer player) {
		if (player != null && !player.world.isRemote && getCurrentAmmo(player) < getMaxAmmo(player)) {
			player.getCooldownTracker().setCooldown(this, reloadTime);
			this.setCurrentAmmo(player, 0, EnumHand.values());
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
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		//delete dev spawned items if not in dev's inventory and delete disabled items (except missingTexture items in SMP)
		if (!world.isRemote && entity instanceof EntityPlayer && stack.hasTagCompound() &&
				stack.getTagCompound().hasKey("devSpawned") && !CommandDev.DEVS.contains(entity.getPersistentID()) &&
				((EntityPlayer)entity).inventory.getStackInSlot(slot) == stack) {
			((EntityPlayer)entity).inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
			return;
		}

		// reloading
		if (!world.isRemote && entity instanceof EntityPlayer && isSelected)
			// automatic reload
			if (this.getCurrentAmmo((EntityPlayer) entity) == 0 && this.getMaxAmmo((EntityPlayer) entity) > 0 &&
					!((EntityPlayer)entity).getCooldownTracker().hasCooldown(this)) 
				this.setCurrentAmmo((EntityPlayer) entity, this.getMaxAmmo((EntityPlayer) entity), EnumHand.values());
		// manual reload
			else if (this.getCurrentAmmo((EntityPlayer) entity) > 0 && Minewatch.keys.reload((EntityPlayer) entity))
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
		if (this.reequipAnimation.containsKey(oldStack)) {
			if (this.reequipAnimation.get(oldStack) - 1 <= 0)
				this.reequipAnimation.remove(oldStack);
			else
				this.reequipAnimation.put(oldStack, this.reequipAnimation.get(oldStack)-1);
			return true;
		}
		else
			return oldStack.getItem() != newStack.getItem();
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	// DEV SPAWN ARMOR ===============================================

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("devSpawned"))
			tooltip.add(TextFormatting.DARK_PURPLE+""+TextFormatting.BOLD+"Dev Spawned");
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	/**Delete dev spawned dropped items*/
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		//delete dev spawned items if not worn by dev and delete disabled items (except missingTexture items in SMP)
		if (!entityItem.world.isRemote && entityItem != null && entityItem.getItem() != null && 
				entityItem.getItem().hasTagCompound() && 
				entityItem.getItem().getTagCompound().hasKey("devSpawned")) {
			entityItem.setDead();
			return true;
		}
		return false;
	}

}