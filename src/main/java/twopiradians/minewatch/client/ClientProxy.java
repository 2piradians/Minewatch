package twopiradians.minewatch.client;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderTippedArrow;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.minewatch.client.key.KeyToggleMode;
import twopiradians.minewatch.client.particle.ParticleHealthPlus;
import twopiradians.minewatch.client.render.entity.RenderAnaBullet;
import twopiradians.minewatch.client.render.entity.RenderGenjiShuriken;
import twopiradians.minewatch.client.render.entity.RenderReaperBullet;
import twopiradians.minewatch.client.render.entity.RenderTracerBullet;
import twopiradians.minewatch.common.CommonProxy;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityAnaBullet;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.entity.EntityHanzoArrow;
import twopiradians.minewatch.common.entity.EntityReaperBullet;
import twopiradians.minewatch.common.entity.EntityTracerBullet;
import twopiradians.minewatch.common.item.ModItems;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		OBJLoader.INSTANCE.addDomain(Minewatch.MODID);
		registerObjRenders();
		registerEntityRenders();
		KeyToggleMode.TOGGLE_MODE = new KeyBinding("Activate Set Effect", Keyboard.KEY_Z, Minewatch.MODNAME);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		registerRenders();
		MinecraftForge.EVENT_BUS.register(Minewatch.keyMode);
		ClientRegistry.registerKeyBinding(KeyToggleMode.TOGGLE_MODE);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}
	
	private static void registerRenders() {
		for (Item item : ModItems.jsonModelItems)
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	private static void registerObjRenders() {
		for (Item item : ModItems.objModelItems)
			// change bow model while pulling
			if (item == ModItems.hanzo_bow) {
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {
						int model = 0;
						if (stack.hasTagCompound()) {
							EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(stack.getTagCompound().getUniqueId("player"));
							if (player != null) {
								model = (int) ((float) (stack.getMaxItemUseDuration() - player.getItemInUseCount()) / 4.0F) + 1;
								if (!player.getActiveItemStack().equals(stack))
									model = 0;
								else if (model > 4)
									model = 4;
							}
						}
						return new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + model + "_3d", "inventory");
					}
				});
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "0_3d", "inventory"));	
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "1_3d", "inventory"));	
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "2_3d", "inventory"));	
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "3_3d", "inventory"));	
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "4_3d", "inventory"));	
			}
			else
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_3d", "inventory"));	
		}

	private void registerEntityRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityReaperBullet.class, RenderReaperBullet::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoArrow.class, RenderTippedArrow::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityAnaBullet.class, RenderAnaBullet::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGenjiShuriken.class, RenderGenjiShuriken::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTracerBullet.class, RenderTracerBullet::new);
	}

	@Override
	protected void registerEventListeners() {
		super.registerEventListeners();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void stitcherEventPre(TextureStitchEvent.Pre event) {
		event.getMap().registerSprite(ParticleHealthPlus.TEXTURE);
	}

	@Override
	public void spawnParticlesHealthPlus(World worldIn, double x, double y, double z, double motionX, double motionY, double motionZ, float scale) {
		ParticleHealthPlus particle = new ParticleHealthPlus(worldIn, x, y, z, motionX, motionY, motionZ, scale);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}
}
