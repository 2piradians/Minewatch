package twopiradians.minewatch.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableMap;

import io.netty.buffer.Unpooled;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabVanilla;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Material;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.minewatch.client.gui.heroSelect.GuiHeroSelect;
import twopiradians.minewatch.client.gui.tab.InventoryTab;
import twopiradians.minewatch.client.gui.teamBlocks.GuiTeamSpawn;
import twopiradians.minewatch.client.gui.teamStick.GuiTeamStick;
import twopiradians.minewatch.client.gui.wildCard.GuiWildCard;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.BakedMWItem;
import twopiradians.minewatch.client.particle.ParticleCustom;
import twopiradians.minewatch.client.particle.ParticleHanzoSonic;
import twopiradians.minewatch.client.particle.ParticleReaperTeleport;
import twopiradians.minewatch.client.particle.ParticleTrail;
import twopiradians.minewatch.client.render.entity.RenderAnaGrenade;
import twopiradians.minewatch.client.render.entity.RenderFactory;
import twopiradians.minewatch.client.render.entity.RenderGenjiShuriken;
import twopiradians.minewatch.client.render.entity.RenderHero;
import twopiradians.minewatch.client.render.entity.RenderJunkratGrenade;
import twopiradians.minewatch.client.render.entity.RenderJunkratMine;
import twopiradians.minewatch.client.render.entity.RenderJunkratTrap;
import twopiradians.minewatch.client.render.entity.RenderLucioSonic;
import twopiradians.minewatch.client.render.entity.RenderMcCreeStun;
import twopiradians.minewatch.client.render.entity.RenderMeiCrystal;
import twopiradians.minewatch.client.render.entity.RenderMeiIcicle;
import twopiradians.minewatch.client.render.entity.RenderMercyBeam;
import twopiradians.minewatch.client.render.entity.RenderMoiraOrb;
import twopiradians.minewatch.client.render.entity.RenderReinhardtStrike;
import twopiradians.minewatch.client.render.entity.RenderSoldier76Heal;
import twopiradians.minewatch.client.render.entity.RenderSombraTranslocator;
import twopiradians.minewatch.client.render.entity.RenderWidowmakerHook;
import twopiradians.minewatch.client.render.entity.RenderWidowmakerMine;
import twopiradians.minewatch.client.render.entity.RenderZenyattaOrb;
import twopiradians.minewatch.client.render.tileentity.TileEntityHealthPackRenderer;
import twopiradians.minewatch.client.render.tileentity.TileEntityOBJRenderer;
import twopiradians.minewatch.client.render.tileentity.TileEntityTeamSpawnRenderer;
import twopiradians.minewatch.common.CommonProxy;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.block.ModBlocks;
import twopiradians.minewatch.common.block.teamBlocks.BlockTeam;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.ability.EntityAnaGrenade;
import twopiradians.minewatch.common.entity.ability.EntityAnaSleepDart;
import twopiradians.minewatch.common.entity.ability.EntityHanzoScatterArrow;
import twopiradians.minewatch.common.entity.ability.EntityHanzoSonicArrow;
import twopiradians.minewatch.common.entity.ability.EntityJunkratMine;
import twopiradians.minewatch.common.entity.ability.EntityJunkratTrap;
import twopiradians.minewatch.common.entity.ability.EntityMcCreeStun;
import twopiradians.minewatch.common.entity.ability.EntityMeiCrystal;
import twopiradians.minewatch.common.entity.ability.EntityMeiIcicle;
import twopiradians.minewatch.common.entity.ability.EntityMercyBeam;
import twopiradians.minewatch.common.entity.ability.EntityMoiraOrb;
import twopiradians.minewatch.common.entity.ability.EntityReinhardtStrike;
import twopiradians.minewatch.common.entity.ability.EntitySoldier76Heal;
import twopiradians.minewatch.common.entity.ability.EntitySoldier76HelixRocket;
import twopiradians.minewatch.common.entity.ability.EntitySombraTranslocator;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerHook;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerMine;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityAnaBullet;
import twopiradians.minewatch.common.entity.projectile.EntityBastionBullet;
import twopiradians.minewatch.common.entity.projectile.EntityDoomfistBullet;
import twopiradians.minewatch.common.entity.projectile.EntityGenjiShuriken;
import twopiradians.minewatch.common.entity.projectile.EntityHanzoArrow;
import twopiradians.minewatch.common.entity.projectile.EntityJunkratGrenade;
import twopiradians.minewatch.common.entity.projectile.EntityLucioSonic;
import twopiradians.minewatch.common.entity.projectile.EntityMcCreeBullet;
import twopiradians.minewatch.common.entity.projectile.EntityMeiBlast;
import twopiradians.minewatch.common.entity.projectile.EntityMercyBullet;
import twopiradians.minewatch.common.entity.projectile.EntityMoiraHealEnergy;
import twopiradians.minewatch.common.entity.projectile.EntityReaperBullet;
import twopiradians.minewatch.common.entity.projectile.EntitySoldier76Bullet;
import twopiradians.minewatch.common.entity.projectile.EntitySombraBullet;
import twopiradians.minewatch.common.entity.projectile.EntityTracerBullet;
import twopiradians.minewatch.common.entity.projectile.EntityWidowmakerBullet;
import twopiradians.minewatch.common.entity.projectile.EntityZenyattaOrb;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.ServerManager;
import twopiradians.minewatch.common.item.IChangingModel;
import twopiradians.minewatch.common.item.ItemTeamStick;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.sound.FollowingSound;
import twopiradians.minewatch.common.sound.ModSoundEvents.ModSoundEvent;
import twopiradians.minewatch.common.tileentity.TileEntityHealthPack;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ServerManager.lookUpServers();
		OBJLoader.INSTANCE.addDomain(Minewatch.MODID);
		registerWeaponRenders();
		registerEntityRenders();
		createKeybinds();
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		registerBlockRenders();
		registerNonWeaponRenders();
		for (KeyBind key : KeyBind.values())
			if (key.keyBind != null)
				ClientRegistry.registerKeyBinding(key.keyBind);
		registerColoredItems();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		registerInventoryTab();
	}

	/**Replace OBJBakedModels with BakedMWItems*/
	@SubscribeEvent
	public void modelBake(ModelBakeEvent event) {
		for (ModelResourceLocation modelLocation : event.getModelRegistry().getKeys()) 
			if (modelLocation.getResourceDomain().equals(Minewatch.MODID) &&
					(modelLocation.getResourcePath().contains("3d") || modelLocation.getResourcePath().contains("health_pack"))) {
				if (event.getModelRegistry().getObject(modelLocation) instanceof OBJBakedModel) {
					OBJBakedModel model = (OBJBakedModel) event.getModelRegistry().getObject(modelLocation);
					IModelState state = model.getState();
					// only show parts in item for health packs
					if (modelLocation.getResourcePath().contains("health_pack_small")) 
						state = new TileEntityOBJRenderer.OBJModelState(state, "base", "small");
					else if (modelLocation.getResourcePath().contains("health_pack_large")) 
						state = new TileEntityOBJRenderer.OBJModelState(state, "base", "large");
					event.getModelRegistry().putObject(modelLocation, new BakedMWItem(model.getModel(), state, DefaultVertexFormats.ITEM, getTextures(model.getModel())));
				}
			}
	}

	public static ImmutableMap<String, TextureAtlasSprite> getTextures(OBJModel model) {
		ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
		builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
		TextureAtlasSprite missing = ModelLoader.defaultTextureGetter().apply(new ResourceLocation("missingno"));
		for (String materialName : model.getMatLib().getMaterialNames()) {
			Material material = model.getMatLib().getMaterial(materialName);
			if (material.getTexture().getTextureLocation().getResourcePath().startsWith("#")) {
				// PORT 1.10.2: bigWarning
				FMLLog.severe("OBJLoaderMW: Unresolved texture '%s' for obj model '%s'", material.getTexture().getTextureLocation().getResourcePath(), model.toString());
				builder.put(materialName, missing);
			}
			else
				builder.put(materialName, ModelLoader.defaultTextureGetter().apply(material.getTexture().getTextureLocation()));
		}
		builder.put("missingno", missing);
		return builder.build();
	}

	private void createKeybinds() {
		KeyBind.HERO_INFORMATION.keyBind = new KeyBinding("Hero Information", Keyboard.KEY_GRAVE, Minewatch.MODNAME);
		KeyBind.RELOAD.keyBind = new KeyBinding("Reload", Keyboard.KEY_R, Minewatch.MODNAME);
		KeyBind.ABILITY_1.keyBind = new KeyBinding("Ability 1", Keyboard.KEY_LMENU, Minewatch.MODNAME);
		KeyBind.ABILITY_2.keyBind = new KeyBinding("Ability 2", Keyboard.KEY_C, Minewatch.MODNAME);
		KeyBind.ULTIMATE.keyBind = new KeyBinding("Ultimate", Keyboard.KEY_Z, Minewatch.MODNAME);	
		KeyBind.CHANGE_HERO.keyBind = new KeyBinding("Change Hero at Team Spawn", Keyboard.KEY_H, Minewatch.MODNAME);
	}

	private void registerColoredItems() {
		for (IChangingModel item : ModItems.changingModelItems)
			Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
				@Override
				public int getColorFromItemstack(ItemStack stack, int tintIndex) {
					return item.getColorFromItemStack(stack, tintIndex);
				}
			}, item.getItem());
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				return ItemTeamStick.getColorFromItemStack(stack, tintIndex);
			}
		}, ModItems.team_stick);
	}

	private void registerInventoryTab() {
		if (TabRegistry.getTabList().size() == 0)
			TabRegistry.registerTab(new InventoryTabVanilla());

		TabRegistry.registerTab(InventoryTab.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new TabRegistry());
	}

	private void registerNonWeaponRenders() {
		for (Item item : ModItems.staticModelItems)
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(Minewatch.MODID+":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	//PORT change to event registration
	private void registerWeaponRenders() {		
		for (IChangingModel item : ModItems.changingModelItems) {
			String loc = Minewatch.MODID+":" + item.getItem().getUnlocalizedName().substring(5);
			for (String modelLoc : item.getAllModelLocations(new ArrayList<String>())) {
				ModelBakery.registerItemVariants(item.getItem(), new ModelResourceLocation(loc+modelLoc, "inventory"));
				ModelBakery.registerItemVariants(item.getItem(), new ModelResourceLocation(loc+modelLoc+"_3d", "inventory"));
			}
			ModelLoader.setCustomMeshDefinition(item.getItem(), new ItemMeshDefinition() {
				@Override
				public ModelResourceLocation getModelLocation(ItemStack stack) {
					return new ModelResourceLocation(loc + 
							item.getModelLocation(stack, ItemMWWeapon.getEntity(Minecraft.getMinecraft().world, stack)) + 
							(Config.useObjModels ? "_3d" : ""), "inventory");
				}
			});
		}
	}

	private void registerEntityRenders() {
		// heroes
		RenderingRegistry.registerEntityRenderingHandler(EntityHero.class, new RenderFactory());
		for (EnumHero hero : EnumHero.values())
			RenderingRegistry.registerEntityRenderingHandler(hero.heroClass, RenderHero::new);

		// projectiles and abilities
		RenderingRegistry.registerEntityRenderingHandler(EntityReaperBullet.class, new RenderFactory(new Color(0xCC0000), 1, 1, 2));
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoArrow.class, new RenderFactory("hanzo_arrow"));
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoSonicArrow.class, new RenderFactory("hanzo_sonic_arrow"));
		RenderingRegistry.registerEntityRenderingHandler(EntityHanzoScatterArrow.class, new RenderFactory("hanzo_arrow"));
		RenderingRegistry.registerEntityRenderingHandler(EntityAnaBullet.class, new RenderFactory(new Color(0xE9D390), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityAnaSleepDart.class, new RenderFactory("soldier76_helix_rocket", 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityAnaGrenade.class, RenderAnaGrenade::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGenjiShuriken.class, RenderGenjiShuriken::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTracerBullet.class, new RenderFactory(new Color(0x73B5C5), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityMcCreeBullet.class, new RenderFactory(new Color(0x73B5C5), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityMcCreeStun.class, RenderMcCreeStun::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySoldier76Bullet.class, new RenderFactory(new Color(0x73B5C5), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntitySoldier76HelixRocket.class, new RenderFactory("soldier76_helix_rocket", 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntitySoldier76Heal.class, RenderSoldier76Heal::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBastionBullet.class, new RenderFactory(new Color(0xE9D390), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityMeiBlast.class, new RenderFactory());
		RenderingRegistry.registerEntityRenderingHandler(EntityMeiIcicle.class, RenderMeiIcicle::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMeiCrystal.class, RenderMeiCrystal::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityWidowmakerBullet.class, new RenderFactory(new Color(0xCC0000), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityWidowmakerMine.class, RenderWidowmakerMine::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityWidowmakerHook.class, RenderWidowmakerHook::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMercyBullet.class, new RenderFactory(new Color(0xE9D390), 1, 1, 3));
		RenderingRegistry.registerEntityRenderingHandler(EntityMercyBeam.class, RenderMercyBeam::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityJunkratGrenade.class, RenderJunkratGrenade::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityJunkratTrap.class, RenderJunkratTrap::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityJunkratMine.class, RenderJunkratMine::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySombraBullet.class, new RenderFactory(new Color(0xFFF1F1), 1, 1, 2));
		RenderingRegistry.registerEntityRenderingHandler(EntitySombraTranslocator.class, RenderSombraTranslocator::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityReinhardtStrike.class, RenderReinhardtStrike::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityLucioSonic.class, RenderLucioSonic::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityZenyattaOrb.class, RenderZenyattaOrb::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMoiraHealEnergy.class, new RenderFactory());
		RenderingRegistry.registerEntityRenderingHandler(EntityMoiraOrb.class, RenderMoiraOrb::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDoomfistBullet.class, new RenderFactory(new Color(0xEFFFF4), 1, 1, 2));
	}

	private void registerBlockRenders() {
		for (Block block : ModBlocks.allBlocks) {
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(block), 0, 
					new ModelResourceLocation(Minewatch.MODID + ":" + block.getUnlocalizedName().substring(5), "inventory"));
			if (block instanceof BlockTeam)
				Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
		            public int colorMultiplier(IBlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int tintIndex) {
		            	return ((BlockTeam)block).colorMultiplier(state, world, pos, tintIndex);
		            }
		        }, block);
		}
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHealthPack.class, new TileEntityHealthPackRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeamSpawn.class, new TileEntityTeamSpawnRenderer());
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
				particle.sprite = event.getMap().registerSprite(particle.loc);
			else 
				for (int i=0; i<particle.variations; ++i)
					particle.sprite = event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, particle.loc.getResourcePath()+"_"+i));
		}
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/mei_icicle"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/mei_crystal"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/junkrat_trap"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/junkrat_mine"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/widowmaker_mine_blue"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/widowmaker_mine_red"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/sombra_translocator"));
		for (int i=0; i<6; ++i)
			event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/reinhardt_strike_"+i));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/lucio_sonic"));
		RenderMoiraOrb.sprite = event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/particle/moira_orb"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/mccree_stun"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/ana_grenade"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/soldier76_heal_0"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/soldier76_heal_1"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/widowmaker_hook"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/widowmaker_hook_rope"));
		event.getMap().registerSprite(new ResourceLocation(Minewatch.MODID, "entity/sombra_hack"));
	}

	@Override
	@Nullable
	public Object playFollowingSound(Entity entity, ModSoundEvent event, SoundCategory category, float volume, float pitch, boolean repeat) {
		if (entity != null && entity.isEntityAlive() && event != null && category != null && entity.world.isRemote) {
			FollowingSound sound = new FollowingSound(entity, event, category, volume, pitch, repeat);
			Minecraft.getMinecraft().getSoundHandler().playSound(sound);
			return sound;
		}
		else
			return super.playFollowingSound(entity, event, category, volume, pitch, repeat);
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
	public void spawnParticlesMuzzle(EnumParticle enumParticle, World world, EntityLivingBase followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust) { 
		spawnParticlesMuzzle(enumParticle, world, followEntity, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed, hand, verticalAdjust, horizontalAdjust, 1);
	}

	@Override
	public void spawnParticlesMuzzle(EnumParticle enumParticle, World world, EntityLivingBase followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust, float distance) { 
		ParticleCustom particle = new ParticleCustom(enumParticle, world, followEntity, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed, hand, verticalAdjust, horizontalAdjust, distance);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}

	@Override
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, Entity followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) { 
		// if not onePerEntity or entity doesn't have particle, OR the particle's tickExisted is outdated (particle might have been killed)
		if ((enumParticle.onePerEntity && enumParticle.particleEntities.containsKey(followEntity.getPersistentID()) &&
				Math.abs(enumParticle.particleEntities.get(followEntity.getPersistentID())-followEntity.ticksExisted) > 5) ||
				(!enumParticle.onePerEntity || !enumParticle.particleEntities.containsKey(followEntity.getPersistentID()))) {
			ParticleCustom particle = new ParticleCustom(enumParticle, world, followEntity, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
			if (enumParticle.onePerEntity)
				enumParticle.particleEntities.put(followEntity.getPersistentID(), followEntity.ticksExisted);
		}
	}

	@Override
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) { 
		this.spawnParticlesCustom(enumParticle, world, x, y, z, motionX, motionY, motionZ, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed, null, false);
	}

	@Override
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, EnumFacing facing, boolean renderOnBlocks) { 
		this.spawnParticlesCustom(enumParticle, world, x, y, z, motionX, motionY, motionZ, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed, 0, facing, renderOnBlocks);
	}

	@Override
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, float pulseRate, EnumFacing facing, boolean renderOnBlocks) { 
		ParticleCustom particle = new ParticleCustom(enumParticle, world, x, y, z, motionX, motionY, motionZ, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed, pulseRate, facing, renderOnBlocks);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}

	@Override
	public void spawnParticlesReaperTeleport(World world, EntityLivingBase player, boolean spawnAtPlayer, int type) { 
		if (spawnAtPlayer || TickHandler.getHandler(player, Identifier.REAPER_TELEPORT) != null) {
			ParticleReaperTeleport particle = new ParticleReaperTeleport(world, player, spawnAtPlayer, type, 
					TickHandler.getHandler(player, Identifier.REAPER_TELEPORT));
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}

	@Override
	public UUID getClientUUID() {
		return this.getClientPlayer() == null ? Minecraft.getMinecraft().getSession().getProfile().getId() :
			this.getClientPlayer().getPersistentID();
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public float getRenderPartialTicks() {
		return Minecraft.getMinecraft().getRenderPartialTicks();
	}

	@Override
	public void stopFollowingSound(Entity followingEntity, ModSoundEvent event) {
		if (followingEntity != null && followingEntity.world.isRemote) 
			FollowingSound.stopPlaying(event, followingEntity);
		else
			super.stopFollowingSound(followingEntity, event);
	}

	@Override
	public void stopFollowingSound(Entity followingEntity, String event) {
		if (followingEntity != null && followingEntity.world.isRemote) 
			FollowingSound.stopPlaying(event, followingEntity);
		else
			super.stopFollowingSound(followingEntity, event);
	}

	@Override
	public void stopSound(EntityPlayer player, ModSoundEvent event, SoundCategory category) {
		if (player instanceof EntityPlayerSP) {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeString(category.getName());
			packetbuffer.writeString(event.getRegistryName().toString());
			((EntityPlayerSP)player).connection.handleCustomPayload(new SPacketCustomPayload("MC|StopSound", packetbuffer));
		}
		else
			super.stopSound(player, event, category);
	}

	@Override
	public void openGui(EnumGui gui) {
		this.openGui(gui, new Object[0]);
	}

	@Override
	public void openGui(EnumGui gui, @Nullable Object... objs) {
		switch (gui) {
		case WILDCARD:
			Minecraft.getMinecraft().displayGuiScreen(new GuiWildCard());
			break;
		case TEAM_STICK:
			if (Minecraft.getMinecraft().objectMouseOver.entityHit == null)
				Minecraft.getMinecraft().displayGuiScreen(new GuiTeamStick());
			break;
		case TEAM_SPAWN:
			if (objs.length == 1 && objs[0] instanceof TileEntityTeamSpawn)
				Minecraft.getMinecraft().displayGuiScreen(new GuiTeamSpawn((TileEntityTeamSpawn) objs[0]));
			break;
		case HERO_SELECT:
			Minecraft.getMinecraft().displayGuiScreen(new GuiHeroSelect());
			break;
		}
	}

	@Override
	public Handler onHandlerRemove(boolean isRemote, Handler handler) {
		return isRemote ? handler.onClientRemove() : handler.onServerRemove();
	}

	@Override
	public Entity getRenderViewEntity() {
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		return entity == null ? Minecraft.getMinecraft().player : entity;
	}

	@Override
	public boolean isPlayerInFirstPerson() {
		return Minecraft.getMinecraft().gameSettings.thirdPersonView == 0;
	}

	@Override
	public void updateFOV() {
		Entity entity = this.getRenderViewEntity();
		if (entity != null)
			Minecraft.getMinecraft().world.markBlockRangeForRenderUpdate(entity.getPosition().add(-100, -100, -100), entity.getPosition().add(100, 100, 100));
	}
}