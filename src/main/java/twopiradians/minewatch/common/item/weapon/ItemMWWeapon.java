package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.IChangingModel;
import twopiradians.minewatch.common.item.armor.ItemMWArmor.SetManager;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSyncAmmo;

public abstract class ItemMWWeapon extends Item implements IChangingModel {

	public EnumHero hero;
	public boolean hasOffhand;
	private HashMap<ItemStack, Integer> reequipAnimation = Maps.newHashMap();
	/**Cooldown in ticks for warning player about misusing weapons (main weapon in offhand, no offhand, etc.) */
	private static Handler WARNING_CLIENT = new Handler(Identifier.WEAPON_WARNING, false) {};
	/**Do not interact with directly - use the getter / setter*/
	private HashMap<UUID, Integer> currentAmmo = Maps.newHashMap();
	private int reloadTime;
	protected boolean saveEntityToNBT;
	protected boolean showHealthParticles;

	public ItemMWWeapon(int reloadTime) {
		this.setMaxDamage(100);
		this.reloadTime = reloadTime;
	}

	public int getMaxAmmo(EntityLivingBase player) {
		if (player != null && hero.hasAltWeapon && hero.playersUsingAlt.contains(player.getPersistentID()))
			return hero.altAmmo;
		else
			return hero.mainAmmo;
	}

	public int getCurrentAmmo(EntityLivingBase player) {
		if (player instanceof EntityPlayer && currentAmmo.containsKey(player.getPersistentID())) {
			if (currentAmmo.get(player.getPersistentID()) > getMaxAmmo(player))
				currentAmmo.put(player.getPersistentID(), getMaxAmmo(player));
			return currentAmmo.get(player.getPersistentID());
		}
		else
			return getMaxAmmo(player);
	}

