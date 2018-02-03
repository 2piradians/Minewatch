package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.BakedMWItem;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.RankManager;
import twopiradians.minewatch.common.hero.RankManager.Rank;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.item.IChangingModel;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;
import twopiradians.minewatch.packet.SPacketSyncAmmo;

public abstract class ItemMWWeapon extends Item implements IChangingModel {

	public EnumHero hero;
	public boolean hasOffhand;
	private HashMap<ItemStack, Integer> reequipAnimation = Maps.newHashMap();
	/**Cooldown in ticks for warning player about misusing weapons (main weapon in offhand, no offhand, etc.) */
	private static Handler WARNING_CLIENT = new Handler(Identifier.WEAPON_WARNING, false) {};
	private static Handler ENTITY_HERO_COOLDOWN = new Handler(Identifier.WEAPON_COOLDOWN, false) {};
	/**Do not interact with directly - use the getter / setter*/
	private HashMap<UUID, Integer> currentAmmo = Maps.newHashMap();
	/**Do not interact with directly - use the getter / setter*/
	private HashMap<UUID, Float> currentChargeServer = Maps.newHashMap();
	/**Do not interact with directly - use the getter / setter*/
	private float currentChargeClient = -1;
	public int maxCharge;
	/**Charge regained per tick*/
	public float rechargeRate;
	private int reloadTime;
	protected boolean saveEntityToNBT;
	public boolean showHealthParticles;

