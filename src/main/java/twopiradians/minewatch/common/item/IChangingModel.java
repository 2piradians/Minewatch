package twopiradians.minewatch.common.item;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IChangingModel {
	
	/**Set weapon model's color*/
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int tintIndex);

	/**Register this weapon model's variants - registers 2D and 3D basic models by default*/
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getAllModelLocations(ArrayList<String> locs);

	/**defautLoc + [return] + (Config.useObjModels ? "_3d" : "")*/
	@SideOnly(Side.CLIENT)
	public String getModelLocation(ItemStack stack, @Nullable EntityLivingBase entity);
	
	public Item getItem();
	
}