	public void setCurrentAmmo(EntityLivingBase player, int amount, EnumHand... hands) {
		if (!(player instanceof EntityPlayer))
			return;

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
			currentAmmo.put(player.getPersistentID(), Math.min(amount, getMaxAmmo(player)));
		}
	}

	public void subtractFromCurrentAmmo(EntityLivingBase player, int amount, EnumHand... hands) {
		if (!(player instanceof EntityPlayer))
			return;

		int ammo = getCurrentAmmo(player);
		if (ammo - amount > 0)
			this.setCurrentAmmo(player, ammo-amount, hands);
		else {
			this.setCurrentAmmo(player, 0, hands);
			reload(player);
		}
	}

	public void reload(EntityLivingBase player) {
		if (!(player instanceof EntityPlayer))
			return;

		if (player != null && !player.world.isRemote && getCurrentAmmo(player) < getMaxAmmo(player)) {
			((EntityPlayer) player).getCooldownTracker().setCooldown(this, reloadTime);
			this.setCurrentAmmo(player, 0, EnumHand.values());
			if (hero.reloadSound != null && player instanceof EntityPlayerMP)
				Minewatch.proxy.playFollowingSound(player, hero.reloadSound, SoundCategory.PLAYERS, 1.0f, 
						player.world.rand.nextFloat()/2+0.75f, false);
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
	public boolean canUse(EntityLivingBase player, boolean shouldWarn, @Nullable EnumHand hand, boolean ignoreAmmo) {
		Handler handler = TickHandler.getHandler(player, Identifier.ABILITY_USING);
		if (player == null || (player instanceof EntityPlayer && ((EntityPlayer) player).getCooldownTracker().hasCooldown(this) && !ignoreAmmo) || 
				(!ignoreAmmo && this.getMaxAmmo(player) > 0 && this.getCurrentAmmo(player) == 0) ||
				TickHandler.hasHandler(player, Identifier.PREVENT_INPUT) ||
				(handler != null && !handler.bool))
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

	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { }
	
	/**Use instead of {@link Item#onItemRightClick(World, EntityPlayer, EnumHand)} to allow EntityLivingBase*/
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		return this.onItemRightClick(world, (EntityLivingBase) player, hand);
	}

	/**Cancel swing animation when left clicking*/
	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
		return true;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		//delete dev spawned items if not in dev's inventory
		if (!world.isRemote && entity instanceof EntityPlayer && stack.hasTagCompound() &&
				stack.getTagCompound().hasKey("devSpawned") && !CommandDev.DEVS.contains(entity.getPersistentID()) &&
				((EntityPlayer)entity).inventory.getStackInSlot(slot) == stack) {
			((EntityPlayer)entity).inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
			return;
		}

		// set entity in nbt for model changer to reference
		if (this.saveEntityToNBT && entity instanceof EntityLivingBase && !entity.world.isRemote && 
				stack != null && stack.getItem() == this) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = stack.getTagCompound();
			if (!nbt.hasKey("entityLeast") || nbt.getLong("entityLeast") != (entity.getPersistentID().getLeastSignificantBits())) {
				nbt.setUniqueId("entity", entity.getPersistentID());
				stack.setTagCompound(nbt);
			}
		}

		// reloading
		if (!world.isRemote && entity instanceof EntityPlayer && (((EntityPlayer)entity).getHeldItemMainhand() == stack ||
				((EntityPlayer)entity).getHeldItemOffhand() == stack) && !TickHandler.hasHandler(entity, Identifier.PREVENT_INPUT))
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
		if (entity instanceof EntityLivingBase && Minewatch.keys.lmb((EntityLivingBase) entity)) {
			EntityLivingBase player = (EntityLivingBase) entity;
			EnumHand hand = this.getHand(player, stack);	
			if (hand != null && (!this.hasOffhand || 
					((hand == EnumHand.MAIN_HAND && player.ticksExisted % 2 == 0) ||
							(hand == EnumHand.OFF_HAND && player.ticksExisted % 2 != 0))))
				onItemLeftClick(stack, world, (EntityPlayer) entity, hand);
		}
		
		// right click - for EntityHeroes
		if (entity instanceof EntityHero && Minewatch.keys.rmb((EntityLivingBase) entity))
			this.onItemRightClick(world, (EntityLivingBase) entity, this.getHand((EntityLivingBase) entity, stack));

		// deselect ability if it has cooldown
		if (entity instanceof EntityPlayer)
			for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
				if (ability.keybind.getCooldown((EntityPlayer) entity) > 0)
					ability.toggle(entity, false);

		// set damage to full if option set to never use durability
		if (!world.isRemote && (Config.durabilityOptionWeapons == 2 || entity instanceof EntityHero) && stack.getItemDamage() != 0)
			stack.setItemDamage(0);
		// set damage to full if wearing full set and option set to not use durability while wearing full set
		else if (!world.isRemote && Config.durabilityOptionWeapons == 1 && stack.getItemDamage() != 0 && 
				SetManager.entitiesWearingSets.get(entity.getPersistentID()) == hero)
			stack.setItemDamage(0);

		// health particles
		if (this.showHealthParticles && isSelected && entity instanceof EntityPlayer && this.canUse((EntityPlayer) entity, false, EnumHand.MAIN_HAND, true) &&
				world.isRemote && entity.ticksExisted % 5 == 0) {
			AxisAlignedBB aabb = entity.getEntityBoundingBox().expandXyz(30);
			List<Entity> list = entity.world.getEntitiesWithinAABBExcludingEntity(entity, aabb);
			for (Entity entity2 : list) 
				if (entity2 instanceof EntityLivingBase && ((EntityLivingBase)entity2).getHealth() > 0 &&
						((EntityLivingBase)entity2).getHealth() < ((EntityLivingBase)entity2).getMaxHealth()/2f) {
					float size = Math.min(entity2.height, entity2.width)*9f;
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.HEALTH, world, entity2, 0xFFFFFF, 0xFFFFFF, 0.7f, Integer.MAX_VALUE, size, size, 0, 0);
				}
		}
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

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(ItemStack stack) {
		return super.hasEffect(stack); //XXX will be used with golden weapons
	}

	/**Called before armor is rendered - mainly used for coloring / alpha*/
	@SideOnly(Side.CLIENT)
	public void preRenderArmor(EntityLivingBase entity, ModelMWArmor model) {}

	/**Called before weapon is rendered*/
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType cameraTransformType, Pair<? extends IBakedModel, Matrix4f> ret) {return ret;}

	/**Set weapon model's color*/
	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		return -1;
	}

	/**Register this weapon model's variants - registers 2D and 3D basic models by default*/
	@Override
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getAllModelLocations(ArrayList<String> locs) {
		locs.add("");
		return locs;
	}

	/**defautLoc + [return] + (Config.useObjModels ? "_3d" : "")*/
	@Override
	@SideOnly(Side.CLIENT)
	public String getModelLocation(ItemStack stack, @Nullable EntityLivingBase entity) {
		return "";
	}

	@Override
	public Item getItem() {
		return this;
	}

	/**Get entity holding stack from stack's nbt*/
	@Nullable
	public static EntityLivingBase getEntity(World world, ItemStack stack) {
		if (stack != null && stack.hasTagCompound() &&
				stack.getTagCompound().getUniqueId("entity") != null) {
			UUID uuid = stack.getTagCompound().getUniqueId("entity");
			for (Entity entity : world.loadedEntityList)
				if (uuid.equals(entity.getPersistentID()) && entity instanceof EntityLivingBase)
					return (EntityLivingBase) entity;
		}
		return null;
	}

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