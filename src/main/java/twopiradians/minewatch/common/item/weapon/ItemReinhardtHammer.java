package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;

import com.google.common.collect.Multimap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.ability.EntityReinhardtStrike;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.RenderManager;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemReinhardtHammer extends ItemMWWeapon {

	// TODO overlay messages

	public static final Handler CHARGE = new Handler(Identifier.REINHARDT_CHARGE, true) {
		private ArrayList<EntityLivingBase> hitEntities = new ArrayList<EntityLivingBase>();

		private void move() {
			entity.setSneaking(false);
			if (entity.world.isRemote) {
				if (number == 0) // number is used to lock yaw (since PREVENT_ROTATIONS is updated)
					this.number = entity.rotationYaw;
				entity.rotationYaw = (float) MathHelper.clamp(number-((EntityLivingBase)entity).moveStrafing, number-1, number+1);
				entity.rotationPitch = 10;
				entity.setRotationYawHead(entity.rotationYaw);
				this.number = entity.rotationYaw;
				Handlers.copyRotations((EntityLivingBase) entity);
			}
			if (this.ticksLeft <= this.initialTicks-14) {
			entity.moveRelative(0, 1, 1);
			Vec3d motion = new Vec3d(entity.motionX, entity.motionY, entity.motionZ).normalize().scale(16.66d/20d);
			entity.motionX = motion.xCoord;
			entity.motionY = motion.yCoord;
			entity.motionZ = motion.zCoord;
			}
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (entity instanceof EntityLivingBase) {
				move();

				// move pinned
				if (this.entityLiving != null) {
					if (!this.entityLiving.isEntityAlive())
						this.entityLiving = null;
					else {
						Vec3d pos = this.entity.getPositionVector().add(this.entity.getLookVec().scale(entity.width/2f+entityLiving.width/2f));
						this.entityLiving.setLocationAndAngles(pos.xCoord, pos.yCoord, pos.zCoord, entity.rotationYaw+180f, 0);
						this.entityLiving.fallDistance = 0;
					}
				}
			}
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (entity instanceof EntityLivingBase) {
				move();

				// check for entities to pin / knockback
				if (this.ticksLeft % 1 == 0) {
					Vec3d look = entity.getLookVec().scale(1d);
					AxisAlignedBB aabb = entity.getEntityBoundingBox().expandXyz(1d).move(look);
					for (Entity target : entity.world.getEntitiesWithinAABBExcludingEntity(entity, aabb)) 
						if (!hitEntities.contains(target) && target != entityLiving && target != entity && 
						target instanceof EntityLivingBase && 
						EntityHelper.attemptDamage(entity, target, 50, true)) {
							hitEntities.add((EntityLivingBase) target);
							if (this.entityLiving == null && target.isEntityAlive()) {
								this.entityLiving = (EntityLivingBase) target;
								Minewatch.network.sendToDimension(new SPacketSimple(56, entity, true, target), entity.world.provider.getDimension());
							}
							else
								((EntityLivingBase)target).knockBack(entity, 5, (double)MathHelper.sin(entity.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(entity.rotationYaw * 0.017453292F)));
						}
				}

				// move pinned
				if (this.entityLiving != null) {
					if (!this.entityLiving.isEntityAlive())
						this.entityLiving = null;
					else {
						Vec3d pos = this.entity.getPositionVector().add(this.entity.getLookVec().scale(entity.width/2f+entityLiving.width/2f));
						this.entityLiving.setLocationAndAngles(pos.xCoord, pos.yCoord, pos.zCoord, entity.rotationYaw+180f, 0);
						this.entityLiving.fallDistance = 0;
					}
				}

				// check for wall impact
				float pitch = this.entity.rotationPitch;
				this.entity.rotationPitch = 0;
				AxisAlignedBB aabb = this.entity.getEntityBoundingBox().contract(0, 0.1d, 0).move(this.entity.getLookVec().scale(1));
				this.entity.rotationPitch = pitch;
				if (this.entity.world.collidesWithAnyBlock(aabb)) {
					this.ticksLeft = 1;
					if (this.entityLiving != null)
						EntityHelper.attemptDamage(entity, entityLiving, 300, true);
				}
			}
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			// move pinned to entity location so it's not in a block
			if (this.entityLiving != null)
				this.entityLiving.setPosition(entity.posX, entity.posY, entity.posZ);
			this.entity.motionX = 0;
			this.entity.motionZ = 0;
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			this.hitEntities.clear();
			// move pinned to entity location so it's not in a block
			if (this.entityLiving != null)
				this.entityLiving.setPosition(entity.posX, entity.posY, entity.posZ);
			this.entity.motionX = 0;
			this.entity.motionZ = 0;
			Minewatch.network.sendToDimension(new SPacketSimple(56, entity, false), entity.world.provider.getDimension());
			TickHandler.unregister(false, TickHandler.getHandler(entity, Identifier.ACTIVE_HAND),
					TickHandler.getHandler(entity, Identifier.PREVENT_ROTATION),
					TickHandler.getHandler(entity, Identifier.PREVENT_MOVEMENT),
					TickHandler.getHandler(entity, Identifier.HERO_SNEAKING),
					TickHandler.getHandler(entity, Identifier.ABILITY_USING));
			return super.onServerRemove();
		}
	};

	public static final Handler STRIKE = new Handler(Identifier.REINHARDT_STRIKE, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (entityLiving != null && this.ticksLeft == 4)
				entityLiving.swingArm(EnumHand.MAIN_HAND);
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (entityLiving != null && this.ticksLeft == 1) {
				EntityReinhardtStrike strike = new EntityReinhardtStrike(entityLiving.world, entityLiving);
				EntityHelper.setAim(strike, entityLiving, entityLiving.rotationPitch, entityLiving.rotationYawHead, (26.66f) * 1f, 0, null, 60, 0);
				entityLiving.world.spawnEntity(strike);
				EnumHero.REINHARDT.ability2.keybind.setCooldown(entityLiving, 120, false); 
			}
			return super.onServerTick();
		}
	};

	public ItemReinhardtHammer() {
		super(0);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return Integer.MAX_VALUE;
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
		if (slot == EntityEquipmentSlot.MAINHAND)
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), 
					new AttributeModifier(ATTACK_DAMAGE_MODIFIER, SharedMonsterAttributes.ATTACK_DAMAGE.getName(), 75d*Config.damageScale-1, 0));
		return multimap;
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase entity, ItemStack stack) {
		if (entity instanceof EntityLivingBase && entity.getHeldItemMainhand() != null && 
				entity.getHeldItemMainhand().getItem() == this)
			return false;
		else 
			return true;
	}

	public void attack(ItemStack stack, EntityLivingBase player, Entity entity) {
		// swing
		if (!player.world.isRemote && this.canUse(player, true, getHand(player, stack), false) && 
				player.canEntityBeSeen(entity) && 
				EntityHelper.attemptDamage(player, entity, 75, false)) {
			if (entity instanceof EntityLivingBase) 
				((EntityLivingBase) entity).knockBack(player, 0.4F, 
						(double)MathHelper.sin(player.rotationYaw * 0.017453292F), 
						(double)(-MathHelper.cos(player.rotationYaw * 0.017453292F)));
			player.getHeldItemMainhand().damageItem(1, player);
		}
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		this.attack(stack, player, entity);
		return false;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// swing
		if (!world.isRemote && this.canUse(player, true, hand, false) && !hero.ability1.isSelected(player) &&
				hand == EnumHand.MAIN_HAND) {
			if (player instanceof EntityPlayerMP)
				Minewatch.network.sendTo(new SPacketSimple(5), (EntityPlayerMP) player);
			for (EntityLivingBase entity : 
				player.world.getEntitiesWithinAABB(EntityLivingBase.class, player.getEntityBoundingBox().expandXyz(5))) 
				if (entity != player && entity != null && player.getDistanceToEntity(entity) <= 5 &&
				EntityHelper.isInFieldOfVision(player, entity, 80)) 
					this.attack(stack, player, entity);
			ModSoundEvents.REINHARDT_WEAPON.playSound(player, 1.0F, player.world.rand.nextFloat()/3+0.8f);
			this.setCooldown(player, 20);
		}
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
		return false;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHeldItemMainhand() == stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;
			player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 8, 3, true, false));

			// fire strike
			if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				Minewatch.network.sendToDimension(new SPacketSimple(33, player, false), world.provider.getDimension());
				TickHandler.register(false, STRIKE.setEntity(player).setTicks(13),
						Ability.ABILITY_USING.setEntity(player).setTicks(13).setAbility(hero.ability2));
				player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 13, 2, true, false));
			}

			// charge
			if (!world.isRemote && hero.ability3.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				Minewatch.network.sendToDimension(new SPacketSimple(56, player, true), world.provider.getDimension());
				TickHandler.register(false, CHARGE.setEntity(player).setEntityLiving(null).setTicks(80),
						Ability.ABILITY_USING.setEntity(player).setTicks(80).setAbility(hero.ability3),
						Handlers.ACTIVE_HAND.setEntity(player).setTicks(80),
						Handlers.PREVENT_ROTATION.setEntity(player).setTicks(80), 
						Handlers.PREVENT_MOVEMENT.setEntity(player).setTicks(80),
						RenderManager.SNEAKING.setEntity(entity).setTicks(80));
			}
		}
	}

}