package twopiradians.minewatch.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import io.netty.buffer.Unpooled;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabVanilla;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
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
import twopiradians.minewatch.client.gui.tab.InventoryTab;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.client.particle.ParticleCustom;
import twopiradians.minewatch.client.particle.ParticleHanzoSonic;
import twopiradians.minewatch.client.particle.ParticleReaperTeleport;
import twopiradians.minewatch.client.particle.ParticleTrail;
import twopiradians.minewatch.client.render.entity.RenderFactory;
import twopiradians.minewatch.client.render.entity.RenderGenjiShuriken;
import twopiradians.minewatch.client.render.entity.RenderJunkratGrenade;
import twopiradians.minewatch.client.render.entity.RenderJunkratTrap;
import twopiradians.minewatch.client.render.entity.RenderMeiIcicle;
import twopiradians.minewatch.client.render.entity.RenderMercyBeam;
import twopiradians.minewatch.common.CommonProxy;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityAnaBullet;
import twopiradians.minewatch.common.entity.EntityAnaSleepDart;
import twopiradians.minewatch.common.entity.EntityBastionBullet;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.entity.EntityHanzoArrow;
import twopiradians.minewatch.common.entity.EntityHanzoScatterArrow;
import twopiradians.minewatch.common.entity.EntityHanzoSonicArrow;
import twopiradians.minewatch.common.entity.EntityJunkratGrenade;
import twopiradians.minewatch.common.entity.EntityJunkratTrap;
import twopiradians.minewatch.common.entity.EntityMcCreeBullet;
import twopiradians.minewatch.common.entity.EntityMeiBlast;
import twopiradians.minewatch.common.entity.EntityMeiIcicle;
import twopiradians.minewatch.common.entity.EntityMercyBeam;
import twopiradians.minewatch.common.entity.EntityMercyBullet;
import twopiradians.minewatch.common.entity.EntityReaperBullet;
import twopiradians.minewatch.common.entity.EntitySoldier76Bullet;
import twopiradians.minewatch.common.entity.EntitySoldier76HelixRocket;
import twopiradians.minewatch.common.entity.EntityTracerBullet;
import twopiradians.minewatch.common.entity.EntityWidowmakerBullet;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.weapon.ItemMercyWeapon;
import twopiradians.minewatch.common.sound.FollowingSound;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

