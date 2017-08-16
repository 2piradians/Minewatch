package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemBastionGun extends ItemMWWeapon 
{
	public ItemBastionGun() {
		super(40);
		this.hero = EnumHero.BASTION;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		// set player in nbt for model changer (in ClientProxy) to reference
		if (entity instanceof EntityPlayer && !entity.world.isRemote && 
				stack != null && stack.getItem() instanceof ItemBastionGun) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = stack.getTagCompound();
			if (!nbt.hasKey("playerLeast") || nbt.getLong("playerLeast") != (entity.getPersistentID().getLeastSignificantBits())) {
				nbt.setUniqueId("player", entity.getPersistentID());
				stack.setTagCompound(nbt);
			}
		}
	}	
	
}
