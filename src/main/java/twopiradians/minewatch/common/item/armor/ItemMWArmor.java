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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
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
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityJunkratGrenade;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;
import twopiradians.minewatch.packet.SPacketSimple;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;

public class ItemMWArmor extends ItemArmor {

	public EnumHero hero;
	@SideOnly(Side.CLIENT)
	public static ModelMWArmor maleModel;
	@SideOnly(Side.CLIENT)
	public static ModelMWArmor femaleModel;
	private ArrayList<EntityLivingBase> playersJumped = new ArrayList<EntityLivingBase>(); // Genji double jump
	private ArrayList<EntityLivingBase> playersHovering = new ArrayList<EntityLivingBase>(); // Mercy hover
	private HashMap<EntityLivingBase, Integer> playersClimbing = Maps.newHashMap(); // Genji/Hanzo climb
	public static ArrayList<Class> classesWithArmor = new ArrayList<Class>();

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
		// keep track of classes with armor
		if (entity != null && !classesWithArmor.contains(entity.getClass()))
			classesWithArmor.add(entity.getClass());
		// create models if null
		if (maleModel == null || femaleModel == null) {
			maleModel = new ModelMWArmor(0, false);
			femaleModel = new ModelMWArmor(0, true);
		}
		ModelMWArmor ret = hero.smallArms && (entity instanceof AbstractClientPlayer || entity instanceof EntityHero) ? femaleModel : maleModel;
		// set arms to be visible after rendering (so held items are rendered in the correct places)
		if (entity instanceof EntityLivingBase && 
				((RenderLivingBase)Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity)).getMainModel() instanceof ModelBiped) {
			ModelBiped model = (ModelBiped) ((RenderLivingBase)Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity)).getMainModel();
			model.bipedRightArm.showModel = true;
			model.bipedLeftArm.showModel = true;
		}
		else if (entity instanceof EntityArmorStand) {
			entity.rotationYawHead = entity.prevRenderYawOffset; // rotate head properly
			entity.ticksExisted = 5; // prevent arm swinging
		}
		return ret;
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

		int skin = entity instanceof EntityHero ? entity.getDataManager().get(EntityHero.SKIN) : 
			entity instanceof EntityGuiPlayer ? ((EntityGuiPlayer)entity).skin : 
			hero.getSkin(entity.getPersistentID());
		return Minewatch.MODID+":textures/models/armor/"+hero.name.toLowerCase()+"_"+skin+"_layer_"+
		(slot == EntityEquipmentSlot.LEGS ? 2 : 1)+".png";
	}

	@Mod.EventBusSubscriber
	public static class SetManager {
		/**List of players wearing full sets and the sets that they are wearing*/
		private static HashMap<UUID, EnumHero> entitiesWearingSets = Maps.newHashMap();	

		/**List of players' last known full sets worn (for knowing when to reset cooldowns)*/
		private static HashMap<UUID, EnumHero> lastWornSets = Maps.newHashMap();

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

		@Nullable
		public static EnumHero getWornSet(Entity entity) {
			return entity == null ? null : 
				entity instanceof EntityHero ? ((EntityHero)entity).hero : 
					getWornSet(entity.getPersistentID());
		}

		@Nullable
		public static EnumHero getWornSet(UUID uuid) {
			return entitiesWearingSets.get(uuid);
		}

		/**Clear cooldowns of players respawning*/
		@SubscribeEvent
		public static void resetCooldowns(PlayerRespawnEvent event) {
			for (KeyBind key : Keys.KeyBind.values()) 
				if (key.getCooldown(event.player) > 0)
					key.setCooldown(event.player, 0, false);
		}

		/**Update entitiesWearingSets each tick
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
						(fullSet && (SetManager.getWornSet(event.player) == null ||
								SetManager.getWornSet(event.player) != hero)))
					for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
						ability.toggle(event.player, false);

				// update entitiesWearingSets
				if (fullSet) {
					SetManager.entitiesWearingSets.put(event.player.getPersistentID(), hero);
					if (SetManager.lastWornSets.get(event.player.getPersistentID()) != hero) {
						for (KeyBind key : Keys.KeyBind.values()) 
							if (key.getCooldown(event.player) > 0)
								key.setCooldown(event.player, 0, true);
						SetManager.lastWornSets.put(event.player.getPersistentID(), hero);
					}
				}
				else
					SetManager.entitiesWearingSets.remove(event.player.getPersistentID());
			}
		}

		@SubscribeEvent
		public static void preventFallDamage(LivingFallEvent event) {
			// prevent fall damage if enabled in config and wearing set
			if (Config.preventFallDamage && event.getEntity() != null &&
					SetManager.getWornSet(event.getEntity()) != null)
				event.setCanceled(true);
			// genji fall
			else if (event.getEntity() != null && 
					SetManager.getWornSet(event.getEntity()) == EnumHero.GENJI) 
				event.setDistance(event.getDistance()*0.8f);
		}

		@SubscribeEvent
		public static void junkratDeath(LivingDeathEvent event) {
			if (event.getEntity() instanceof EntityLivingBase && !event.getEntity().world.isRemote &&
					SetManager.getWornSet(event.getEntity()) == EnumHero.JUNKRAT) {
				ModSoundEvents.JUNKRAT_DEATH.playSound(event.getEntity(), 1, 1);
				for (int i=0; i<6; ++i) {
					EntityJunkratGrenade grenade = new EntityJunkratGrenade(event.getEntity().world, 
							(EntityLivingBase) event.getEntity(), -1);
					grenade.explodeTimer = 20+i*2;
					grenade.setPosition(event.getEntity().posX, event.getEntity().posY+event.getEntity().height/2d, event.getEntity().posZ);
					grenade.motionX = (event.getEntity().world.rand.nextDouble()-0.5d)*0.1d;
					grenade.motionY = (event.getEntity().world.rand.nextDouble()-0.5d)*0.1d;
					grenade.motionZ = (event.getEntity().world.rand.nextDouble()-0.5d)*0.1d;
					event.getEntity().world.spawnEntity(grenade);
					grenade.isDeathGrenade = true;
					Minewatch.network.sendToAll(new SPacketSimple(24, grenade, false, grenade.explodeTimer, 0, 0));
				}
			}
		}
	}

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

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {	
		this.onArmorTick(world, (EntityLivingBase) player, stack);
	}

	/**Handles most of the armor set special effects and bonuses.*/
	public void onArmorTick(World world, EntityLivingBase player, ItemStack stack) {	
		EnumHero set = SetManager.getWornSet(player);

		// delete dev spawned items if not worn by dev
		if (stack.isEmpty() || (!world.isRemote && stack.hasTagCompound() && 
				stack.getTagCompound().hasKey("devSpawned") && 
				!CommandDev.DEVS.contains(player.getPersistentID()) && 
				player.getItemStackFromSlot(this.armorType) == stack)) {
			player.setItemStackToSlot(this.armorType, ItemStack.EMPTY);
			return;
		}

		// genji jump boost/double jump
		if (this.armorType == EntityEquipmentSlot.CHEST && player != null && 
				set == EnumHero.GENJI) {
			// jump boost
			if (!world.isRemote && (player.getActivePotionEffect(MobEffects.JUMP_BOOST) == null || 
					player.getActivePotionEffect(MobEffects.JUMP_BOOST).getDuration() == 0))
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 10, 0, true, false));
			// double jump
			else if (world.isRemote && (player.onGround || player.isInWater() || player.isInLava()))
				playersJumped.remove(player);
			else if (KeyBind.JUMP.isKeyDown(player) && !player.onGround && !player.isOnLadder() && 
					player.motionY < 0.0d && !playersJumped.contains(player)) {
				if (world.isRemote) {
					if (player instanceof EntityPlayer)
						((EntityPlayer)player).jump();
					else if (player instanceof EntityHero)
						((EntityHero)player).jump();
					player.motionY += 0.2d;
					playersJumped.add(player);
					ModSoundEvents.GENJI_JUMP.playSound(player, 0.8f, world.rand.nextFloat()/6f+0.9f);
				}
				player.fallDistance = 0;
			}
		}

		// genji/hanzo wall climb
		if (this.armorType == EntityEquipmentSlot.CHEST && player != null && 
				(set == EnumHero.GENJI || set == EnumHero.HANZO) && world.isRemote == player instanceof EntityPlayer) {
			// reset climbing
			BlockPos pos = new BlockPos(player.posX, player.getEntityBoundingBox().minY, player.posZ);
			if ((player instanceof EntityPlayer && player.onGround) || (world.isAirBlock(pos.offset(player.getHorizontalFacing())) &&
					world.isAirBlock(pos.up().offset(player.getHorizontalFacing()))) || player.isInWater() || player.isInLava()) {
				playersClimbing.remove(player);
			}
			else if (player.isCollidedHorizontally && 
					!(player instanceof EntityPlayer && ((EntityPlayer)player).capabilities.isFlying) && 
					KeyBind.JUMP.isKeyDown(player)) {
				int ticks = playersClimbing.containsKey(player) ? playersClimbing.get(player)+1 : 1;
				if (ticks <= 17) {
					if (ticks % 4 == 0 || ticks == 1) { // reset fall distance and play sound
						if (world.isRemote)
							Minewatch.network.sendToServer(new CPacketSimple(0, player, false));
						else 
							ModSoundEvents.WALL_CLIMB.playSound(player, 0.9f, 1);
						player.fallDistance = 0.0F;
					}
					player.motionX = MathHelper.clamp(player.motionX, -0.15D, 0.15D);
					player.motionZ = MathHelper.clamp(player.motionZ, -0.15D, 0.15D);
					player.motionY = Math.max(0.2d, player.motionY);
					player.move(MoverType.SELF, player.motionX, player.motionY, player.motionZ);
					if (player instanceof EntityHero) {
						Vec3d vec = player.getPositionVector().add(new Vec3d(player.getHorizontalFacing().getDirectionVec()));
						((EntityHero) player).getLookHelper().setLookPosition(vec.xCoord, vec.yCoord, vec.zCoord, 30, 30);
					}
					playersClimbing.put(player, ticks);
				}
			}
		}
		// mercy's regen/slow fall
		if (this.armorType == EntityEquipmentSlot.CHEST && player != null && set == EnumHero.MERCY) 
			if (TickHandler.getHandler(player, Identifier.MERCY_NOT_REGENING) == null &&
			!world.isRemote && (player.getActivePotionEffect(MobEffects.REGENERATION) == null || 
			player.getActivePotionEffect(MobEffects.REGENERATION).getDuration() == 0))
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 0, true, false));
			else if (KeyBind.JUMP.isKeyDown(player) && player.motionY < 0 && !player.isInWater() && !player.isInLava()) {
				player.motionY = Math.min(player.motionY*0.75f, -0.1f);
				player.fallDistance = Math.max(player.fallDistance*0.75f, 1);
				if (!playersHovering.contains(player) && !world.isRemote) {
					ModSoundEvents.MERCY_HOVER.playSound(player, 0.2f, 1);
					playersHovering.add(player);
				}
				if (world.isRemote)
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, 
							player.posX+world.rand.nextFloat()-0.5f, player.posY+(player.getEyeHeight()-player.height/2f), player.posZ+world.rand.nextFloat()-0.5f, 
							0, 0, 0, 0xFFFAC3, 0xC1C090, 0.8f, 10, 1f+world.rand.nextFloat(), 0.3f, world.rand.nextFloat(), world.rand.nextFloat());
			}
			else if (playersHovering.contains(player)) 
				playersHovering.remove(player);

		// tracer chestplate particles
		double x = player instanceof EntityPlayer ? ((EntityPlayer)player).chasingPosX : player.prevPosX;
		double y = player instanceof EntityPlayer ? ((EntityPlayer)player).chasingPosY : player.prevPosY;
		double z = player instanceof EntityPlayer ? ((EntityPlayer)player).chasingPosZ : player.prevPosZ;
		if (this.armorType == EntityEquipmentSlot.CHEST &&
				set == EnumHero.TRACER && world.isRemote && 
				(x != 0 || y != 0 || z != 0)) {
			int numParticles = (int) ((Math.abs(x-player.posX)+Math.abs(y-player.posY)+Math.abs(z-player.posZ))*10d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(player.world, 
						player.posX+(x-player.posX)*i/numParticles, 
						player.posY+(y-player.posY)*i/numParticles+player.height/2+0.3f, 
						player.posZ+(z-player.posZ)*i/numParticles, 
						0, 0, 0, 0x5EDCE5, 0x007acc, 1, 7, 0, 1);
		}

		// set damage to full if wearing full set and option set to not use durability while wearing full set
		if (!world.isRemote && (Config.durabilityOptionArmors == 1 || player instanceof EntityHero) && 
				stack.getItemDamage() != 0 && set == hero)
			stack.setItemDamage(0);
	}

}