	private Handler CHARGE_RECOVERY = new Handler(Identifier.WEAPON_CHARGE, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (entityLiving != null && currentChargeClient < maxCharge) {
				if (entityLiving.ticksExisted > this.number+3) 
					currentChargeClient = Math.min(maxCharge, currentChargeClient+rechargeRate);
				this.ticksLeft = 2;
			}
			else
				return true;

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (entityLiving != null && currentChargeServer.containsKey(entityLiving.getPersistentID()) && 
					currentChargeServer.get(entityLiving.getPersistentID()) < maxCharge) {
				if (entityLiving.ticksExisted > this.number+3)
					currentChargeServer.put(entityLiving.getPersistentID(), Math.min(maxCharge, currentChargeServer.get(entityLiving.getPersistentID())+rechargeRate));
				this.ticksLeft = 2;
			}
			else
				return true;

			return super.onServerTick();
		}
	};

	public ItemMWWeapon(int reloadTime) {
		this.setMaxDamage(100);
		this.reloadTime = reloadTime;
	}

	public float getCurrentCharge(Entity player) {
		if (player != null)
			if (!player.worldObj.isRemote && currentChargeServer.containsKey(player.getPersistentID())) 
				return currentChargeServer.get(player.getPersistentID());
			else if (player.worldObj.isRemote && this.currentChargeClient >= 0 && player == Minewatch.proxy.getClientPlayer())
				return currentChargeClient;
		return this.maxCharge;
	}

	public void setCurrentCharge(Entity player, float amount, boolean sendPacket) {
		if (player != null) {
			amount = MathHelper.clamp_float(amount, 0, this.maxCharge);
			if (player instanceof EntityPlayerMP && sendPacket) 
				Minewatch.network.sendTo(new SPacketSimple(46, player, false, amount, 0, 0), (EntityPlayerMP) player);
			Handler handler = TickHandler.getHandler(player, Identifier.WEAPON_CHARGE);
			if (handler != null)
				handler.setNumber(player.ticksExisted);
			else if (amount < this.maxCharge)
				TickHandler.register(player.worldObj.isRemote, this.CHARGE_RECOVERY.setEntity(player).setTicks(2));
			if (player.worldObj.isRemote && player == Minewatch.proxy.getClientPlayer())
				currentChargeClient = amount;
			else if (!player.worldObj.isRemote)
				currentChargeServer.put(player.getPersistentID(), amount);
		}
	}

	public void subtractFromCurrentCharge(Entity player, float amount, boolean sendPacket) {
		float charge = getCurrentCharge(player);
		if (charge - amount > 0.5f)
			this.setCurrentCharge(player, charge-amount, sendPacket);
		else {
			this.setCooldown(player, 20);
			this.setCurrentCharge(player, 0, sendPacket);
		}
	}

	public int getMaxAmmo(Entity player) {
		if (player instanceof EntityLivingBase && hero.hasAltWeapon && isAlternate(((EntityLivingBase) player).getHeldItemMainhand()))
			return (int) Math.ceil(hero.altAmmo*Config.ammoMultiplier);
		else
			return (int) Math.ceil(hero.mainAmmo*Config.ammoMultiplier);
	}

	public int getCurrentAmmo(Entity player) {
		if (player != null && currentAmmo.containsKey(player.getPersistentID())) {
			if (currentAmmo.get(player.getPersistentID()) > getMaxAmmo(player))
				currentAmmo.put(player.getPersistentID(), getMaxAmmo(player));
			return currentAmmo.get(player.getPersistentID());
		}
		else
			return getMaxAmmo(player);
	}

	public void setCurrentAmmo(Entity player, int amount, EnumHand... hands) {
		if (player != null) {
			if (player instanceof EntityPlayerMP) {
				EnumHand hand = ((EntityLivingBase) player).getHeldItemMainhand() != null && 
						((EntityLivingBase) player).getHeldItemMainhand().getItem() == this ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
				Minewatch.network.sendTo(new SPacketSyncAmmo(hero, player.getPersistentID(), hand, amount, hands), (EntityPlayerMP) player); 
			}
			if (player.worldObj.isRemote && player instanceof EntityLivingBase)
				for (EnumHand hand2 : hands)
					if (((EntityLivingBase) player).getHeldItem(hand2) != null && ((EntityLivingBase) player).getHeldItem(hand2).getItem() == this) 
						this.reequipAnimation(((EntityLivingBase) player).getHeldItem(hand2));
			currentAmmo.put(player.getPersistentID(), Math.min(amount, getMaxAmmo(player)));
		}
	}

	public void subtractFromCurrentAmmo(Entity player, int amount, EnumHand... hands) {
		int ammo = getCurrentAmmo(player);
		if (ammo - amount > 0)
			this.setCurrentAmmo(player, ammo-amount, hands);
		else {
			this.setCurrentAmmo(player, 0, hands);
			reload(player);
		}
	}

	public void reload(Entity player) {
		if (player != null && !player.worldObj.isRemote && getCurrentAmmo(player) < getMaxAmmo(player)) {
			this.setCooldown(player, reloadTime, true);
			this.setCurrentAmmo(player, 0, EnumHand.values());
			if (hero.reloadSound != null)
				hero.reloadSound.playFollowingSound(player, 1.0f, player.worldObj.rand.nextFloat()/2+0.75f, false);
		}
	}

	@Nullable
	public EnumHand getHand(EntityLivingBase entity, ItemStack stack) {
		for (EnumHand hand : EnumHand.values())
			if (entity.getHeldItem(hand) == stack)
				return hand;
		return null;
	}

	public void reequipAnimation(ItemStack stack) {
		this.reequipAnimation(stack, 2);
	}

	public void reequipAnimation(ItemStack stack, int ticks) {
		if (stack != null)
			this.reequipAnimation.put(stack, ticks);
	}

	/**Check that weapon is in correct hand and that offhand weapon is held if hasOffhand.
	 * Also checks that weapon is not on cooldown.
	 * Warns player if something is incorrect.*/
	public boolean canUse(EntityLivingBase player, boolean shouldWarn, @Nullable EnumHand hand, boolean ignoreAmmo) {
		return canUse(player, shouldWarn, hand, ignoreAmmo, new Ability[0]);
	}

	/**Check that weapon is in correct hand and that offhand weapon is held if hasOffhand.
	 * Also checks that weapon is not on cooldown.
	 * Warns player if something is incorrect.*/
	public boolean canUse(EntityLivingBase player, boolean shouldWarn, @Nullable EnumHand hand, boolean ignoreAmmo, Ability...ignoreAbilities) {
		Handler handler = TickHandler.getHandler(player, Identifier.ABILITY_USING);
		boolean ignoreAbility = false;
		for (Ability ability : ignoreAbilities)
			if (handler != null && handler.ability == ability)
				ignoreAbility = true;
		if (player == null || !player.isEntityAlive() || (hasCooldown(player) && !ignoreAmmo) || 
				(!ignoreAmmo && this.getMaxAmmo(player) > 0 && this.getCurrentAmmo(player) == 0) ||
				TickHandler.hasHandler(player, Identifier.PREVENT_INPUT) ||
				(handler != null && !handler.bool && !ignoreAbility) ||
				(player instanceof EntityPlayer && ((EntityPlayer)player).isSpectator()))
			return false;

		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();
		String displayName = (main != null && main.getItem() == this) ? this.getItemStackDisplayName(main) :
			(off != null && off.getItem() == this) ? this.getItemStackDisplayName(off) : 
				new ItemStack(this).getDisplayName();

			if (!Config.allowGunWarnings)
				return true;
			else if (this.hasOffhand && ((off == null || off.getItem() != this) || (main == null || main.getItem() != this))) {
				if (shouldWarn && !TickHandler.hasHandler(player, Identifier.WEAPON_WARNING) && player.worldObj.isRemote)
					player.addChatMessage(new TextComponentString(TextFormatting.RED+
							displayName+" must be held in the main-hand and off-hand to work."));
			} 
			else if ((hand == EnumHand.OFF_HAND && !this.hasOffhand) ||(main == null || main.getItem() != this)) {
				if (shouldWarn && !TickHandler.hasHandler(player, Identifier.WEAPON_WARNING) && player.worldObj.isRemote)
					player.addChatMessage(new TextComponentString(TextFormatting.RED+
							displayName+" must be held in the main-hand to work."));
			}
			else
				return true;

			if (shouldWarn && player.worldObj.isRemote && !TickHandler.hasHandler(player, Identifier.WEAPON_WARNING))
				TickHandler.register(true, WARNING_CLIENT.setEntity(player).setTicks(60));

			return false;
	}

	public void onItemLeftClick(ItemStack stack, World worldObj, EntityLivingBase player, EnumHand hand) { }

	/**Use instead of {@link Item#onItemRightClick(World, EntityPlayer, EnumHand)} to allow EntityLivingBase
	 * NOTE: this is only called every 4 ticks for some reason*/
	public ActionResult<ItemStack> onItemRightClick(World worldObj, EntityLivingBase player, EnumHand hand) {
		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldObj, EntityPlayer player, EnumHand hand) {
		return this.onItemRightClick(worldObj, (EntityLivingBase) player, hand);
	}

	/**Cancel swing animation when left clicking*/
	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
		return true;
	}

	@Override
	public void onUpdate(ItemStack stack, World worldObj, Entity entity, int slot, boolean isSelected) {	
		if (entity == null || !entity.isEntityAlive())
			return;

		//delete dev spawned items if not in dev's inventory
		if (!worldObj.isRemote && entity instanceof EntityPlayer && stack.hasTagCompound() &&
				stack.getTagCompound().hasKey("devSpawned") && RankManager.getHighestRank(entity) != Rank.DEV &&
				((EntityPlayer)entity).inventory.getStackInSlot(slot) == stack) {
			((EntityPlayer)entity).inventory.setInventorySlotContents(slot, null);
			return;
		}

		// set entity in nbt for model changer to reference
		if (this.saveEntityToNBT && entity instanceof EntityLivingBase && !entity.worldObj.isRemote && 
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
		if (!worldObj.isRemote && entity instanceof EntityLivingBase && (((EntityLivingBase)entity).getHeldItemMainhand() == stack ||
				((EntityLivingBase)entity).getHeldItemOffhand() == stack) && !TickHandler.hasHandler(entity, Identifier.PREVENT_INPUT))
			// automatic reload
			if (this.getCurrentAmmo((EntityLivingBase) entity) == 0 && this.getMaxAmmo((EntityLivingBase) entity) > 0 &&
			!this.hasCooldown(entity))
				this.setCurrentAmmo((EntityLivingBase) entity, this.getMaxAmmo((EntityLivingBase) entity), EnumHand.values());
		// manual reload
			else if (this.getCurrentAmmo((EntityLivingBase) entity) > 0 && KeyBind.RELOAD.isKeyDown((EntityLivingBase) entity))
				this.reload((EntityLivingBase) entity);

		// left click 
		// note: this alternates stopping hands for weapons with hasOffhand, 
		// so make sure weapons with hasOffhand use an odd numbered cooldown
		if (entity instanceof EntityLivingBase && KeyBind.LMB.isKeyDown((EntityLivingBase) entity)) {
			EntityLivingBase player = (EntityLivingBase) entity;
			EnumHand hand = this.getHand(player, stack);	
			if (hand != null && (!this.hasOffhand || this.hero == EnumHero.MOIRA || 
					((hand == EnumHand.MAIN_HAND && player.ticksExisted % 2 == 0) ||
							(hand == EnumHand.OFF_HAND && player.ticksExisted % 2 != 0 || 
							this == EnumHero.TRACER.weapon))))
				onItemLeftClick(stack, worldObj, (EntityLivingBase) entity, hand);
		}

		// right click - for EntityHeroes
		if (entity instanceof EntityHero)
			if (KeyBind.RMB.isKeyPressed((EntityLivingBase) entity) || 
					(KeyBind.RMB.isKeyDown((EntityLivingBase) entity) && !((EntityLivingBase) entity).isHandActive()) &&
					entity.ticksExisted % 4 == 0)
				this.onItemRightClick(worldObj, (EntityLivingBase) entity, this.getHand((EntityLivingBase) entity, stack));
			else if (KeyBind.RMB.isKeyDown((EntityLivingBase) entity))
				this.onUsingTick(stack, (EntityLivingBase) entity, ((EntityHero) entity).getItemInUseCount());
			else if (((EntityHero) entity).isHandActive())
				((EntityHero) entity).stopActiveHand();

		// deselect ability if it has cooldown
		if (entity instanceof EntityPlayer)
			for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
				if (ability.keybind.getCooldown((EntityPlayer) entity) > 0)
					ability.toggle(entity, false);

		// set damage to full if option set to never use durability
		if (!worldObj.isRemote && (Config.durabilityOptionWeapons == 2 || entity instanceof EntityHero) && stack.getItemDamage() != 0)
			stack.setItemDamage(0);
		// set damage to full if wearing full set and option set to not use durability while wearing full set
		else if (!worldObj.isRemote && Config.durabilityOptionWeapons == 1 && stack.getItemDamage() != 0 && 
				SetManager.getWornSet(entity.getPersistentID(), entity.worldObj.isRemote) == hero)
			stack.setItemDamage(0);

		// health particles
		if (this.showHealthParticles && entity instanceof EntityPlayer && ((EntityPlayer)entity).getHeldItemMainhand() == stack && 
				this.canUse((EntityPlayer) entity, false, EnumHand.MAIN_HAND, true) && worldObj.isRemote && entity.ticksExisted % 5 == 0) {
			AxisAlignedBB aabb = entity.getEntityBoundingBox().expandXyz(30);
			List<Entity> list = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, aabb);
			for (Entity entity2 : list) 
				if (entity2 instanceof EntityLivingBase && ((EntityLivingBase)entity2).getHealth() > 0 &&
						((EntityLivingBase)entity2).getHealth() < ((EntityLivingBase)entity2).getMaxHealth()/2f && 
						EntityHelper.shouldTarget(entity, entity2, true) && !(entity2 instanceof EntityLivingBaseMW)) {
					float size = Math.min(entity2.height, entity2.width)*9f;
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.HEALTH, worldObj, entity2, 0xFFFFFF, 0xFFFFFF, 0.7f, Integer.MAX_VALUE, size, size, 0, 0);
				}
		}

		// aim assist
		if (worldObj.isRemote && entity instanceof EntityPlayer && Config.aimAssist > 0 && ((EntityPlayer)entity).getHeldItemMainhand() == stack && 
				 (KeyBind.LMB.isKeyDown((EntityLivingBase) entity) || KeyBind.RMB.isKeyDown((EntityLivingBase) entity))) {
			float delta = Config.aimAssist;
			float yaw = MathHelper.wrapDegrees(entity.rotationYaw);
			float pitch = MathHelper.wrapDegrees(entity.rotationPitch);
			if (EntityHelper.getMouseOverEntity((EntityLivingBase) entity, 512, false, pitch, yaw) == null) {
				EntityLivingBase targetEntity = EntityHelper.getTargetInFieldOfVision((EntityLivingBase) entity, entity instanceof EntityHero ? 64 : 512, 10, false);
				if (targetEntity != null) {
					Vector2f angles = EntityHelper.getDirectLookAngles(entity.getPositionVector().addVector(0, entity.getEyeHeight(), 0), targetEntity.getPositionVector().addVector(0, targetEntity.getEyeHeight(), 0));
					if (Math.abs(yaw-angles.x) > 180) {
						angles.x = (angles.x + 360f) % 360;
						yaw = (yaw + 360f) % 360;
					}
					entity.rotationYaw = MathHelper.clamp_float(angles.x, yaw-delta, yaw+delta);
					entity.rotationPitch = MathHelper.clamp_float(angles.y, pitch-delta, pitch+delta);
				} 
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

		return oldStack != newStack || slotChanged; 
	}

	/**If item has cooldown - cooldown tracker for players, handler for entity heroes*/
	public boolean hasCooldown(Entity entity) {
		// use cooldown tracker for players
		if (entity instanceof EntityPlayer) 
			return ((EntityPlayer)entity).getCooldownTracker().hasCooldown(this);
		// use handler for entity heroes
		else if (entity instanceof EntityHero) 
			return TickHandler.hasHandler(entity, Identifier.WEAPON_COOLDOWN);
		else
			return false;
	}

	/**Set item's cooldown - cooldown tracker for players, handler for entity heroes*/
	public void setCooldown(Entity entity, int cooldown) {
		this.setCooldown(entity, cooldown, false);
	}

	/**Set item's cooldown - cooldown tracker for players, handler for entity heroes*/
	public void setCooldown(Entity entity, int cooldown, boolean overrideCooldown) {
		// use cooldown tracker for players
		if (entity instanceof EntityPlayer) {
			CooldownTracker tracker = ((EntityPlayer)entity).getCooldownTracker();
			if (!tracker.hasCooldown(this) || overrideCooldown)
				tracker.setCooldown(this, Math.max(2, cooldown));
		}
		// use handler for entity heroes
		else if (entity instanceof EntityHero) {
			cooldown *= Config.mobAttackCooldown;
			cooldown++;
			Handler handler = TickHandler.getHandler(entity, Identifier.WEAPON_COOLDOWN);
			if (handler == null)
				TickHandler.register(entity.worldObj.isRemote, ENTITY_HERO_COOLDOWN.setEntity(entity).setTicks(cooldown));
			else if (handler.ticksLeft < cooldown)
				handler.ticksLeft = cooldown;

			if (!entity.worldObj.isRemote)
				Minewatch.network.sendToDimension(new SPacketSimple(36, entity, false, cooldown, 0, 0), entity.worldObj.provider.getDimension());
		}
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(ItemStack stack) {
		return super.hasEffect(stack); 
	}

	/**Called before armor is rendered - mainly used for coloring / alpha
	 * Returns if further coloring / alpha changing should be prevented*/
	@SideOnly(Side.CLIENT)
	public boolean preRenderArmor(EntityLivingBase entity, ModelMWArmor model) { return false; }

	/**Called before weapon is rendered*/
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType transform, Pair<? extends IBakedModel, Matrix4f> ret) {return ret;}

	/**Called before game overlay is rendered if holding weapon in either hand (only called once per tick regardless of hand(s))*/
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height, EnumHand hand) {
		// render charge
		if (this.maxCharge > 0 && event.getType() == ElementType.CROSSHAIRS && 
				this.getCurrentCharge(player) < this.maxCharge && TickHandler.hasHandler(player, Identifier.WEAPON_CHARGE)) {
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GlStateManager.pushMatrix();
			GlStateManager.translate(width/2d, height/2d, 0);
			GlStateManager.rotate(180, 1, 0, 0);
			GlStateManager.rotate(54, 0, 0, 1);
			float size = 60;
			GlStateManager.disableLighting();
			GlStateManager.disableCull();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.disableTexture2D();
			GlStateManager.glLineWidth(10f);

			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer vertexbuffer = tessellator.getBuffer();

			// background
			vertexbuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
			double deg_to_rad = 0.0174532925d;
			double precision = 0.5d;
			double angle_from = 270;
			double angle_to = angle_from + 0.2f * 360d;
			double angle_diff = angle_to-angle_from;
			double steps = Math.round(angle_diff*precision);
			double angle = angle_from;
			for (int i = 1; i<=steps; i++) {
				angle = angle_from+angle_diff/steps*i;
				vertexbuffer.pos(size*Math.cos(angle*deg_to_rad), size*Math.sin(angle*deg_to_rad), 0).color(0.7f, 0.7f, 0.7f, 0.5F).endVertex();
			}
			tessellator.draw();

			// foreground
			vertexbuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
			angle_to = angle_from + (this.getCurrentCharge(player) / this.maxCharge) * 0.2f * 360d;
			angle_diff = angle_to-angle_from;
			steps = Math.round(angle_diff*precision);
			angle = angle_from;
			for (int i = 1; i <= steps; i++) {
				angle = angle_from+angle_diff/steps*i;
				vertexbuffer.pos(size*Math.cos(angle*deg_to_rad), size*Math.sin(angle*deg_to_rad), 0).color(1, 1, 1, 0.4F).endVertex();
			}
			tessellator.draw();

			GlStateManager.glLineWidth(1);
			GlStateManager.enableTexture2D();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.popMatrix();
		}
	}
	
	/**Called before player hands are rendered, for player holding weapon in mainhand*/
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderHand(AbstractClientPlayer player, EnumHand hand) {
		return false;
	}

	/**Called before entity wearing set and holding weapon in mainhand is rendered*/
	@SideOnly(Side.CLIENT)
	public void preRenderEntity(RenderLivingEvent.Pre<EntityLivingBase> event) {}

	/**Called after entity wearing set and holding weapon in mainhand is rendered*/
	@SideOnly(Side.CLIENT)
	public void postRenderEntity(RenderLivingEvent.Post<EntityLivingBase> event) {}

	/**Called when client player is wearing set and holding weapon in mainhand*/
	@SideOnly(Side.CLIENT)
	public void renderWorldLast(RenderWorldLastEvent event, EntityPlayerSP player) {}

	/**Should this quad be recolored by getColorFromItemStack color*/
	@SideOnly(Side.CLIENT)
	public boolean shouldRecolor(BakedMWItem model, BakedQuad quad) {
		return true;
	}

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
	public static EntityLivingBase getEntity(World worldObj, ItemStack stack) {
		if (stack != null && stack.hasTagCompound() &&
				stack.getTagCompound().getUniqueId("entity") != null) {
			UUID uuid = stack.getTagCompound().getUniqueId("entity");
			for (Entity entity : worldObj.loadedEntityList)
				if (uuid.equals(entity.getPersistentID()) && entity instanceof EntityLivingBase)
					return (EntityLivingBase) entity;
		}
		return null;
	}

	/**Is this weapon using its alternate version*/
	public static boolean isAlternate(ItemStack stack) {
		return stack != null && stack.hasTagCompound() &&
				stack.getTagCompound().hasKey("alt_weapon");
	}

	/**Set this weapon to use / not use alternate version - can be called on client if needed (make sure it's correct value)*/
	public static void setAlternate(ItemStack stack, boolean usingAlt) {
		if (stack != null && stack.getItem() instanceof ItemMWWeapon && 
				((ItemMWWeapon)stack.getItem()).hero.hasAltWeapon) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());

			NBTTagCompound nbt = stack.getTagCompound();
			if (usingAlt)
				nbt.setBoolean("alt_weapon", true);
			else
				nbt.removeTag("alt_weapon");
			stack.setTagCompound(nbt);
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

	/**Delete dev spawned dropped items*/
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		//delete dev spawned items if not worn by dev
		if (!entityItem.worldObj.isRemote && entityItem != null && entityItem.getEntityItem() != null && 
				entityItem.getEntityItem().hasTagCompound() && 
				entityItem.getEntityItem().getTagCompound().hasKey("devSpawned")) {
			entityItem.setDead();
			return true;
		}
		return false;
	}

}