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
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;

@SideOnly(Side.CLIENT)
public class ParticleReaperTeleport extends ParticleSimpleAnimated {

	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
			new ResourceLocation(Minewatch.MODID, "entity/particle/reaper_teleport_0"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/reaper_teleport_1"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/reaper_teleport_2"),
			new ResourceLocation(Minewatch.MODID, "entity/particle/reaper_teleport_3")
	};

	private EntityPlayer player;
	private boolean spawnAtPlayer;
	private Vec3d origin;
	private Vec3d offset;
	private float randomNumber;

	private boolean base;

	public ParticleReaperTeleport(World world, EntityPlayer player, boolean spawnAtPlayer, boolean base) {
		super(world, spawnAtPlayer ? player.posX : ItemReaperShotgun.clientTps.get(player).getSecond().xCoord, 
				spawnAtPlayer ? player.posY : ItemReaperShotgun.clientTps.get(player).getSecond().yCoord, 
						spawnAtPlayer ? player.posZ : ItemReaperShotgun.clientTps.get(player).getSecond().zCoord, 0, 0, 0);
		this.origin = spawnAtPlayer ? player.getPositionVector() : ItemReaperShotgun.clientTps.get(player).getSecond();
		double t = 2d*Math.PI*world.rand.nextDouble();
		double u = world.rand.nextDouble()+world.rand.nextDouble();
		double r = u > 1 ? 2-u : u;
		this.offset = base ? new Vec3d(0, 0.01d, 0) : new Vec3d(r*Math.cos(t), 0, r*Math.sin(t));
		this.motionX = 0;
		this.motionY = base ? 0 : 0.4f;
		this.motionZ = 0;
		this.particleGravity = 0;
		this.particleMaxAge = base ? Integer.MAX_VALUE : 20;
		this.particleScale = base ? 14f : 3f + world.rand.nextFloat()*3f;
		if (!base)
			this.setColor(0x3B1515+world.rand.nextInt(10));
		this.player = player;
		this.spawnAtPlayer = spawnAtPlayer;
		this.base = base;
		if (base)
			this.particleAlpha = 0.9f;
		this.posX = offset.xCoord + origin.xCoord;
		this.posY = offset.yCoord + origin.yCoord;
		this.posZ = offset.zCoord + origin.zCoord;
		this.prevPosX = posX;
		this.prevPosY = posY;
		this.prevPosZ = posZ;
		this.randomNumber = rand.nextFloat();
		String texture = base ? TEXTURES[3].toString() : TEXTURES[world.rand.nextInt(3)].toString();
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture);
		this.setParticleTexture(sprite); 
	}

	@Override
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		//super.renderParticle(buffer, entityIn, partialTicks, rotationX, base ? 0 : rotationZ, base ? rotationYZ : rotationYZ + randomNumber, rotationXY, base ? 0 : rotationXZ);

		/*if (base) { // FIXME keep flat and spin pls (this keeps flat if particleAngle == 0)
			rotationZ = 0;
		}*/

		float f = (float)this.particleTextureIndexX / 16.0F;
		float f1 = f + 0.0624375F;
		float f2 = (float)this.particleTextureIndexY / 16.0F;
		float f3 = f2 + 0.0624375F;
		float f4 = 0.1F * this.particleScale;

		if (this.particleTexture != null)
		{
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

		if (this.particleAngle != 0.0F)
		{
			float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
			float f9 = MathHelper.cos(f8 * 0.5F);
			float f10 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.xCoord;
			float f11 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.yCoord;
			float f12 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.zCoord;
			Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

			for (int l = 0; l < 4; ++l)
			{
				avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
			}
		}

		buffer.pos((double)f5 + avec3d[0].xCoord, (double)f6 + avec3d[0].yCoord, (double)f7 + avec3d[0].zCoord).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[1].xCoord, (double)f6 + avec3d[1].yCoord, (double)f7 + avec3d[1].zCoord).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[2].xCoord, (double)f6 + avec3d[2].yCoord, (double)f7 + avec3d[2].zCoord).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[3].xCoord, (double)f6 + avec3d[3].yCoord, (double)f7 + avec3d[3].zCoord).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		//if (!base)
		this.prevParticleAngle = this.particleAngle;
		this.particleAngle += base ? 0.2f : 0.01f;

		if (!base) {
			this.particleAlpha = (float)(this.particleMaxAge - this.particleAge) / this.particleMaxAge;
			this.particleScale -= 0.07f;
		}

		if (player == null || player.isDead || !ItemReaperShotgun.clientTps.containsKey(player))
			this.setExpired();
		else {
			this.origin = spawnAtPlayer ? player.getPositionVector() : ItemReaperShotgun.clientTps.get(player).getSecond();

			this.offset = this.offset.addVector(motionX, motionY, motionZ);

			this.posX = offset.xCoord + origin.xCoord;
			this.posY = offset.yCoord + origin.yCoord;
			this.posZ = offset.zCoord + origin.zCoord;
		}
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}

}
