package twopiradians.minewatch.common;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import twopiradians.minewatch.common.block.ModBlocks;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.recipe.ShapelessMatchingDamageRecipe;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.sound.ModSoundEvents.ModSoundEvent;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
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
		WILDCARD, TEAM_STICK
	}
	
	public enum EnumParticle {
		CIRCLE, SLEEP, SMOKE(4, 1, 0), SPARK(1, 4, 0), HEALTH(true, true),
		EXPLOSION(16, 1, 0), ANA_HEAL, ANA_DAMAGE(1, 4, 0),
		JUNKRAT_TRAP(true), JUNKRAT_TRAP_TRIGGERED(true), 
		JUNKRAT_TRAP_DESTROYED(true),
		WIDOWMAKER_MINE(true), WIDOWMAKER_MINE_TRIGGERED(true), 
		WIDOWMAKER_MINE_DESTROYED(true),
		SOMBRA_TRANSPOSER(true), REINHARDT_STRIKE,
		HOLLOW_CIRCLE, ZENYATTA(4, 1, 0), ZENYATTA_HARMONY(true, true), ZENYATTA_DISCORD(true, true),
		ZENYATTA_DISCORD_ORB(4, 1, 0, false, true), ZENYATTA_HARMONY_ORB(4, 1, 0, false, true),
		HEALTH_PLUS(1, 1, -0.005f);

		public HashSet<UUID> particleEntities = new HashSet();
		
		public final ResourceLocation loc;
		public final int frames;
		public final int variations;
		public final float gravity;
		public final boolean disableDepth;
		public final boolean onePerEntity;

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
			this.frames = frames;
			this.variations = variations;
			this.gravity = gravity;
			this.disableDepth = disableDepth;
			this.onePerEntity = onePerEntity;
		}
	}

	//PORT add registerEventListeners(); AND REMOVE THE OLD ONE IN init()
	public void preInit(FMLPreInitializationEvent event) {
		Minewatch.logger = event.getModLog();
		Minewatch.configFile = event.getSuggestedConfigurationFile();
		Config.preInit(Minewatch.configFile);
		registerPackets();
		ModEntities.registerEntities();
		ModItems.preInit();
		ModBlocks.preInit();
		ModSoundEvents.preInit();
	}

	public void init(FMLInitializationEvent event) {
		ModPotions.init();
		registerEventListeners();
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
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, Entity followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) {}
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) {}	
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, EnumFacing facing) {}
	public void spawnParticlesCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, float pulseRate, EnumFacing facing) {}	
	public void spawnParticlesReaperTeleport(World world, EntityLivingBase entityLiving, boolean spawnAtPlayer, int type) {}

	protected void registerEventListeners() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new Config());
		MinecraftForge.EVENT_BUS.register(new ItemMWToken());
		MinecraftForge.EVENT_BUS.register(new TickHandler());
		MinecraftForge.EVENT_BUS.register(new Handlers());
	}

	private void registerCraftingRecipes() {
		RecipeSorter.register("Matching Damage Recipe", ShapelessMatchingDamageRecipe.class, Category.SHAPELESS, "");

		for (EnumHero hero : EnumHero.values()) {
			GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(hero.helmet), new ItemStack(hero.token), new ItemStack(Items.IRON_HELMET, 1, OreDictionary.WILDCARD_VALUE)));
			GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(hero.chestplate), new ItemStack(hero.token), new ItemStack(Items.IRON_CHESTPLATE, 1, OreDictionary.WILDCARD_VALUE)));
			GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(hero.leggings), new ItemStack(hero.token), new ItemStack(Items.IRON_LEGGINGS, 1, OreDictionary.WILDCARD_VALUE)));
			GameRegistry.addRecipe(new ShapelessMatchingDamageRecipe(new ItemStack(hero.boots), new ItemStack(hero.token), new ItemStack(Items.IRON_BOOTS, 1, OreDictionary.WILDCARD_VALUE)));
			GameRegistry.addShapelessRecipe(new ItemStack(hero.weapon, hero.weapon.hasOffhand ? 2 : 1), new ItemStack(hero.token));
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
	public Object playFollowingSound(Entity entity, ModSoundEvent sound, SoundCategory category, float volume, float pitch, boolean repeat) {
		if (entity != null && entity.isEntityAlive() && sound != null && category != null) 
			Minewatch.network.sendToDimension(new SPacketFollowingSound(entity, sound, category, volume, pitch, repeat), entity.world.provider.getDimension());
		return null;
	}

	public void stopSound(EntityPlayer player, SoundEvent event, SoundCategory category) {
		if (player instanceof EntityPlayerMP) {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeString(category.getName());
			packetbuffer.writeString(event.getRegistryName().toString());
			((EntityPlayerMP)player).connection.sendPacket(new SPacketCustomPayload("MC|StopSound", packetbuffer));
		}
	}

	/**Modified from {@link Explosion#doExplosionA()} && {@link Explosion#doExplosionB(boolean)}*/
	public void createExplosion(World world, Entity exploder, double x, double y, double z, float size, float exploderDamage, float minDamage, float maxDamage, @Nullable Entity directHit, float directHitDamage, boolean resetHurtResist, float exploderKnockback, float knockback) {
		if (!world.isRemote) {
			Explosion explosion = new Explosion(world, exploder, x, y, z, size, false, false);

			float f3 = size * 2.0F;
			int k1 = MathHelper.floor(x - (double)f3 - 1.0D);
			int l1 = MathHelper.floor(x + (double)f3 + 1.0D);
			int i2 = MathHelper.floor(y - (double)f3 - 1.0D);
			int i1 = MathHelper.floor(y + (double)f3 + 1.0D);
			int j2 = MathHelper.floor(z - (double)f3 - 1.0D);
			int j1 = MathHelper.floor(z + (double)f3 + 1.0D);
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
			net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(world, explosion, list, f3);

			for (int k2 = 0; k2 < list.size(); ++k2) {
				Entity entity = (Entity)list.get(k2);

				if (!entity.isImmuneToExplosions() && (!(entity instanceof EntityLivingBase) || 
						((EntityLivingBase)entity).getHealth() > 0)) {
					double d12 = entity.getDistance(x, y, z) / (double)f3;

					if (d12 <= 1.0D) {
						d12 /= 2d;
						double d5 = entity.posX - x;
						double d7 = entity.posY + (double)entity.getEyeHeight() - y;
						double d9 = entity.posZ - z;
						double d13 = (double)MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

						if (d13 != 0.0D) {
							d5 = d5 / d13;
							d7 = d7 / d13;
							d9 = d9 / d13; 
							double d14 = 1;//(double)world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
							double d10 = (1.0D - d12) * d14;
							float damage = (float) (entity == exploder ? exploderDamage : entity == directHit ? directHitDamage : minDamage+(1f-d12)*(maxDamage-minDamage));
							double d11 = d10;
							if (EntityHelper.attemptDamage(exploder, entity, damage, true, DamageSource.causeExplosionDamage(explosion)) ||
									entity == exploder) {
								if (resetHurtResist)
									entity.hurtResistantTime = 0;

								if (entity instanceof EntityLivingBase)
									d11 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase)entity, d10);

								entity.motionX += d5 * d11 * (entity == exploder ? exploderKnockback : knockback);
								entity.motionY += d7 * d11 * (entity == exploder ? exploderKnockback : knockback);
								entity.motionZ += d9 * d11 * (entity == exploder ? exploderKnockback : knockback);
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

	public Handler onHandlerRemove(boolean isRemote, Handler handler) {
		return handler.onServerRemove();
	}

	public Entity getRenderViewEntity() {
		return null;
	}
}