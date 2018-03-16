package twopiradians.minewatch.common.item;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.model.BakedMWItem;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class ItemRoadhogHealth extends Item implements IChangingModel {
	
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

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		if (!world.isRemote) {
			// if not wearing full set or main is not sombra's gun
			if ((entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHeldItemOffhand() == stack) &&
					(SetManager.getWornSet((EntityLivingBase) entity) != EnumHero.ROADHOG ||
					((EntityLivingBase)entity).getHeldItemMainhand() == null || 
					((EntityLivingBase)entity).getHeldItemMainhand().getItem() != EnumHero.ROADHOG.weapon || 
					!TickHandler.hasHandler(entity, Identifier.ROADHOG_HEALING))) {
				((EntityLivingBase)entity).setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
			}
			// if not in offhand
			else if (entity instanceof EntityPlayer && 
					((EntityPlayer)entity).getHeldItemOffhand() != stack &&
					((EntityPlayer)entity).inventory.getStackInSlot(slot) == stack) {
				((EntityPlayer)entity).inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		if (!entityItem.world.isRemote && entityItem != null && entityItem.getItem() != null) {
			entityItem.setDead();
			return true;
		}
		return false;
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

	@Override
	public boolean shouldRecolor(BakedMWItem model, BakedQuad quad) {
		return false;
	}

}