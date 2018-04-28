package twopiradians.minewatch.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import io.netty.buffer.Unpooled;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import twopiradians.minewatch.client.particle.ParticleCustom;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.recipe.ShapelessMatchingDamageRecipe;
import twopiradians.minewatch.common.sound.ModSoundEvents.ModSoundEvent;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;
import twopiradians.minewatch.packet.CPacketSyncKeys;
import twopiradians.minewatch.packet.CPacketSyncSkins;
import twopiradians.minewatch.packet.PacketSyncConfig;
import twopiradians.minewatch.packet.SPacketFollowingSound;
import twopiradians.minewatch.packet.SPacketSimple;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;
import twopiradians.minewatch.packet.SPacketSyncAmmo;
import twopiradians.minewatch.packet.SPacketSyncCooldown;
import twopiradians.minewatch.packet.SPacketSyncSkins;

public class CommonProxy {

	public enum EnumGui {
		WILDCARD, HERO_SELECT, TEAM_STICK, TEAM_SPAWN
	}

	public enum EnumParticle {
		CIRCLE, SLEEP, SMOKE, SPARK(1, 4, 0), HEALTH(true, true),
		EXPLOSION(16, 1, 0), ANA_HEAL, ANA_DAMAGE(1, 4, 0),
		JUNKRAT_TRAP(true), JUNKRAT_TRAP_TRIGGERED(true), 
		JUNKRAT_TRAP_DESTROYED(true),
		WIDOWMAKER_MINE(true), WIDOWMAKER_MINE_TRIGGERED(true), 
		WIDOWMAKER_MINE_DESTROYED(true),
		SOMBRA_TRANSPOSER(true), REINHARDT_STRIKE,
		HOLLOW_CIRCLE, ZENYATTA(4, 1, 0), ZENYATTA_HARMONY(true, true), ZENYATTA_DISCORD(true, true),
		ZENYATTA_DISCORD_ORB(4, 1, 0, false, true), ZENYATTA_HARMONY_ORB(4, 1, 0, false, true),
		HEALTH_PLUS(1, 1, -0.005f), REAPER_TELEPORT_BASE_0, MOIRA_DAMAGE(4, 1, 0), MOIRA_ORB, STUN,
		ANA_GRENADE_HEAL, ANA_GRENADE_DAMAGE, HOLLOW_CIRCLE_2, HOLLOW_CIRCLE_3, BEAM,
		REINHARDT_CHARGE, SOMBRA_HACK, SOMBRA_HACK_MESH(1, 4, 0), SOMBRA_HACK_NUMBERS,
		DOOMFIST_PUNCH_0, DOOMFIST_PUNCH_1, DOOMFIST_PUNCH_2, DOOMFIST_PUNCH_3, 
		DOOMFIST_SLAM_0(false, true), DOOMFIST_SLAM_1, DOOMFIST_SLAM_2,
		DEATH_BLOCK, PUSH_BLOCK, SOMBRA_ULTIMATE_0;

		public HashMap<UUID, Integer> particleEntities = Maps.newHashMap();
		/**List of particles with a facing - because they are rendered separately*/
		public ArrayList<ParticleCustom> facingParticles = new ArrayList<ParticleCustom>();

		public final ResourceLocation loc;
		public final ResourceLocation facingLoc;
		public final int frames;
		public final int variations;
		public final float gravity;
		public final boolean disableDepth;
		public final boolean onePerEntity;
		public TextureAtlasSprite sprite;

		private EnumParticle() {
			this(false);
		}

		private EnumParticle(boolean disableDepth) {
			this(1, 1, 0, disableDepth, false);
		}

		private EnumParticle(boolean disableDepth, boolean onePerEntity) {
			this(1, 1, 0, disableDepth, onePerEntity);
		}

		private EnumParticle(int frames, int variations, float gravity) {
			this(frames, variations, gravity, false, false);
		}

		private EnumParticle(int frames, int variations, float gravity, boolean disableDepth, boolean onePerEntity) {
			this.loc = new ResourceLocation(Minewatch.MODID, "entity/particle/"+this.name().toLowerCase());
			this.facingLoc = new ResourceLocation(Minewatch.MODID, "textures/entity/particle/"+this.name().toLowerCase()+".png");
			this.frames = frames;
			this.variations = variations;
			this.gravity = gravity;
			this.disableDepth = disableDepth;
			this.onePerEntity = onePerEntity;
		}
	}

