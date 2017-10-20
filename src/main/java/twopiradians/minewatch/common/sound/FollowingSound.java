package twopiradians.minewatch.common.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.EntityJunkratGrenade;

@SideOnly(Side.CLIENT)
public class FollowingSound extends MovingSound {

	private final Entity entity;
	private int junkratGrenadeBounces;

	public FollowingSound(Entity entity, SoundEvent event, SoundCategory category, float volume, float pitch, boolean repeat) {
		super(event, category);
		this.entity = entity;
		this.volume = volume;
		this.pitch = pitch;
		this.repeat = repeat;
		this.attenuationType = ISound.AttenuationType.LINEAR;

		// junkrat grenade tick
		if (entity instanceof EntityJunkratGrenade)
			for (int i=0; i<ModSoundEvents.junkratGrenadeTick.length; ++i)
				if (event.equals(ModSoundEvents.junkratGrenadeTick[i]))
					junkratGrenadeBounces = i;
	}

	public void update() {
		if (this.entity != null && !this.entity.isDead && (!(entity instanceof EntityJunkratGrenade) || 
				((EntityJunkratGrenade)entity).bounces == this.junkratGrenadeBounces)) {
			this.xPosF = (float)this.entity.posX;
			this.yPosF = (float)this.entity.posY;
			this.zPosF = (float)this.entity.posZ;
		}
		else 
			this.donePlaying = true;
	}
}
