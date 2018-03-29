package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ChargeManager {

	/**Do not interact with directly - use the getter / setter*/
	private static HashMap<UUID, Float> currentChargeServer = Maps.newHashMap();
	/**Do not interact with directly - use the getter / setter*/
	private static float currentChargeClient = -1;

	private static final Handler CHARGE_COOLDOWN = new Handler(Identifier.CHARGE_COOLDOWN, false) {};
	private static final Handler CHARGE_RECOVERY = new Handler(Identifier.CHARGE_RECOVERY, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			EnumHero hero = SetManager.getWornSet(entityLiving);
			if (entityLiving != null && currentChargeClient < getMaxCharge(hero)) {
				if (entityLiving.ticksExisted > number+3) 
					currentChargeClient = Math.min(getMaxCharge(hero), currentChargeClient+getRechargeRate(hero));
				ticksLeft = 2;
			}
			else
				return true;

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			EnumHero hero = SetManager.getWornSet(entityLiving);
			if (entityLiving != null && currentChargeServer.containsKey(entityLiving.getPersistentID()) && 
					currentChargeServer.get(entityLiving.getPersistentID()) < getMaxCharge(hero)) {
				if (entityLiving.ticksExisted > number+3)
					currentChargeServer.put(entityLiving.getPersistentID(), Math.min(getMaxCharge(hero), currentChargeServer.get(entityLiving.getPersistentID())+getRechargeRate(hero)));
				ticksLeft = 2;
			}
			else
				return true;

			return super.onServerTick();
		}
	};
	
	/**Wearing set, set has charge, charge greater than a min value*/
	public static boolean canUseCharge(Entity entity) {
		float current = getCurrentCharge(entity);
		float max = getMaxCharge(SetManager.getWornSet(entity));
		Handler handler = TickHandler.getHandler(entity, Identifier.CHARGE_COOLDOWN);
		return max > 0 && (handler == null || current > max*0.2f);
	}

	/**Charge regained per tick*/
	public static float getRechargeRate(Entity entity) {
		return getRechargeRate(SetManager.getWornSet(entity));
	}

	/**Charge regained per tick*/
	public static float getRechargeRate(@Nullable EnumHero hero) {
		if (hero != null)
			switch(hero) {
			case BASTION:
				return 80f/140f;
			case MOIRA:
				return 1/5f;
			case PHARAH:
				return 1;
			}
		return 0;
	}

	public static float getMaxCharge(Entity entity) {
		return getMaxCharge(SetManager.getWornSet(entity));
	}

	public static float getMaxCharge(@Nullable EnumHero hero) {
		if (hero != null)
			switch(hero) {
			case BASTION:
				return 80;
			case MOIRA:
				return 180;
			case PHARAH:
				return 40;
			}
		return 0;
	}

	public static float getCurrentCharge(Entity player) {
		if (player != null)
			if (!player.world.isRemote && currentChargeServer.containsKey(player.getPersistentID())) 
				return currentChargeServer.get(player.getPersistentID());
			else if (player.world.isRemote && currentChargeClient >= 0 && player == Minewatch.proxy.getClientPlayer())
				return currentChargeClient;
		return getMaxCharge(player);
	}

	public static void setCurrentCharge(Entity player, float amount, boolean sendPacket) {
		if (player != null) {
			EnumHero hero = SetManager.getWornSet(player);
			amount = MathHelper.clamp(amount, 0, getMaxCharge(hero));
			if (player instanceof EntityPlayerMP && sendPacket) 
				Minewatch.network.sendTo(new SPacketSimple(46, player, false, amount, 0, 0), (EntityPlayerMP) player);
			Handler handler = TickHandler.getHandler(player, Identifier.CHARGE_RECOVERY);
			if (handler != null)
				handler.setNumber(player.ticksExisted);
			else if (amount < getMaxCharge(hero))
				TickHandler.register(player.world.isRemote, CHARGE_RECOVERY.setEntity(player).setTicks(2));
			if (player.world.isRemote && player == Minewatch.proxy.getClientPlayer())
				currentChargeClient = amount;
			else if (!player.world.isRemote)
				currentChargeServer.put(player.getPersistentID(), amount);
		}
	}

	public static void subtractFromCurrentCharge(Entity player, float amount, boolean sendPacket) {
		float charge = getCurrentCharge(player);
  		if (charge - amount > 2)
			setCurrentCharge(player, charge-amount, sendPacket);
		else {
			setCurrentCharge(player, 0, sendPacket);
			TickHandler.register(player.world.isRemote, CHARGE_COOLDOWN.setEntity(player).setTicks(20));
		}
	}

	@SideOnly(Side.CLIENT)
	public static void renderChargeOverlay(EntityPlayer player, double width, double height) {
		EnumHero hero = SetManager.getWornSet(player);
		// render charge
		if (getMaxCharge(hero) > 0 && 
				getCurrentCharge(player) < getMaxCharge(hero) && TickHandler.hasHandler(player, Identifier.CHARGE_RECOVERY)) {
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GlStateManager.pushMatrix();
			GlStateManager.translate(width/2d, height/2d, 0);
			GlStateManager.rotate(180, 1, 0, 0);
			GlStateManager.rotate(54, 0, 0, 1);
			float size = 60;
			GlStateManager.disableLighting();
			GlStateManager.disableCull();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.disableTexture2D();
			GlStateManager.glLineWidth(10f);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();

			// background
			vertexbuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
			double deg_to_rad = 0.0174532925d;
			double precision = 0.5d;
			double angle_from = 270;
			double angle_to = angle_from + 0.2f * 360d;
			double angle_diff = angle_to-angle_from;
			double steps = Math.round(angle_diff*precision);
			double angle = angle_from;
			for (int i = 1; i<=steps; i++) {
				angle = angle_from+angle_diff/steps*i;
				vertexbuffer.pos(size*Math.cos(angle*deg_to_rad), size*Math.sin(angle*deg_to_rad), 0).color(0.7f, 0.7f, 0.7f, 0.5F).endVertex();
			}
			tessellator.draw();

			// foreground
			vertexbuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
			angle_to = angle_from + (getCurrentCharge(player) / getMaxCharge(hero)) * 0.2f * 360d;
			angle_diff = angle_to-angle_from;
			steps = Math.round(angle_diff*precision);
			angle = angle_from;
			for (int i = 1; i <= steps; i++) {
				angle = angle_from+angle_diff/steps*i;
				vertexbuffer.pos(size*Math.cos(angle*deg_to_rad), size*Math.sin(angle*deg_to_rad), 0).color(1, 1, 1, 0.4F).endVertex();
			}
			tessellator.draw();

			GlStateManager.glLineWidth(1);
			GlStateManager.enableTexture2D();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.popMatrix();
		}
	}

}