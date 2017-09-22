package twopiradians.minewatch.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCustom extends ParticleSimpleAnimated {

	private float fadeTargetRed;
	private float fadeTargetGreen;
	private float fadeTargetBlue;
	private float initialAlpha;
	private float initialScale;
	private float finalScale;
	private float rotationSpeed;

	public ParticleCustom(ResourceLocation texture, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) {
		super(world, x, y, z, 0, 0, 0);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		this.particleGravity = 0.0f;
		this.particleMaxAge = maxAge; 
		this.particleScale = initialScale;
		this.initialScale = initialScale;
		this.finalScale = finalScale;
		this.particleAlpha = alpha;
		this.initialAlpha = alpha;
		this.particleAngle = initialRotation;
		this.rotationSpeed = rotationSpeed;
		this.setColor(color);
		this.setColorFade(colorFade);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
		this.setParticleTexture(sprite); 	
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		this.prevParticleAngle = this.particleAngle;
		this.particleAngle += rotationSpeed;

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

	public void oneTickToLive() {
		this.particleMaxAge = this.particleAge + 3;
		this.particleAlpha = this.initialAlpha;
	}

}