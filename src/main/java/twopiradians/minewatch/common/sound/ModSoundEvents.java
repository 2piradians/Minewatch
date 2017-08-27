package twopiradians.minewatch.common.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

public class ModSoundEvents {

	public static SoundEvent hurt;
	public static SoundEvent abilityRecharge;
	public static SoundEvent abilityMultiRecharge;
	public static SoundEvent abilityNotReady;
	public static SoundEvent anaShoot;
	public static SoundEvent anaHeal;
	public static SoundEvent reaperShoot;
	public static SoundEvent hanzoShoot;
	public static SoundEvent hanzoDraw;
	public static SoundEvent hanzoSonicArrow;
	public static SoundEvent hanzoScatterArrow;
	public static SoundEvent reinhardtWeapon;
	public static SoundEvent genjiShoot;
	public static SoundEvent tracerShoot;
	public static SoundEvent tracerBlink;
	public static SoundEvent mccreeShoot;
	public static SoundEvent soldier76Shoot;
	public static SoundEvent soldier76Helix;
	public static SoundEvent bastionShoot;
	public static SoundEvent bastionTurretReload;
	public static SoundEvent meiShoot;
	public static SoundEvent meiIcicleShoot;
	public static SoundEvent meiFreeze;
	public static SoundEvent meiUnfreeze;
	public static SoundEvent widowmakerShoot;

	public static void preInit() {
		hurt = registerSound("hurt");
		abilityRecharge = registerSound("ability_recharge");
		abilityMultiRecharge = registerSound("ability_multi_recharge");
		abilityNotReady = registerSound("ability_not_ready");
		anaShoot = registerSound("ana_shoot");
		anaHeal = registerSound("ana_heal");
		EnumHero.ANA.reloadSound = registerSound("ana_reload");
		reaperShoot = registerSound("reaper_shoot");
		EnumHero.REAPER.reloadSound = registerSound("reaper_reload");
		hanzoShoot = registerSound("hanzo_shoot");
		hanzoDraw = registerSound("hanzo_draw");
		hanzoSonicArrow = registerSound("hanzo_sonic_arrow");
		hanzoScatterArrow = registerSound("hanzo_scatter_arrow");
		reinhardtWeapon = registerSound("reinhardt_weapon");
		genjiShoot = registerSound("genji_shoot");
		EnumHero.GENJI.reloadSound = registerSound("genji_reload");
		tracerShoot = registerSound("tracer_shoot");
		tracerBlink = registerSound("tracer_blink");
		EnumHero.TRACER.reloadSound = registerSound("tracer_reload");
		mccreeShoot = registerSound("mccree_shoot");
		EnumHero.MCCREE.reloadSound = registerSound("mccree_reload");
		soldier76Shoot = registerSound("soldier76_shoot");
		soldier76Helix = registerSound("soldier76_helix");
		EnumHero.SOLDIER76.reloadSound = registerSound("soldier76_reload");
		bastionShoot = registerSound("bastion_shoot");
		EnumHero.BASTION.reloadSound = registerSound("bastion_reload_0");
		bastionTurretReload = registerSound("bastion_reload_1");
		meiShoot = registerSound("mei_shoot_0");
		EnumHero.MEI.reloadSound = registerSound("mei_reload");
		meiIcicleShoot = registerSound("mei_shoot_1");
		meiFreeze = registerSound("mei_freeze");
		meiUnfreeze = registerSound("mei_unfreeze");
		widowmakerShoot = registerSound("widowmaker_shoot");
		EnumHero.WIDOWMAKER.reloadSound = registerSound("widowmaker_reload");
	}
	
	private static SoundEvent registerSound(String soundName) {
		ResourceLocation loc = new ResourceLocation(Minewatch.MODID, soundName);
		SoundEvent sound = new SoundEvent(loc);
		GameRegistry.register(sound, loc);
		return sound;
	}
}