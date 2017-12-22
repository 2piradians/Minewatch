package twopiradians.minewatch.client.particle;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.entity.ability.EntityJunkratTrap;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

@SideOnly(Side.CLIENT)
public class ParticleCustom extends ParticleSimpleAnimated {

	private float fadeTargetRed;
	private float fadeTargetGreen;
	private float fadeTargetBlue;
	private float initialAlpha;
	private float initialScale;
	private float finalScale;
	private float rotationSpeed;
	@Nullable
	private Entity followEntity;
	private EnumParticle enumParticle;
	private float verticalAdjust;
	private float horizontalAdjust;
	private EnumHand hand;
	@Nullable
	private EnumFacing facing;
	private float pulseRate;

	public ParticleCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, float pulseRate, @Nullable EnumFacing facing) {
		super(world, x, y, z, 0, 0, 0);
		this.enumParticle = enumParticle;
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
		this.prevParticleAngle = initialRotation;
		this.particleAngle = initialRotation;
		this.rotationSpeed = rotationSpeed;
		this.setColor(color);
		this.setColorFade(colorFade);
		this.pulseRate = pulseRate;
		this.facing = facing;
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
				enumParticle.loc.toString()+(enumParticle.variations > 1 ? "_"+world.rand.nextInt(enumParticle.variations) : ""));
		this.setParticleTexture(sprite);
	}

	public ParticleCustom(EnumParticle enumParticle, World world, Entity followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, EnumHand hand, float verticalAdjust, float horizontalAdjust) {
		this(enumParticle, world, 0, 0, 0, 0, 0, 0, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed, 0, null);
		this.hand = hand;
		this.verticalAdjust = verticalAdjust;
		this.horizontalAdjust = horizontalAdjust;
		this.followEntity = followEntity;
		this.followEntity();
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
	}

	public ParticleCustom(EnumParticle enumParticle, World world, Entity followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) {
		this(enumParticle, world, followEntity, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed, null, 0, 0);
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

		this.prevParticleAngle = this.particleAngle;
		this.particleAngle += rotationSpeed;
		this.particleAlpha = Math.max((float)(this.particleMaxAge - this.particleAge) / this.particleMaxAge * this.initialAlpha, 0.05f);
		this.particleScale = ((float)this.particleAge / this.particleMaxAge) * (this.finalScale - this.initialScale) + this.initialScale;
		if (this.enumParticle.disableDepth && Minecraft.getMinecraft().player != null)
			this.particleScale = (float) (this.initialScale + Minecraft.getMinecraft().player.getDistance(posX, posY, posZ) / 5f);

		// pulse (untested)
		if (this.pulseRate > 0) {
			float pulse = (this.particleAge % pulseRate)/(pulseRate*2) - 0.25f;
			if (this.particleAge % pulseRate > pulseRate/2)
				pulse *= -1;
			this.particleRed += pulse;
			this.particleGreen += pulse;
			this.particleBlue += pulse;
		}
		
		// gravity
		if (this.enumParticle.gravity != 0)
			this.motionY += this.enumParticle.gravity;

		// follow entity
		this.followEntity();
	}

	public void followEntity() {
		if (this.followEntity != null) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (this.enumParticle.equals(EnumParticle.HEALTH) && followEntity instanceof EntityLivingBase) {
				if (followEntity.isDead || ((EntityLivingBase) followEntity).getHealth() >= ((EntityLivingBase) followEntity).getMaxHealth()/2f ||
						((EntityLivingBase) followEntity).getHealth() <= 0 ||
						player.getHeldItemMainhand() == null || !(player.getHeldItemMainhand().getItem() instanceof ItemMWWeapon) || 
						!((ItemMWWeapon)player.getHeldItemMainhand().getItem()).showHealthParticles) 
					this.setExpired();
				this.setPosition(this.followEntity.posX, this.followEntity.posY+this.followEntity.height/2d, this.followEntity.posZ);
			}
			else if (this.enumParticle.equals(EnumParticle.JUNKRAT_TRAP)) {
				this.setPosition(this.followEntity.posX, this.followEntity.posY+1.5d+(Math.sin(this.followEntity.ticksExisted/5d))/10d, this.followEntity.posZ);
				if (!(this.followEntity instanceof EntityJunkratTrap) || ((EntityJunkratTrap)this.followEntity).trappedEntity != null || !this.followEntity.onGround)
					this.setExpired();
			}
			else if (this.enumParticle.equals(EnumParticle.WIDOWMAKER_MINE)) {
				this.setPosition(this.followEntity.posX, this.followEntity.posY+1d+(Math.sin(this.followEntity.ticksExisted/5d))/10d, this.followEntity.posZ);
				if (!this.followEntity.onGround)
					this.setExpired();
			}
			else if (this.enumParticle.equals(EnumParticle.SOMBRA_TRANSPOSER)) {
				this.setPosition(this.followEntity.posX, this.followEntity.posY+1d+(Math.sin(this.followEntity.ticksExisted/5d))/10d, this.followEntity.posZ);
				if (!this.followEntity.onGround)
					this.setExpired();
			}
			else if (this.enumParticle.equals(EnumParticle.ZENYATTA_HARMONY)) {
				this.setPosition(this.followEntity.posX, this.followEntity.posY+this.followEntity.height+0.5d+(Math.sin(this.followEntity.ticksExisted/5d))/10d, this.followEntity.posZ);
				if (!TickHandler.hasHandler(handler -> handler.identifier == Identifier.ZENYATTA_HARMONY && handler.entityLiving == this.followEntity, true))
					this.setExpired();
			}
			else if (this.enumParticle.equals(EnumParticle.ZENYATTA_DISCORD)) {
				this.setPosition(this.followEntity.posX, this.followEntity.posY+this.followEntity.height+0.5d+(Math.sin(this.followEntity.ticksExisted/5d))/10d, this.followEntity.posZ);
				if (!TickHandler.hasHandler(handler -> handler.identifier == Identifier.ZENYATTA_DISCORD && handler.entityLiving == this.followEntity, true))
					this.setExpired();
			}
			else if (this.enumParticle.equals(EnumParticle.ZENYATTA_DISCORD_ORB)) {
				this.setPosition(this.followEntity.posX+0.25d, this.followEntity.posY+this.followEntity.height+0.25d, this.followEntity.posZ);
				if (!TickHandler.hasHandler(handler -> handler.identifier == Identifier.ZENYATTA_DISCORD && handler.entityLiving == this.followEntity, true))
					this.setExpired();
			}
			else if (this.enumParticle.equals(EnumParticle.ZENYATTA_HARMONY_ORB)) {
				this.setPosition(this.followEntity.posX-0.25d, this.followEntity.posY+this.followEntity.height+0.25d, this.followEntity.posZ);
				if (!TickHandler.hasHandler(handler -> handler.identifier == Identifier.ZENYATTA_HARMONY && handler.entityLiving == this.followEntity, true))
					this.setExpired();
			}
			else if ((this.verticalAdjust != 0 || this.horizontalAdjust != 0) && followEntity instanceof EntityLivingBase) {
				Vec3d vec = EntityHelper.getShootingPos((EntityLivingBase) followEntity, followEntity.rotationPitch, followEntity.rotationYaw, hand, verticalAdjust, horizontalAdjust);
				this.setPosition(vec.x, vec.y, vec.z);
				this.prevPosX = this.posX;
				this.prevPosY = this.posY;
				this.prevPosZ = this.posZ;
			}
			else
				this.setPosition(this.followEntity.posX, this.followEntity.posY+this.followEntity.height/2d, this.followEntity.posZ);

			if (this.enumParticle == EnumParticle.ZENYATTA) {
				this.particleAlpha = this.initialAlpha;
				if (!(this.followEntity instanceof EntityLivingBase && ((EntityLivingBase)this.followEntity).getActiveItemStack() != null && 
						((EntityLivingBase)this.followEntity).getActiveItemStack().getItem() == EnumHero.ZENYATTA.weapon))
					this.setExpired();
			}

			if (!this.followEntity.isEntityAlive())
				this.setExpired();
		}
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
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.enableBlend();
		if (this.particleTexture != null) {
			// face a direction on an axis TODO have these only render on blocks, like Render#renderShadow
			if (facing != null) {
				float pitch = 0, yaw = 0;
				float rotation = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
				if (facing == EnumFacing.WEST || facing == EnumFacing.EAST) {
					pitch = 0;
					yaw = 90;
				}
				else if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
					pitch = 0;
					yaw = 0;
				}
				else if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
					pitch = 90;
					yaw = 180f * rotation - 90f;
				}
				// translated from ActiveRenderInfo#updateRenderInfo
				rotationX = MathHelper.cos(yaw * 0.017453292F) * (float)(1 - 0 * 2);
				rotationYZ = MathHelper.sin(yaw * 0.017453292F) * (float)(1 - 0 * 2);
				rotationXY = -rotationYZ * MathHelper.sin(pitch * 0.017453292F) * (float)(1 - 0 * 2);
				rotationXZ = rotationX * MathHelper.sin(pitch * 0.017453292F) * (float)(1 - 0 * 2);
				rotationZ = MathHelper.cos(pitch * 0.017453292F);
			}

			// update muzzle every render so it's always rendered accurately
			if ((this.verticalAdjust != 0 || this.horizontalAdjust != 0) && followEntity instanceof EntityLivingBase)
				this.followEntity();

			// change frame
			int frame = MathHelper.clamp(this.particleAge / Math.max(1, this.particleMaxAge / enumParticle.frames) + 1, 1, enumParticle.frames);
			if (enumParticle == EnumParticle.ZENYATTA_DISCORD_ORB || enumParticle == EnumParticle.ZENYATTA_HARMONY_ORB)
				frame = Minecraft.getMinecraft().player.ticksExisted % 15 / 4 + 1;
			int framesPerRow = (int) Math.sqrt(enumParticle.frames);
			int row = (frame-1) / framesPerRow;
			int col = (frame-1) % framesPerRow;
			double uSize = (this.particleTexture.getMaxU()-this.particleTexture.getMinU()) / framesPerRow;
			double vSize = (this.particleTexture.getMaxV()-this.particleTexture.getMinV()) / framesPerRow;

			float f = (float) (this.particleTexture.getMinU()+uSize*col);
			float f1 = (float) (f+uSize);
			float f2 = (float) (this.particleTexture.getMinV()+vSize*row);
			float f3 = (float) (f2+vSize);
			float f4 = 0.1F * this.particleScale;

			float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
			float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
			float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

			int i = this.getBrightnessForRender(partialTicks);
			int j = i >> 16 & 65535;
			int k = i & 65535;
			Vec3d[] avec3d = new Vec3d[] {new Vec3d((double)(-rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double)(-rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(rotationYZ * f4 - rotationXZ * f4))};

			if (this.particleAngle != 0.0F) {
				float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
				float f9 = MathHelper.cos(f8 * 0.5F);
				float f10 = MathHelper.sin(f8 * 0.5F) * (facing == null ? (float)cameraViewDir.x : 0);
				float f11 = MathHelper.sin(f8 * 0.5F) * (facing == null ? (float)cameraViewDir.y : 0);
				float f12 = MathHelper.sin(f8 * 0.5F) * (facing == null ? (float)cameraViewDir.z : 0);
				Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

				for (int l = 0; l < 4; ++l)
					avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
			}
			
			// draw normally to clear buffer
			if (enumParticle.disableDepth) {
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder vertexbuffer = tessellator.getBuffer();
				tessellator.draw();
				vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			}

			buffer.pos((double)f5 + avec3d[0].x, (double)f6 + avec3d[0].y, (double)f7 + avec3d[0].z).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double)f5 + avec3d[1].x, (double)f6 + avec3d[1].y, (double)f7 + avec3d[1].z).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double)f5 + avec3d[2].x, (double)f6 + avec3d[2].y, (double)f7 + avec3d[2].z).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double)f5 + avec3d[3].x, (double)f6 + avec3d[3].y, (double)f7 + avec3d[3].z).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();

			// disable depth for health particles
			if (enumParticle.disableDepth) {
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder vertexbuffer = tessellator.getBuffer();
				GlStateManager.disableDepth();
				tessellator.draw();
				GlStateManager.enableDepth();
				vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			}

		}
	}
	
	@Override
    public void setExpired() {
        super.setExpired();
        
        if (this.enumParticle.onePerEntity && this.followEntity != null)
        	this.enumParticle.particleEntities.remove(this.followEntity.getPersistentID());
    }

}