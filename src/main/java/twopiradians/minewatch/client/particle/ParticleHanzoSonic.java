package twopiradians.minewatch.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;
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
	
	public ParticleHanzoSonic(World world, Entity entity, boolean isBig) {
		this(world, entity.posX, entity.posY+entity.height/2, entity.posZ, isBig);
		this.entity = entity;
	}
	
	public ParticleHanzoSonic(World world, double x, double y, double z, boolean isBig) {
		super(world, x, y, z, 0, 0, 0);
		this.isBig = isBig;
		this.particleGravity = 0.0f;
		this.particleMaxAge = 40;
		this.particleScale = 1;
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

		this.particleScale += this.isBig ? 2.5d : 0.3d;
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}
}
