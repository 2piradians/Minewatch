package twopiradians.minewatch.client;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import twopiradians.minewatch.client.render.entity.RenderReaperPellet;
import twopiradians.minewatch.common.CommonProxy;
import twopiradians.minewatch.common.entity.EntityReaperPellet;
import twopiradians.minewatch.common.item.ModItems;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit() {
		super.preInit();
		registerEntityRenders();
	}

	@Override
	public void init() {
		super.init();
		ModItems.registerRenders();
	}

	@Override
	public void postInit() {
		super.postInit();
	}
	
	private void registerEntityRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityReaperPellet.class, RenderReaperPellet::new);
	}
}
