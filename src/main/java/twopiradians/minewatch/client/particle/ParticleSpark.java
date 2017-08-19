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
public class ParticleSpark extends ParticleSimpleAnimated {
	
	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
			new ResourceLocation(Minewatch.MODID, "entity/particle/spark_0"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/spark_1"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/spark_2"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/spark_3")
	};

	private float originalScale;
	
	public ParticleSpark(World world, double x, double y, double z, int color, int colorFade, float scale, int maxAge) {
		super(world, x, y, z, 0, 0, 0);
		this.particleGravity = 0;
		this.particleMaxAge = maxAge;
		this.particleScale = scale;
		this.originalScale = scale;
		this.particleAlpha = 0.5f;
		this.setColor(color);
		this.setColorFade(colorFade);
		ResourceLocation texture = new ResourceLocation(Minewatch.MODID, "entity/particle/spark_"+world.rand.nextInt(4));
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
		this.setParticleTexture(sprite); 	
	}
	
	@Override
    public void onUpdate() {
		super.onUpdate();

		this.particleAlpha = Math.max((float)(this.particleMaxAge - this.particleAge) / this.particleMaxAge - 0.1f, 0);
		this.particleScale -= this.originalScale / 30f;
    }

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}
	
}
