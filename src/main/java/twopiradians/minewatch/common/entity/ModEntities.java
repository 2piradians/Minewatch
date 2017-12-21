package twopiradians.minewatch.common.entity;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityAnaSleepDart;
import twopiradians.minewatch.common.entity.ability.EntityHanzoScatterArrow;
import twopiradians.minewatch.common.entity.ability.EntityHanzoSonicArrow;
import twopiradians.minewatch.common.entity.ability.EntityJunkratMine;
import twopiradians.minewatch.common.entity.ability.EntityJunkratTrap;
import twopiradians.minewatch.common.entity.ability.EntityMeiCrystal;
import twopiradians.minewatch.common.entity.ability.EntityMeiIcicle;
import twopiradians.minewatch.common.entity.ability.EntityMercyBeam;
import twopiradians.minewatch.common.entity.ability.EntityReinhardtStrike;
import twopiradians.minewatch.common.entity.ability.EntitySoldier76HelixRocket;
import twopiradians.minewatch.common.entity.ability.EntitySombraTranslocator;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerMine;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityAnaBullet;
import twopiradians.minewatch.common.entity.projectile.EntityBastionBullet;
import twopiradians.minewatch.common.entity.projectile.EntityGenjiShuriken;
import twopiradians.minewatch.common.entity.projectile.EntityHanzoArrow;
import twopiradians.minewatch.common.entity.projectile.EntityJunkratGrenade;
import twopiradians.minewatch.common.entity.projectile.EntityLucioSonic;
import twopiradians.minewatch.common.entity.projectile.EntityMcCreeBullet;
import twopiradians.minewatch.common.entity.projectile.EntityMeiBlast;
import twopiradians.minewatch.common.entity.projectile.EntityMercyBullet;
import twopiradians.minewatch.common.entity.projectile.EntityReaperBullet;
import twopiradians.minewatch.common.entity.projectile.EntitySoldier76Bullet;
import twopiradians.minewatch.common.entity.projectile.EntitySombraBullet;
import twopiradians.minewatch.common.entity.projectile.EntityTracerBullet;
import twopiradians.minewatch.common.entity.projectile.EntityWidowmakerBullet;
import twopiradians.minewatch.common.entity.projectile.EntityZenyattaOrb;
import twopiradians.minewatch.common.hero.EnumHero;

public class ModEntities {

	public static void registerEntities() {
		int id = 0;

		// heroes
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "random_hero"), EntityHero.class, "random_hero", id++, Minewatch.instance, 144, 3, true, 0xffffff, 0xeaeaea);
		Minewatch.tabMapMaking.getOrderedStacks().add(getSpawnEgg(new ResourceLocation(Minewatch.MODID, "random_hero")));
		for (EnumHero hero : EnumHero.values()) {
			EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, hero.toString().toLowerCase()+"_hero"), hero.heroClass, hero.toString().toLowerCase()+"_hero", id++, Minewatch.instance, 144, 3, true, hero.color.getRGB(), hero.color.darker().getRGB());
			Minewatch.tabMapMaking.getOrderedStacks().add(getSpawnEgg(new ResourceLocation(Minewatch.MODID, hero.toString().toLowerCase()+"_hero")));
		}

		// projectile / ability
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "reaper_bullet"), EntityReaperBullet.class, "reaper_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoSonicArrow.class, "hanzo_sonic_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("arrow"), EntityHanzoScatterArrow.class, "hanzo_scatter_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "ana_bullet"), EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "ana_sleep_dart"), EntityAnaSleepDart.class, "ana_sleep_dart", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "genji_shuriken"), EntityGenjiShuriken.class, "genji_shuriken", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "tracer_bullet"), EntityTracerBullet.class, "tracer_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mccree_bullet"), EntityMcCreeBullet.class, "mccree_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "soldier76_bullet"), EntitySoldier76Bullet.class, "soldier76_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "soldier76_helix_rocket"), EntitySoldier76HelixRocket.class, "soldier76_helix_rocket", id++, Minewatch.instance, 64, 200, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "bastion_bullet"), EntityBastionBullet.class, "bastion_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mei_blast"), EntityMeiBlast.class, "mei_blast", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mei_icicle"), EntityMeiIcicle.class, "mei_icicle", id++, Minewatch.instance, 64, Integer.MAX_VALUE, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mei_crystal"), EntityMeiCrystal.class, "mei_crystal", id++, Minewatch.instance, 64, Integer.MAX_VALUE, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "widowmaker_bullet"), EntityWidowmakerBullet.class, "widowmaker_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "widowmaker_mine"), EntityWidowmakerMine.class, "widowmaker_mine", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mercy_bullet"), EntityMercyBullet.class, "mercy_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "mercy_beam"), EntityMercyBeam.class, "mercy_beam", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "junkrat_grenade"), EntityJunkratGrenade.class, "junkrat_grenade", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "junkrat_trap"), EntityJunkratTrap.class, "junkrat_trap", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "junkrat_mine"), EntityJunkratMine.class, "junkrat_mine", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "sombra_bullet"), EntitySombraBullet.class, "sombra_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "sombra_translocator"), EntitySombraTranslocator.class, "sombra_translocator", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "reinhardt_strike"), EntityReinhardtStrike.class, "reinhardt_strike", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "lucio_sonic"), EntityLucioSonic.class, "lucio_sonic", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Minewatch.MODID, "zenyatta_orb"), EntityZenyattaOrb.class, "zenyatta_orb", id++, Minewatch.instance, 64, 20, false);
	}
	
    /**Get spawn egg for given entity class*/
	public static ItemStack getSpawnEgg(ResourceLocation id) {
		ItemStack stack = new ItemStack(Items.SPAWN_EGG);
		NBTTagCompound nbt = new NBTTagCompound();    	
		nbt.setString("id", id.toString());
		NBTTagCompound nbt2 = new NBTTagCompound();
		nbt2.setTag("EntityTag", nbt);
		stack.setTagCompound(nbt2);
		return stack;
	}
	
}