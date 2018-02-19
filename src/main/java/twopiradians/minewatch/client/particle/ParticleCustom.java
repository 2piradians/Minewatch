package twopiradians.minewatch.client.particle;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.entity.ability.EntityJunkratTrap;
import twopiradians.minewatch.common.entity.ability.EntityMoiraOrb;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.RenderManager;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.item.weapon.ItemDoomfistWeapon;
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
	private float distance;
	private EnumHand hand;
	@Nullable
	private EnumFacing facing;
	private float pulseRate;
	private boolean renderOnBlocks;

	public ParticleCustom(EnumParticle enumParticle, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, float pulseRate, @Nullable EnumFacing facing, boolean renderOnBlocks) {
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
		this.renderOnBlocks = renderOnBlocks;
		if (facing != null && renderOnBlocks)
			enumParticle.facingParticles.add(this);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
				enumParticle.loc.toString()+(enumParticle.variations > 1 ? "_"+world.rand.nextInt(enumParticle.variations) : ""));
		this.setParticleTexture(sprite);
	}

	public ParticleCustom(EnumParticle enumParticle, World world, Entity followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, EnumHand hand, float verticalAdjust, float horizontalAdjust, float distance) {
		this(enumParticle, world, 0, 0, 0, 0, 0, 0, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed, 0, null, false);
		this.hand = hand;
		this.verticalAdjust = verticalAdjust;
		this.horizontalAdjust = horizontalAdjust;
		this.distance = distance;
		this.followEntity = followEntity;
		this.followEntity();
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
	}

	public ParticleCustom(EnumParticle enumParticle, World world, Entity followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed, EnumHand hand, float verticalAdjust, float horizontalAdjust) {
		this(enumParticle, world, followEntity, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed, hand, verticalAdjust, horizontalAdjust, 1);
	}

	public ParticleCustom(EnumParticle enumParticle, World world, Entity followEntity, int color, int colorFade, float alpha, int maxAge, float initialScale, float finalScale, float initialRotation, float rotationSpeed) {
		this(enumParticle, world, followEntity, color, colorFade, alpha, maxAge, initialScale, finalScale, initialRotation, rotationSpeed, null, 0, 0, 0);
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
		this.particleAlpha = Math.max((float)(this.particleMaxAge - this.particleAge) / this.particleMaxAge * this.initialAlpha, 0.01f);
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
			// update ticksExisted for keeping onePerEntity particle alive
			if (this.enumParticle.onePerEntity)
				this.enumParticle.particleEntities.put(this.followEntity.getPersistentID(), this.followEntity.ticksExisted);

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
			else if (this.enumParticle.equals(EnumParticle.MOIRA_ORB) && followEntity instanceof EntityLivingBase) {
				this.horizontalAdjust = this.verticalAdjust = 1; 
				Vector2f rotations = EntityHelper.getEntityPartialRotations(followEntity);
				Vec3d vec = EntityHelper.getShootingPos((EntityLivingBase) followEntity, rotations.x, rotations.y, this.initialAlpha != 1 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND, 10, 0.65f);
				this.setPosition(vec.xCoord, vec.yCoord, vec.zCoord);
				this.prevPosX = this.posX;
				this.prevPosY = this.posY;
				this.prevPosZ = this.posZ;
				this.particleAlpha = this.initialAlpha;
				if (!TickHandler.hasHandler(followEntity, Identifier.MOIRA_ORB_SELECT))
					this.setExpired();
			}
			else if (this.enumParticle.equals(EnumParticle.STUN)) {
				this.setPosition(this.followEntity.posX, this.followEntity.posY+this.followEntity.height+0.6d, this.followEntity.posZ);
				this.particleAlpha *= 5f;
			}
			else if (this.enumParticle.equals(EnumParticle.ANA_GRENADE_HEAL) || this.enumParticle.equals(EnumParticle.ANA_GRENADE_DAMAGE)) {
				this.setPosition(this.followEntity.posX, this.followEntity.posY+this.followEntity.height+0.6d, this.followEntity.posZ);
				this.particleAlpha = this.initialAlpha;
			}
			else if ((this.verticalAdjust != 0 || this.horizontalAdjust != 0) && followEntity instanceof EntityLivingBase) {
				Vector2f rotations = EntityHelper.getEntityPartialRotations(followEntity);
				Vec3d vec = EntityHelper.getShootingPos((EntityLivingBase) followEntity, rotations.x, rotations.y, hand, verticalAdjust, horizontalAdjust, distance);
				this.setPosition(vec.xCoord, vec.yCoord, vec.zCoord);
				this.prevPosX = this.posX;
				this.prevPosY = this.posY;
				this.prevPosZ = this.posZ;
			}
			else if (this.enumParticle == EnumParticle.DOOMFIST_SLAM_0 && followEntity instanceof EntityLivingBase) {
				Vector2f rotations = EntityHelper.getEntityPartialRotations(followEntity);
				this.facing = EnumFacing.UP;
				this.particleAngle = (rotations.y + 90f) / 180f;
				this.prevParticleAngle = this.particleAngle;
				int model = ((ItemDoomfistWeapon)EnumHero.DOOMFIST.weapon).getModel((EntityLivingBase) followEntity);
				RayTraceResult result = EntityHelper.getMouseOverBlock((EntityLivingBase) followEntity, 30, rotations.x, rotations.y);
				if (result != null && result.sideHit == EnumFacing.UP && !followEntity.onGround && 
						(model == 0 || model == 3) && EntityHelper.isHoldingItem((EntityLivingBase) followEntity, EnumHero.DOOMFIST.weapon, EnumHand.MAIN_HAND) &&
						SetManager.getWornSet(followEntity) == EnumHero.DOOMFIST) {
					Vec3d vec = result.hitVec.add(EntityHelper.getLook(0, rotations.y).scale(4d));
					this.setPosition(vec.xCoord, vec.yCoord+0.05d, vec.zCoord);
					this.prevPosX = this.posX;
					this.prevPosY = this.posY;
					this.prevPosZ = this.posZ;
				}
				else
					this.setExpired();
			}
			else
				this.setPosition(this.followEntity.posX, this.followEntity.posY+this.followEntity.height/2d, this.followEntity.posZ);

			if (this.enumParticle == EnumParticle.ZENYATTA) {
				this.particleAlpha = this.initialAlpha;
				if (!(this.followEntity instanceof EntityLivingBase && ((EntityLivingBase)this.followEntity).getActiveItemStack() != null && 
						((EntityLivingBase)this.followEntity).getActiveItemStack().getItem() == EnumHero.ZENYATTA.weapon))
					this.setExpired();
			}
			else if (this.enumParticle.equals(EnumParticle.MOIRA_ORB) && followEntity instanceof EntityMoiraOrb) {
				this.particleScale = (((EntityMoiraOrb)followEntity).chargeClient / 80f) * this.initialScale;
			}
			else if (this.enumParticle == EnumParticle.DOOMFIST_PUNCH_3) {
				boolean first = followEntity == Minewatch.proxy.getClientPlayer() && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0;
				float charge = ItemDoomfistWeapon.getCharge((EntityLivingBase) followEntity);
				if (charge >= 0) {
					this.particleAlpha = first ? charge : charge/2f;
					this.verticalAdjust = first ? 5 : 20;
					this.horizontalAdjust = first ? 1 : 0.35f;
					this.distance = first ? 1 : 0.8f;
					this.finalScale = 6;
				}
				else if (TickHandler.hasHandler(followEntity, Identifier.DOOMFIST_PUNCH)) {
					this.particleAlpha = first ? 1 : 0.8f;
					this.verticalAdjust = first ? 0 : 20;
					this.horizontalAdjust = first ? 0.5f : 0.4f;
					this.distance = first ? 1 : 1f;
					this.finalScale = 3;
				}
				else 
					this.particleAlpha = 0;
			}

			if (!this.followEntity.isEntityAlive() || TickHandler.hasHandler(followEntity, Identifier.MOIRA_FADE) || 
					TickHandler.hasHandler(followEntity, Identifier.SOMBRA_INVISIBLE))
				this.setExpired();
		}

		if (this.enumParticle.equals(EnumParticle.BEAM) || this.enumParticle.equals(EnumParticle.HOLLOW_CIRCLE_2)) {
			if (this.particleMaxAge - this.particleAge < 10)
				this.particleAlpha = Math.max((float)(this.particleMaxAge - this.particleAge) / 10f * this.initialAlpha, 0.05f);
			else
				this.particleAlpha = this.initialAlpha;
		}
		else if (this.enumParticle.equals(EnumParticle.SOMBRA_HACK_MESH))
			this.setBaseAirFriction(0.97f);
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
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.enableBlend();
		if (this.particleTexture != null && !renderOnBlocks) {
			// update muzzle every render so it's always rendered accurately
			if ((this.verticalAdjust != 0 || this.horizontalAdjust != 0 || this.enumParticle == EnumParticle.DOOMFIST_SLAM_0) && 
					followEntity instanceof EntityLivingBase)
				this.followEntity();

			// face a direction on an axis
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

			if (enumParticle.equals(EnumParticle.SOMBRA_HACK_MESH)) {
				rotationX *= 1f - (float) this.particleAge / this.particleMaxAge;
				rotationZ *= MathHelper.cos(entityIn.ticksExisted/50f);
			}

			float f = this.particleTexture.getMinU();
			float f1 = this.particleTexture.getMaxU();
			float f2 = this.particleTexture.getMinV();
			float f3 = this.particleTexture.getMaxV();
			float f4 = 0.1F * this.particleScale;

			// change frame
			if (enumParticle.frames > 1) {
				int frame = MathHelper.clamp(this.particleAge / Math.max(1, this.particleMaxAge / enumParticle.frames) + 1, 1, enumParticle.frames);
				if (enumParticle == EnumParticle.ZENYATTA_DISCORD_ORB || enumParticle == EnumParticle.ZENYATTA_HARMONY_ORB)
					frame = Minecraft.getMinecraft().player.ticksExisted % 15 / 4 + 1;
				int framesPerRow = (int) Math.sqrt(enumParticle.frames);
				int row = (frame-1) / framesPerRow;
				int col = (frame-1) % framesPerRow;
				double uSize = (this.particleTexture.getMaxU()-this.particleTexture.getMinU()) / framesPerRow;
				double vSize = (this.particleTexture.getMaxV()-this.particleTexture.getMinV()) / framesPerRow;

				f = (float) (this.particleTexture.getMinU()+uSize*col);
				f1 = (float) (f+uSize);
				f2 = (float) (this.particleTexture.getMinV()+vSize*row);
				f3 = (float) (f2+vSize);
			}

			float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
			float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
			float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

			int i = this.getBrightnessForRender(partialTicks);
			int j = i >> 16 & 65535;
			int k = i & 65535;
			Vec3d[] avec3d = new Vec3d[] {new Vec3d((double)(-rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double)(-rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(rotationYZ * f4 - rotationXZ * f4))};

			if (this.particleAngle != 0.0F && facing == null) {
				float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
				float f9 = MathHelper.cos(f8 * 0.5F);
				float f10 = MathHelper.sin(f8 * 0.5F) * (facing == null ? (float)cameraViewDir.xCoord : 0);
				float f11 = MathHelper.sin(f8 * 0.5F) * (facing == null ? (float)cameraViewDir.yCoord : 0);
				float f12 = MathHelper.sin(f8 * 0.5F) * (facing == null ? (float)cameraViewDir.zCoord : 0);
				Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

				for (int l = 0; l < 4; ++l)
					avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
			}

			// draw normally to clear buffer
			if (enumParticle.disableDepth) {
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer vertexbuffer = tessellator.getBuffer();
				tessellator.draw();
				vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			}

			buffer.pos((double)f5 + avec3d[0].xCoord, (double)f6 + avec3d[0].yCoord, (double)f7 + avec3d[0].zCoord).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double)f5 + avec3d[1].xCoord, (double)f6 + avec3d[1].yCoord, (double)f7 + avec3d[1].zCoord).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double)f5 + avec3d[2].xCoord, (double)f6 + avec3d[2].yCoord, (double)f7 + avec3d[2].zCoord).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double)f5 + avec3d[3].xCoord, (double)f6 + avec3d[3].yCoord, (double)f7 + avec3d[3].zCoord).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();

			// disable depth for health particles
			if (enumParticle.disableDepth) {
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer vertexbuffer = tessellator.getBuffer();
				GlStateManager.disableDepth();
				tessellator.draw();
				GlStateManager.enableDepth();
				vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			}
		}
	}

	/**Render facing particles*/
	public void renderOnBlocks(VertexBuffer buffer) {
		RenderManager.renderOnBlocks(world, buffer, particleRed, particleGreen, particleBlue, particleAlpha, 
				particleScale/10f, new Vec3d(this.posX, this.posY, this.posZ).subtract(new Vec3d(facing.getDirectionVec())), facing, true);
	}

	@Override
	public void setExpired() {
		super.setExpired();

		if (this.facing != null)
			enumParticle.facingParticles.remove(this);

		if (this.enumParticle.onePerEntity && this.followEntity != null)
			this.enumParticle.particleEntities.remove(this.followEntity.getPersistentID());
	}

}