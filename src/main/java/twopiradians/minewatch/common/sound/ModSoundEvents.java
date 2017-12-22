package twopiradians.minewatch.common.sound;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public enum ModSoundEvents {

	/**Registry name must be equal to name.toLowerCase()
	 * Voice lines must have "voice" in them*/
	GUI_HOVER,
	MULTIKILL_2,
	MULTIKILL_3,
	MULTIKILL_4,
	MULTIKILL_5,
	MULTIKILL_6,
	KILL,
	HEADSHOT,
	HURT,
	ABILITY_RECHARGE,
	ABILITY_MULTI_RECHARGE,
	ABILITY_NOT_READY,
	WALL_CLIMB,
	ANA_RELOAD,
	ANA_SHOOT,
	ANA_HEAL,
	ANA_SLEEP_SHOOT,
	ANA_SLEEP_HIT,
	ANA_SLEEP_VOICE,
	REAPER_RELOAD,
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
	REINHARDT_STRIKE_THROW,
	REINHARDT_STRIKE_DURING,
	REINHARDT_STRIKE_COLLIDE,
	GENJI_RELOAD,
	GENJI_SHOOT,
	GENJI_DEFLECT,
	GENJI_DEFLECT_HIT,
	GENJI_STRIKE,
	GENJI_JUMP,
	TRACER_RELOAD,
	TRACER_SHOOT,
	TRACER_BLINK,
	MCCREE_RELOAD,
	MCCREE_SHOOT,
	MCCREE_FLASHBANG,
	MCCREE_ROLL,
	SOLDIER76_RELOAD,
	SOLDIER76_SHOOT,
	SOLDIER76_HELIX,
	BASTION_SHOOT_0,
	BASTION_SHOOT_1,
	BASTION_RECONFIGURE_0,
	BASTION_RECONFIGURE_1,
	BASTION_RELOAD_0,
	BASTION_RELOAD_1,
	MEI_RELOAD,
	MEI_SHOOT_0,
	MEI_SHOOT_1,
	MEI_FREEZE,
	MEI_UNFREEZE,
	MEI_CRYSTAL_START,
	MEI_CRYSTAL_STOP,
	WIDOWMAKER_RELOAD,
	WIDOWMAKER_SHOOT_0,
	WIDOWMAKER_SHOOT_1,
	WIDOWMAKER_CHARGE,
	WIDOWMAKER_MINE_THROW,
	WIDOWMAKER_MINE_LAND,
	WIDOWMAKER_MINE_TRIGGER,
	WIDOWMAKER_MINE_DESTROYED,
	MERCY_RELOAD,
	MERCY_SHOOT,
	MERCY_HEAL_VOICE,
	MERCY_DAMAGE_VOICE,
	MERCY_HOVER,
	MERCY_ANGEL,
	MERCY_ANGEL_VOICE,
	MERCY_BEAM_START,
	MERCY_BEAM_DURING,
	MERCY_BEAM_STOP,
	JUNKRAT_RELOAD,
	JUNKRAT_SHOOT,
	JUNKRAT_LAUGH_VOICE,
	JUNKRAT_DEATH,
	JUNKRAT_GRENADE_BOUNCE,
	JUNKRAT_GRENADE_EXPLODE,
	JUNKRAT_GRENADE_TICK_0,
	JUNKRAT_GRENADE_TICK_1,
	JUNKRAT_GRENADE_TICK_2,
	JUNKRAT_GRENADE_TICK_3,
	JUNKRAT_TRAP_THROW,
	JUNKRAT_TRAP_LAND,
	JUNKRAT_TRAP_TRIGGER,
	JUNKRAT_TRAP_BREAK,
	JUNKRAT_TRAP_TRIGGER_OWNER,
	JUNKRAT_TRAP_TRIGGER_VOICE,
	JUNKRAT_TRAP_PLACED_VOICE,
	JUNKRAT_MINE_THROW,
	JUNKRAT_MINE_LAND,
	JUNKRAT_MINE_EXPLODE,
	SOMBRA_RELOAD,
	SOMBRA_SHOOT,
	SOMBRA_INVISIBLE_START,
	SOMBRA_INVISIBLE_STOP,
	SOMBRA_INVISIBLE_VOICE,
	SOMBRA_TRANSLOCATOR_LAND,
	SOMBRA_TRANSLOCATOR_THROW,
	SOMBRA_TRANSLOCATOR_DURING,
	SOMBRA_TRANSLOCATOR_TELEPORT,
	LUCIO_RELOAD,
	LUCIO_SHOOT,
	LUCIO_CROSSFADE,
	LUCIO_AMP,
	LUCIO_AMP_VOICE,
	LUCIO_PASSIVE_SPEED,
	LUCIO_PASSIVE_SPEED_VOICE,
	LUCIO_PASSIVE_HEAL,
	LUCIO_PASSIVE_HEAL_VOICE,
	LUCIO_SOUNDWAVE,
	LUCIO_SOUNDWAVE_VOICE,
	
	ZENYATTA_RELOAD,
	ZENYATTA_SHOOT,
	ZENYATTA_HEAL,
	ZENYATTA_HEAL_RETURN,
	ZENYATTA_HEAL_VOICE,
	ZENYATTA_DAMAGE,
	ZENYATTA_DAMAGE_RETURN,
	ZENYATTA_DAMAGE_VOICE,
	ZENYATTA_VOLLEY_CHARGE,
	ZENYATTA_VOLLEY_SHOOT,
	HEALTH_PACK_USE,
	HEALTH_PACK_RESPAWN;

	public final ModSoundEvent event;
	public final ResourceLocation loc;
	public boolean isVoiceLine;
	@Nullable
	public EnumHero hero;

	private ModSoundEvents() {
		loc = new ResourceLocation(Minewatch.MODID, this.name().toLowerCase());
		event = new ModSoundEvent(loc, this);
		// PORT 1.12: event.setRegistryName(loc.getResourcePath());
		this.isVoiceLine = this.name().contains("VOICE");
		String heroName = this.name().split("_")[0];
		for (EnumHero hero : EnumHero.values())
			if (hero.name().equals(heroName))
				this.hero = hero;
		if (this.hero != null && this.name().contains("RELOAD") && this.hero.reloadSound == null)
			this.hero.reloadSound = this;
	}

	/**To allow future customization - i.e. adjust volume based on teams*/
	public void playSound(Entity entity, float volume, float pitch) {
		this.playSound(entity, volume, pitch, false);
	}

	/**To allow future customization - i.e. adjust volume based on teams*/
	public void playSound(Entity entity, float volume, float pitch, boolean onlyPlayToEntity) {
		if (entity != null && (!entity.worldObj.isRemote || !onlyPlayToEntity || entity == Minewatch.proxy.getClientPlayer()) && 
				this.shouldPlay(entity)) 
			this.playSound(entity.worldObj, entity.posX, entity.posY, entity.posZ, volume, pitch);
	}

	/**To allow future customization - i.e. adjust volume based on teams*/
	public void playSound(World worldObj, double x, double y, double z, float volume, float pitch) {
		if (worldObj != null) 
			worldObj.playSound(worldObj.isRemote ? Minewatch.proxy.getClientPlayer() : null, x, y, z, event, SoundCategory.PLAYERS, volume, pitch);
	}

	/**To allow future customization - i.e. adjust volume based on teams*/
	public Object playFollowingSound(Entity entity, float volume, float pitch, boolean repeat) {
		return entity != null && this.shouldPlay(entity) ? Minewatch.proxy.playFollowingSound(entity, event, SoundCategory.PLAYERS, volume, pitch, repeat) : null;
	}

	/**Handles voice cooldown - only works for same client / server...*/
	public boolean shouldPlay(Entity entity) {
		if (!this.isVoiceLine)
			return true;
		else if (entity == null || TickHandler.hasHandler(entity, Identifier.VOICE_COOLDOWN))
			return false;
		else {
			TickHandler.register(entity.worldObj.isRemote, EnumHero.VOICE_COOLDOWN.setEntity(entity).setTicks(200));
			return true;
		}
	}

	/**Stops sound for all players*/
	public void stopSound(World worldObj) {
		if (worldObj != null)
			for (EntityPlayer player : worldObj.playerEntities)
				Minewatch.proxy.stopSound(player, event, SoundCategory.PLAYERS);
	}

	/**Stops sound for a player*/
	public void stopSound(EntityPlayer player) {
		if (player != null)
			Minewatch.proxy.stopSound(player, event, SoundCategory.PLAYERS);
	}

	private void register() {
		GameRegistry.register(event, loc);	
	}

	/**Separate Sound Event class to prevent bypassing using these methods (i.e. proxy playFollowSound method)*/
	public static class ModSoundEvent extends SoundEvent {

		public ModSoundEvents event;

		private ModSoundEvent(ResourceLocation soundName, ModSoundEvents event) {
			super(soundName);
			this.event = event;
		}

	}

	public static void preInit() {
		for (ModSoundEvents event : ModSoundEvents.values())
			event.register();
	}
}