package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemHanzoBow;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.item.weapon.ItemMcCreeGun;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;
import twopiradians.minewatch.common.item.weapon.ItemReinhardtHammer;
import twopiradians.minewatch.common.item.weapon.ItemSoldier76Gun;
import twopiradians.minewatch.common.item.weapon.ItemTracerPistol;

public enum EnumHero {

	// do not change order - this is the order in ability_overlay.png
	ANA("Ana", true, KeyBind.ABILITY_2, KeyBind.ABILITY_1, KeyBind.NONE, 10, 10, new int[] {2,3,3,2}, new ItemAnaRifle()), 
	GENJI("Genji", false, KeyBind.ABILITY_2, KeyBind.ABILITY_1, KeyBind.NONE, 24, 0, new int[] {2,3,3,2}, new ItemGenjiShuriken()),
	HANZO("Hanzo", false, KeyBind.ABILITY_2, KeyBind.ABILITY_1, KeyBind.NONE, 0, 0, new int[] {2,3,3,2}, new ItemHanzoBow()),
	MCCREE("McCree", false, KeyBind.ABILITY_2, KeyBind.ABILITY_1, KeyBind.NONE, 6, 0, new int[] {2,3,3,2}, new ItemMcCreeGun()),
	REAPER("Reaper", false, KeyBind.ABILITY_2, KeyBind.ABILITY_1, KeyBind.NONE, 8, 0, new int[] {2,3,3,2}, new ItemReaperShotgun()),
	REINHARDT("Reinhardt", false, KeyBind.RMB, KeyBind.ABILITY_2, KeyBind.ABILITY_1, 0, 0, new int[] {4,6,6,4}, new ItemReinhardtHammer()),
	SOLDIER76("Soldier76", false, KeyBind.RMB, KeyBind.ABILITY_2, KeyBind.ABILITY_1, 25, 0, new int[] {2,3,3,2}, new ItemSoldier76Gun()),
	TRACER("Tracer", false, KeyBind.ABILITY_2, KeyBind.ABILITY_1, KeyBind.NONE, 40, 0, new int[] {2,2,2,2}, new ItemTracerPistol());

	public HashMap<UUID, Boolean> playersUsingAlt = Maps.newHashMap();

	private KeyBind slot1;
	private KeyBind slot2;
	private KeyBind slot3;

	public String name;
	/**index from top of ability_overlay.png for this hero*/
	public int overlayIndex;
	/**index for alternate weapon*/
	public int altWeaponIndex;
	/**if mouse wheel can scroll between weapons*/
	public boolean hasAltWeapon;
	/**max ammo for main weapon*/
	public int mainAmmo;
	/**max ammo for alt weapon*/
	public int altAmmo;

	public int[] armorReductionAmounts;
	public ArmorMaterial material;
	public ItemMWArmor helmet;
	public ItemMWArmor chestplate;
	public ItemMWArmor leggings;
	public ItemMWArmor boots;
	public ItemMWWeapon weapon;
	public ItemMWToken token;

	public SoundEvent reloadSound;

	private static final class IndexCounter {
		/**used to calculate overlayIndex*/
		public static int index;
	}

	static {
		for (EnumHero hero : EnumHero.values())
			hero.weapon.hero = hero;

	}

	private EnumHero(String name, boolean hasAltWeapon, KeyBind slot1, KeyBind slot2, KeyBind slot3, 
			int mainAmmo, int altAmmo, int[] armorReductionAmounts, ItemMWWeapon weapon) {
		this.overlayIndex = IndexCounter.index++;
		this.name = name;
		this.hasAltWeapon = hasAltWeapon;
		if (this.hasAltWeapon)
			this.altWeaponIndex = IndexCounter.index++;
		this.slot1 = slot1;
		this.slot2 = slot2;
		this.slot3 = slot3;
		this.mainAmmo = mainAmmo;
		this.altAmmo = altAmmo;
		this.armorReductionAmounts = armorReductionAmounts;
		this.weapon = weapon;
	}

	public Item getEquipment(EntityEquipmentSlot slot) {
		switch (slot) {
		case HEAD:
			return helmet;
		case CHEST:
			return chestplate;
		case LEGS:
			return leggings;
		case FEET:
			return boots;
		case MAINHAND:
			return weapon;
		case OFFHAND:
			return weapon.hasOffhand ? weapon : null;
		}
		return null;
	}

