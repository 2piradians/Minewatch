package twopiradians.minewatch.common.item.weapon;

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
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.ability.EntityReinhardtStrike;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemReinhardtHammer extends ItemMWWeapon {

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
				player.world.getEntitiesWithinAABB(EntityLivingBase.class, 
						player.getEntityBoundingBox().offset(player.getLookVec().scale(3)).expand(2.0D, 1D, 2.0D))) 
				if (entity != player) 
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

		if (isSelected && entity instanceof EntityLivingBase) {	
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

		}
	}

}