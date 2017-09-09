package twopiradians.minewatch.common.hero;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.item.weapon.ItemBastionGun;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemHanzoBow;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.item.weapon.ItemMcCreeGun;
import twopiradians.minewatch.common.item.weapon.ItemMeiBlaster;
import twopiradians.minewatch.common.item.weapon.ItemMercyWeapon;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;
import twopiradians.minewatch.common.item.weapon.ItemReinhardtHammer;
import twopiradians.minewatch.common.item.weapon.ItemSoldier76Gun;
import twopiradians.minewatch.common.item.weapon.ItemTracerPistol;
import twopiradians.minewatch.common.item.weapon.ItemWidowmakerRifle;

public enum EnumHero {

	// do not change order - this is the order in ability_overlay.png
	ANA("Ana", true, new Ability(KeyBind.ABILITY_2, false, false, 0, 0), 
			new Ability(KeyBind.ABILITY_1, false, false, 0, 0), 
			new Ability(KeyBind.NONE, false, false, 0, 0), 
			10, 10, new int[] {2,3,3,2}, new ItemAnaRifle(), Crosshair.CIRCLE_SMALL, true, 
			new Skin("Classic", "Overwatch - Ana", "Drzzter", "https://www.planetminecraft.com/skin/overwatch---ana-shrike/"), 
			new Skin(TextFormatting.DARK_PURPLE+"Ghoul", "Ana Ghoul Skin", "DaDerpNarwhal", "http://www.minecraftskins.com/skin/11300611/ana-ghoul-skin/"), 
			new Skin(TextFormatting.DARK_PURPLE+"Merciful", "Ana Merciful", "QuantumQuark", "http://www.minecraftskins.com/skin/11038160/ana-merciful/"), 
			new Skin(TextFormatting.GOLD+"Captain Amari", "Captain Amari", "yana2princess", "http://www.minecraftskins.com/skin/11380464/captain-amari/")), 
	GENJI("Genji", false, new Ability(KeyBind.ABILITY_2, true, true, 0, 0), 
			new Ability(KeyBind.ABILITY_1, true, true, 0, 0), 
			new Ability(KeyBind.NONE, false, false, 0, 0), 
			24, 0, new int[] {2,3,3,2}, new ItemGenjiShuriken(), Crosshair.CIRCLE_SMALL, false, 
			new Skin("Classic", "Overwatch- Genji", "Ringoster", "https://www.planetminecraft.com/skin/genji-3709302/"), 
			new Skin(TextFormatting.DARK_PURPLE+"Carbon Fiber", "Genji: Carbon Fiber", "EP_Schnellnut", "https://www.planetminecraft.com/skin/genji-carbon-fiber/"), 
			new Skin(TextFormatting.GOLD+"Young Genji", "Young Genji", "Aegeah", "https://www.planetminecraft.com/skin/young-genji/"), 
			new Skin(TextFormatting.GOLD+"Blackwatch", "GENJI - BLACKWATCH! [Overwatch]", "Thinkingz", "https://www.planetminecraft.com/skin/genji---blackwatch-overwatch/"), 
			new Skin(TextFormatting.GOLD+"Sentai", "Sentai Genji", "Blastronaut360", "http://www.minecraftskins.com/skin/11247630/sentai-genji/"),
			new Skin(TextFormatting.GOLD+"Nomad", "Nomad Genji Overwatch", "Aireters", "https://www.planetminecraft.com/skin/-nomad-genji-overwatch/")),
	HANZO("Hanzo", false, new Ability(KeyBind.ABILITY_2, true, true, 0, 0), 
			new Ability(KeyBind.ABILITY_1, true, true, 0, 0), 
			new Ability(KeyBind.NONE, false, false, 0, 0), 
			0, 0, new int[] {2,3,3,2}, new ItemHanzoBow(), Crosshair.BOW, false, 
			new Skin("Classic", "Overwatch- Hanzo", "Ringoster", "https://www.planetminecraft.com/skin/overwatch--hanzo/"), 
			new Skin(TextFormatting.GOLD+"Cyber Ninja", "Cyber Ninja Hanzo", "Arctrooper7802", "http://www.minecraftskins.com/skin/11071427/cyber-ninja-hanzo/"), 
			new Skin(TextFormatting.GOLD+"Lone Wolf", "Hanzo, Lone Wolf | Overwatch", "Cayde - 6", "https://www.planetminecraft.com/skin/hanzo-lone-wolf-overwatch/"), 
			new Skin(TextFormatting.GOLD+"Okami", "Okami Hanzo (OW)", "SublimePNG", "https://www.planetminecraft.com/skin/okami-hanzo-ow/")),
	MCCREE("McCree", false, new Ability(KeyBind.ABILITY_2, false, false, 0, 0), 
			new Ability(KeyBind.ABILITY_1, true, false, 0, 0), 
			new Ability(KeyBind.NONE, false, false, 0, 0), 
			6, 0, new int[] {2,3,3,2}, new ItemMcCreeGun(), Crosshair.CIRCLE_SMALL, false, 
			new Skin("Classic", "im yer huckleberry | Jesse McCree", "PlantyBox", "https://www.planetminecraft.com/skin/im-yer-huckleberry-jesse-mccree/"),
			new Skin("Classic", "it's high noon", "HazelOrb", "https://www.planetminecraft.com/skin/its-high-noon/"),
			new Skin(TextFormatting.GOLD+"Riverboat", "Overwatch - McCree (Riverboat)", "Ford", "https://www.planetminecraft.com/skin/overwatch-mccree-riverboat/"),
			new Skin(TextFormatting.GOLD+"Blackwatch", "BlackWatch McCree", "12TheDoctor12", "http://www.minecraftskins.com/skin/10858794/blackwatch-mccree/")),
	REAPER("Reaper", false, new Ability(KeyBind.ABILITY_2, true, true, 0, 0), 
			new Ability(KeyBind.ABILITY_1, false, true, 0, 0), 
			new Ability(KeyBind.NONE, false, false, 0, 0), 
			8, 0, new int[] {2,3,3,2}, new ItemReaperShotgun(), Crosshair.CIRCLE_BIG, false, 
			new Skin("Classic", "Reaper [Overwatch]", "Aegeah", "https://www.planetminecraft.com/skin/reaper-overwatch-3670094/"), 
			new Skin("Classic", "Reaper (PlayOfTheGame)", "_Phantom", "https://www.planetminecraft.com/skin/reaper-playofthegame-overwatch/"),
			new Skin(TextFormatting.DARK_PURPLE+"Shiver", "Reaper shiver holiday skin ( Overwatch)", "Hiccup415", "https://www.planetminecraft.com/skin/reaper-shiver-holiday-skin-overwatch/"), 
			new Skin(TextFormatting.GOLD+"Mariachi", "Mariachi skin Reaper (OverwWatch)", "Roostinator", "https://www.planetminecraft.com/skin/mariachi-skin-overwatch/"),
			new Skin(TextFormatting.GOLD+"Blackwatch Reyes", "Blackwatch Reyes", "Razmoto", "https://www.planetminecraft.com/skin/blackwatch-reyes/")),
	REINHARDT("Reinhardt", false, new Ability(KeyBind.RMB, false, false, 0, 0), 
			new Ability(KeyBind.ABILITY_2, false, false, 0, 0), 
			new Ability(KeyBind.ABILITY_1, false, true, 0, 0), 
			0, 0, new int[] {4,6,6,4}, new ItemReinhardtHammer(), Crosshair.CIRCLE_SMALL, false, 
			new Skin("Classic", "Overwatch Reinhardt","Kohicup", "https://www.planetminecraft.com/skin/overwatch-reinhardt/"),
			new Skin(TextFormatting.GOLD+"Lionhardt", "LionHardt Reinhardt", "ReinhardtWillhelm", "http://www.minecraftskins.com/skin/8764321/lionhardt-reinhardt/"),
			new Skin(TextFormatting.GOLD+"Stonehardt", "Reinhardt - Overwatch", "Baccup", "https://www.planetminecraft.com/skin/reinhardt---overwatch/"),
			new Skin(TextFormatting.GOLD+"Balderich", "Balderich", "TheGuardian755", "http://www.minecraftskins.com/skin/10356345/balderich/")),
	SOLDIER76("Soldier76", false, new Ability(KeyBind.RMB, true, false, 0, 0), 
			new Ability(KeyBind.ABILITY_2, false, false, 0, 0), 
			new Ability(KeyBind.NONE, false, false, 0, 0), 
			25, 0, new int[] {2,3,3,2}, new ItemSoldier76Gun(), Crosshair.PLUS, false, 
			new Skin("Classic", "Soldier 76 (Overwatch)", "sixfootblue", "https://www.planetminecraft.com/skin/soldier-76-overwatch-3819528/"),
			new Skin(TextFormatting.AQUA+"Smoke", "smoke update", "Shadowstxr", "http://www.minecraftskins.com/skin/9559771/smoke-update/"),
			new Skin(TextFormatting.DARK_PURPLE+"Golden", "Golden Soldier 76", "riddler55", "http://www.minecraftskins.com/skin/10930005/golden-soldier-76/"),
			new Skin(TextFormatting.DARK_PURPLE+"Bone", "Soldier 76 Bone SKin", "BagelSki", "http://www.minecraftskins.com/skin/9737491/soldier-76-bone-skin/"),
			new Skin(TextFormatting.GOLD+"Strike Commander", "Strike Commander Morrison - Soldier 76 - Overwatch", "Obvial", "https://www.planetminecraft.com/skin/strike-commander-morrison-3938568/"),
			new Skin(TextFormatting.GOLD+"Grill Master: 76", "Grill Master 76 (Soldier 76 Summer Games 2017)", "InfamousHN", "https://www.planetminecraft.com/skin/grill-master-76-soldier-76-summer-games-2017/")),
	TRACER("Tracer", false, new Ability(KeyBind.ABILITY_2, false, true, 0, 0), 
			new Ability(KeyBind.ABILITY_1, true, false, 3, 60), 
			new Ability(KeyBind.NONE, false, false, 0, 0), 
			40, 0, new int[] {2,2,2,2}, new ItemTracerPistol(), Crosshair.CIRCLE_SMALL, true, 
			new Skin("Classic", "Tracer- Overwatch", "Ringoster", "https://www.planetminecraft.com/skin/tracer--overwatch-feat-19-transparency/"),
			new Skin(TextFormatting.GOLD+"Graffiti", "Graffiti Tracer (Overwatch)", "RyutoMatsuki", "https://www.planetminecraft.com/skin/graffiti-tracer-overwatch-better-in-preview-3982890/"),
			new Skin(TextFormatting.GOLD+"Slipstream", "Overwatch - Slipstream Tracer", "WeegeeTheLucario", "https://www.planetminecraft.com/skin/slipstream-tracer/"),
			new Skin(TextFormatting.GOLD+"Ultraviolet", "[Overwatch] Tracer ~Ultraviolet Skin~", "Vamp1re_", "https://www.planetminecraft.com/skin/overwatch-tracer-ultraviolet-skin/"),
			new Skin(TextFormatting.GOLD+"Cadet Oxton", "Overwatch - Cadet Oxton", "WeegeeTheLucario", "https://www.planetminecraft.com/skin/overwatch-cadet-oxton/"),
			new Skin(TextFormatting.GOLD+"Jingle", "Tracer Jingle", "salmanalansarii", "http://www.minecraftskins.com/skin/10175651/tracer-jingle/")),
	BASTION("Bastion", false, new Ability(KeyBind.ABILITY_2, false, false, 0, 0), 
			new Ability(KeyBind.ABILITY_1, false, true, 0, 0), 
			new Ability(KeyBind.NONE, false, false, 0, 0), 
			25, 0, new int[] {2,3,3,2}, new ItemBastionGun(), Crosshair.PLUS, false,
			new Skin("Classic", "Bastion- Overwatch", "Ringoster", "https://www.planetminecraft.com/skin/bastion--overwatch/"),
			new Skin(TextFormatting.DARK_PURPLE+"Omnic Crisis", "Bastion Omnic Crisis", "LegitNickname", "http://www.minecraftskins.com/skin/10155984/bastion-omnic-crisis/"),
			new Skin(TextFormatting.DARK_PURPLE+"Blizzcon 2016", "Blizcon Bastion HD", "LegitNickname", "http://www.minecraftskins.com/skin/10221741/blizcon-bastion-hd/"),
			new Skin(TextFormatting.DARK_PURPLE+"Tombstone", "HD tombstone bastion", "LegitNickname", "http://www.minecraftskins.com/skin/10225172/hd-tombstone-bastion/"),
			new Skin(TextFormatting.GOLD+"Overgrown", "The last Bastion", "MikKurt", "http://www.minecraftskins.com/skin/10601249/the-last-bastion/")), 
	MEI("Mei", false, new Ability(KeyBind.ABILITY_2, false, true, 0, 0), 
			new Ability(KeyBind.ABILITY_1, false, true, 0, 0), 
			new Ability(KeyBind.NONE, false, false, 0, 0), 
			200, 0, new int[] {2,3,3,2}, new ItemMeiBlaster(), Crosshair.CIRCLE_SMALL, true, 
			new Skin("Classic", "A-Mei-Zing! ...get it? 'cause Mei..", "mareridt", "https://www.planetminecraft.com/skin/a-mei-zing-get-it-cause-mei/")),
	WIDOWMAKER("Widowmaker", false, new Ability(KeyBind.ABILITY_2, false, false, 0, 0), 
			new Ability(KeyBind.ABILITY_1, false, false, 0, 0), 
			new Ability(KeyBind.NONE, false, false, 0, 0), 
			30, 0, new int[] {2,3,3,2}, new ItemWidowmakerRifle(), Crosshair.CIRCLE_SMALL, true, 
			new Skin("Classic", "Widowmaker - Overwatch: 1.8 Skin, Female", "sir-connor", "https://www.planetminecraft.com/skin/widowmaker---overwatch-18-skin-female/"),
			new Skin(TextFormatting.DARK_PURPLE+"Winter", "Winter Widowmaker", "Nudle", "https://www.planetminecraft.com/skin/winter-widowmaker/"),
			new Skin(TextFormatting.GOLD+"Huntress", "Ouh La La", "Katalisa", "https://www.planetminecraft.com/skin/huntress-widowmaker/"),
			new Skin(TextFormatting.GOLD+"Cote d'Azur", "Widowmaker: Cote d'Azur", "Althestane", "https://www.planetminecraft.com/skin/widowmaker-c-te-d-azur/"),
			new Skin(TextFormatting.GOLD+"Talon", "Widowmaker Talon Skin - IISavageDreamzII", "StarryDreamz", "https://www.planetminecraft.com/skin/widowmaker-talon-skin-iisavagedreamzii/")),
	MERCY("Mercy", true, new Ability(KeyBind.NONE, false, false, 0, 0), 
			new Ability(KeyBind.ABILITY_2, false, false, 0, 0), 
			new Ability(KeyBind.ABILITY_1, false, false, 0, 0), 
			0, 20, new int[] {2,2,2,2}, new ItemMercyWeapon(), Crosshair.CIRCLE_SMALL, true, 
			new Skin("Classic", "Overwatch | Mercy", "Efflorescence", "https://www.planetminecraft.com/skin/-overwatch-mercy-/"),
			new Skin("Classic", "Mercy", "FireBoltCreeper", "https://www.planetminecraft.com/skin/mercy-3684205/"),
			new Skin(TextFormatting.GOLD+"Imp", "Imp Mercy Overwatch", "Aireters", "https://www.planetminecraft.com/skin/-imp-mercy-overwatch/"),
			new Skin(TextFormatting.GOLD+"Winged Victory", "Mercy (Winged Victory) - Overwatch", "Benenwren", "https://www.planetminecraft.com/skin/overwatch-mercy-winged-victory/"),
			new Skin(TextFormatting.GOLD+"Witch", "Witch Mercy [OVERWATCH]", "Nudle", "https://www.planetminecraft.com/skin/witch-mercy-overwatch-mind-the-collar-oops/"),
			new Skin(TextFormatting.GOLD+"Combat Medic", "Combat Medit Ziegler", "Noire_", "https://www.planetminecraft.com/skin/combat-medic-ziegler-3967530/"));

