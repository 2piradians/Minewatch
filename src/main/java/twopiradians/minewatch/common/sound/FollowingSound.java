package twopiradians.minewatch.common.sound;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.projectile.EntityJunkratGrenade;

@SideOnly(Side.CLIENT)
public class FollowingSound extends MovingSound {

	public static CopyOnWriteArrayList<FollowingSound> sounds = new CopyOnWriteArrayList<FollowingSound>();

	private final Entity entity;
	private int junkratGrenadeBounces;
	public boolean lucioSound;

	public FollowingSound(Entity entity, SoundEvent event, SoundCategory category, float volume, float pitch, boolean repeat, ISound.AttenuationType type) {
		super(event, category);
		this.entity = entity;
		this.volume = volume;
		this.pitch = pitch;
		this.repeat = repeat;
		this.attenuationType = type;
		sounds.add(this);

		// junkrat grenade tick
		if (entity instanceof EntityJunkratGrenade)
			junkratGrenadeBounces = ((EntityJunkratGrenade)entity).bounces;
	}

	public void update() {
		if (this.entity != null && this.entity.isEntityAlive() && 
				(!(entity instanceof EntityJunkratGrenade) || 
						((EntityJunkratGrenade)entity).bounces == this.junkratGrenadeBounces)) {
			this.xPosF = (float)this.entity.posX;
			this.yPosF = (float)this.entity.posY;
			this.zPosF = (float)this.entity.posZ;
			if (this.lucioSound && this.volume > 0.1f)
				this.volume = Math.max(0.1f, this.volume-0.005f);
		}
		else 
			this.donePlaying = true;
	}

	@Override
	public boolean isDonePlaying() {
		boolean ret = super.isDonePlaying();
		if (ret) // remove from list if it says it's done playing
			sounds.remove(this);
		return ret;
	}

	/**Mark the sound as donePlaying (to make public)*/
	public static void stopPlaying(@Nullable FollowingSound sound) {
		if (sound != null)
			sound.donePlaying = true;
	}

	/**Stop playing a FollowingSound that's playing event with this followingEntity*/
	public static void stopPlaying(SoundEvent event, Entity followingEntity) {
		if (event != null && followingEntity != null)
			stopPlaying(event.getSoundName().toString(), followingEntity);
	}
	
	/**Stop playing a FollowingSound that's playing event with this followingEntity*/
	public static void stopPlaying(String event, Entity followingEntity) {
		if (event != null && followingEntity != null)
			for (FollowingSound sound : sounds)
				if (sound.entity == followingEntity && sound.positionedSoundLocation.toString().equals(event))
					sound.donePlaying = true;
	}

}