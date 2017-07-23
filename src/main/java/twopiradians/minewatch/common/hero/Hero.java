package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;

public class Hero {

	// do not change order - this is the order in ability_overlay.png
	public static final Hero ANA = new HeroAna();
	public static final Hero GENJI = new HeroGenji();
	public static final Hero HANZO = new HeroHanzo();
	public static final Hero MCCREE = new HeroMcCree();
	public static final Hero REAPER = new HeroReaper();
	public static final Hero REINHARDT = new HeroReinhardt();
	public static final Hero SOLDIER76 = new HeroSoldier76();
	public static final Hero TRACER = new HeroTracer();
	
	// The keys that will display underneath the icon
	protected static enum KeyBind {
		NONE, ABILITY_1, ABILITY_2
	}

	public HashMap<UUID, Boolean> playersUsingAlt = Maps.newHashMap();
	
	private KeyBind slot1;
	private KeyBind slot2;
	private KeyBind slot3;
	
	public String name;
	/**used to calculate overlayIndex*/
	private static int index;
	/**index from top of ability_overlay.png for this hero*/
	public int overlayIndex;
	/**index for alternate weapon*/
	public int altWeaponIndex;
	/**if mouse wheel can scroll between weapons*/
	public boolean hasAltWeapon;
	
	protected Hero(String name, boolean hasAltWeapon, KeyBind slot1, KeyBind slot2, KeyBind slot3) {
		this.overlayIndex = index++;
		this.name = name;
		this.hasAltWeapon = hasAltWeapon;
		if (this.hasAltWeapon)
			this.altWeaponIndex = index++;
		this.slot1 = slot1;
		this.slot2 = slot2;
		this.slot3 = slot3;
	}
	
	@SideOnly(Side.CLIENT)
	public void renderOverlay(EntityPlayer player, ScaledResolution resolution) {

		// display icon
		GlStateManager.pushMatrix();
		GlStateManager.enableDepth();
		
		double scale = 0.35d;
		GlStateManager.scale(scale, scale, 1);
		GlStateManager.translate(60, (int) ((resolution.getScaledHeight() - 256*scale) / scale) - 20, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/icon_background.png"));
		GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+this.name+"_icon.png"));
		GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);
		
		GlStateManager.disableDepth();
		GlStateManager.popMatrix();
		
		// display abilities/weapon
		if (player.getHeldItemMainhand() != null/* && player.getHeldItemMainhand().getItem() instanceof ModWeapon &&
				((ModWeapon)player.getHeldItemMainhand().getItem()).hero == this*/) {
			GlStateManager.pushMatrix();
			GlStateManager.enableDepth();
	        
			GlStateManager.scale(1, 4d, 1);
			GlStateManager.translate((int) (resolution.getScaledWidth())-125, ((int)resolution.getScaledHeight())-265, 0);
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/ability_overlay.png"));
			int index = player.inventory.currentItem;//playersUsingAlt.containsKey(player.getPersistentID()) && playersUsingAlt.get(player.getPersistentID()) && 
					//this.hasAltWeapon ? altWeaponIndex : overlayIndex;
			int vertical = 11;
			// weapon
			GuiUtils.drawTexturedModalRect(0, 0, 1, (index+1)+index*vertical, 122, vertical, 0);
			// slot 1
			GuiUtils.drawTexturedModalRect(-50, -2, 124, (index+1)+index*vertical, 40, vertical, 0);
			// slot 2
			GuiUtils.drawTexturedModalRect(-87, -2, 165, (index+1)+index*vertical, 40, vertical, 0);
			// slot 3
			GuiUtils.drawTexturedModalRect(-124, -2, 206, (index+1)+index*vertical, 40, vertical, 0);
			
			GlStateManager.disableDepth();
			GlStateManager.popMatrix();
		}
		

	}
	
	
}
