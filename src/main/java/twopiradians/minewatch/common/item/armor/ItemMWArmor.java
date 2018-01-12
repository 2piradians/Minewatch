package twopiradians.minewatch.common.item.armor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.display.EntityGuiPlayer;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;

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
			if (skin < 0 || skin >= hero.skinInfo.length)
				skin = 0;
			return Minewatch.MODID+":textures/models/armor/"+hero.name.toLowerCase()+"_"+skin+"_layer_"+
			(slot == EntityEquipmentSlot.LEGS ? 2 : 1)+".png";
	}

	

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("devSpawned"))
			tooltip.add(TextFormatting.DARK_PURPLE+""+TextFormatting.BOLD+"Dev Spawned");
		super.addInformation(stack, player, tooltip, advanced);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldObj, Entity entity, int slot, boolean isSelected) {	
		// delete dev spawned items if not in dev's inventory and delete disabled items (except missingTexture items in SMP)
		if (!worldObj.isRemote && entity instanceof EntityPlayer && stack.hasTagCompound() &&
				stack.getTagCompound().hasKey("devSpawned") && !CommandDev.DEVS.contains(entity.getPersistentID()) &&
				((EntityPlayer)entity).inventory.getStackInSlot(slot) == stack) {
			((EntityPlayer)entity).inventory.setInventorySlotContents(slot, null);
			return;
		}

		// set damage to full if option set to never use durability
		if (Config.durabilityOptionArmors == 2 && stack.getItemDamage() != 0)
			stack.setItemDamage(0);

		super.onUpdate(stack, worldObj, entity, slot, isSelected);
	}

	/**Delete dev spawned dropped items*/
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		// delete dev spawned items if not worn by dev and delete disabled items (except missingTexture items in SMP)
		if (!entityItem.worldObj.isRemote && entityItem != null && entityItem.getEntityItem() != null && 
				entityItem.getEntityItem().hasTagCompound() && 
				entityItem.getEntityItem().getTagCompound().hasKey("devSpawned")) {
			entityItem.setDead();
			return true;
		}
		return false;
	}

	@Override
	public void onArmorTick(World worldObj, EntityPlayer player, ItemStack stack) {	
		this.onArmorTick(worldObj, (EntityLivingBase) player, stack);
	}

	/**Handles most of the armor set special effects and bonuses.*/
	public void onArmorTick(World worldObj, EntityLivingBase player, ItemStack stack) {	
		EnumHero set = SetManager.getWornSet(player);

		// delete dev spawned items if not worn by dev
		if (stack == null || (!worldObj.isRemote && stack.hasTagCompound() && 
				stack.getTagCompound().hasKey("devSpawned") && 
				!CommandDev.DEVS.contains(player.getPersistentID()) && 
				player.getItemStackFromSlot(this.armorType) == stack)) {
			player.setItemStackToSlot(this.armorType, null);
			return;
		}

		// genji jump boost/double jump
		if (this.armorType == EntityEquipmentSlot.CHEST && player != null && 
				set == EnumHero.GENJI) {
			// jump boost
			if (!worldObj.isRemote && (player.getActivePotionEffect(MobEffects.JUMP_BOOST) == null || 
					player.getActivePotionEffect(MobEffects.JUMP_BOOST).getDuration() == 0))
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 10, 0, true, false));
			// double jump
			else if (worldObj.isRemote && (player.onGround || player.isInWater() || player.isInLava()))
				playersJumped.remove(player);
			else if (KeyBind.JUMP.isKeyPressed(player) && !player.onGround && !player.isOnLadder() && 
					player.motionY < 0.2d && !playersJumped.contains(player)) {
				if (worldObj.isRemote) {
					if (player instanceof EntityPlayer)
						((EntityPlayer)player).jump();
					else if (player instanceof EntityHero)
						((EntityHero)player).jump();
					player.motionY += 0.2d;
					playersJumped.add(player);
					ModSoundEvents.GENJI_JUMP.playSound(player, 0.8f, worldObj.rand.nextFloat()/6f+0.9f);
				}
				player.fallDistance = 0;
			}
		}

		// genji/hanzo wall climb
		if (this.armorType == EntityEquipmentSlot.CHEST && player != null && 
				(set == EnumHero.GENJI || set == EnumHero.HANZO) && worldObj.isRemote == player instanceof EntityPlayer) {
			// reset climbing
			BlockPos pos = new BlockPos(player.posX, player.getEntityBoundingBox().minY, player.posZ);
			if ((player instanceof EntityPlayer && player.onGround) || (worldObj.isAirBlock(pos.offset(player.getHorizontalFacing())) &&
					worldObj.isAirBlock(pos.up().offset(player.getHorizontalFacing()))) || player.isInWater() || player.isInLava()) {
				playersClimbing.remove(player);
			}
			else if (player.isCollidedHorizontally && 
					!(player instanceof EntityPlayer && ((EntityPlayer)player).capabilities.isFlying) && 
					KeyBind.JUMP.isKeyDown(player)) {
				int ticks = playersClimbing.containsKey(player) ? playersClimbing.get(player)+1 : 1;
				if (ticks <= 17) {
					if (ticks % 4 == 0 || ticks == 1) { // reset fall distance and play sound
						if (worldObj.isRemote)
							Minewatch.network.sendToServer(new CPacketSimple(0, player, false));
						else 
							ModSoundEvents.WALL_CLIMB.playSound(player, 0.9f, 1);
						player.fallDistance = 0.0F;
					}
					player.motionX = MathHelper.clamp_double(player.motionX, -0.15D, 0.15D);
					player.motionZ = MathHelper.clamp_double(player.motionZ, -0.15D, 0.15D);
					player.motionY = Math.max(0.2d, player.motionY);
					player.moveEntity(player.motionX, player.motionY, player.motionZ);
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
			!worldObj.isRemote && (player.getActivePotionEffect(MobEffects.REGENERATION) == null || 
			player.getActivePotionEffect(MobEffects.REGENERATION).getDuration() == 0))
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 0, true, false));
			else if (KeyBind.JUMP.isKeyDown(player) && player.motionY <= -0.09d && !player.isInWater() && !player.isInLava()) {
				player.motionY = Math.min(player.motionY*0.75f, -0.1f);
				player.fallDistance = Math.max(player.fallDistance*0.75f, 1);
				if (!playersHovering.contains(player) && !worldObj.isRemote) {
					ModSoundEvents.MERCY_HOVER.playSound(player, 0.2f, 1);
					playersHovering.add(player);
				}
				if (worldObj.isRemote)
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, worldObj, 
							player.posX+worldObj.rand.nextFloat()-0.5f, player.posY+(player.getEyeHeight()-player.height/2f), player.posZ+worldObj.rand.nextFloat()-0.5f, 
							0, 0, 0, 0xFFFAC3, 0xC1C090, 0.8f, 10, 1f+worldObj.rand.nextFloat(), 0.3f, worldObj.rand.nextFloat(), worldObj.rand.nextFloat());
			}
			else if (playersHovering.contains(player)) 
				playersHovering.remove(player);

		// tracer chestplate particles
		double x = player instanceof EntityPlayer ? ((EntityPlayer)player).chasingPosX : player.prevPosX;
		double y = player instanceof EntityPlayer ? ((EntityPlayer)player).chasingPosY : player.prevPosY;
		double z = player instanceof EntityPlayer ? ((EntityPlayer)player).chasingPosZ : player.prevPosZ;
		if (this.armorType == EntityEquipmentSlot.CHEST &&
				set == EnumHero.TRACER && worldObj.isRemote && 
				(x != 0 || y != 0 || z != 0)) {
			int numParticles = (int) ((Math.abs(x-player.posX)+Math.abs(y-player.posY)+Math.abs(z-player.posZ))*10d);
			for (int i=0; i<numParticles; ++i)
				Minewatch.proxy.spawnParticlesTrail(player.worldObj, 
						player.posX+(x-player.posX)*i/numParticles, 
						player.posY+(y-player.posY)*i/numParticles+player.height/2+0.3f, 
						player.posZ+(z-player.posZ)*i/numParticles, 
						0, 0, 0, 0x5EDCE5, 0x007acc, 1, 7, 0, 1);
		}

		// set damage to full if wearing full set and option set to not use durability while wearing full set
		if (!worldObj.isRemote && (Config.durabilityOptionArmors == 1 || player instanceof EntityHero) && 
				stack.getItemDamage() != 0 && set == hero)
			stack.setItemDamage(0);
	}

}
