package twopiradians.minewatch.common.item.armor;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;

public class ItemMWArmor extends ItemArmor 
{
	public EnumHero hero;

	public static final EntityEquipmentSlot[] SLOTS = new EntityEquipmentSlot[] 
			{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

	public ItemMWArmor(EnumHero hero, ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.hero = hero;
	}

	@Override
	@Nullable
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return Minewatch.MODID+":textures/models/armor/"+hero.name.toLowerCase()+"_"+hero.textureVariation+"_layer_"+
				(slot == EntityEquipmentSlot.LEGS ? 2 : 1)+".png";    
	}

	@Mod.EventBusSubscriber
	public static class SetManager {
		/**List of players wearing full sets and the sets that they are wearing*/
		public static HashMap<UUID, EnumHero> playersWearingSets = Maps.newHashMap();	

		/**List of players' last known full sets worn (for knowing when to reset cooldowns)*/
		public static HashMap<UUID, EnumHero> lastWornSets = Maps.newHashMap();

		/**Clear cooldowns of players logging in (for when switching worlds)*/
		@SubscribeEvent
		public static void resetCooldowns(PlayerLoggedInEvent event) {
			for (KeyBind key : Keys.KeyBind.values()) 
				if (key.getCooldown(event.player) > 0)
					key.setCooldown(event.player, 0, false);
			for (EnumHero hero : EnumHero.values())
				for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3}) 
					if (ability.multiAbilityUses.remove(event.player.getPersistentID()) != null &&
					event.player instanceof EntityPlayerMP) {
						Minewatch.network.sendTo(
								new SPacketSyncAbilityUses(event.player.getPersistentID(), hero, ability.getNumber(), 
										ability.maxUses, false), (EntityPlayerMP) event.player);
					}
		}

		/**Clear cooldowns of players respawning*/
		@SubscribeEvent
		public static void resetCooldowns(PlayerRespawnEvent event) {
			for (KeyBind key : Keys.KeyBind.values()) 
				if (key.getCooldown(event.player) > 0)
					key.setCooldown(event.player, 0, false);
		}

		/**Update playersWearingSets each tick
		 * This way it's only checked once per tick, no matter what:
		 * very useful for checking if HUDs should be rendered*/
		@SubscribeEvent
		public static void updateSets(TickEvent.PlayerTickEvent event) {			
			if (event.phase == TickEvent.Phase.START) {
				//detect if player is wearing a set
				ItemStack helm = event.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
				EnumHero hero = null;
				boolean fullSet = helm != null && helm.getItem() instanceof ItemMWArmor;
				if (fullSet) {
					hero = ((ItemMWArmor)helm.getItem()).hero;
					for (EntityEquipmentSlot slot : SLOTS) {
						ItemStack armor = event.player.getItemStackFromSlot(slot);
						if (armor == null || !(armor.getItem() instanceof ItemMWArmor)
								|| ((ItemMWArmor)(armor.getItem())).hero != hero) 
							fullSet = false;
					}
				}

				// clear toggles when switching to set or if not holding weapon
				if (hero != null && (event.player.getHeldItemMainhand() == null || 
						event.player.getHeldItemMainhand().getItem() != hero.weapon) || 
						(fullSet && (!SetManager.playersWearingSets.containsKey(event.player.getPersistentID()) ||
								SetManager.playersWearingSets.get(event.player.getPersistentID()) != hero)))
					for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
						ability.toggled.remove(event.player.getPersistentID());

				// update playersWearingSets
				if (fullSet) {
					SetManager.playersWearingSets.put(event.player.getPersistentID(), hero);
					if (SetManager.lastWornSets.get(event.player.getPersistentID()) != hero) {
						for (KeyBind key : Keys.KeyBind.values()) 
							if (key.getCooldown(event.player) > 0)
								key.setCooldown(event.player, 0, false);
						SetManager.lastWornSets.put(event.player.getPersistentID(), hero);
					}
				}
				else
					SetManager.playersWearingSets.remove(event.player.getPersistentID());
			}
		}
	}

	// DEV SPAWN ARMOR ===============================================

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("devSpawned"))
			tooltip.add(TextFormatting.DARK_PURPLE+""+TextFormatting.BOLD+"Dev Spawned");
		super.addInformation(stack, player, tooltip, advanced);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		//delete dev spawned items if not in dev's inventory and delete disabled items (except missingTexture items in SMP)
		if (!world.isRemote && entity instanceof EntityPlayer && stack.hasTagCompound() &&
				stack.getTagCompound().hasKey("devSpawned") && !CommandDev.DEVS.contains(entity.getPersistentID()) &&
				((EntityPlayer)entity).inventory.getStackInSlot(slot) == stack) {
			((EntityPlayer)entity).inventory.setInventorySlotContents(slot, null);
			return;
		}
		
		// set damage to full if option set to never use durability
		if (Config.durabilityOptionArmors == 2 && stack.getItemDamage() != 0)
			stack.setItemDamage(0);
		
		super.onUpdate(stack, world, entity, slot, isSelected);
	}

	/**Delete dev spawned dropped items*/
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		//delete dev spawned items if not worn by dev and delete disabled items (except missingTexture items in SMP)
		if (!entityItem.worldObj.isRemote && entityItem != null && entityItem.getEntityItem() != null && 
				entityItem.getEntityItem().hasTagCompound() && 
				entityItem.getEntityItem().getTagCompound().hasKey("devSpawned")) {
			entityItem.setDead();
			return true;
		}
		return false;
	}

	/**Handles most of the armor set special effects and bonuses.*/
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {		
		//delete dev spawned items if not worn by dev and delete disabled items (except missingTexture items in SMP)
		if (stack == null || (!world.isRemote && stack.hasTagCompound() && 
				stack.getTagCompound().hasKey("devSpawned") && 
				!CommandDev.DEVS.contains(player.getPersistentID()) && 
				player.getItemStackFromSlot(this.armorType) == stack)) {
			player.setItemStackToSlot(this.armorType, null);
			return;
		}

		// tracer chestplate particles
		if (this.armorType == EntityEquipmentSlot.CHEST && 
				hero == EnumHero.TRACER && world.isRemote && player != null && 
				(player.chasingPosX != 0 || player.chasingPosY != 0 || player.chasingPosZ != 0)) {
			int numParticles = (int) ((Math.abs(player.chasingPosX-player.posX)+Math.abs(player.chasingPosY-player.posY)+Math.abs(player.chasingPosZ-player.posZ))*5d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(player.worldObj, 
						player.posX+(player.chasingPosX-player.posX)*i/numParticles, 
						player.posY+(player.chasingPosY-player.posY)*i/numParticles+player.height/2+0.3f, 
						player.posZ+(player.chasingPosZ-player.posZ)*i/numParticles, 
						0, 0, 0, 0x5EDCE5, 0x007acc, 1, 7, 1);
		}
		
		// set damage to full if wearing full set and option set to not use durability while wearing full set
		if (!world.isRemote && Config.durabilityOptionArmors == 1 && stack.getItemDamage() != 0 && 
				SetManager.playersWearingSets.get(player.getPersistentID()) == hero)
			stack.setItemDamage(0);
	}

}
