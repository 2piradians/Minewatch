package twopiradians.minewatch.common.item.armor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
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
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.HealthManager;
import twopiradians.minewatch.common.hero.RankManager;
import twopiradians.minewatch.common.hero.RankManager.Rank;
import twopiradians.minewatch.common.hero.RenderManager;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;

public class ItemMWArmor extends ItemArmor {

	protected static final UUID HEALTH_MODIFIER_HEAD = UUID.fromString("CB3F4AD3-645C-4F88-A497-9C13A23DB5CF");
	protected static final UUID HEALTH_MODIFIER_CHEST = UUID.fromString("DB3F4AD3-645C-4F88-A497-9C13A23DB5CF");
	protected static final UUID HEALTH_MODIFIER_LEGS = UUID.fromString("EB3F4AD3-645C-4F88-A497-9C13A23DB5CF");
	protected static final UUID HEALTH_MODIFIER_FEET = UUID.fromString("FB3F4AD3-645C-4F88-A497-9C13A23DB5CF");
	
	// copied from ItemArmor
    private static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};

	public EnumHero hero;
	@SideOnly(Side.CLIENT)
	private static ModelMWArmor maleModel;
	@SideOnly(Side.CLIENT)
	private static ModelMWArmor femaleModel;
	private ArrayList<EntityLivingBase> playersJumped = new ArrayList<EntityLivingBase>(); // Genji double jump
	private ArrayList<EntityLivingBase> playersHovering = new ArrayList<EntityLivingBase>(); // Mercy hover
	private HashMap<EntityLivingBase, Integer> playersClimbing = Maps.newHashMap(); // Genji/Hanzo climb
	public static ArrayList<Class> classesWithArmor = new ArrayList<Class>();

	public static final EntityEquipmentSlot[] SLOTS = new EntityEquipmentSlot[] 
			{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

	public ItemMWArmor(EnumHero hero, ArmorMaterial material, int renderIndexIn, EntityEquipmentSlot slot) {
		super(material, renderIndexIn, slot);
		this.hero = hero;
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> map = super.getAttributeModifiers(slot, stack);

		double health = (HealthManager.getTotalBaseHealth(hero)-200)/40d;
		if (slot == this.armorType) {
			UUID uuid = null;
			switch (slot) {
			case CHEST:
				uuid = HEALTH_MODIFIER_CHEST;
				break;
			case FEET:
				uuid = HEALTH_MODIFIER_FEET;
				break;
			case HEAD:
				uuid = HEALTH_MODIFIER_HEAD;
				break;
			case LEGS:
				uuid = HEALTH_MODIFIER_LEGS;
				break;
			}
			if (uuid != null) 
				map.put(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier(uuid, "Health modifier", health, 0));
			
			// override armor with config value
			map.removeAll(SharedMonsterAttributes.ARMOR.getName());
			map.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], "Armor modifier", Config.armor, 0));
		}

		return map;
	}

	/**Gets the model for an entity (either male or female model) - creates the models if null*/
	@SideOnly(Side.CLIENT)
	public ModelMWArmor getModel(Entity entity) {
		// create models if null
		if (maleModel == null || femaleModel == null) {
			maleModel = new ModelMWArmor(0, false);
			femaleModel = new ModelMWArmor(0, true);
		}
		return hero.smallArms && (entity instanceof AbstractClientPlayer || entity instanceof EntityHero || entity instanceof EntityArmorStand) ? femaleModel : maleModel;
	}

	@Override
	@Nullable
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
		// keep track of classes with armor
		if (entity != null && !classesWithArmor.contains(entity.getClass()))
			classesWithArmor.add(entity.getClass());
		ModelMWArmor ret = getModel(entity);
		ret.slot = slot;
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
		ModelMWArmor model = getModel(entity);
		// show layers
		if (slot == EntityEquipmentSlot.CHEST) {
			model.bipedBodyWear.showModel = true;
			model.bipedLeftArmwear.showModel = true;
			model.bipedRightArmwear.showModel = true;
		}
		else if (slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET) {
			model.bipedLeftLegwear.showModel = true;
			model.bipedRightLegwear.showModel = true;
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
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("devSpawned"))
			tooltip.add(TextFormatting.DARK_PURPLE+""+TextFormatting.BOLD+"Dev Spawned");
		super.addInformation(stack, world, tooltip, flag);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		// delete dev spawned items if not in dev's inventory and delete disabled items (except missingTexture items in SMP)
		if (!world.isRemote && entity instanceof EntityPlayer && stack.hasTagCompound() &&
				stack.getTagCompound().hasKey("devSpawned") && RankManager.getHighestRank((EntityPlayer) entity) != Rank.DEV &&
				((EntityPlayer)entity).inventory.getStackInSlot(slot) == stack) {
			((EntityPlayer)entity).inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
			return;
		}

		// set damage to full if option set to never use durability
		if (Config.durabilityOptionArmors == 2 && stack.getItemDamage() != 0)
			stack.setItemDamage(0);

		super.onUpdate(stack, world, entity, slot, isSelected);
	}

	/**Delete dev spawned dropped items and if config option enabled*/
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		if (!entityItem.world.isRemote && entityItem != null && entityItem.getItem() != null && 
				(Config.deleteItemsOnGround || (entityItem.getItem().hasTagCompound() && 
						entityItem.getItem().getTagCompound().hasKey("devSpawned")))) {
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
				RankManager.getHighestRank(player) != Rank.DEV && 
				player.getItemStackFromSlot(this.armorType) == stack)) {
			player.setItemStackToSlot(this.armorType, ItemStack.EMPTY);
			return;
		}

		boolean hacked = TickHandler.hasHandler(player, Identifier.SOMBRA_HACKED);

		// genji jump boost/double jump
		if (!hacked && this.armorType == EntityEquipmentSlot.CHEST && player != null && 
				set == EnumHero.GENJI) {
			// jump boost
			if (!world.isRemote && (player.getActivePotionEffect(MobEffects.JUMP_BOOST) == null || 
					player.getActivePotionEffect(MobEffects.JUMP_BOOST).getDuration() == 0))
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 10, 0, true, false));
			// double jump
			else if (world.isRemote && (player.onGround || player.isInWater() || player.isInLava()))
				playersJumped.remove(player);
			else if (KeyBind.JUMP.isKeyPressed(player) && !player.onGround && !player.isOnLadder() && 
					player.motionY < 0.2d && !playersJumped.contains(player)) {
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
		if (!hacked && this.armorType == EntityEquipmentSlot.CHEST && player != null && 
				(set == EnumHero.GENJI || set == EnumHero.HANZO) && world.isRemote == player instanceof EntityPlayer) {
			// reset climbing
			boolean badBlock = false;
			for (BlockPos pos : BlockPos.getAllInBox(player.getPosition().up().east().north(), player.getPosition().up().west().south()))
				if (EntityHelper.shouldIgnoreBlock(player.world.getBlockState(pos).getBlock())) {
					badBlock = true;
					break;
				}
			if (badBlock || (player instanceof EntityPlayer && player.onGround) ||  player.isInWater() || player.isInLava()) {
				playersClimbing.remove(player);
			}
			else if (player.isCollidedHorizontally && player.moveForward > 0 && 
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
						((EntityHero) player).getLookHelper().setLookPosition(vec.x, vec.y, vec.z, 30, 30);
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
			else if (!hacked && KeyBind.JUMP.isKeyDown(player) && player.motionY <= -0.09d && !player.isInWater() && !player.isInLava()) {
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

		// set damage to full if wearing full set and option set to not use durability while wearing full set
		if (!world.isRemote && (Config.durabilityOptionArmors == 1 || player instanceof EntityHero) && 
				stack.getItemDamage() != 0 && set == hero)
			stack.setItemDamage(0);
	}

}
