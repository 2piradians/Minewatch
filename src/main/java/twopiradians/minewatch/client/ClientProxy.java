package twopiradians.minewatch.client;

import java.util.ArrayList;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
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
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.client.particle.ParticleAnaHealth;
import twopiradians.minewatch.client.particle.ParticleHanzoSonic;
import twopiradians.minewatch.client.particle.ParticleMeiBlaster;
import twopiradians.minewatch.client.particle.ParticleSmoke;
import twopiradians.minewatch.client.particle.ParticleSpark;
import twopiradians.minewatch.client.particle.ParticleTrail;
import twopiradians.minewatch.client.render.entity.RenderAnaBullet;
import twopiradians.minewatch.client.render.entity.RenderBastionBullet;
import twopiradians.minewatch.client.render.entity.RenderGenjiShuriken;
import twopiradians.minewatch.client.render.entity.RenderHanzoArrow;
import twopiradians.minewatch.client.render.entity.RenderHanzoScatterArrow;
import twopiradians.minewatch.client.render.entity.RenderHanzoSonicArrow;
import twopiradians.minewatch.client.render.entity.RenderInvisible;
import twopiradians.minewatch.client.render.entity.RenderMcCreeBullet;
import twopiradians.minewatch.client.render.entity.RenderReaperBullet;
import twopiradians.minewatch.client.render.entity.RenderSoldier76Bullet;
import twopiradians.minewatch.client.render.entity.RenderSoldier76HelixRocket;
import twopiradians.minewatch.client.render.entity.RenderTracerBullet;
import twopiradians.minewatch.common.CommonProxy;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityAnaBullet;
import twopiradians.minewatch.common.entity.EntityBastionBullet;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.entity.EntityHanzoArrow;
import twopiradians.minewatch.common.entity.EntityHanzoScatterArrow;
import twopiradians.minewatch.common.entity.EntityHanzoSonicArrow;
import twopiradians.minewatch.common.entity.EntityMcCreeBullet;
import twopiradians.minewatch.common.entity.EntityMeiBlast;
import twopiradians.minewatch.common.entity.EntityReaperBullet;
import twopiradians.minewatch.common.entity.EntitySoldier76Bullet;
import twopiradians.minewatch.common.entity.EntitySoldier76HelixRocket;
import twopiradians.minewatch.common.entity.EntityTracerBullet;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ModItems;

