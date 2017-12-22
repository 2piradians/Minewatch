package twopiradians.minewatch.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;

@SideOnly(Side.CLIENT)
public class ParticleHanzoSonic extends ParticleSimpleAnimated {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Minewatch.MODID, "entity/particle/hanzo_sonic");
	private boolean isBig;
	private Entity entity;
	private boolean isFast;

	public ParticleHanzoSonic(World worldObj, Entity entity, boolean isBig) {
		this(worldObj, entity.posX, entity.posY+entity.height/2, entity.posZ, isBig, false);
		this.entity = entity;
	}

	public ParticleHanzoSonic(World worldObj, double x, double y, double z, boolean isBig, boolean isFast) {
		super(worldObj, x, y, z, 0, 0, 0);
		this.isBig = isBig;
		this.isFast = isFast;
		this.particleGravity = 0.0f;
		this.particleMaxAge = isFast ? 7 : 40;
		this.particleScale = isFast ? 0.8f : 1;
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

		if (this.entity != null) {
			this.posX = entity.posX;
			this.posY = entity.posY+entity.height/2;
			this.posZ = entity.posZ;
		}

		if (this.isBig)
			this.particleScale += 2.5d;
		else if (this.isFast)
			this.particleScale += (float) Math.min(particleScale+0.1d, 1.5d);
		else
			this.particleScale += 0.3d;
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}
	
	@Override
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.enableBlend();
		super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}
	
}