package twopiradians.minewatch.client.render.entity;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityMoiraOrb;
import twopiradians.minewatch.common.util.EntityHelper;

public class RenderMoiraOrb extends RenderSimple<EntityMoiraOrb> {

	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite sprite;

	public RenderMoiraOrb(RenderManager manager) {
		super(manager, null, "", 0, 0, 0);
	}

	@Override
	public void doRender(EntityMoiraOrb entity, double x, double y, double z, float entityYaw, float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.4F);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		if (!entity.isFriendly) 
			GlStateManager.color(39/255f, 27/255f, 80/255f, 1); // dark purple
		else 
			GlStateManager.color(255/255f, 240/255f, 46/255f, 1); // yellow

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();

		// tethers
		for (EntityLivingBase tether : entity.tethered) {
			buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);

			Vec3d tetherVec = EntityHelper.getEntityPartialPos(tether).subtract(EntityHelper.getEntityPartialPos(Minewatch.proxy.getRenderViewEntity()));
			Vector2f rotations = EntityHelper.getDirectLookAngles(tether, entity);
			rotations = new Vector2f(rotations.y, rotations.x);
			double distance = entity.getDistanceToEntity(tether)*0.1f-entity.width/2f;
			double size = (entity.chargeClient / 80f) * 35d;
			double deg_to_rad = 0.0174532925d;
			double precision = 0.05d;
			double degrees = 360d;
			double steps = Math.round(degrees*precision);
			degrees += 21.2d;
			double angle = 0;
			double diff = sprite.getMaxU() - sprite.getMinU();

			buffer.pos(tetherVec.xCoord, tetherVec.yCoord+tether.height*0.7f, tetherVec.zCoord).tex(diff/2f+sprite.getMinU()+diff*0.2f, sprite.getMinV()+diff*0.2f).endVertex();

			for (int j=0; j<2; j++) {
				size += j;

				for (int i=1; i<=steps; i++) {
					angle = degrees/steps*i;
					Vec3d target = EntityHelper.getLook((float) (rotations.x+size*Math.cos(angle*deg_to_rad)), (float) (rotations.y+size*Math.sin(angle*deg_to_rad))).scale(distance);
					buffer.pos(x+target.xCoord, y+target.yCoord+entity.height*0.5f, z+target.zCoord).tex(((i-1)/(steps-1))*diff*0.6f+sprite.getMinU()+diff*0.2f, sprite.getMaxV()-diff*0.2f).endVertex();
				}

				for (int i=(int) steps; i>0; i--) {
					angle = degrees/steps*i;
					Vec3d target = EntityHelper.getLook((float) (rotations.x+size*Math.cos(angle*deg_to_rad)), (float) (rotations.y+size*Math.sin(angle*deg_to_rad))).scale(distance);
					buffer.pos(x+target.xCoord, y+target.yCoord+entity.height*0.5f, z+target.zCoord).tex(((i-1)/(steps-1))*diff*0.6f+sprite.getMinU()+diff*0.2f, sprite.getMaxV()-diff*0.2f).endVertex();
				}
			}

			tessellator.draw();
		}

		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
}