public class ClientProxy extends CommonProxy
{
	public static ArrayList<UUID> healthParticleEntities = new ArrayList<UUID>();

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		OBJLoader.INSTANCE.addDomain(Minewatch.MODID);
		registerObjRenders();
		registerEntityRenders();
		Keys.HERO_INFORMATION = new KeyBinding("Hero Information", Keyboard.KEY_GRAVE, Minewatch.MODNAME);
		Keys.RELOAD = new KeyBinding("Reload", Keyboard.KEY_R, Minewatch.MODNAME);
		Keys.ABILITY_1 = new KeyBinding("Ability 1", Keyboard.KEY_LMENU, Minewatch.MODNAME);
		Keys.ABILITY_2 = new KeyBinding("Ability 2", Keyboard.KEY_C, Minewatch.MODNAME);
		Keys.ULTIMATE = new KeyBinding("Ultimate", Keyboard.KEY_Z, Minewatch.MODNAME);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		registerRenders();
		ClientRegistry.registerKeyBinding(Keys.HERO_INFORMATION);
		ClientRegistry.registerKeyBinding(Keys.RELOAD);
		ClientRegistry.registerKeyBinding(Keys.ABILITY_1);
		ClientRegistry.registerKeyBinding(Keys.ABILITY_2);
		ClientRegistry.registerKeyBinding(Keys.ULTIMATE);
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
			if (item == EnumHero.HANZO.weapon) {
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {
						int model = 0;
						if (stack.hasTagCompound()) {
							EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(stack.getTagCompound().getUniqueId("player"));
							if (player != null) {
								model = (int) ((float) (stack.getMaxItemUseDuration() - player.getItemInUseCount()) / 4.0F) + 1;
								if (player.getActiveItemStack() == null || !player.getActiveItemStack().equals(stack))
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
		// change soldier model when running
			else if (item == EnumHero.SOLDIER76.weapon) {
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {						
						boolean blocking = false;
						if (stack.hasTagCompound()) {
							EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(stack.getTagCompound().getUniqueId("player"));
							blocking = player != null ? player.isSprinting() : false;
						}
						return new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + (blocking ? "_blocking_3d" : "_3d"), "inventory");
					}
				});
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_3d", "inventory"));	
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_blocking_3d", "inventory"));
			}
		// change bastion model depending on form
			else if (item == EnumHero.BASTION.weapon) {
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {						
						boolean turret = false;//TODO
						if (stack.hasTagCompound()) {
							EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(stack.getTagCompound().getUniqueId("player"));
							turret = player != null ? EnumHero.BASTION.ability2.isSelected(player) : false;
						}
						return new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + (turret ? "_1_3d" : "_0_3d"), "inventory");
					}
				});
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_0_3d", "inventory"));	
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_1_3d", "inventory"));
			}
			else
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_3d", "inventory"));	
	}

	private void registerEntityRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityReaperBullet.class, RenderReaperBullet::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoArrow.class, RenderHanzoArrow::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoSonicArrow.class, RenderHanzoSonicArrow::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoScatterArrow.class, RenderHanzoScatterArrow::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityAnaBullet.class, RenderAnaBullet::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGenjiShuriken.class, RenderGenjiShuriken::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTracerBullet.class, RenderTracerBullet::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMcCreeBullet.class, RenderMcCreeBullet::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySoldier76Bullet.class, RenderSoldier76Bullet::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySoldier76HelixRocket.class, RenderSoldier76HelixRocket::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBastionBullet.class, RenderBastionBullet::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMeiBlast.class, RenderInvisible::new);
	}

	@Override
	protected void registerEventListeners() {
		super.registerEventListeners();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void stitcherEventPre(TextureStitchEvent.Pre event) {
		event.getMap().registerSprite(ParticleAnaHealth.TEXTURE);
		event.getMap().registerSprite(ParticleHanzoSonic.TEXTURE);
		event.getMap().registerSprite(ParticleTrail.TEXTURE);
		for (ResourceLocation loc : ParticleSmoke.TEXTURES)
			event.getMap().registerSprite(loc);
		for (ResourceLocation loc : ParticleSpark.TEXTURES)
			event.getMap().registerSprite(loc);
		event.getMap().registerSprite(ParticleMeiBlaster.TEXTURE);
	}

	/**Copied from Minecraft to allow Reinhardt to continue attacking while holding lmb*/
	@Override
	public void mouseClick() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.objectMouseOver != null && !mc.player.isRowingBoat()) {
			switch (mc.objectMouseOver.typeOfHit) {
			case ENTITY:
				mc.playerController.attackEntity(mc.player, mc.objectMouseOver.entityHit);
				break;
			case BLOCK:
			case MISS:
				mc.player.resetCooldown();
				net.minecraftforge.common.ForgeHooks.onEmptyLeftClick(mc.player);
			}
			mc.player.swingArm(EnumHand.MAIN_HAND);
		}
	}

	@Override
	public void spawnParticlesAnaHealth(EntityLivingBase entity) {
		if (!healthParticleEntities.contains(entity.getPersistentID())) {
			ParticleAnaHealth particle = new ParticleAnaHealth(entity);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
			healthParticleEntities.add(entity.getPersistentID());
		}
	}

	@Override
	public void spawnParticlesHanzoSonic(World world, double x, double y, double z, boolean isBig, boolean isFast) {
		ParticleHanzoSonic particle = new ParticleHanzoSonic(world, x, y, z, isBig, isFast);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}

	@Override
	public void spawnParticlesHanzoSonic(World world, Entity trackEntity, boolean isBig) {
		ParticleHanzoSonic particle = new ParticleHanzoSonic(world, trackEntity, isBig);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}

	@Override
	public void spawnParticlesTrail(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge) {
		int i = Minecraft.getMinecraft().gameSettings.particleSetting;
		if (i == 0 || world.rand.nextInt(i*2) == 0) {
			ParticleTrail particle = new ParticleTrail(world, x, y, z, motionX, motionY, motionZ, color, colorFade, scale, maxAge);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}

	@Override
	public void spawnParticlesSmoke(World world, double x, double y, double z, int color, int colorFade, float scale, int maxAge) {
		ParticleSmoke particle = new ParticleSmoke(world, x, y, z, color, colorFade, scale, maxAge);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}

	@Override
	public void spawnParticlesSpark(World world, double x, double y, double z, int color, int colorFade, float scale, int maxAge) {
		ParticleSpark particle = new ParticleSpark(world, x, y, z, color, colorFade, scale, maxAge);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}

	@Override
	public void spawnParticlesMeiBlaster(World world, double x, double y, double z, double motionX, double motionY, double motionZ, float alpha, int maxAge) { 
		ParticleMeiBlaster particle = new ParticleMeiBlaster(world, x, y, z, motionX, motionY, motionZ, alpha, maxAge);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}
}
