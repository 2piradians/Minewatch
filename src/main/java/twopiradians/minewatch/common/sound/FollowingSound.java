package twopiradians.minewatch.common.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FollowingSound extends MovingSound {
	
    private final Entity entity;

    public FollowingSound(Entity entity, SoundEvent event, SoundCategory category, float volume, float pitch) {
        super(event, category);
        this.entity = entity;
        this.volume = volume;
        this.pitch = pitch;
        this.attenuationType = ISound.AttenuationType.LINEAR;
    }

    public void update() {
        if (this.entity != null && !this.entity.isDead) {
            this.xPosF = (float)this.entity.posX;
            this.yPosF = (float)this.entity.posY;
            this.zPosF = (float)this.entity.posZ;
        }
        else {
            this.donePlaying = true;
        }
    }
}
