package twopiradians.minewatch.common.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

public enum ModSoundEvents {

	/**Registry name must be equal to name.toLowerCase()
	 * Voice lines must have "voice" in them*/
	GUI_HOVER,
	//public static SoundEvent[] multikill = new SoundEvent[5],
	KILL,
	HEADSHOT,
	HURT,
	ABILITY_RECHARGE,
	ABILITY_MULTI_RECHARGE,
	ABILITY_NOT_READY,
	WALL_CLIMB,
	ANA_SHOOT,
	ANA_HEAL,
	ANA_SLEEP_SHOOT,
	ANA_SLEEP_HIT,
	ANA_SLEEP_VOICE,
	REAPER_SHOOT,
	REAPER_TELEPORT_START,
	REAPER_TELEPORT_DURING,
	REAPER_TELEPORT_STOP,
	REAPER_TELEPORT_FINAL,
	REAPER_TELEPORT_VOICE,
	REAPER_WRAITH,
	HANZO_SHOOT,
	HANZO_DRAW,
	HANZO_SONIC_ARROW,
	HANZO_SCATTER_ARROW,
	REINHARDT_WEAPON,
	GENJI_SHOOT,
	GENJI_DEFLECT,
	GENJI_DEFLECT_HIT,
	GENJI_STRIKE,
	GENJI_JUMP,
	TRACER_SHOOT,
	TRACER_BLINK,
	MCCREE_SHOOT,
	MCCREE_FLASHBANG,
	MCCREE_ROLL,
	SOLDIER_76_SHOOT,
	SOLDIER_76_HELIX,
	BASTION_SHOOT_0,
	BASTION_SHOOT_1,
	BASTION_RELOAD,
	BASTION_TURRET_RELOAD,
	MEI_SHOOT,
	MEI_ICICLE_SHOOT,
	MEI_FREEZE,
	MEI_UNFREEZE,
	WIDOWMAKER_SCOPED_SHOOT,
	WIDOWMAKER_UNSCOPED_SHOOT,
	WIDOWMAKER_CHARGE,
	MERCY_SHOOT,
	MERCY_HEAL,
	MERCY_DAMAGE,
	MERCY_HOVER,
	MERCY_ANGEL,
	MERCY_ANGEL_VOICE,
	MERCY_BEAM_START,
	MERCY_BEAM_DURING,
	MERCY_BEAM_STOP,
	JUNKRAT_SHOOT,
	JUNKRAT_LAUGH,
	JUNKRAT_DEATH,
	JUNKRAT_GRENADE_BOUNCE,
	JUNKRAT_GRENADE_EXPLODE,
	//PUBLIC STATIC _SOUND_EVENT[] JUNKRAT_GRENADE_TICK = NEW _SOUND_EVENT[_4],
	JUNKRAT_TRAP_THROW,
	JUNKRAT_TRAP_LAND,
	JUNKRAT_TRAP_TRIGGER,
	JUNKRAT_TRAP_BREAK,
	JUNKRAT_TRAP_TRIGGER_OWNER,
	JUNKRAT_TRAP_TRIGGER_VOICE,
	JUNKRAT_TRAP_PLACED_VOICE,
	SOMBRA_SHOOT,
	SOMBRA_INVISIBLE_START,
	SOMBRA_INVISIBLE_STOP,
	SOMBRA_INVISIBLE_VOICE,
	SOMBRA_TRANSLOCATOR_LAND,
	SOMBRA_TRANSLOCATOR_THROW,
	SOMBRA_TRANSLOCATOR_DURING,
	SOMBRA_TRANSLOCATOR_TELEPORT,
	WIDOWMAKER_MINE_THROW,
	WIDOWMAKER_MINE_LAND,
	WIDOWMAKER_MINE_TRIGGER,
	WIDOWMAKER_MINE_DESTROYED,
	JUNKRAT_MINE_THROW,
	JUNKRAT_MINE_LAND,
	JUNKRAT_MINE_EXPLODE,
	BASTION_RECONFIGURE_0,
	BASTION_RECONFIGURE_1,
	MEI_CRYSTAL_START,
	MEI_CRYSTAL_STOP,
	REINHARDT_STRIKE_THROW,
	REINHARDT_STRIKE_DURING,
	REINHARDT_STRIKE_COLLIDE,

	LUCIO_SHOOT,
	LUCIO_CROSSFADE,
	LUCIO_AMP,
	LUCIO_AMP_VOICE,
	LUCIO_PASSIVE_SPEED,
	LUCIO_PASSIVE_SPEED_VOICE,
	LUCIO_PASSIVE_HEAL,
	LUCIO_PASSIVE_HEAL_VOICE,
	LUCIO_SOUNDWAVE,
	LUCIO_SOUNDWAVE_VOICE;

