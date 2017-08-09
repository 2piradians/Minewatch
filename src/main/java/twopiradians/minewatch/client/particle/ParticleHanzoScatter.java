package twopiradians.minewatch.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;

@SideOnly(Side.CLIENT)
public class ParticleHanzoScatter extends ParticleSimpleAnimated {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Minewatch.MODID, "entity/particle/hanzo_scatter");
	
	public ParticleHanzoScatter(World world, double x, double y, double z) {
		super(world, x, y, z, 0, 0, 0);
		this.particleGravity = 0.01f;
		this.particleMaxAge = 20;
		this.particleScale = 1;
		this.setColorFade(0x007acc);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEXTURE.toString());
		this.setParticleTexture(sprite); 	
	}
	
	@Override
    public void onUpdate() {
		super.onUpdate();
		
		this.particleAlpha = (float)(this.particleMaxAge - this.particleAge) / this.particleMaxAge;
    }

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}
	
}