	@Mod.EventBusSubscriber(Side.CLIENT)
	public static class RenderManager {

		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public static void renderOverlay(RenderGameOverlayEvent.Post event) {
			if (event.getType() == ElementType.HELMET) {			
				EntityPlayer player = Minecraft.getMinecraft().player;
				EnumHero hero = ItemMWArmor.SetManager.playersWearingSets.containsKey(player.getPersistentID()) ? ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) : null;

				// hero information screen
				if (hero != null &&
						player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemMWWeapon &&
						((ItemMWWeapon)player.getHeldItemMainhand().getItem()).hero == hero && Minewatch.keys.heroInformation(player)) {
					GlStateManager.pushMatrix();
					GlStateManager.disableDepth();

					GlStateManager.scale(event.getResolution().getScaledWidth_double()/256d, event.getResolution().getScaledHeight_double()/256d, 1);
					Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+hero.name+"_info.png"));
					GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 1920, 1080, 0);

					GlStateManager.popMatrix();
				}		
				else {

					if (hero != null) {
						// display icon
						GlStateManager.pushMatrix();
						GlStateManager.enableDepth();
						GlStateManager.enableAlpha();

						double scale = 0.35d;
						GlStateManager.scale(scale, scale, 1);
						GlStateManager.translate(60, (int) ((event.getResolution().getScaledHeight() - 256*scale) / scale) - 50, 0);
						Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/icon_background.png"));
						GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 240, 230, 0);
						Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+hero.name+"_icon.png"));
						GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 240, 230, 0);

						GlStateManager.popMatrix();
					}

					// display abilities/weapon
					if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemMWWeapon) {
						ItemMWWeapon weapon = (ItemMWWeapon) player.getHeldItemMainhand().getItem();

						GlStateManager.pushMatrix();
						GlStateManager.enableDepth();
						GlStateManager.enableAlpha();

						GlStateManager.scale(1, 4d, 1);
						GlStateManager.translate((int) (event.getResolution().getScaledWidth())-125, ((int)event.getResolution().getScaledHeight()/4)-22, 0);
						Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/ability_overlay.png"));
						int index = weapon.hero.playersUsingAlt.containsKey(player.getPersistentID()) && weapon.hero.playersUsingAlt.get(player.getPersistentID()) && 
								weapon.hero.hasAltWeapon ? weapon.hero.altWeaponIndex : weapon.hero.overlayIndex;
						int vertical = 11;
						// weapon
						GuiUtils.drawTexturedModalRect(0, 0, 1, (index+1)+index*vertical, 122, vertical, 0);

						if (hero != null && weapon.hero == hero && ItemMWArmor.SetManager.playersWearingSets.containsKey(player.getPersistentID())) {
							// slot 1
							if (hero.slot1.getCooldown(player) > 0) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							else if (hero.slot1.isKeyDown(player)) {
								GlStateManager.color(0.8f, 0.6f, 0);
								GlStateManager.translate(1, 1, 0);
							}
							GuiUtils.drawTexturedModalRect(-50, -2, 124, (index+1)+index*vertical, 40, vertical, 0);
							GlStateManager.color(1, 1, 1);
							if (hero.slot1.getCooldown(player) <= 0 && hero.slot1.isKeyDown(player)) 
								GlStateManager.translate(-1, -1, 0);
							// slot 2
							if (hero.slot2.getCooldown(player) > 0) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							else if (hero.slot2.isKeyDown(player)) {
								GlStateManager.color(0.8f, 0.6f, 0);
								GlStateManager.translate(1, 1, 0);
							}
							GuiUtils.drawTexturedModalRect(-87, -2, 165, (index+1)+index*vertical, 40, vertical, 0);
							GlStateManager.color(1, 1, 1);
							if (hero.slot2.getCooldown(player) <= 0 && hero.slot2.isKeyDown(player)) 
								GlStateManager.translate(-1, -1, 0);
							// slot 3
							if (hero.slot3.getCooldown(player) > 0) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							else if (hero.slot3.isKeyDown(player)) {
								GlStateManager.color(0.8f, 0.6f, 0);
								GlStateManager.translate(1, 1, 0);
							}
							GuiUtils.drawTexturedModalRect(-124, -2, 206, (index+1)+index*vertical, 40, vertical, 0);
							GlStateManager.color(1, 1, 1);
							if (hero.slot3.getCooldown(player) <= 0 && hero.slot3.isKeyDown(player)) 
								GlStateManager.translate(-1, -1, 0);

							// keybinds 
							int width1 = Minecraft.getMinecraft().fontRenderer.getStringWidth(hero.slot1.getKeyName());
							int width2 = Minecraft.getMinecraft().fontRenderer.getStringWidth(hero.slot2.getKeyName());
							int width3 = Minecraft.getMinecraft().fontRenderer.getStringWidth(hero.slot3.getKeyName());
							// background
							// slot 1
							if (hero.slot1.getCooldown(player) > 0) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							if (hero.slot1.getKeyName() != "")
								GuiUtils.drawTexturedModalRect(-58, 3, 0, 1015, 40, 9, 0);
							else if (hero.slot1 == KeyBind.RMB)
								GuiUtils.drawTexturedModalRect(-43, 3, 46, 1015, 10, 9, 0);
							GlStateManager.color(1, 1, 1);
							// slot 2
							if (hero.slot2.getCooldown(player) > 0) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							if (hero.slot2.getKeyName() != "")
								GuiUtils.drawTexturedModalRect(-98, 2, 0, 1015, 40, 9, 0);
							GlStateManager.color(1, 1, 1);
							// slot 3
							if (hero.slot3.getCooldown(player) > 0) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							if (hero.slot3.getKeyName() != "")
								GuiUtils.drawTexturedModalRect(-137, 1, 0, 1015, 40, 9, 0);
							GlStateManager.color(1, 1, 1);
							// text
							GlStateManager.scale(1, 0.25d, 1);
							GlStateManager.rotate(4.5f, 0, 0, 1);
							// slot 1
							Minecraft.getMinecraft().fontRenderer.drawString(hero.slot1.getKeyName(), -33-width1/2, 38, 0);
							// slot 2
							Minecraft.getMinecraft().fontRenderer.drawString(hero.slot2.getKeyName(), -74-width2/2, 37, 0);
							// slot 3
							Minecraft.getMinecraft().fontRenderer.drawString(hero.slot3.getKeyName(), -114-width3/2, 37, 0);

							// cooldowns
							double scale = 2d;
							GlStateManager.scale(scale, scale, 1);
							if (hero.slot1.getCooldown(player) > 0) { 
								String num = String.valueOf(hero.slot1.getCooldown(player)/20);
								int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(num);
								Minecraft.getMinecraft().fontRenderer.drawString(num, -14-width/2, 4, 0xFFFFFF);
							}
							if (hero.slot2.getCooldown(player) > 0) { 
								String num = String.valueOf(hero.slot2.getCooldown(player)/20);
								int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(num);
								Minecraft.getMinecraft().fontRenderer.drawString(num, -33-width/2, 4, 0xFFFFFF);
							}
							if (hero.slot3.getCooldown(player) > 0) { 
								String num = String.valueOf(hero.slot3.getCooldown(player)/20);
								int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(num);
								Minecraft.getMinecraft().fontRenderer.drawString(num, -51-width/2, 4, 0xFFFFFF);
							}
						}
						// ammo
						if (weapon.getMaxAmmo(player) > 0) {
							if (weapon.hero != hero || hero == null) { // adjust things that were skipped
								GlStateManager.scale(1, 0.25d, 1);
								GlStateManager.rotate(4.5f, 0, 0, 1);
								double scale = 2d;
								GlStateManager.scale(scale, scale, 1);
							}

							double scale = 0.9d;
							GlStateManager.scale(scale, scale, 1);
							int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(
									String.valueOf(weapon.getCurrentAmmo(player)));
							Minecraft.getMinecraft().fontRenderer.drawString(
									String.valueOf(weapon.getCurrentAmmo(player)), 30-width, -11, 0xFFFFFF);
							scale = 0.6d;
							GlStateManager.scale(scale, scale, 1);
							Minecraft.getMinecraft().fontRenderer.drawString("/", 53, -13, 0x00D5FF);
							Minecraft.getMinecraft().fontRenderer.drawString(
									String.valueOf(weapon.getMaxAmmo(player)), 59, -13, 0xFFFFFF);
						}

						GlStateManager.popMatrix();
					}
				}
			}
		}
	}

}