	private final ModSoundEvent event;
	private final ResourceLocation loc;

	public boolean isVoiceLine;

	private ModSoundEvents() {
		loc = new ResourceLocation(Minewatch.MODID, this.name().toLowerCase());
		event = new ModSoundEvent(loc);
		this.isVoiceLine = this.name().contains("VOICE");
	}

	/**To allow future customization - i.e. adjust volume based on teams*/
	public void playSound(Entity entity, float volume, float pitch) {
		if (entity != null) 
			this.playSound(entity.world, entity.posX, entity.posY, entity.posZ, volume, pitch);
	}

	/**To allow future customization - i.e. adjust volume based on teams*/
	public void playSound(World world, double x, double y, double z, float volume, float pitch) {
		if (world != null) 
			world.playSound(x, y, z, event, SoundCategory.PLAYERS, volume, pitch, false);
	}

	/**To allow future customization - i.e. adjust volume based on teams*/
	public Object playFollowingSound(Entity entity, float volume, float pitch, boolean repeat) {
		return Minewatch.proxy.playFollowingSound(entity, event, SoundCategory.PLAYERS, volume, pitch, repeat);
	}

	private void register() {
		GameRegistry.register(event, loc);	
	}

	/**Separate Sound Event class to prevent bypassing using these methods (i.e. proxy playFollowSound method)*/
	public class ModSoundEvent extends SoundEvent {

		private ModSoundEvent(ResourceLocation soundName) {
			super(soundName);
		}}

	//public static ModSound guiHover = new ModSound("gui_hover");
	

