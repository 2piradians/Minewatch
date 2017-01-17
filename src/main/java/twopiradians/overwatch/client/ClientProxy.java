package twopiradians.overwatch.client;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import twopiradians.overwatch.client.render.entity.RenderReaperPellet;
import twopiradians.overwatch.common.CommonProxy;
import twopiradians.overwatch.common.entity.EntityReaperPellet;
import twopiradians.overwatch.common.item.ModItems;

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
