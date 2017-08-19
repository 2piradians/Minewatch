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
public class ParticleSmoke extends ParticleSimpleAnimated {
	
	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
			new ResourceLocation(Minewatch.MODID, "entity/particle/smoke_0"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/smoke_1"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/smoke_2"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/smoke_3")
	};
	private float fadeTargetRed;
	private float fadeTargetGreen;
	private float fadeTargetBlue;
	private float originalScale;
	
	public ParticleSmoke(World world, double x, double y, double z, int color, int colorFade, float scale, int maxAge) {
		super(world, x, y, z, 0, 0, 0);
		this.particleGravity = 0;
		this.particleMaxAge = maxAge;
		this.particleScale = scale;
		this.originalScale = scale;
		this.setColor(color);
		this.setColorFade(colorFade);
		ResourceLocation texture = new ResourceLocation(Minewatch.MODID, "entity/particle/smoke_0");
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
		this.setParticleTexture(sprite); 	
	}
	
	@Override
    public void onUpdate() {
		super.onUpdate();
		
		// color fade (faster than vanilla)
        this.particleRed += (this.fadeTargetRed - this.particleRed) * 0.4F;
        this.particleGreen += (this.fadeTargetGreen - this.particleGreen) * 0.4F;
        this.particleBlue += (this.fadeTargetBlue - this.particleBlue) * 0.4F;
		
		this.particleAlpha = (float)(this.particleMaxAge - this.particleAge) / this.particleMaxAge;
		this.particleScale -= this.originalScale / 30f;

		if (this.particleAge % (this.particleMaxAge / 4) == 0) {
			int num = Math.min(this.particleAge / (this.particleMaxAge / 4), 3);
			ResourceLocation texture = new ResourceLocation(Minewatch.MODID, "entity/particle/smoke_"+num);
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
			this.setParticleTexture(sprite); 
		}
    }
	
	@Override
    public void setColorFade(int rgb) {
        this.fadeTargetRed = (float)((rgb & 16711680) >> 16) / 255.0F;
        this.fadeTargetGreen = (float)((rgb & 65280) >> 8) / 255.0F;
        this.fadeTargetBlue = (float)((rgb & 255) >> 0) / 255.0F;
    }

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}
	
}
