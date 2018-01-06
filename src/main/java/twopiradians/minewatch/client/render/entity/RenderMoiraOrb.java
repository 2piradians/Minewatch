package twopiradians.minewatch.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderFireball;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
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

		//sprite = mc.getTextureMapBlocks().getAtlasSprite("minewatch:entity/moira_orb");
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		double scale = 2;
		GlStateManager.translate(x, y+entity.height/2f, z);
		GlStateManager.scale(scale, scale, scale);

		GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();

		// main
		for (int i=0; i<2; ++i) {
			if (!entity.isFriendly) {
				if (i == 0) 
					GlStateManager.color(255/255f, 171/255f, 255/255f, 1); // pink
				else 
					GlStateManager.color(39/255f, 27/255f, 80/255f, 1); // dark purple
			}
			else {
				if (i == 0) 
					GlStateManager.color(90/255f, 76/255f, 22/255f, 1); // brown
				else 
					GlStateManager.color(255/255f, 240/255f, 46/255f, 1); // yellow
			}

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

			buffer.pos(-entity.width/2f, -entity.width/2f, -entity.width/2f).tex(sprite.getMinU(), sprite.getMinV()).normal(0.0F, 1.0F, 0.0F).endVertex();
			buffer.pos(entity.width/2f, -entity.width/2f, -entity.width/2f).tex(sprite.getMaxU(), sprite.getMinV()).normal(0.0F, 1.0F, 0.0F).endVertex();
			buffer.pos(entity.width/2f, entity.width/2f, -entity.width/2f).tex(sprite.getMaxU(), sprite.getMaxV()).normal(0.0F, 1.0F, 0.0F).endVertex();
			buffer.pos(-entity.width/2f, entity.width/2f, -entity.width/2f).tex(sprite.getMinU(), sprite.getMaxV()).normal(0.0F, 1.0F, 0.0F).endVertex();

			tessellator.draw();
		}
		
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		
		// tethers
		for (EntityLivingBase tether : entity.tethered) {
			buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);

			Vec3d tetherVec = EntityHelper.getEntityPartialPos(tether).subtract(EntityHelper.getEntityPartialPos(Minewatch.proxy.getRenderViewEntity()));
			Vec2f rotations = EntityHelper.getDirectLookAngles(tether, entity);
			rotations = new Vec2f(rotations.y, rotations.x);
			double distance = entity.getDistanceToEntity(tether)-entity.width;
			double size = 1.5d;
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
					buffer.pos(target.xCoord, target.yCoord+entity.height*1.5f, target.zCoord).tex(((i-1)/(steps-1))*diff*0.6f+sprite.getMinU()+diff*0.2f, sprite.getMaxV()-diff*0.2f).endVertex();
				}

				for (int i=(int) steps; i>0; i--) {
					angle = degrees/steps*i;
					Vec3d target = EntityHelper.getLook((float) (rotations.x+size*Math.cos(angle*deg_to_rad)), (float) (rotations.y+size*Math.sin(angle*deg_to_rad))).scale(distance);
					buffer.pos(target.xCoord, target.yCoord+entity.height*1.5f, target.zCoord).tex(((i-1)/(steps-1))*diff*0.6f+sprite.getMinU()+diff*0.2f, sprite.getMaxV()-diff*0.2f).endVertex();
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