public class ClientProxy extends CommonProxy {

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
		registerInventoryTab();
	}

	private static void registerInventoryTab() {
		if (TabRegistry.getTabList().size() == 0)
			TabRegistry.registerTab(new InventoryTabVanilla());

		TabRegistry.registerTab(new InventoryTab());
		MinecraftForge.EVENT_BUS.register(new TabRegistry());
	}

	private static void registerRenders() {
		for (Item item : ModItems.jsonModelItems)
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	//PORT change to event registration
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
						boolean turret = false;
						/*if (stack.hasTagCompound()) {
							EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(stack.getTagCompound().getUniqueId("player"));
							turret = player != null ? EnumHero.BASTION.ability2.isSelected(player) : false;
						}*/
						return new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + (turret ? "_1_3d" : "_0_3d"), "inventory");
					}
				});
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_0_3d", "inventory"));	
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_1_3d", "inventory"));
			}
		//change widowmaker model based on scoping
			else if (item == EnumHero.WIDOWMAKER.weapon) {
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {						
						boolean scoping = false;
						if (stack.hasTagCompound()) {
							EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(stack.getTagCompound().getUniqueId("player"));
							scoping = (player != null && player.getHeldItemMainhand() != null && 
									player.getHeldItemMainhand().getItem() == EnumHero.WIDOWMAKER.weapon &&
									(player.getActiveItemStack() == stack || Minewatch.keys.rmb(player)) && EnumHero.WIDOWMAKER.weapon.getCurrentAmmo(player) > 0);
						}
						return new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + (scoping ? "_scoping_3d" : "_3d"), "inventory");
					}
				});
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_3d", "inventory"));	
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_scoping_3d", "inventory"));
			}
		//change ana model based on scoping
			else if (item == EnumHero.ANA.weapon) {
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {						
						boolean scoping = false;
						if (stack.hasTagCompound()) {
							EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(stack.getTagCompound().getUniqueId("player"));
							scoping = (player != null && player.getHeldItemMainhand() != null && 
									player.getHeldItemMainhand().getItem() == EnumHero.ANA.weapon &&
									(player.getActiveItemStack() == stack || Minewatch.keys.rmb(player)) && EnumHero.ANA.weapon.getCurrentAmmo(player) > 0);		
						}
						return new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + (scoping ? "_scoping_3d" : "_3d"), "inventory");
					}
				});
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_3d", "inventory"));	
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_scoping_3d", "inventory"));
			}
		//change genji model based on active abilities
			else if (item == EnumHero.GENJI.weapon) {
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {						
						boolean sword = false;
						if (stack.hasTagCompound()) {
							EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(stack.getTagCompound().getUniqueId("player"));
							sword = player != null && TickHandler.hasHandler(player, Identifier.GENJI_SWORD);		
						}
						return new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + (sword ? "_sword_3d" : "_3d"), "inventory");
					}
				});
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_3d", "inventory"));	
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_sword_3d", "inventory"));
			}
		// change mercy model
			else if (item == EnumHero.MERCY.weapon) {
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {
						boolean battleMercy = !ItemMercyWeapon.isStaff(stack);
						return new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + (battleMercy ? "_1_3d" : "_0_3d"), "inventory");
					}
				});
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_0_3d", "inventory"));
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_1_3d", "inventory"));
			}
		// Reaper's tp
			else if (item == EnumHero.REAPER.weapon) {
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {
						boolean tping = false;
						if (stack.hasTagCompound()) {
							EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(stack.getTagCompound().getUniqueId("player"));
							Handler handler = TickHandler.getHandler(player, Identifier.REAPER_TELEPORT);
							tping = handler != null && handler.ticksLeft != -1;
						}
						return new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + (tping ? "_1_3d" : "_0_3d"), "inventory");
					}
				});
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_0_3d", "inventory"));
				ModelBakery.registerItemVariants(item, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_1_3d", "inventory"));
			}
			else
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5) + "_3d", "inventory"));	
	}

	private void registerEntityRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityReaperBullet.class, new RenderFactory(new Color(0xCC0000), 1, 1, 2));
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoArrow.class, new RenderFactory("hanzo_arrow"));
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoSonicArrow.class, new RenderFactory("hanzo_sonic_arrow"));
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoScatterArrow.class, new RenderFactory("hanzo_arrow"));
		RenderingRegistry.registerEntityRenderingHandler(EntityAnaBullet.class, new RenderFactory(new Color(0xE9D390), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityAnaSleepDart.class, new RenderFactory("soldier76_helix_rocket", 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityGenjiShuriken.class, RenderGenjiShuriken::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTracerBullet.class, new RenderFactory(new Color(0x73B5C5), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityMcCreeBullet.class, new RenderFactory(new Color(0x73B5C5), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntitySoldier76Bullet.class, new RenderFactory(new Color(0x73B5C5), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntitySoldier76HelixRocket.class, new RenderFactory("soldier76_helix_rocket", 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityBastionBullet.class, new RenderFactory(new Color(0xE9D390), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityMeiBlast.class, new RenderFactory());
		RenderingRegistry.registerEntityRenderingHandler(EntityMeiIcicle.class, RenderMeiIcicle::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityWidowmakerBullet.class, new RenderFactory(new Color(0xCC0000), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityMercyBullet.class, new RenderFactory(new Color(0xE9D390), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityMercyBeam.class, RenderMercyBeam::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityJunkratGrenade.class, RenderJunkratGrenade::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityJunkratTrap.class, RenderJunkratTrap::new);
	}

	@Override
	protected void registerEventListeners() {
		super.registerEventListeners();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void stitchEventPre(TextureStitchEvent.Pre event) {
		event.getMap().registerSprite(ParticleHanzoSonic.TEXTURE);
		event.getMap().registerSprite(ParticleTrail.TEXTURE);
		for (ResourceLocation loc : ParticleReaperTeleport.TEXTURES)
			event.getMap().registerSprite(loc);
		for (EnumParticle particle : EnumParticle.values()) {
			if (particle.variations == 1)
				event.getMap().registerSprite(particle.loc);
			else 
				for (int i=0; i<particle.variations; ++i)
					event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, particle.loc.getResourcePath()+"_"+i));
		}
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/mei_icicle"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/junkrat_trap"));
	}

	@Override
	public void playFollowingSound(Entity entity, SoundEvent event, SoundCategory category, float volume, float pitch, boolean repeat) {
		if (entity != null && event != null && category != null) {
			FollowingSound sound = new FollowingSound(entity, event, category, volume, pitch, repeat);
			Minecraft.getMinecraft().getSoundHandler().playSound(sound);
		}
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
	public void spawnParticlesTrail(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge, float initialAge, float alpha) {
		int i = Minecraft.getMinecraft().gameSettings.particleSetting;
		if (i == 0 || world.rand.nextInt(i*2) == 0) {
			ParticleTrail particle = new ParticleTrail(world, x, y, z, motionX, motionY, motionZ, color, colorFade, scale, maxAge, initialAge, alpha);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}

	@Override
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, Entity followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) { 
		if (!enumParticle.equals(EnumParticle.HEALTH) || !healthParticleEntities.contains(followEntity.getPersistentID())) {
			ParticleCustom particle = new ParticleCustom(enumParticle, world, followEntity, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
			if (enumParticle.equals(EnumParticle.HEALTH))
				healthParticleEntities.add(followEntity.getPersistentID());
		}
	}

	@Override
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) { 
		ParticleCustom particle = new ParticleCustom(enumParticle, world, x, y, z, motionX, motionY, motionZ, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}

	@Override
	public void spawnParticlesReaperTeleport(World world, EntityPlayer player, boolean spawnAtPlayer, int type) { 
		if (spawnAtPlayer || TickHandler.getHandler(player, Identifier.REAPER_TELEPORT) != null) {
			ParticleReaperTeleport particle = new ParticleReaperTeleport(world, player, spawnAtPlayer, type, 
					TickHandler.getHandler(player, Identifier.REAPER_TELEPORT));
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}

	@Override
	public UUID getClientUUID() {
		return Minecraft.getMinecraft().getSession().getProfile().getId();
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public void stopSound(EntityPlayer player, SoundEvent event, SoundCategory category) {
		if (player instanceof EntityPlayerSP) {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeString(category.getName());
			packetbuffer.writeString(event.getRegistryName().toString());
			((EntityPlayerSP)player).connection.handleCustomPayload(new SPacketCustomPayload("MC|StopSound", packetbuffer));
		}
		else
			super.stopSound(player, event, category);
	}

}