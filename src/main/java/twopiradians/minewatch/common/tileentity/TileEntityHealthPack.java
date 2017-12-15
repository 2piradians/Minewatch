package twopiradians.minewatch.common.tileentity;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.block.ModBlocks;

@SideOnly(Side.CLIENT)
public class TileEntityHealthPack extends TileEntity {
//minewatch:models/block/health_pack_base.obj
	public OBJModel model;
	
	public TileEntityHealthPack() {
		super();
		try {
			IModel model = ModelLoaderRegistry.getModel(new ModelResourceLocation(new ResourceLocation(Minewatch.MODID, ModBlocks.healthPackSmall.getUnlocalizedName().substring(5)), "inventory"));
			if (model instanceof OBJModel)
				this.model = (OBJModel) model;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}