	public void preInit(FMLPreInitializationEvent event) {
		Minewatch.logger = event.getModLog();
		Minewatch.configFile = event.getSuggestedConfigurationFile();
		registerPackets();
		Config.preInit(Minewatch.configFile);
		registerEventListeners();
		ModEntities.registerEntities();
	}

	public void init(FMLInitializationEvent event) {
		ModPotions.init();
		registerCraftingRecipes();
	}

	public void postInit(FMLPostInitializationEvent event) {}

	private void registerPackets() { // Side is where the packets goes TO
		int id = 0;
		Minewatch.network.registerMessage(CPacketSyncKeys.Handler.class, CPacketSyncKeys.class, id++, Side.SERVER);
		Minewatch.network.registerMessage(SPacketSyncAmmo.Handler.class, SPacketSyncAmmo.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(SPacketSyncCooldown.Handler.class, SPacketSyncCooldown.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(SPacketSimple.Handler.class, SPacketSimple.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(SPacketSyncAbilityUses.Handler.class, SPacketSyncAbilityUses.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(SPacketSyncSkins.Handler.class, SPacketSyncSkins.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(CPacketSyncSkins.Handler.class, CPacketSyncSkins.class, id++, Side.SERVER);
		Minewatch.network.registerMessage(PacketSyncConfig.HandlerServer.class, PacketSyncConfig.class, id++, Side.SERVER);
		Minewatch.network.registerMessage(PacketSyncConfig.HandlerClient.class, PacketSyncConfig.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(CPacketSimple.Handler.class, CPacketSimple.class, id++, Side.SERVER);
		Minewatch.network.registerMessage(SPacketFollowingSound.Handler.class, SPacketFollowingSound.class, id++, Side.CLIENT);
	}

	public void spawnParticlesHanzoSonic(World world, double x, double y, double z, boolean isBig, boolean isFast) {}
	public void spawnParticlesHanzoSonic(World world, Entity trackEntity, boolean isBig) {}
	public void spawnParticlesTrail(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge, float initialAge, float alpha) {}
	public void spawnParticlesMuzzle(EnumParticle enumParticle, World world, EntityLivingBase followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust) {}
	public void spawnParticlesMuzzle(EnumParticle enumParticle, World world, EntityLivingBase followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, @Nullable EnumHand hand, float verticalAdjust, float horizontalAdjust, float distance) {}
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, Entity followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) {}
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) {}	
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, EnumFacing facing, boolean renderOnBlocks) {}
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, float pulseRate, EnumFacing facing, boolean renderOnBlocks) {}	
	public void spawnParticlesReaperTeleport(World world, EntityLivingBase entityLiving, boolean spawnAtPlayer, int type) {}

	protected void registerEventListeners() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new Config());
		MinecraftForge.EVENT_BUS.register(new ItemMWToken(null));
		MinecraftForge.EVENT_BUS.register(new TickHandler());
		MinecraftForge.EVENT_BUS.register(new Handlers());
	}