	public HashMap<UUID, Boolean> playersUsingAlt = Maps.newHashMap();

	public Ability ability1;
	public Ability ability2;
	public Ability ability3;

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
	public boolean smallArms;
	public Skin[] skinInfo;
	public String[] skinCredits;
	public HashMap<String, Integer> skins = Maps.newHashMap();
	private Crosshair crosshair;

	private static enum Crosshair {
		CIRCLE_SMALL(new ResourceLocation(Minewatch.MODID, "textures/gui/crosshair_circle_small.png")),
		CIRCLE_BIG(new ResourceLocation(Minewatch.MODID, "textures/gui/crosshair_circle_big.png")),
		PLUS(new ResourceLocation(Minewatch.MODID, "textures/gui/crosshair_plus.png")),
		BOW(new ResourceLocation(Minewatch.MODID, "textures/gui/crosshair_bow.png"));

		public ResourceLocation loc;

		private Crosshair(ResourceLocation loc) {
			this.loc = loc;
		}
	}

	public static class Skin {
		public String owName;
		public String skinName;
		public String author;
		public String address;

		private Skin(String owName, String skinName, String author, String address) {
			this.owName = owName;
			this.skinName = skinName;
			this.author = author;
			this.address = address;
		}

		/**(skin name) by (author)*/
		public String getCreditText() {
			return this.skinName+TextFormatting.RESET+" by "+this.author;
		}
	}

