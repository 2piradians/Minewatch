package twopiradians.minewatch.client;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderTippedArrow;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
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
import twopiradians.minewatch.client.render.entity.RenderReaperBullet;
import twopiradians.minewatch.common.CommonProxy;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityAnaBullet;
import twopiradians.minewatch.common.entity.EntityHanzoArrow;
import twopiradians.minewatch.common.entity.EntityReaperBullet;
import twopiradians.minewatch.common.item.ModItems;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		registerEntityRenders();
		KeyToggleMode.TOGGLE_MODE = new KeyBinding("Activate Set Effect", Keyboard.KEY_Z, Minewatch.MODNAME);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		ModItems.registerRenders();
		MinecraftForge.EVENT_BUS.register(Minewatch.keyMode);
		ClientRegistry.registerKeyBinding(KeyToggleMode.TOGGLE_MODE);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}
	
	private void registerEntityRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityReaperBullet.class, RenderReaperBullet::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoArrow.class, RenderTippedArrow::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityAnaBullet.class, RenderAnaBullet::new);
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
