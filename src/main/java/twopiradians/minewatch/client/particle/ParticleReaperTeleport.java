package twopiradians.minewatch.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

@SideOnly(Side.CLIENT)
public class ParticleReaperTeleport extends ParticleSimpleAnimated {

	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
			new ResourceLocation(Minewatch.MODID, "entity/particle/reaper_teleport_base_0"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/reaper_teleport_base_1"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/reaper_teleport_inside"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/reaper_teleport_outside_0"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/reaper_teleport_outside_1"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/reaper_teleport_outside_2")
	};

	private EntityPlayer player;
	private boolean spawnAtPlayer;
	private Vec3d origin;
	private Vec3d offset;
	private float randomNumber;
	private int type;
	private Handler handler;

	/**type: 0 = base, 1 = inside, 2 = outside, 3 = small outside*/
	public ParticleReaperTeleport(World world, EntityPlayer player, boolean spawnAtPlayer, int type, Handler handler) {
		super(world, spawnAtPlayer ? player.posX : handler.position.xCoord, 
				spawnAtPlayer ? player.posY : handler.position.yCoord, 
						spawnAtPlayer ? player.posZ : handler.position.zCoord, 0, 0, 0);
		this.handler = handler;
		this.origin = spawnAtPlayer ? player.getPositionVector() : handler.position;
		double t = 2d*Math.PI*world.rand.nextDouble();
		double u = world.rand.nextDouble()+world.rand.nextDouble();
		double r = u > 1 ? 2-u : u;
		this.offset = type == 0 ? new Vec3d(0, 0.01d, 0) : type == 1 ? new Vec3d(0, 0.5d, 0) : new Vec3d(r*Math.cos(t), 0.3d, r*Math.sin(t));
		this.motionX = 0;
		this.motionY = type == 0 ? 0 : type == 3 ? 0.1f : 0.4f;
		this.motionZ = 0;
		this.particleGravity = 0;
		this.particleMaxAge = type == 0 ? Integer.MAX_VALUE : type == 3 ? 50 : 17;
		this.particleScale = type == 0 ? 14f : type == 1 ? 3f + world.rand.nextFloat()*2f : type == 2 ? 3f + world.rand.nextFloat()*3f : 0.5f;
		if (type != 0)
			if (player == Minecraft.getMinecraft().player) 
				this.setColor(type == 1 ? 0x743BEF : type == 3 ? 0xCB97FF : 0x130029+world.rand.nextInt(10));
			else
				this.setColor(type == 1 ? 0xCF4F17 : type == 3 ? 0xC12828 : 0x511D12+world.rand.nextInt(10));
		this.player = player;
		this.spawnAtPlayer = spawnAtPlayer;
		this.type = type;
		if (type == 0)
			this.particleAlpha = 0.9f;
		this.posX = offset.xCoord + origin.xCoord;
		this.posY = offset.yCoord + origin.yCoord;
		this.posZ = offset.zCoord + origin.zCoord;
		this.prevPosX = posX;
		this.prevPosY = posY;
		this.prevPosZ = posZ;
		this.randomNumber = rand.nextFloat();
		String texture = type == 0 ? (player == Minecraft.getMinecraft().player ? TEXTURES[0].toString() : TEXTURES[1].toString()) : type == 1 || type == 3 ? TEXTURES[2].toString() : TEXTURES[world.rand.nextInt(3)+3].toString();
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture);
		this.setParticleTexture(sprite); 
	}

	@Override
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (type != 0 && type != 3)
			rotationZ += this.randomNumber;

		float f = (float)this.particleTextureIndexX / 16.0F;
		float f1 = f + 0.0624375F;
		float f2 = (float)this.particleTextureIndexY / 16.0F;
		float f3 = f2 + 0.0624375F;
		float f4 = 0.1F * this.particleScale;

		if (this.particleTexture != null) {
			f = this.particleTexture.getMinU();
			f1 = this.particleTexture.getMaxU();
			f2 = this.particleTexture.getMinV();
			f3 = this.particleTexture.getMaxV();
		}

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
			float f10 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.xCoord;
			float f11 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.yCoord;
			float f12 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.zCoord;
			Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

			for (int l = 0; l < 4; ++l)
				avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
		}

		buffer.pos((double)f5 + avec3d[0].xCoord, (double)f6 + (type == 0 ? 0 : avec3d[0].yCoord), (double)f7 + avec3d[0].zCoord).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[1].xCoord, (double)f6 + (type == 0 ? 0 : avec3d[1].yCoord), (double)f7 + avec3d[1].zCoord).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[2].xCoord, (double)f6 + (type == 0 ? 0 : avec3d[2].yCoord), (double)f7 + avec3d[2].zCoord).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[3].xCoord, (double)f6 + (type == 0 ? 0 : avec3d[3].yCoord), (double)f7 + avec3d[3].zCoord).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		this.prevParticleAngle = this.particleAngle;
		this.particleAngle += type == 0 ? 0.2f : 0.01f;

		if (type != 0) {
			this.particleAlpha = (float)(this.particleMaxAge - this.particleAge) / this.particleMaxAge;
			if (type != 3)
				this.particleScale -= 0.07f;
		}

		if (player == null || player.isDead || TickHandler.getHandler(player, Identifier.REAPER_TELEPORT) != handler)
			this.setExpired();
		else {
			if (type == 1)
				this.particleScale -= 0.2f;
			else if (type == 3)
				this.motionY = 0.05f;

			if (type == 2 || type == 3) {
				double rotateSpeed = 10;

				this.posX = 2 * offset.xCoord * Math.cos(rotateSpeed * this.particleAge * Math.PI/180) + origin.xCoord;
				this.posY = offset.yCoord + origin.yCoord;
				this.posZ = 2 * offset.zCoord * Math.sin(rotateSpeed * this.particleAge * Math.PI/180) + origin.zCoord;
			}

			this.origin = spawnAtPlayer ? player.getPositionVector() : handler.position;

			this.offset = this.offset.addVector(motionX, motionY, motionZ);

			if (type == 2 || type == 3) {
				double rotateSpeed = 10;

				this.posX = 2 * offset.xCoord * Math.cos(rotateSpeed * this.particleAge * Math.PI/180) + origin.xCoord;
				this.posY = offset.yCoord + origin.yCoord;
				this.posZ = 2 * offset.zCoord * Math.sin(rotateSpeed * this.particleAge * Math.PI/180) + origin.zCoord;
			}
			else {
				this.posX = offset.xCoord + origin.xCoord;
				this.posY = offset.yCoord + origin.yCoord;
				this.posZ = offset.zCoord + origin.zCoord;
			}
		}
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}

}