	private static final class IndexCounter {
		/**used to calculate overlayIndex*/
		public static int index;
	}

	static {
		for (EnumHero hero : EnumHero.values())
			hero.weapon.hero = hero;

	}

	private EnumHero(String name, boolean hasAltWeapon, Ability ability1, Ability ability2, Ability ability3,
			int mainAmmo, int altAmmo, int[] armorReductionAmounts, ItemMWWeapon weapon, Crosshair crosshair, 
			boolean smallArms, Skin... skinInfo) {
		this.overlayIndex = IndexCounter.index++;
		this.name = name;
		this.hasAltWeapon = hasAltWeapon;
		if (this.hasAltWeapon)
			this.altWeaponIndex = IndexCounter.index++;
		this.ability1 = ability1;
		this.ability2 = ability2;
		this.ability3 = ability3;
		this.ability1.hero = this;
		this.ability2.hero = this;
		this.ability3.hero = this;
		this.mainAmmo = mainAmmo;
		this.altAmmo = altAmmo;
		this.armorReductionAmounts = armorReductionAmounts;
		this.weapon = weapon;
		this.crosshair = crosshair;
		this.smallArms = smallArms;
		this.skinInfo = skinInfo;
		this.skinCredits = new String[skinInfo.length];
		for (int i=0; i<skinInfo.length; ++i)
			this.skinCredits[i] = this.skinInfo[i].getCreditText();
	}

