package twopiradians.minewatch.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.ModEntities;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.recipe.ShapelessMatchingDamageRecipe;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.CPacketSyncKeys;
import twopiradians.minewatch.packet.SPacketPotionEffect;
import twopiradians.minewatch.packet.SPacketSpawnParticle;
import twopiradians.minewatch.packet.SPacketSyncAbilityUses;
import twopiradians.minewatch.packet.SPacketSyncAmmo;
import twopiradians.minewatch.packet.SPacketSyncCooldown;
import twopiradians.minewatch.packet.SPacketSyncSpawningEntity;
import twopiradians.minewatch.packet.SPacketTriggerAbility;

public class CommonProxy 
{
	public void preInit(FMLPreInitializationEvent event) {
		Minewatch.configFile = event.getSuggestedConfigurationFile();
		Config.preInit(Minewatch.configFile);
		registerPackets();
		ModEntities.registerEntities();
		ModItems.preInit();
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
		Minewatch.network.registerMessage(SPacketSyncSpawningEntity.Handler.class, SPacketSyncSpawningEntity.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(SPacketSyncCooldown.Handler.class, SPacketSyncCooldown.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(SPacketSpawnParticle.Handler.class, SPacketSpawnParticle.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(SPacketPotionEffect.Handler.class, SPacketPotionEffect.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(SPacketTriggerAbility.Handler.class, SPacketTriggerAbility.class, id++, Side.CLIENT);
		Minewatch.network.registerMessage(SPacketSyncAbilityUses.Handler.class, SPacketSyncAbilityUses.class, id++, Side.CLIENT);
	}

	public void spawnParticlesAnaHealth(EntityLivingBase entity) { }
	public void spawnParticlesHanzoSonic(World world, double x, double y, double z, boolean isBig, boolean isFast) { }
	public void spawnParticlesHanzoSonic(World world, Entity trackEntity, boolean isBig) { }
	public void spawnParticlesTrail(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge, float alpha) { }
	public void spawnParticlesSmoke(World world, double x, double y, double z, int color, int colorFade, float scale, int maxAge) {}
	public void spawnParticlesSpark(World world, double x, double y, double z, int color, int colorFade, float scale, int maxAge) {}
	public void spawnParticlesMeiBlaster(World world, double x, double y, double z, double motionX, double motionY, double motionZ, float alpha, int maxAge, float initialScale, float finalScale) { }
	
	protected void registerEventListeners() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new Config());
		MinecraftForge.EVENT_BUS.register(new ItemMWToken());
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
		if (event.getCommand().getName().equalsIgnoreCase("dev") && 
				event.getCommand().checkPermission(event.getSender().getServer(), event.getSender()) &&
				CommandDev.runCommand(event.getSender().getServer(), event.getSender(), event.getParameters())) 
			event.setCanceled(true);
		}
		catch (Exception e) {}
	}

	public void mouseClick() { }
	
}
