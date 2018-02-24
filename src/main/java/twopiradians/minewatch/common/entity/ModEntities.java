package twopiradians.minewatch.common.entity;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import twopiradians.minewatch.common.Minewatch;
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

public class ModEntities {

	public static void registerEntities() {
		int id = 0;

		// heroes
		EntityRegistry.registerModEntity(EntityHero.class, "random_hero", id++, Minewatch.instance, 144, 3, true, 0xffffff, 0xeaeaea);
		Minewatch.tabMapMaking.getOrderedStacks().add(getSpawnEgg(new ResourceLocation(Minewatch.MODID, "random_hero")));
		for (EnumHero hero : EnumHero.values()) {
			EntityRegistry.registerModEntity(hero.heroClass, hero.toString().toLowerCase()+"_hero", id++, Minewatch.instance, 144, 3, true, hero.color.getRGB(), hero.color.darker().getRGB());
			Minewatch.tabMapMaking.getOrderedStacks().add(getSpawnEgg(new ResourceLocation(Minewatch.MODID, hero.toString().toLowerCase()+"_hero")));
		}

		// projectile / ability
		EntityRegistry.registerModEntity(EntityReaperBullet.class, "reaper_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityHanzoArrow.class, "hanzo_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityHanzoSonicArrow.class, "hanzo_sonic_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityHanzoScatterArrow.class, "hanzo_scatter_arrow", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityAnaBullet.class, "ana_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityAnaSleepDart.class, "ana_sleep_dart", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityAnaGrenade.class, "ana_grenade", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityGenjiShuriken.class, "genji_shuriken", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityTracerBullet.class, "tracer_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMcCreeBullet.class, "mccree_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMcCreeStun.class, "mccree_stun", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntitySoldier76Bullet.class, "soldier76_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntitySoldier76HelixRocket.class, "soldier76_helix_rocket", id++, Minewatch.instance, 64, 200, false);
		EntityRegistry.registerModEntity(EntitySoldier76Heal.class, "soldier76_heal", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityBastionBullet.class, "bastion_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMeiBlast.class, "mei_blast", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMeiIcicle.class, "mei_icicle", id++, Minewatch.instance, 64, Integer.MAX_VALUE, false);
		EntityRegistry.registerModEntity(EntityMeiCrystal.class, "mei_crystal", id++, Minewatch.instance, 64, Integer.MAX_VALUE, false);
		EntityRegistry.registerModEntity(EntityWidowmakerBullet.class, "widowmaker_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityWidowmakerMine.class, "widowmaker_mine", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityWidowmakerHook.class, "widowmaker_hook", id++, Minewatch.instance, 64, 1000, false);
		EntityRegistry.registerModEntity(EntityMercyBullet.class, "mercy_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMercyBeam.class, "mercy_beam", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityJunkratGrenade.class, "junkrat_grenade", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityJunkratTrap.class, "junkrat_trap", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityJunkratMine.class, "junkrat_mine", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntitySombraBullet.class, "sombra_bullet", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntitySombraTranslocator.class, "sombra_translocator", id++, Minewatch.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityReinhardtStrike.class, "reinhardt_strike", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityLucioSonic.class, "lucio_sonic", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityZenyattaOrb.class, "zenyatta_orb", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMoiraHealEnergy.class, "moira_heal_energy", id++, Minewatch.instance, 64, 20, false);
		EntityRegistry.registerModEntity(EntityMoiraOrb.class, "moira_orb", id++, Minewatch.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntityDoomfistBullet.class, "doomfist_bullet", id++, Minewatch.instance, 64, 20, false);
	}
	
    /**Get spawn egg for given entity class*/
	public static ItemStack getSpawnEgg(ResourceLocation id) {
		ItemStack stack = new ItemStack(Items.SPAWN_EGG);
		NBTTagCompound nbt = new NBTTagCompound();    	
		nbt.setString("id", id.toString().replace(":", ".")); // PORT 1.10.2 add .replace(":", ".")
		NBTTagCompound nbt2 = new NBTTagCompound();
		nbt2.setTag("EntityTag", nbt);
		stack.setTagCompound(nbt2);
		return stack;
	}
	
}