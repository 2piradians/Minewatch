package twopiradians.minewatch.common.item.weapon;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor.SetManager;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSyncAmmo;

public abstract class ItemMWWeapon extends Item {

	/**Used to uniformly scale damage for all weapons/abilities*/
	public static float damageScale;

	public EnumHero hero;
	public boolean hasOffhand;
	private HashMap<ItemStack, Integer> reequipAnimation = Maps.newHashMap();
	/**Cooldown in ticks for warning player about misusing weapons (main weapon in offhand, no offhand, etc.) */
	private static Handler WARNING_CLIENT = new Handler(Identifier.WEAPON_WARNING, false) {};
	/**Do not interact with directly - use the getter / setter*/
	private HashMap<UUID, Integer> currentAmmo = Maps.newHashMap();
	private int reloadTime;
	protected boolean savePlayerToNBT;

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
			if (player instanceof EntityPlayerMP) {
				EnumHand hand = player.getHeldItemMainhand() != null && 
						player.getHeldItemMainhand().getItem() == this ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
				Minewatch.network.sendTo(new SPacketSyncAmmo(hero, player.getPersistentID(), hand, amount, hands), (EntityPlayerMP) player); 
			}
			if (player.world.isRemote)
				for (EnumHand hand2 : hands)
					if (player.getHeldItem(hand2) != null && player.getHeldItem(hand2).getItem() == this) 
						this.reequipAnimation.put(player.getHeldItem(hand2), 2);
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
			if (hero.reloadSound != null && player instanceof EntityPlayerMP)
				Minewatch.proxy.playFollowingSound(player, hero.reloadSound, SoundCategory.PLAYERS, 1.0f, 
						player.world.rand.nextFloat()/2+0.75f);
		}
	}

	@Nullable
	public EnumHand getHand(EntityLivingBase entity, ItemStack stack) {
		for (EnumHand hand : EnumHand.values())
			if (entity.getHeldItem(hand) == stack)
				return hand;
		return null;
	}

	/**Check that weapon is in correct hand and that offhand weapon is held if hasOffhand.
	 * Also checks that weapon is not on cooldown.
	 * Warns player if something is incorrect.*/
	public boolean canUse(EntityPlayer player, boolean shouldWarn, @Nullable EnumHand hand) {
		if (player == null || player.getCooldownTracker().hasCooldown(this) || 
				(this.getMaxAmmo(player) > 0 && this.getCurrentAmmo(player) == 0) ||
				TickHandler.hasHandler(player, Identifier.PREVENT_INPUT) ||
				TickHandler.hasHandler(player, Identifier.ABILITY_USING))
			return false;

		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();
		String displayName = (main != null && main.getItem() == this) ? this.getItemStackDisplayName(main) :
			(off != null && off.getItem() == this) ? this.getItemStackDisplayName(off) : 
				new ItemStack(this).getDisplayName();

			if (!Config.allowGunWarnings)
				return true;
			else if (this.hasOffhand && ((off == null || off.getItem() != this) || (main == null || main.getItem() != this))) {
				if (shouldWarn && !TickHandler.hasHandler(player, Identifier.WEAPON_WARNING) && player.world.isRemote)
					player.sendMessage(new TextComponentString(TextFormatting.RED+
							displayName+" must be held in the main-hand and off-hand to work."));
			} 
			else if ((hand == EnumHand.OFF_HAND && !this.hasOffhand) ||(main == null || main.getItem() != this)) {
				if (shouldWarn && !TickHandler.hasHandler(player, Identifier.WEAPON_WARNING) && player.world.isRemote)
					player.sendMessage(new TextComponentString(TextFormatting.RED+
							displayName+" must be held in the main-hand to work."));
			}
			else
				return true;

			if (shouldWarn && player.world.isRemote && !TickHandler.hasHandler(player, Identifier.WEAPON_WARNING))
				TickHandler.register(true, WARNING_CLIENT.setEntity(player).setTicks(60));

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

		// set player in nbt for model changer (in ClientProxy) to reference
		if (this.savePlayerToNBT && entity instanceof EntityPlayer && !entity.world.isRemote && 
				stack != null && stack.getItem() == this) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = stack.getTagCompound();
			if (!nbt.hasKey("playerLeast") || nbt.getLong("playerLeast") != (entity.getPersistentID().getLeastSignificantBits())) {
				nbt.setUniqueId("player", entity.getPersistentID());
				stack.setTagCompound(nbt);
			}
		}

		// reloading
		if (!world.isRemote && entity instanceof EntityPlayer && (((EntityPlayer)entity).getHeldItemMainhand() == stack ||
				((EntityPlayer)entity).getHeldItemOffhand() == stack))
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
		if (entity instanceof EntityPlayer && Minewatch.keys.lmb((EntityPlayer) entity)) {
			EntityPlayer player = (EntityPlayer) entity;
			EnumHand hand = this.getHand(player, stack);	
			if (hand != null && (!this.hasOffhand || 
					((hand == EnumHand.MAIN_HAND && player.ticksExisted % 2 == 0) ||
							(hand == EnumHand.OFF_HAND && player.ticksExisted % 2 != 0))))
				onItemLeftClick(stack, world, (EntityPlayer) entity, hand);
		}

		// deselect ability if it has cooldown
		if (entity instanceof EntityPlayer)
			for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
				if (ability.keybind.getCooldown((EntityPlayer) entity) > 0)
					ability.toggle(entity, false);

		// set damage to full if option set to never use durability
		if (!world.isRemote && Config.durabilityOptionWeapons == 2 && stack.getItemDamage() != 0)
			stack.setItemDamage(0);
		// set damage to full if wearing full set and option set to not use durability while wearing full set
		else if (!world.isRemote && Config.durabilityOptionWeapons == 1 && stack.getItemDamage() != 0 && 
				SetManager.playersWearingSets.get(entity.getPersistentID()) == hero)
			stack.setItemDamage(0);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		for (ItemStack stack : this.reequipAnimation.keySet()) {
			if (newStack != null && newStack == stack) {
				if (this.reequipAnimation.get(stack) - 1 <= 0)
					this.reequipAnimation.remove(stack);
				else
					this.reequipAnimation.put(stack, this.reequipAnimation.get(stack)-1);
				return true;
			}
		}

		return oldStack.getItem() != newStack.getItem() || slotChanged;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	//PORT keep for 1.10 PosEyes vec
	/*
	*//**Copied from {@link Entity#getPositionEyes(float)} bc client-side only*//*
	private Vec3d getPositionEyes(Entity entity, float partialTicks)  {
		if (partialTicks == 1.0F)
			return new Vec3d(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ);
		else {
			double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
			double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)entity.getEyeHeight();
			double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;
			return new Vec3d(d0, d1, d2);
		}
	}*/
	
	// DEV SPAWN ARMOR ===============================================

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("devSpawned"))
			tooltip.add(TextFormatting.DARK_PURPLE+""+TextFormatting.BOLD+"Dev Spawned");
		super.addInformation(stack, player, tooltip, advanced);
	}

	/**Delete dev spawned dropped items*/
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		//delete dev spawned items if not worn by dev
		if (!entityItem.world.isRemote && entityItem != null && entityItem.getEntityItem() != null && 
				entityItem.getEntityItem().hasTagCompound() && 
				entityItem.getEntityItem().getTagCompound().hasKey("devSpawned")) {
			entityItem.setDead();
			return true;
		}
		return false;
	}

}