	public static void preInit() {
		/*for (EnumHero hero : EnumHero.values())
			if (hero != EnumHero.HANZO && hero != EnumHero.REINHARDT)
				hero.reloadSound = registerSound(hero.name.toLowerCase()+"_reload"+(hero.equals(EnumHero.BASTION) ? "_0" : ""));*/

		//guiHover = registerSound("gui_hover");
		/*for (ModSound event : ModSound.EVENTS)
			event.register();*/
		for (ModSoundEvents event : ModSoundEvents.values())
			event.register();

		/*for (int i=2; i<7; ++i)
			multikill[i-2] = registerSound("multikill_"+i);
		kill = registerSound("kill");
		headshot = registerSound("headshot");
		hurt = registerSound("hurt");
		abilityRecharge = registerSound("ability_recharge");
		abilityMultiRecharge = registerSound("ability_multi_recharge");
		abilityNotReady = registerSound("ability_not_ready");
		wallClimb = registerSound("wall_climb");
		anaShoot = registerSound("ana_shoot");
		anaHeal = registerSound("ana_heal");
		anaSleepShoot = registerSound("ana_sleep_shoot");
		anaSleepHit = registerSound("ana_sleep_hit");
		anaSleepVoice = registerSound("ana_sleep_voice");
		reaperShoot = registerSound("reaper_shoot");
		reaperTeleportStart = registerSound("reaper_teleport_start");
		reaperTeleportDuring = registerSound("reaper_teleport_during");
		reaperTeleportStop = registerSound("reaper_teleport_stop");
		reaperTeleportFinal = registerSound("reaper_teleport_final");
		reaperTeleportVoice = registerSound("reaper_teleport_voice");
		reaperWraith = registerSound("reaper_wraith");
		hanzoShoot = registerSound("hanzo_shoot");
		hanzoDraw = registerSound("hanzo_draw");
		hanzoSonicArrow = registerSound("hanzo_sonic_arrow");
		hanzoScatterArrow = registerSound("hanzo_scatter_arrow");
		reinhardtWeapon = registerSound("reinhardt_weapon");
		genjiShoot = registerSound("genji_shoot");
		genjiDeflect = registerSound("genji_deflect");
		genjiDeflectHit = registerSound("genji_deflect_hit");
		genjiStrike = registerSound("genji_strike");
		genjiJump = registerSound("genji_jump");
		tracerShoot = registerSound("tracer_shoot");
		tracerBlink = registerSound("tracer_blink");
		mccreeShoot = registerSound("mccree_shoot");
		mccreeFlashbang = registerSound("mccree_flashbang");
		mccreeRoll = registerSound("mccree_roll");
		soldier76Shoot = registerSound("soldier76_shoot");
		soldier76Helix = registerSound("soldier76_helix");
		bastionReload = EnumHero.BASTION.reloadSound;
		bastionTurretReload = registerSound("bastion_reload_1");
		meiShoot = registerSound("mei_shoot_0");
		meiIcicleShoot = registerSound("mei_shoot_1");
		meiFreeze = registerSound("mei_freeze");
		meiUnfreeze = registerSound("mei_unfreeze");
		widowmakerUnscopedShoot = registerSound("widowmaker_shoot_0");
		widowmakerScopedShoot = registerSound("widowmaker_shoot_1");
		widowmakerCharge = registerSound("widowmaker_charge");
		mercyShoot = registerSound("mercy_shoot");
		mercyHeal = registerSound("mercy_heal");
		mercyDamage = registerSound("mercy_damage");
		mercyHover = registerSound("mercy_hover");
		mercyAngel = registerSound("mercy_angel");
		mercyAngelVoice = registerSound("mercy_angel_voice");
		mercyBeamStart = registerSound("mercy_beam_start");
		mercyBeamDuring = registerSound("mercy_beam_during");
		mercyBeamStop = registerSound("mercy_beam_stop");
		junkratShoot = registerSound("junkrat_shoot");
		junkratLaugh = registerSound("junkrat_laugh");
		junkratDeath = registerSound("junkrat_death");
		junkratGrenadeBounce = registerSound("junkrat_grenade_bounce");
		junkratGrenadeExplode = registerSound("junkrat_grenade_explode");
		for (int i=0; i<junkratGrenadeTick.length; ++i)
			junkratGrenadeTick[i] = registerSound("junkrat_grenade_tick_"+i);
		junkratTrapThrow = registerSound("junkrat_trap_throw");
		junkratTrapLand = registerSound("junkrat_trap_land");
		junkratTrapTrigger = registerSound("junkrat_trap_trigger");
		junkratTrapBreak = registerSound("junkrat_trap_break");
		junkratTrapTriggerOwner = registerSound("junkrat_trap_trigger_owner");
		junkratTrapTriggerVoice = registerSound("junkrat_trap_trigger_voice");
		junkratTrapPlacedVoice = registerSound("junkrat_trap_placed_voice");
		sombraShoot = registerSound("sombra_shoot");
		sombraInvisibleStart = registerSound("sombra_invisible_start");
		sombraInvisibleStop = registerSound("sombra_invisible_stop");
		sombraInvisibleVoice = registerSound("sombra_invisible_voice");
		sombraTranslocatorLand = registerSound("sombra_translocator_land");
		sombraTranslocatorThrow = registerSound("sombra_translocator_throw");
		sombraTranslocatorDuring = registerSound("sombra_translocator_during");
		sombraTranslocatorTeleport = registerSound("sombra_translocator_teleport");
		widowmakerMineThrow = registerSound("widowmaker_mine_throw");
		widowmakerMineLand = registerSound("widowmaker_mine_land");
		widowmakerMineTrigger = registerSound("widowmaker_mine_trigger");
		widowmakerMineDestroyed = registerSound("widowmaker_mine_destroyed");
		junkratMineThrow = registerSound("junkrat_mine_throw");
		junkratMineLand = registerSound("junkrat_mine_land");
		junkratMineExplode = registerSound("junkrat_mine_explode");
		bastionShoot0 = registerSound("bastion_shoot_0");
		bastionShoot1 = registerSound("bastion_shoot_1");
		bastionReconfigure0 = registerSound("bastion_reconfigure_0");
		bastionReconfigure1 = registerSound("bastion_reconfigure_1");
		meiCrystalStart = registerSound("mei_crystal_start");
		meiCrystalStop = registerSound("mei_crystal_stop");
		reinhardtStrikeThrow = registerSound("reinhardt_strike_throw");
		reinhardtStrikeDuring = registerSound("reinhardt_strike_during");
		reinhardtStrikeCollide = registerSound("reinhardt_strike_collide");

		lucioShoot = registerSound("lucio_shoot");
		lucioAmp = registerSound("lucio_amp");
		lucioAmpVoice = registerSound("lucio_amp_voice");
		lucioPassiveSpeed = registerSound("lucio_passive_speed");
		lucioPassiveSpeedVoice = registerSound("lucio_passive_speed_voice");
		lucioPassiveHeal = registerSound("lucio_passive_heal");
		lucioPassiveHealVoice = registerSound("lucio_passive_heal_voice");
		lucioSoundwave = registerSound("lucio_soundwave");
		lucioSoundwaveVoice = registerSound("lucio_soundwave_voice");*/
	}

	private static SoundEvent registerSound(String soundName) {
		ResourceLocation loc = new ResourceLocation(Minewatch.MODID, soundName);
		SoundEvent sound = new SoundEvent(loc);
		GameRegistry.register(sound, loc);
		return sound;
	}
}