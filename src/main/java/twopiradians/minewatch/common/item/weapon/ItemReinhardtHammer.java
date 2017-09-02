package twopiradians.minewatch.common.item.weapon;

import com.google.common.collect.Multimap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemReinhardtHammer extends ItemMWWeapon {

	public ItemReinhardtHammer() {
		super(0);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
		if (slot == EntityEquipmentSlot.MAINHAND)
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), 
					new AttributeModifier(ATTACK_DAMAGE_MODIFIER, SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), 75d*damageScale-1, 0));
		return multimap;
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase entity, ItemStack stack) {
		if (entity instanceof EntityPlayer && entity.getHeldItemMainhand() != null && 
				entity.getHeldItemMainhand().getItem() == this)
			return false;
		else 
			return true;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && this.canUse(player, true)) {
			entity.attackEntityFrom(DamageSource.causePlayerDamage(player), 75f*damageScale);
			if (entity instanceof EntityLivingBase) 
				((EntityLivingBase) entity).knockBack(player, 0.4F, 
						(double)MathHelper.sin(player.rotationYaw * 0.017453292F), 
						(double)(-MathHelper.cos(player.rotationYaw * 0.017453292F)));
			if (ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) != hero)
				player.getHeldItemMainhand().damageItem(1, player);
		}
		return false;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (this.canUse(player, true) && !hero.ability1.isSelected(player)) {
			if (world.isRemote)
				Minewatch.proxy.mouseClick();
			else {
				for (EntityLivingBase entity : 
					player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, 
							player.getEntityBoundingBox().offset(player.getLookVec().xCoord*3, player.getLookVec().yCoord*3, player.getLookVec().zCoord*3).expand(2.0D, 1D, 2.0D))) 
					if (entity != player && !player.isOnSameTeam(entity)) 
						this.onLeftClickEntity(stack, player, entity);
				player.worldObj.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.reinhardtWeapon, SoundCategory.PLAYERS, 
						1.0F, player.worldObj.rand.nextFloat()/3+0.8f);
				player.getCooldownTracker().setCooldown(this, 20);
			}
		}
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		return true;
	}

	public boolean canHarvestBlock(IBlockState state, ItemStack stack)
	{
		return false;
	}

}
