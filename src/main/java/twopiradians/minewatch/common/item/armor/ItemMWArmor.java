package twopiradians.minewatch.common.item.armor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.display.EntityGuiPlayer;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;

public class ItemMWArmor extends ItemArmor 
{
	public EnumHero hero;
	@SideOnly(Side.CLIENT)
	private ModelPlayer maleModel;
	@SideOnly(Side.CLIENT)
	private ModelPlayer femaleModel;
	private ArrayList<EntityPlayer> playersJumped = new ArrayList<EntityPlayer>(); // Genji double jump
	private ArrayList<EntityPlayer> playersHovering = new ArrayList<EntityPlayer>(); // Mercy hover
	private HashMap<EntityPlayer, Integer> playersClimbing = Maps.newHashMap(); // Genji/Hanzo climb

	public static final EntityEquipmentSlot[] SLOTS = new EntityEquipmentSlot[] 
			{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

	public ItemMWArmor(EnumHero hero, ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.hero = hero;
	}

	@Override
	@Nullable
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
		// set arms to be visible after rendering (so held items are rendered in the correct places)
		if (entity instanceof AbstractClientPlayer) {
			ModelPlayer model = (ModelPlayer) ((RenderLivingBase)Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity)).getMainModel();
			model.bipedRightArm.showModel = true;
			model.bipedLeftArm.showModel = true;
		}
		else if (entity instanceof EntityArmorStand) {
			entity.rotationYawHead = entity.prevRenderYawOffset; // rotate head properly
			entity.ticksExisted = 5; // prevent arm swinging
		}
		if (maleModel == null || femaleModel == null) {
			maleModel = new ModelMWArmor(0, false);
			femaleModel = new ModelMWArmor(0, true);
		}
		return hero.smallArms ? femaleModel : maleModel;
	}

	@Override
	@Nullable
	@SideOnly(Side.CLIENT)
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		// show layers
		if (slot == EntityEquipmentSlot.CHEST) {
			(hero.smallArms ? femaleModel : maleModel).bipedBodyWear.showModel = true;
			(hero.smallArms ? femaleModel : maleModel).bipedLeftArmwear.showModel = true;
			(hero.smallArms ? femaleModel : maleModel).bipedRightArmwear.showModel = true;
		}
		else if (slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET) {
			(hero.smallArms ? femaleModel : maleModel).bipedLeftLegwear.showModel = true;
			(hero.smallArms ? femaleModel : maleModel).bipedRightLegwear.showModel = true;
		}

		int skin = entity instanceof EntityGuiPlayer ? ((EntityGuiPlayer)entity).skin : 
			hero.getSkin(entity.getPersistentID());
		return Minewatch.MODID+":textures/models/armor/"+hero.name.toLowerCase()+"_"+skin+"_layer_"+
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
						ability.toggle(event.player, false);

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
		// delete dev spawned items if not in dev's inventory and delete disabled items (except missingTexture items in SMP)
		if (!world.isRemote && entity instanceof EntityPlayer && stack.hasTagCompound() &&
				stack.getTagCompound().hasKey("devSpawned") && !CommandDev.DEVS.contains(entity.getPersistentID()) &&
				((EntityPlayer)entity).inventory.getStackInSlot(slot) == stack) {
			((EntityPlayer)entity).inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
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
		// delete dev spawned items if not worn by dev and delete disabled items (except missingTexture items in SMP)
		if (!entityItem.world.isRemote && entityItem != null && entityItem.getEntityItem() != null && 
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
		//delete dev spawned items if not worn by dev
		if (stack.isEmpty() || (!world.isRemote && stack.hasTagCompound() && 
				stack.getTagCompound().hasKey("devSpawned") && 
				!CommandDev.DEVS.contains(player.getPersistentID()) && 
				player.getItemStackFromSlot(this.armorType) == stack)) {
			player.setItemStackToSlot(this.armorType, ItemStack.EMPTY);
			return;
		}

		// genji jump boost/double jump
		if (this.armorType == EntityEquipmentSlot.CHEST && player != null && 
				SetManager.playersWearingSets.get(player.getPersistentID()) == EnumHero.GENJI) {
			// jump boost
			if (!world.isRemote && (player.getActivePotionEffect(MobEffects.JUMP_BOOST) == null || 
					player.getActivePotionEffect(MobEffects.JUMP_BOOST).getDuration() == 0))
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 10, 0, true, false));
			// double jump
			else if (world.isRemote && player.onGround)
				playersJumped.remove(player);
			else if (Minewatch.keys.jump(player) && !player.onGround && !player.isOnLadder() && 
					player.motionY < 0.0d && !playersJumped.contains(player)) {
				if (world.isRemote) {
					player.jump();
					player.motionY += 0.2d;
					playersJumped.add(player);
					player.playSound(ModSoundEvents.genjiJump, 0.8f, world.rand.nextFloat()/6f+0.9f);
				}
				player.fallDistance = 0;
			}
		}

		// genji/hanzo wall climb
		if (this.armorType == EntityEquipmentSlot.CHEST && player != null && 
				(SetManager.playersWearingSets.get(player.getPersistentID()) == EnumHero.GENJI ||
				SetManager.playersWearingSets.get(player.getPersistentID()) == EnumHero.HANZO) && world.isRemote) {
			// reset climbing
			BlockPos pos = new BlockPos(player.posX, player.getEntityBoundingBox().minY, player.posZ);
			if (player.onGround || (world.isAirBlock(pos.offset(player.getHorizontalFacing())) &&
					world.isAirBlock(pos.up().offset(player.getHorizontalFacing()))))
				playersClimbing.remove(player);
			else if (player.isCollidedHorizontally && !player.capabilities.isFlying && Minewatch.keys.jump(player)) {
				int ticks = playersClimbing.containsKey(player) ? playersClimbing.get(player)+1 : 1;
				if (ticks <= 17) {
					if (ticks % 4 == 0) { // reset fall distance and play sound
						Minewatch.network.sendToServer(new CPacketSimple(0, player));
						player.fallDistance = 0.0F;
					}
					player.motionX = MathHelper.clamp(player.motionX, -0.15D, 0.15D);
					player.motionZ = MathHelper.clamp(player.motionZ, -0.15D, 0.15D);
					player.motionY = Math.max(0.2d, player.motionY);
					player.move(MoverType.SELF, player.motionX, player.motionY, player.motionZ);
					playersClimbing.put(player, ticks);
				}
			}
		}

		// mercy's regen/slow fall
		if (this.armorType == EntityEquipmentSlot.CHEST && player != null && 
				SetManager.playersWearingSets.get(player.getPersistentID()) == EnumHero.MERCY) 
			if (TickHandler.getHandler(player, Identifier.MERCY_NOT_REGENING) == null &&
			!world.isRemote && (player.getActivePotionEffect(MobEffects.REGENERATION) == null || 
			player.getActivePotionEffect(MobEffects.REGENERATION).getDuration() == 0))
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 0, true, false));
			else if (Minewatch.keys.jump(player) && player.motionY < 0) {
				player.motionY *= 0.75f;
				player.fallDistance *= 0.75f;
				if (!playersHovering.contains(player) && !world.isRemote) {
					world.playSound(null, player.posX, player.posY, player.posZ, 
							ModSoundEvents.mercyHover, SoundCategory.PLAYERS, 0.2f, 1.0f);
					playersHovering.add(player);
				}
			}
			else if (playersHovering.contains(player))
				playersHovering.remove(player);

		// tracer chestplate particles
		if (this.armorType == EntityEquipmentSlot.CHEST && 
				hero == EnumHero.TRACER && world.isRemote && player != null && 
				(player.chasingPosX != 0 || player.chasingPosY != 0 || player.chasingPosZ != 0)) {
			int numParticles = (int) ((Math.abs(player.chasingPosX-player.posX)+Math.abs(player.chasingPosY-player.posY)+Math.abs(player.chasingPosZ-player.posZ))*10d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(player.world, 
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
