package twopiradians.minewatch.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;

@SideOnly(Side.CLIENT)
public class ParticleTrail extends ParticleSimpleAnimated {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Minewatch.MODID, "entity/particle/trail");
	private float fadeTargetRed;
	private float fadeTargetGreen;
	private float fadeTargetBlue;
	private float initialAlpha;
	private float maxAge;
	private float age;
	private float initialScale;
	private int initialColor;
	
	public ParticleTrail(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float scale, int maxAge, float initialAge, float alpha) {
		super(world, x, y, z, 0, 0, 0);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		this.particleGravity = 0.01f;
		this.age = initialAge;
		this.maxAge = maxAge;
		this.particleMaxAge = Integer.MAX_VALUE;
		this.particleScale = scale;
		this.initialScale = scale;
		this.particleAlpha = alpha;
		this.initialAlpha = alpha;
		this.initialColor = color;
		this.setColor(color);
		this.setColorFade(colorFade);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEXTURE.toString());
		this.setParticleTexture(sprite); 
		this.onUpdate();
	}
	
	@Override
    public void onUpdate() {
		super.onUpdate();

        float agePercent = this.age / this.maxAge;
		
		this.particleScale = (1f-agePercent/2f) * this.initialScale;
		
		// color fade
		this.setColor(this.initialColor);
        this.particleRed += (this.fadeTargetRed - this.particleRed) * agePercent;
        this.particleGreen += (this.fadeTargetGreen - this.particleGreen) * agePercent;
        this.particleBlue += (this.fadeTargetBlue - this.particleBlue) * agePercent;
		
		this.particleAlpha = (1f-agePercent) * this.initialAlpha;
		
        if (this.age++ >= this.maxAge)
            this.setExpired();

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
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.enableBlend();
		super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}
	
}