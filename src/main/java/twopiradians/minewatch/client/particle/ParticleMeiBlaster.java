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
public class ParticleMeiBlaster extends ParticleSimpleAnimated {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Minewatch.MODID, "entity/particle/mei_blaster");
	private float fadeTargetRed;
	private float fadeTargetGreen;
	private float fadeTargetBlue;
	private float initialAlpha;
	private float initialScale;
	private float finalScale;

	public ParticleMeiBlaster(World world, double x, double y, double z, double motionX, double motionY, double motionZ, float alpha, int maxAge, float initialScale, float finalScale) {
		super(world, x, y, z, 0, 0, 0);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		this.particleGravity = 0.0f;
		this.particleMaxAge = maxAge+99999; //TODO
		this.particleScale = initialScale;
		this.initialScale = initialScale;
		this.finalScale = finalScale;
		this.particleAlpha = alpha;
		this.initialAlpha = alpha;
		this.setColorFade(0xAED4FF);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEXTURE.toString());
		this.setParticleTexture(sprite); 	
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		// color fade (faster than vanilla)
		this.particleRed += (this.fadeTargetRed - this.particleRed) * 0.4F;
		this.particleGreen += (this.fadeTargetGreen - this.particleGreen) * 0.4F;
		this.particleBlue += (this.fadeTargetBlue - this.particleBlue) * 0.4F;

		this.particleAlpha = Math.max((float)(this.particleMaxAge - this.particleAge) / this.particleMaxAge * this.initialAlpha, 0.1f);

		this.particleScale = ((float)this.particleAge / this.particleMaxAge) * (this.finalScale - this.initialScale) + this.initialScale;
	}

	@Override
	public void setColorFade(int rgb) {
		this.fadeTargetRed = (float)((rgb & 16711680) >> 16) / 255.0F;
		this.fadeTargetGreen = (float)((rgb & 65280) >> 8) / 255.0F;
		this.fadeTargetBlue = (float)((rgb & 255) >> 0) / 255.0F;
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}

}