	public int getSkin(UUID uuid) {
		if (skins.containsKey(uuid.toString()))
			return skins.get(uuid.toString());
		else
			return 0;
	}

	public void setSkin(UUID uuid, int skin) {
		if (uuid == null)
			return;
		if (skin < 0 || skin >= this.skinInfo.length)
			skin = 0;
		if (!skins.containsKey(uuid) || skins.get(uuid) != skin) {
			skins.put(uuid.toString(), skin);
			// sync to config
			Property prop = Config.getHeroTextureProp(this);
			if (!this.skinInfo[skin].getCreditText().equals(prop.getString())) {
				prop.set(this.skinInfo[skin].getCreditText());
				Config.config.save();
			}
		}
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
		public static void hidePlayerWearingArmor(RenderLivingEvent.Pre<EntityPlayer> event) {
			if (event.getRenderer().getMainModel() instanceof ModelPlayer) {
				ModelPlayer model = (ModelPlayer) event.getRenderer().getMainModel();
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					ItemStack stack = event.getEntity().getItemStackFromSlot(slot);
					if (stack != null && stack.getItem() instanceof ItemMWArmor) {
						if (slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET) {
							model.bipedLeftLegwear.showModel = false;
							model.bipedRightLegwear.showModel = false;
						}
						else if (slot == EntityEquipmentSlot.CHEST) {
							model.bipedBody.showModel = false;
							model.bipedBodyWear.showModel = false;
							model.bipedLeftArm.showModel = false;
							model.bipedLeftArmwear.showModel = false;
							model.bipedRightArm.showModel = false;
							model.bipedRightArmwear.showModel = false;
						}
						else if (slot == EntityEquipmentSlot.HEAD) {
							model.bipedHeadwear.showModel = false;
							model.bipedHead.showModel = false;
						}
					}
				}
			}
		}

		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public static void renderCrosshairs(RenderGameOverlayEvent.Pre event) {
			if (event.getType() == ElementType.CROSSHAIRS && Config.guiScale > 0) {
				EntityPlayer player = Minecraft.getMinecraft().player;
				EnumHero hero = ItemMWArmor.SetManager.playersWearingSets.containsKey(player.getPersistentID()) ? ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) : null;
				EnumHand hand = null;
				for (EnumHand hand2 : EnumHand.values())
					if (player.getHeldItem(hand2) != null && player.getHeldItem(hand2).getItem() instanceof ItemMWWeapon && (((ItemMWWeapon)player.getHeldItem(hand2).getItem()).hero == hero || hand == null || ((ItemMWWeapon)player.getHeldItem(hand).getItem()).hero != hero))
						hand = hand2;
				ItemMWWeapon weapon = hand == null ? null : (ItemMWWeapon) player.getHeldItem(hand).getItem();

				if (weapon != null) {
					if (!(weapon.hero == hero && Minewatch.keys.heroInformation(player)) &&
							Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
						GlStateManager.pushMatrix();
						GlStateManager.enableBlend();

						// render crosshair
						double scale = 0.2d*Config.guiScale;
						GlStateManager.scale(scale, scale, 1);
						GlStateManager.translate((int) ((event.getResolution().getScaledWidth_double() - 256*scale)/2d / scale), (int) ((event.getResolution().getScaledHeight_double() - 256*scale)/2d / scale), 0);
						if (Config.customCrosshairs) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(weapon.hero.crosshair.loc);
							GuiUtils.drawTexturedModalRect(3, 3, 0, 0, 256, 256, 0);
						}

						GlStateManager.disableBlend();
						GlStateManager.popMatrix();

						// tracer's dash
						if (weapon.hero == EnumHero.TRACER && ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) == EnumHero.TRACER) {
							GlStateManager.pushMatrix();
							GlStateManager.enableBlend();

							scale = 0.8d*Config.guiScale;
							GlStateManager.scale(scale, scale*4, 1);
							GlStateManager.translate((int) ((event.getResolution().getScaledWidth_double() - 83*scale)/2d / scale), (int) ((event.getResolution().getScaledHeight_double()- 80*scale)/8d / scale), 0);
							Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/ability_overlay.png"));
							int uses = weapon.hero.ability2.getUses(player);
							GuiUtils.drawTexturedModalRect(23, 21, 1, uses > 2 ? 1011 : 1015, 40, 4, 0);
							GlStateManager.scale(0.75f, 0.75f, 1);
							GuiUtils.drawTexturedModalRect(37, 25, 1, uses > 1 ? 1011 : 1015, 40, 4, 0);
							GlStateManager.scale(0.75f, 0.75f, 1);
							GuiUtils.drawTexturedModalRect(56, 30, 1, uses > 0 ? 1011 : 1015, 40, 4, 0);

							GlStateManager.disableBlend();
							GlStateManager.popMatrix();
						}
						// reaper's teleport/cancel overlay
						else if (weapon.hero == EnumHero.REAPER && ItemReaperShotgun.clientTps.containsKey(player) &&
								ItemReaperShotgun.clientTps.get(player).getFirst() == -1) {
							GlStateManager.pushMatrix();
							GlStateManager.enableBlend();

							scale = 0.8d*Config.guiScale;
							GlStateManager.scale(scale, scale, 1);
							GlStateManager.translate((int) ((event.getResolution().getScaledWidth_double() - 256*scale)/2d / scale), (int) ((event.getResolution().getScaledHeight_double() - 256*scale)/2d / scale), 0);
							Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/reaper_teleport.png"));
							GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);

							GlStateManager.disableBlend();
							GlStateManager.popMatrix();
						}
					}
					if (Config.customCrosshairs)
						event.setCanceled(true);
				}
			}
		}

		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public static void renderOverlay(RenderGameOverlayEvent.Post event) {
			if (event.getType() == ElementType.HELMET && Config.guiScale > 0) {			
				EntityPlayer player = Minecraft.getMinecraft().player;
				EnumHero hero = ItemMWArmor.SetManager.playersWearingSets.containsKey(player.getPersistentID()) ? ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) : null;
				EnumHand hand = null;
				for (EnumHand hand2 : EnumHand.values())
					if (player.getHeldItem(hand2) != null && player.getHeldItem(hand2).getItem() instanceof ItemMWWeapon && (((ItemMWWeapon)player.getHeldItem(hand2).getItem()).hero == hero || hand == null || ((ItemMWWeapon)player.getHeldItem(hand).getItem()).hero != hero))
						hand = hand2;
				ItemMWWeapon weapon = hand == null ? null : (ItemMWWeapon) player.getHeldItem(hand).getItem();

				// hero information screen
				if (hero != null && weapon != null && weapon.hero == hero && Minewatch.keys.heroInformation(player))
					hero.displayInfoScreen(event.getResolution());
				else {
					if (hero != null) {
						// display icon
						GlStateManager.pushMatrix();
						GlStateManager.enableDepth();
						GlStateManager.enableAlpha();

						double scale = 0.35d*Config.guiScale;
						GlStateManager.scale(scale, scale, 1);
						GlStateManager.translate(40-scale*120, (int) ((event.getResolution().getScaledHeight() - 256*scale) / scale) - 35+scale*110, 0);
						Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/icon_background.png"));
						GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 240, 230, 0);
						Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+hero.name.toLowerCase()+"_icon.png"));
						GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 240, 230, 0);

						GlStateManager.popMatrix();
					}

					// display abilities/weapon
					if (weapon != null) {
						GlStateManager.pushMatrix();
						GlStateManager.enableDepth();
						GlStateManager.enableAlpha();

						double scale = 1d*Config.guiScale;
						GlStateManager.scale(1*scale, 4*scale, 1);
						GlStateManager.translate((int) (event.getResolution().getScaledWidth()/scale)-125, ((int)event.getResolution().getScaledHeight()/scale/4)-18+scale*3, 0);
						Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/ability_overlay.png"));
						int index = weapon.hero.playersUsingAlt.containsKey(player.getPersistentID()) && weapon.hero.playersUsingAlt.get(player.getPersistentID()) && 
								weapon.hero.hasAltWeapon ? weapon.hero.altWeaponIndex : weapon.hero.overlayIndex;
						int vertical = 11;
						// weapon
						GuiUtils.drawTexturedModalRect(0, 0, 1, (index+1)+index*vertical, 122, vertical, 0);

						if (hero != null && weapon.hero == hero && ItemMWArmor.SetManager.playersWearingSets.containsKey(player.getPersistentID())) {
							// slot 1
							if (hero.ability1.keybind.getCooldown(player) > 0 || (hero.ability1.maxUses > 0 && hero.ability1.getUses(player) == 0)) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							else if (hero.ability1.isSelected(player)) {
								GlStateManager.color(0.8f, 0.6f, 0);
								GlStateManager.translate(1, 1, 0);
							}
							GuiUtils.drawTexturedModalRect(-50, -2, 124, (index+1)+index*vertical, 40, vertical, 0);
							if (!hero.ability1.isEnabled && hero.ability1.keybind != KeyBind.NONE) 
								GuiUtils.drawTexturedModalRect(-28, 0, 65, 1015, 12, 9, 0);
							GlStateManager.color(1, 1, 1);
							if (hero.ability1.isSelected(player)) 
								GlStateManager.translate(-1, -1, 0);
							// slot 2
							if (hero.ability2.keybind.getCooldown(player) > 0 || (hero.ability2.maxUses > 0 && hero.ability2.getUses(player) == 0)) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							else if (hero.ability2.isSelected(player)) {
								GlStateManager.color(0.8f, 0.6f, 0);
								GlStateManager.translate(1, 1, 0);
							}
							GuiUtils.drawTexturedModalRect(-87, -2, 165, (index+1)+index*vertical, 40, vertical, 0);
							if (!hero.ability2.isEnabled && hero.ability2.keybind != KeyBind.NONE) {
								GlStateManager.translate(0, 0.3f, 0);
								GuiUtils.drawTexturedModalRect(-65, -1, 65, 1015, 12, 9, 0);
								GlStateManager.translate(0, -0.3f, 0);
							}
							GlStateManager.color(1, 1, 1);
							if (hero.ability2.isSelected(player)) 
								GlStateManager.translate(-1, -1, 0);
							// slot 3
							if (hero.ability3.keybind.getCooldown(player) > 0 || (hero.ability3.maxUses > 0 && hero.ability3.getUses(player) == 0)) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							else if (hero.ability3.isSelected(player)) {
								GlStateManager.color(0.8f, 0.6f, 0);
								GlStateManager.translate(1, 1, 0);
							}
							GuiUtils.drawTexturedModalRect(-124, -2, 206, (index+1)+index*vertical, 40, vertical, 0);
							if (!hero.ability3.isEnabled && hero.ability3.keybind != KeyBind.NONE) {
								GlStateManager.translate(0, 0.5f, 0);
								GuiUtils.drawTexturedModalRect(-102, -2, 65, 1015, 12, 9, 0);
								GlStateManager.translate(0, -0.5f, 0);
							}
							GlStateManager.color(1, 1, 1);
							if (hero.ability3.isSelected(player))
								GlStateManager.translate(-1, -1, 0);

							// keybinds 
							int width1 = Minecraft.getMinecraft().fontRendererObj.getStringWidth(hero.ability1.keybind.getKeyName());
							int width2 = Minecraft.getMinecraft().fontRendererObj.getStringWidth(hero.ability2.keybind.getKeyName());
							int width3 = Minecraft.getMinecraft().fontRendererObj.getStringWidth(hero.ability3.keybind.getKeyName());
							// background
							// slot 1
							if (hero.ability1.keybind.getCooldown(player) > 0 || (hero.ability1.maxUses > 0 && hero.ability1.getUses(player) == 0)) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							if (hero.ability1.keybind.getKeyName() != "")
								GuiUtils.drawTexturedModalRect(-58, 7, 0, 1019, 40, 5, 0);
							else if (hero.ability1.keybind == KeyBind.RMB)
								GuiUtils.drawTexturedModalRect(-43, 3, 46, 1015, 10, 9, 0);
							if (hero.ability1.maxUses > 0)
								GuiUtils.drawTexturedModalRect(-30, -10, 81, 1015, 20, 9, 0);
							GlStateManager.color(1, 1, 1);
							// slot 2
							if (hero.ability2.keybind.getCooldown(player) > 0 || (hero.ability2.maxUses > 0 && hero.ability2.getUses(player) == 0)) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							if (hero.ability2.keybind.getKeyName() != "")
								GuiUtils.drawTexturedModalRect(-98, 6, 0, 1019, 40, 5, 0);
							if (hero.ability2.maxUses > 0)
								GuiUtils.drawTexturedModalRect(-69, -10, 81, 1015, 20, 9, 0);
							GlStateManager.color(1, 1, 1);
							// slot 3
							if (hero.ability3.keybind.getCooldown(player) > 0 || (hero.ability3.maxUses > 0 && hero.ability3.getUses(player) == 0)) 
								GlStateManager.color(0.4f, 0.4f, 0.4f);
							if (hero.ability3.keybind.getKeyName() != "")
								GuiUtils.drawTexturedModalRect(-137, 5, 0, 1019, 40, 5, 0);
							if (hero.ability3.maxUses > 0)
								GuiUtils.drawTexturedModalRect(-106, -11, 81, 1015, 20, 9, 0);
							GlStateManager.color(1, 1, 1);
							// text
							GlStateManager.scale(1, 0.25d, 1);
							GlStateManager.rotate(4.5f, 0, 0, 1);
							// slot 1
							Minecraft.getMinecraft().fontRendererObj.drawString(hero.ability1.keybind.getKeyName(), -33-width1/2, 38, 0);
							if (hero.ability1.maxUses > 0)
								Minecraft.getMinecraft().fontRendererObj.drawString(String.valueOf(hero.ability1.getUses(player)), -99, -15, 0);
							// slot 2
							Minecraft.getMinecraft().fontRendererObj.drawString(hero.ability2.keybind.getKeyName(), -74-width2/2, 37, 0);
							if (hero.ability2.maxUses > 0)
								Minecraft.getMinecraft().fontRendererObj.drawString(String.valueOf(hero.ability2.getUses(player)), -62, -14, 0);
							// slot 3
							Minecraft.getMinecraft().fontRendererObj.drawString(hero.ability3.keybind.getKeyName(), -114-width3/2, 37, 0);
							if (hero.ability3.maxUses > 0)
								Minecraft.getMinecraft().fontRendererObj.drawString(String.valueOf(hero.ability3.getUses(player)), -23, -16, 0);

							// cooldowns
							scale = 2d;
							GlStateManager.scale(scale, scale, 1);
							if (hero.ability1.keybind.getCooldown(player) > 0) { 
								String num = String.valueOf((int)Math.ceil(hero.ability1.keybind.getCooldown(player)/20d));
								int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(num);
								Minecraft.getMinecraft().fontRendererObj.drawString(num, -14-width/2, 4, 0xFFFFFF);
							}
							if (hero.ability2.keybind.getCooldown(player) > 0) { 
								String num = String.valueOf((int)Math.ceil(hero.ability2.keybind.getCooldown(player)/20d));
								int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(num);
								Minecraft.getMinecraft().fontRendererObj.drawString(num, -33-width/2, 4, 0xFFFFFF);
							}
							if (hero.ability3.keybind.getCooldown(player) > 0) { 
								String num = String.valueOf((int)Math.ceil(hero.ability3.keybind.getCooldown(player)/20d));
								int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(num);
								Minecraft.getMinecraft().fontRendererObj.drawString(num, -51-width/2, 4, 0xFFFFFF);
							}
						}
						// ammo
						if (weapon.getMaxAmmo(player) > 0) {
							if (weapon.hero != hero || hero == null) { // adjust things that were skipped
								GlStateManager.scale(1, 0.25d, 1);
								GlStateManager.rotate(4.5f, 0, 0, 1);
								scale = 2d;
								GlStateManager.scale(scale, scale, 1);
							}

							scale = 0.9d;
							GlStateManager.scale(scale, scale, 1);
							int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(
									String.valueOf(weapon.getCurrentAmmo(player)));
							Minecraft.getMinecraft().fontRendererObj.drawString(
									String.valueOf(weapon.getCurrentAmmo(player)), 30-width, -11, 0xFFFFFF);
							scale = 0.6d;
							GlStateManager.scale(scale, scale, 1);
							Minecraft.getMinecraft().fontRendererObj.drawString("/", 53, -13, 0x00D5FF);
							Minecraft.getMinecraft().fontRendererObj.drawString(
									String.valueOf(weapon.getMaxAmmo(player)), 59, -13, 0xFFFFFF);
						}

						GlStateManager.popMatrix();
					}
				}
			}
		}
	}

	public void displayInfoScreen(ScaledResolution resolution) {
		GlStateManager.pushMatrix();
		GlStateManager.disableDepth();

		GlStateManager.scale(resolution.getScaledWidth_double()/256d, resolution.getScaledHeight_double()/256d, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/info_background.png"));
		GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 1920, 1080, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+this.name.toLowerCase()+"_info.png"));
		GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 1920, 1080, 0);

		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}

}
