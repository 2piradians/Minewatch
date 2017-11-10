package twopiradians.minewatch.common.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

public class ModSoundEvents {

	public static SoundEvent anaShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ana_shoot")).setRegistryName("ana_shoot");
	public static SoundEvent anaReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ana_reload")).setRegistryName("ana_reload");
	public static SoundEvent reaperShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_shoot")).setRegistryName("reaper_shoot");
	public static SoundEvent reaperReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_reload")).setRegistryName("reaper_reload");
	public static SoundEvent hanzoShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hanzo_shoot")).setRegistryName("hanzo_shoot");
	public static SoundEvent hanzoDraw = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hanzo_draw")).setRegistryName("hanzo_draw");
	public static SoundEvent reinhardtWeapon = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reinhardt_weapon")).setRegistryName("reinhardt_weapon");
	public static SoundEvent genjiShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "genji_shoot")).setRegistryName("genji_shoot");
	public static SoundEvent genjiReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "genji_reload")).setRegistryName("genji_reload");
	public static SoundEvent tracerShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "tracer_shoot")).setRegistryName("tracer_shoot");
	public static SoundEvent tracerReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "tracer_reload")).setRegistryName("tracer_reload");
	public static SoundEvent mccreeShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mccree_shoot")).setRegistryName("mccree_shoot");
	public static SoundEvent mccreeReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mccree_reload")).setRegistryName("mccree_reload");
	public static SoundEvent soldier76Shoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "soldier76_shoot")).setRegistryName("soldier76_shoot");
	public static SoundEvent soldier76Reload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "soldier76_reload")).setRegistryName("soldier76_reload");

	public static SoundEvent hurt = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hurt")).setRegistryName("hurt");
	public static SoundEvent anaHeal = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ana_heal")).setRegistryName("ana_heal");
	public static SoundEvent hanzoSonicArrow = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hanzo_sonic_arrow")).setRegistryName("hanzo_sonic_arrow");
	public static SoundEvent hanzoScatterArrow = new SoundEvent(new ResourceLocation(Minewatch.MODID, "hanzo_scatter_arrow")).setRegistryName("hanzo_scatter_arrow");
	public static SoundEvent soldier76Helix = new SoundEvent(new ResourceLocation(Minewatch.MODID, "soldier76_helix")).setRegistryName("soldier76_helix");
	public static SoundEvent bastionShoot0 = new SoundEvent(new ResourceLocation(Minewatch.MODID, "bastion_shoot_0")).setRegistryName("bastion_shoot_0");
	public static SoundEvent bastionShoot1 = new SoundEvent(new ResourceLocation(Minewatch.MODID, "bastion_shoot_1")).setRegistryName("bastion_shoot_1");
	public static SoundEvent bastionReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "bastion_reload_0")).setRegistryName("bastion_reload_0");
	public static SoundEvent bastionTurretReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "bastion_reload_1")).setRegistryName("bastion_reload_1");
	public static SoundEvent meiShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_shoot_0")).setRegistryName("mei_shoot_0");
	public static SoundEvent meiIcicleShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_shoot_1")).setRegistryName("mei_shoot_1");
	public static SoundEvent meiFreeze = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_freeze")).setRegistryName("mei_freeze");
	public static SoundEvent meiUnfreeze = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_unfreeze")).setRegistryName("mei_unfreeze");
	public static SoundEvent meiReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_reload")).setRegistryName("mei_reload");

	public static SoundEvent abilityRecharge = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ability_recharge")).setRegistryName("ability_recharge");
	public static SoundEvent abilityMultiRecharge = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ability_multi_recharge")).setRegistryName("ability_multi_recharge");
	public static SoundEvent abilityNotReady = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ability_not_ready")).setRegistryName("ability_not_ready");
	public static SoundEvent reaperTeleportStart = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_teleport_start")).setRegistryName("reaper_teleport_start");
	public static SoundEvent reaperTeleportDuring = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_teleport_during")).setRegistryName("reaper_teleport_during");
	public static SoundEvent reaperTeleportStop = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_teleport_stop")).setRegistryName("reaper_teleport_stop");
	public static SoundEvent reaperTeleportFinal = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_teleport_final")).setRegistryName("reaper_teleport_final");
	public static SoundEvent reaperTeleportVoice = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_teleport_voice")).setRegistryName("reaper_teleport_voice");
	public static SoundEvent tracerBlink = new SoundEvent(new ResourceLocation(Minewatch.MODID, "tracer_blink")).setRegistryName("tracer_blink");
	public static SoundEvent widowmakerScopedShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "widowmaker_shoot_1")).setRegistryName("widowmaker_shoot_1");
	public static SoundEvent widowmakerUnscopedShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "widowmaker_shoot_0")).setRegistryName("widowmaker_shoot_0");
	public static SoundEvent widowmakerCharge = new SoundEvent(new ResourceLocation(Minewatch.MODID, "widowmaker_charge")).setRegistryName("widowmaker_charge");
	public static SoundEvent widowmakerReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "widowmaker_reload")).setRegistryName("widowmaker_reload");

	public static SoundEvent[] multikill = new SoundEvent[5];
	static {
		for (int i = 2; i < 7; ++i)
			multikill[i-2] = new SoundEvent(new ResourceLocation(Minewatch.MODID, "multikill_"+i)).setRegistryName("multikill_"+i);
	}
	public static SoundEvent kill = new SoundEvent(new ResourceLocation(Minewatch.MODID, "kill")).setRegistryName("kill");
	public static SoundEvent headshot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "headshot")).setRegistryName("headshot");
	public static SoundEvent wallClimb = new SoundEvent(new ResourceLocation(Minewatch.MODID, "wall_climb")).setRegistryName("wall_climb");
	public static SoundEvent anaSleepShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ana_sleep_shoot")).setRegistryName("ana_sleep_shoot");
	public static SoundEvent anaSleepHit = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ana_sleep_hit")).setRegistryName("ana_sleep_hit");
	public static SoundEvent anaSleepVoice = new SoundEvent(new ResourceLocation(Minewatch.MODID, "ana_sleep_voice")).setRegistryName("ana_sleep_voice");
	public static SoundEvent reaperWraith = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reaper_wraith")).setRegistryName("reaper_wraith");
	public static SoundEvent genjiDeflect = new SoundEvent(new ResourceLocation(Minewatch.MODID, "genji_deflect")).setRegistryName("genji_deflect");
	public static SoundEvent genjiDeflectHit = new SoundEvent(new ResourceLocation(Minewatch.MODID, "genji_deflect_hit")).setRegistryName("genji_deflect_hit");
	public static SoundEvent genjiStrike = new SoundEvent(new ResourceLocation(Minewatch.MODID, "genji_strike")).setRegistryName("genji_strike");
	public static SoundEvent genjiJump = new SoundEvent(new ResourceLocation(Minewatch.MODID, "genji_jump")).setRegistryName("genji_jump");
	public static SoundEvent mccreeFlashbang = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mccree_flashbang")).setRegistryName("mccree_flashbang");
	public static SoundEvent mccreeRoll = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mccree_roll")).setRegistryName("mccree_roll");
	public static SoundEvent mercyShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mercy_shoot")).setRegistryName("mercy_shoot");
	public static SoundEvent mercyHeal = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mercy_heal")).setRegistryName("mercy_heal");
	public static SoundEvent mercyDamage = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mercy_damage")).setRegistryName("mercy_damage");
	public static SoundEvent mercyHover = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mercy_hover")).setRegistryName("mercy_hover");
	public static SoundEvent mercyBeamStart = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mercy_beam_start")).setRegistryName("mercy_beam_start");
	public static SoundEvent mercyBeamDuring = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mercy_beam_during")).setRegistryName("mercy_beam_during");
	public static SoundEvent mercyBeamStop = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mercy_beam_stop")).setRegistryName("mercy_beam_stop");
	public static SoundEvent mercyReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mercy_reload")).setRegistryName("mercy_reload");

	public static SoundEvent guiHover = new SoundEvent(new ResourceLocation(Minewatch.MODID, "gui_hover")).setRegistryName("gui_hover");	
	public static SoundEvent mercyAngel = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mercy_angel")).setRegistryName("mercy_angel");
	public static SoundEvent mercyAngelVoice = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mercy_angel_voice")).setRegistryName("mercy_angel_voice");
	public static SoundEvent junkratShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_shoot")).setRegistryName("junkrat_shoot");
	public static SoundEvent junkratLaugh = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_laugh")).setRegistryName("junkrat_laugh");
	public static SoundEvent junkratDeath = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_death")).setRegistryName("junkrat_death");
	public static SoundEvent junkratGrenadeBounce = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_grenade_bounce")).setRegistryName("junkrat_grenade_bounce");
	public static SoundEvent junkratGrenadeExplode = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_grenade_explode")).setRegistryName("junkrat_grenade_explode");
	public static SoundEvent[] junkratGrenadeTick = new SoundEvent[4];
	static {
		for (int i=0; i<junkratGrenadeTick.length; ++i)
			junkratGrenadeTick[i] = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_grenade_tick_"+i)).setRegistryName("junkrat_grenade_tick_"+i);
	}
	public static SoundEvent junkratReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_reload")).setRegistryName("junkrat_reload");
	public static SoundEvent junkratTrapThrow = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_trap_throw")).setRegistryName("junkrat_trap_throw");
	public static SoundEvent junkratTrapLand = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_trap_land")).setRegistryName("junkrat_trap_land");
	public static SoundEvent junkratTrapTrigger = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_trap_trigger")).setRegistryName("junkrat_trap_trigger");
	public static SoundEvent junkratTrapBreak = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_trap_break")).setRegistryName("junkrat_trap_break");
	public static SoundEvent junkratTrapTriggerOwner = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_trap_trigger_owner")).setRegistryName("junkrat_trap_trigger_owner");
	public static SoundEvent junkratTrapTriggerVoice = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_trap_trigger_voice")).setRegistryName("junkrat_trap_trigger_voice");
	public static SoundEvent junkratTrapPlacedVoice = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_trap_placed_voice")).setRegistryName("junkrat_trap_placed_voice");

	public static SoundEvent sombraReload = new SoundEvent(new ResourceLocation(Minewatch.MODID, "sombra_reload")).setRegistryName("sombra_reload");
	public static SoundEvent sombraShoot = new SoundEvent(new ResourceLocation(Minewatch.MODID, "sombra_shoot")).setRegistryName("sombra_shoot");
	public static SoundEvent sombraInvisibleStart = new SoundEvent(new ResourceLocation(Minewatch.MODID, "sombra_invisible_start")).setRegistryName("sombra_invisible_start");
	public static SoundEvent sombraInvisibleStop = new SoundEvent(new ResourceLocation(Minewatch.MODID, "sombra_invisible_stop")).setRegistryName("sombra_invisible_stop");
	public static SoundEvent sombraInvisibleVoice = new SoundEvent(new ResourceLocation(Minewatch.MODID, "sombra_invisible_voice")).setRegistryName("sombra_invisible_voice");
	public static SoundEvent sombraTranslocatorLand = new SoundEvent(new ResourceLocation(Minewatch.MODID, "sombra_translocator_land")).setRegistryName("sombra_translocator_land");
	public static SoundEvent sombraTranslocatorThrow = new SoundEvent(new ResourceLocation(Minewatch.MODID, "sombra_translocator_throw")).setRegistryName("sombra_translocator_throw");
	public static SoundEvent sombraTranslocatorDuring = new SoundEvent(new ResourceLocation(Minewatch.MODID, "sombra_translocator_during")).setRegistryName("sombra_translocator_during");
	public static SoundEvent sombraTranslocatorTeleport = new SoundEvent(new ResourceLocation(Minewatch.MODID, "sombra_translocator_teleport")).setRegistryName("sombra_translocator_teleport");
	public static SoundEvent widowmakerMineThrow = new SoundEvent(new ResourceLocation(Minewatch.MODID, "widowmaker_mine_throw")).setRegistryName("widowmaker_mine_throw");
	public static SoundEvent widowmakerMineLand = new SoundEvent(new ResourceLocation(Minewatch.MODID, "widowmaker_mine_land")).setRegistryName("widowmaker_mine_land");
	public static SoundEvent widowmakerMineTrigger = new SoundEvent(new ResourceLocation(Minewatch.MODID, "widowmaker_mine_trigger")).setRegistryName("widowmaker_mine_trigger");
	public static SoundEvent widowmakerMineDestroyed = new SoundEvent(new ResourceLocation(Minewatch.MODID, "widowmaker_mine_destroyed")).setRegistryName("widowmaker_mine_destroyed");
	public static SoundEvent junkratMineThrow = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_mine_throw")).setRegistryName("junkrat_mine_throw");
	public static SoundEvent junkratMineLand = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_mine_land")).setRegistryName("junkrat_mine_land");
	public static SoundEvent junkratMineExplode = new SoundEvent(new ResourceLocation(Minewatch.MODID, "junkrat_mine_explode")).setRegistryName("junkrat_mine_explode");
	public static SoundEvent bastionReconfigure0 = new SoundEvent(new ResourceLocation(Minewatch.MODID, "bastion_reconfigure_0")).setRegistryName("bastion_reconfigure_0");
	public static SoundEvent bastionReconfigure1 = new SoundEvent(new ResourceLocation(Minewatch.MODID, "bastion_reconfigure_1")).setRegistryName("bastion_reconfigure_1");
	public static SoundEvent meiCrystalStart = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_crystal_start")).setRegistryName("mei_crystal_start");
	public static SoundEvent meiCrystalStop = new SoundEvent(new ResourceLocation(Minewatch.MODID, "mei_crystal_stop")).setRegistryName("mei_crystal_stop");
	public static SoundEvent reinhardtStrikeThrow = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reinhardt_strike_throw")).setRegistryName("reinhardt_strike_throw");
	public static SoundEvent reinhardtStrikeDuring = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reinhardt_strike_during")).setRegistryName("reinhardt_strike_during");
	public static SoundEvent reinhardtStrikeCollide = new SoundEvent(new ResourceLocation(Minewatch.MODID, "reinhardt_strike_collide")).setRegistryName("reinhardt_strike_collide");

	@Mod.EventBusSubscriber
	public static class RegistrationHandler {

		@SubscribeEvent
		public static void registerSounds(final RegistryEvent.Register<SoundEvent> event) { 
			event.getRegistry().register(anaShoot);
			event.getRegistry().register(anaReload);
			event.getRegistry().register(reaperShoot);
			event.getRegistry().register(reaperReload);
			event.getRegistry().register(hanzoShoot);
			event.getRegistry().register(hanzoDraw);
			event.getRegistry().register(reinhardtWeapon);
			event.getRegistry().register(genjiShoot);
			event.getRegistry().register(genjiReload);
			event.getRegistry().register(tracerShoot);
			event.getRegistry().register(tracerReload);
			event.getRegistry().register(mccreeShoot);
			event.getRegistry().register(mccreeReload);
			event.getRegistry().register(soldier76Shoot);
			event.getRegistry().register(soldier76Reload);

			event.getRegistry().register(hurt);
			event.getRegistry().register(anaHeal);
			event.getRegistry().register(hanzoSonicArrow);
			event.getRegistry().register(hanzoScatterArrow);
			event.getRegistry().register(soldier76Helix);
			event.getRegistry().register(bastionShoot0);
			event.getRegistry().register(bastionShoot1);
			event.getRegistry().register(bastionReload);
			event.getRegistry().register(bastionTurretReload);
			event.getRegistry().register(meiShoot);
			event.getRegistry().register(meiIcicleShoot);
			event.getRegistry().register(meiFreeze);
			event.getRegistry().register(meiUnfreeze);
			event.getRegistry().register(meiReload);
			event.getRegistry().register(abilityRecharge);
			event.getRegistry().register(abilityMultiRecharge);
			event.getRegistry().register(abilityNotReady);
			event.getRegistry().register(reaperTeleportStart);
			event.getRegistry().register(reaperTeleportDuring);
			event.getRegistry().register(reaperTeleportStop);
			event.getRegistry().register(reaperTeleportFinal);
			event.getRegistry().register(reaperTeleportVoice);
			event.getRegistry().register(tracerBlink);
			event.getRegistry().register(widowmakerScopedShoot);
			event.getRegistry().register(widowmakerUnscopedShoot);
			event.getRegistry().register(widowmakerCharge);

			for (int i=2; i<7; ++i)
				event.getRegistry().register(multikill[i-2]);
			event.getRegistry().register(kill);
			event.getRegistry().register(headshot);
			event.getRegistry().register(wallClimb);
			event.getRegistry().register(anaSleepShoot);
			event.getRegistry().register(anaSleepHit);
			event.getRegistry().register(anaSleepVoice);
			event.getRegistry().register(reaperWraith);
			event.getRegistry().register(genjiDeflect);
			event.getRegistry().register(genjiDeflectHit);
			event.getRegistry().register(genjiStrike);
			event.getRegistry().register(genjiJump);
			event.getRegistry().register(mccreeFlashbang);
			event.getRegistry().register(mccreeRoll);
			event.getRegistry().register(mercyShoot);
			event.getRegistry().register(mercyHeal);
			event.getRegistry().register(mercyDamage);
			event.getRegistry().register(mercyHover);
			event.getRegistry().register(mercyBeamStart);
			event.getRegistry().register(mercyBeamDuring);
			event.getRegistry().register(mercyBeamStop);

			event.getRegistry().register(guiHover);	
			event.getRegistry().register(mercyAngel);
			event.getRegistry().register(mercyAngelVoice);
			event.getRegistry().register(junkratShoot);
			event.getRegistry().register(junkratLaugh);
			event.getRegistry().register(junkratDeath);
			event.getRegistry().register(junkratGrenadeBounce);
			event.getRegistry().register(junkratGrenadeExplode);
			for (int i=0; i<junkratGrenadeTick.length; ++i)
				event.getRegistry().register(junkratGrenadeTick[i]);
			event.getRegistry().register(junkratReload);
			event.getRegistry().register(junkratTrapThrow);
			event.getRegistry().register(junkratTrapLand);
			event.getRegistry().register(junkratTrapTrigger);
			event.getRegistry().register(junkratTrapBreak);
			event.getRegistry().register(junkratTrapTriggerOwner);
			event.getRegistry().register(junkratTrapTriggerVoice);
			event.getRegistry().register(junkratTrapPlacedVoice);
			
			event.getRegistry().register(sombraReload);
			event.getRegistry().register(sombraShoot);
			event.getRegistry().register(sombraInvisibleStart);
			event.getRegistry().register(sombraInvisibleStop);
			event.getRegistry().register(sombraInvisibleVoice);
			event.getRegistry().register(sombraTranslocatorLand);
			event.getRegistry().register(sombraTranslocatorThrow);
			event.getRegistry().register(sombraTranslocatorDuring);
			event.getRegistry().register(sombraTranslocatorTeleport);
			event.getRegistry().register(widowmakerMineThrow);
			event.getRegistry().register(widowmakerMineLand);
			event.getRegistry().register(widowmakerMineTrigger);
			event.getRegistry().register(widowmakerMineDestroyed);
			event.getRegistry().register(junkratMineThrow);
			event.getRegistry().register(junkratMineLand);
			event.getRegistry().register(junkratMineExplode);
			event.getRegistry().register(bastionReconfigure0);
			event.getRegistry().register(bastionReconfigure1);
			event.getRegistry().register(meiCrystalStart);
			event.getRegistry().register(meiCrystalStop);
			event.getRegistry().register(reinhardtStrikeThrow);
			event.getRegistry().register(reinhardtStrikeDuring);
			event.getRegistry().register(reinhardtStrikeCollide);
		}
	}
	
	public static void postInit() {
		EnumHero.ANA.reloadSound = anaReload;
		EnumHero.REAPER.reloadSound = reaperReload;
		EnumHero.GENJI.reloadSound = genjiReload;
		EnumHero.TRACER.reloadSound = tracerReload;
		EnumHero.MCCREE.reloadSound = mccreeReload;
		EnumHero.SOLDIER76.reloadSound = soldier76Reload;
		EnumHero.BASTION.reloadSound = bastionReload;
		EnumHero.MEI.reloadSound = meiReload;
		EnumHero.WIDOWMAKER.reloadSound = widowmakerReload;
		EnumHero.MERCY.reloadSound = mercyReload;
		EnumHero.JUNKRAT.reloadSound = junkratReload;
		EnumHero.SOMBRA.reloadSound = sombraReload;
	}
}