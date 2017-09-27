package twopiradians.minewatch.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.ClientProxy;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;

@SideOnly(Side.CLIENT)
public class ParticleAnaHealth extends ParticleSimpleAnimated {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Minewatch.MODID, "entity/particle/ana_health");
	private EntityLivingBase entity;

	public ParticleAnaHealth(EntityLivingBase entity) {
		super(entity.worldObj, entity.posX, entity.posY+entity.height+0.8d, entity.posZ, 0, 0, 0);
		this.entity = entity;
		this.particleGravity = 0.0f;
		this.particleMaxAge = Integer.MAX_VALUE;
		this.particleScale = 3;
		this.particleAlpha = 0.7f;
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

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (entity.isDead || entity.getHealth() >= entity.getMaxHealth()  ||
				player.getHeldItemMainhand() == null || (player.getHeldItemMainhand().getItem() != EnumHero.ANA.weapon &&
				player.getHeldItemMainhand().getItem() != EnumHero.MERCY.weapon)) {
			ClientProxy.healthParticleEntities.remove(entity.getPersistentID());
			this.setExpired();
		}
		else 
			this.setPosition(entity.posX, entity.posY+entity.height+0.8d, entity.posZ);
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}
}
