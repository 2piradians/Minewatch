package twopiradians.minewatch.client.render.entity;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerHook;
import twopiradians.minewatch.common.util.EntityHelper;

public class RenderWidowmakerHook extends RenderOBJModel<EntityWidowmakerHook> {

	public RenderWidowmakerHook(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
				new ResourceLocation(Minewatch.MODID, "entity/widowmaker_hook.obj")
		};
	}

	@Override
	protected boolean preRender(EntityWidowmakerHook entity, int model, VertexBuffer buffer, double x, double y, double z, float entityYaw, float partialTicks) {	
		GlStateManager.translate(0, -entity.height/2f, 0);

		return true;
	}

	@Override
	public void doRender(EntityWidowmakerHook entity, double x, double y, double z, float entityYaw, float partialTicks) {	
		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		/*// rope
		if (entity.getThrower() != null) {
			Minecraft mc = Minecraft.getMinecraft();
			GlStateManager.pushMatrix();
			mc.getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/entity/widowmaker_hook_rope.png"));
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX);

			double width = 0.04d;
			Vec3d throwerPos = EntityHelper.getEntityPartialPos(entity.getThrower());
			Vector2f rotations = EntityHelper.getEntityPartialRotations(entity.getThrower());
			Vec3d shooting = EntityHelper.getShootingPos(entity.getThrower(), rotations.x, rotations.y, EnumHand.OFF_HAND, 23, 0.7f).subtract(throwerPos);

			Vec3d hookLook = entity.getLook(partialTicks).scale(0.17d);
			Vec3d hookPos = new Vec3d(x, y+entity.height/2f, z).subtract(hookLook);
			double v = hookPos.distanceTo(shooting)*2d;

			double deg_to_rad = 0.0174532925d;
			double precision = 0.05d;
			double degrees = 360d;
			double steps = Math.round(degrees*precision);
			degrees += 21.2d;
			double angle = 0;
			
			for (int i=1; i<=steps; i+=2) {
				angle = degrees/steps*i;
				double circleX = Math.cos(angle*deg_to_rad);
				double circleY = Math.sin(angle*deg_to_rad);
				double circleZ = 0;//Math.cos(angle*deg_to_rad);
				Vec3d vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(hookPos);
				buffer.pos(vec.xCoord, vec.yCoord, vec.zCoord).tex(i/steps, 0).endVertex();
				
				vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(shooting);
				buffer.pos(vec.xCoord, vec.yCoord, vec.zCoord).tex(i/steps, v).endVertex();
				
				angle = degrees/steps*(i+1);
				circleX = Math.cos(angle*deg_to_rad);
				circleY = Math.sin(angle*deg_to_rad);
				circleZ = 0;//Math.cos(angle*deg_to_rad);
				vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(hookPos);
				buffer.pos(vec.xCoord, vec.yCoord, vec.zCoord).tex((i+1)/steps, 0).endVertex();
				
				vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(shooting);
				buffer.pos(vec.xCoord, vec.yCoord, vec.zCoord).tex((i+1)/steps, v).endVertex();
			}


			tessellator.draw();
			GlStateManager.popMatrix();
		}*/
	}
}