	private void registerCraftingRecipes() {
		for (EnumHero hero : EnumHero.values()) {
			ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(hero.helmet), 
					new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(hero.token))); 
					add(Ingredient.fromStacks(new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, hero.name.toLowerCase()+"_helmet"));
			ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(hero.chestplate), 
					new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(hero.token))); 
					add(Ingredient.fromStacks(new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, hero.name.toLowerCase()+"_chestplate"));
			ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(hero.leggings), 
					new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(hero.token))); 
					add(Ingredient.fromStacks(new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, hero.name.toLowerCase()+"_leggings"));
			ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(hero.boots), 
					new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(hero.token))); 
					add(Ingredient.fromStacks(new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));}}).setRegistryName(Minewatch.MODID, hero.name.toLowerCase()+"_boots"));
			ForgeRegistries.RECIPES.register(new ShapelessMatchingDamageRecipe(Minewatch.MODNAME, new ItemStack(hero.weapon, hero.weapon.hasOffhand ? 2 : 1), 
					new NonNullList<Ingredient>() {{add(Ingredient.fromStacks(new ItemStack(hero.token)));}}).setRegistryName(Minewatch.MODID, hero.name.toLowerCase()+"_weapon"));

		}
	}

	@SubscribeEvent(receiveCanceled=true)
	public void commandDev(CommandEvent event) {
		try {
			if ((event.getCommand().getName().equalsIgnoreCase("mwdev") || event.getCommand().getName().equalsIgnoreCase("minewatchdev")) && 
					event.getCommand().checkPermission(event.getSender().getServer(), event.getSender()) &&
					CommandDev.runCommand(event.getSender().getServer(), event.getSender(), event.getParameters())) 
				event.setCanceled(true);
		}
		catch (Exception e) {}
	}

	public void mouseClick() {}

	public UUID getClientUUID() {
		return null;
	}

	public EntityPlayer getClientPlayer() {
		return null;
	}
	
	@Nullable
	public Object playFollowingSound(EntityPlayer player, Entity entity, ModSoundEvent sound, SoundCategory category, float volume, float pitch, boolean repeat, int attentuationType) {
		if (entity != null && entity.isEntityAlive() && sound != null && category != null && player instanceof EntityPlayerMP) 
			Minewatch.network.sendTo(new SPacketFollowingSound(entity, sound, category, volume, pitch, repeat, attentuationType), (EntityPlayerMP) player);
		return null;
	}

	@Nullable
	public Object playFollowingSound(Entity entity, ModSoundEvent sound, SoundCategory category, float volume, float pitch, boolean repeat, int attentuationType) {
		if (entity != null && entity.isEntityAlive() && sound != null && category != null) 
			Minewatch.network.sendToDimension(new SPacketFollowingSound(entity, sound, category, volume, pitch, repeat, attentuationType), entity.world.provider.getDimension());
		return null;
	}

	public void stopFollowingSound(Entity followingEntity, ModSoundEvent event) {
		if (followingEntity != null && event != null && !followingEntity.world.isRemote) 
			Minewatch.network.sendToDimension(new SPacketSimple(57, followingEntity, event.getSoundName().toString()), followingEntity.world.provider.getDimension());
	}

	public void stopFollowingSound(Entity followingEntity, String event) {
		if (followingEntity != null && event != null && !followingEntity.world.isRemote) 
			Minewatch.network.sendToDimension(new SPacketSimple(57, followingEntity, event), followingEntity.world.provider.getDimension());
	}

	public void stopSound(EntityPlayer player, ModSoundEvent event, SoundCategory category) {
		if (player instanceof EntityPlayerMP) {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeString(category.getName());
			packetbuffer.writeString(event.getRegistryName().toString());
			((EntityPlayerMP)player).connection.sendPacket(new SPacketCustomPayload("MC|StopSound", packetbuffer));
		}
	}
	
	public void createExplosion(World world, Entity damageSource, double x, double y, double z, float size, float exploderDamage, float minDamage, float maxDamage, @Nullable Entity directHit, float directHitDamage, boolean resetHurtResist, float exploderKnockback, float knockback) {
		createExplosion(world, damageSource, x, y, z, size, exploderDamage, minDamage, maxDamage, directHit, directHitDamage, resetHurtResist, exploderKnockback, exploderKnockback, knockback, knockback);
	}
	
	public void createExplosion(World world, Entity damageSource, double x, double y, double z, float size, float exploderDamage, float minDamage, float maxDamage, @Nullable Entity directHit, float directHitDamage, boolean resetHurtResist, float exploderKnockback, float knockback, boolean giveUltCharge) {
		createExplosion(world, damageSource, x, y, z, size, exploderDamage, minDamage, maxDamage, directHit, directHitDamage, resetHurtResist, exploderKnockback, exploderKnockback, knockback, knockback, giveUltCharge);
	}
	
	public void createExplosion(World world, Entity damageSource, double x, double y, double z, float size, float exploderDamage, float minDamage, float maxDamage, @Nullable Entity directHit, float directHitDamage, boolean resetHurtResist, float exploderKnockbackVertical, float exploderKnockbackHorizontal, float knockbackVertical, float knockbackHorizontal) {
		createExplosion(world, damageSource, x, y, z, size, exploderDamage, minDamage, maxDamage, directHit, directHitDamage, resetHurtResist, exploderKnockbackVertical, exploderKnockbackHorizontal, knockbackVertical, knockbackHorizontal, true);
	}

	/**Modified from {@link Explosion#doExplosionA()} && {@link Explosion#doExplosionB(boolean)}*/
	public void createExplosion(World world, Entity damageSource, double x, double y, double z, float size, float exploderDamage, float minDamage, float maxDamage, @Nullable Entity directHit, float directHitDamage, boolean resetHurtResist, float exploderKnockbackVertical, float exploderKnockbackHorizontal, float knockbackVertical, float knockbackHorizontal, boolean giveUltCharge) {
		if (!world.isRemote) {
			Entity actualThrower = EntityHelper.getThrower(damageSource);
			Explosion explosion = new Explosion(world, actualThrower, x, y, z, size, false, false);

			float diameter = size * 2.0F;
			int k1 = MathHelper.floor(x - (double)diameter - 1.0D);
			int l1 = MathHelper.floor(x + (double)diameter + 1.0D);
			int i2 = MathHelper.floor(y - (double)diameter - 1.0D);
			int i1 = MathHelper.floor(y + (double)diameter + 1.0D);
			int j2 = MathHelper.floor(z - (double)diameter - 1.0D);
			int j1 = MathHelper.floor(z + (double)diameter + 1.0D);
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
			net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(world, explosion, list, diameter);

			for (int k2 = 0; k2 < list.size(); ++k2) {
				Entity entity = (Entity)list.get(k2);

				if (!entity.isImmuneToExplosions() && (!(entity instanceof EntityLivingBase) || 
						((EntityLivingBase)entity).getHealth() > 0) && !TickHandler.hasHandler(entity, Identifier.TEAM_SPAWN_IN_RANGE)) {
					double distance = EntityHelper.getDistance(new Vec3d(x, y, z), entity);
					if (distance <= size) {
						double diffX = entity.posX - x;
						double diffY = entity.posY + (double)entity.getEyeHeight() - y;
						double diffZ = entity.posZ - z;
						double diffAverage = (double)MathHelper.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);

						if (diffAverage != 0.0D) {
							diffX = diffX / diffAverage;
							diffY = diffY / diffAverage;
							diffZ = diffZ / diffAverage; 
							float damage = (float) (entity == actualThrower ? exploderDamage : entity == directHit ? directHitDamage : minDamage+(1f-distance/size)*(maxDamage-minDamage));
							if (damage == 0 || EntityHelper.attemptDamage(damageSource, actualThrower, entity, damage, true, true, entity == actualThrower, giveUltCharge, DamageSource.causeExplosionDamage(explosion)) ||
									entity == actualThrower) {
								if (resetHurtResist)
									entity.hurtResistantTime = 0;

								double kbHorizontal = (1f-distance/size) * (entity == actualThrower ? exploderKnockbackHorizontal : knockbackHorizontal);
								double kbVertical = (1f-distance/size) * (entity == actualThrower ? exploderKnockbackVertical : knockbackVertical);
								entity.motionX += diffX * kbHorizontal;
								entity.motionY += diffY * kbVertical;
								entity.motionZ += diffZ * kbHorizontal;
								entity.velocityChanged = true;
							}
						}
					}
				}
			}
		}
	}

	public float getRenderPartialTicks() {
		return 1;
	}

	public void openGui(EnumGui gui) {}

	public void openGui(EnumGui gui, Object... obj) {}

	public Handler onHandlerRemove(boolean isRemote, Handler handler) {
		return handler.onServerRemove();
	}

	public Entity getRenderViewEntity() {
		return null;
	}

	public boolean isPlayerInFirstPerson() {
		return false;
	}

	public void updateFOV() {}

	public void setThirdPersonView(int mode) {}

	public boolean isSinglePlayer() {
		return false;
	}

	public int getParticleSettings() {
		return 2;
	}

	public void onSetChanged(EntityLivingBase player, @Nullable EnumHero prevHero, @Nullable EnumHero newHero) {}

	public void reassignRunKeybind(boolean reassign) {}
}