package twopiradians.minewatch.common.hero;

import java.awt.Color;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiUtils;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityAna;
import twopiradians.minewatch.common.entity.hero.EntityBastion;
import twopiradians.minewatch.common.entity.hero.EntityGenji;
import twopiradians.minewatch.common.entity.hero.EntityHanzo;
import twopiradians.minewatch.common.entity.hero.EntityJunkrat;
import twopiradians.minewatch.common.entity.hero.EntityLucio;
import twopiradians.minewatch.common.entity.hero.EntityMcCree;
import twopiradians.minewatch.common.entity.hero.EntityMei;
import twopiradians.minewatch.common.entity.hero.EntityMercy;
import twopiradians.minewatch.common.entity.hero.EntityMoira;
import twopiradians.minewatch.common.entity.hero.EntityReaper;
import twopiradians.minewatch.common.entity.hero.EntityReinhardt;
import twopiradians.minewatch.common.entity.hero.EntitySoldier76;
import twopiradians.minewatch.common.entity.hero.EntitySombra;
import twopiradians.minewatch.common.entity.hero.EntityTracer;
import twopiradians.minewatch.common.entity.hero.EntityWidowmaker;
import twopiradians.minewatch.common.entity.hero.EntityZenyatta;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.item.weapon.ItemBastionGun;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemHanzoBow;
import twopiradians.minewatch.common.item.weapon.ItemJunkratLauncher;
import twopiradians.minewatch.common.item.weapon.ItemLucioSoundAmplifier;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.item.weapon.ItemMcCreeGun;
import twopiradians.minewatch.common.item.weapon.ItemMeiBlaster;
import twopiradians.minewatch.common.item.weapon.ItemMercyWeapon;
import twopiradians.minewatch.common.item.weapon.ItemMoiraWeapon;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;
import twopiradians.minewatch.common.item.weapon.ItemReinhardtHammer;
import twopiradians.minewatch.common.item.weapon.ItemSoldier76Gun;
import twopiradians.minewatch.common.item.weapon.ItemSombraMachinePistol;
import twopiradians.minewatch.common.item.weapon.ItemTracerPistol;
import twopiradians.minewatch.common.item.weapon.ItemWidowmakerRifle;
import twopiradians.minewatch.common.item.weapon.ItemZenyattaWeapon;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public enum EnumHero {

	// do not change order - this is the order in ability_overlay.png
	ANA("Ana", true, true, new Ability(KeyBind.ABILITY_2, false, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			10, 10, new int[] {2,3,3,2}, new ItemAnaRifle(), Crosshair.CIRCLE_SMALL, 0x6E8AB1, true, EntityAna.class,
			new Skin("Classic", "Overwatch - Ana", "Drzzter", "https://www.planetminecraft.com/skin/overwatch---ana-shrike/"), 
			new Skin("Classic", "Until The End - Ana [Overwatch]", "Orbiter", "https://www.planetminecraft.com/skin/until-the-end-ana-overwatch/"),
			new Skin(TextFormatting.DARK_PURPLE+"Ghoul", "Ana Ghoul Skin", "DaDerpNarwhal", "http://www.minecraftskins.com/skin/11300611/ana-ghoul-skin/"), 
			new Skin(TextFormatting.DARK_PURPLE+"Merciful", "Ana Merciful", "QuantumQuark", "http://www.minecraftskins.com/skin/11038160/ana-merciful/"), 
			new Skin(TextFormatting.GOLD+"Captain Amari", "Captain Amari", "yana2princess", "http://www.minecraftskins.com/skin/11380464/captain-amari/")), 
	GENJI("Genji", false, false, new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			24, 0, new int[] {2,3,3,2}, new ItemGenjiShuriken(), Crosshair.CIRCLE_SMALL, 0x95EF42, false, EntityGenji.class, 
			new Skin("Classic", "Overwatch- Genji", "Ringoster", "https://www.planetminecraft.com/skin/genji-3709302/"), 
			new Skin(TextFormatting.DARK_PURPLE+"Carbon Fiber", "Genji: Carbon Fiber", "EP_Schnellnut", "https://www.planetminecraft.com/skin/genji-carbon-fiber/"), 
			new Skin(TextFormatting.GOLD+"Young Genji", "Young Genji", "Aegeah", "https://www.planetminecraft.com/skin/young-genji/"), 
			new Skin(TextFormatting.GOLD+"Blackwatch", "GENJI - BLACKWATCH! [Overwatch]", "Thinkingz", "https://www.planetminecraft.com/skin/genji---blackwatch-overwatch/"), 
			new Skin(TextFormatting.GOLD+"Sentai", "Sentai Genji", "Blastronaut360", "http://www.minecraftskins.com/skin/11247630/sentai-genji/"),
			new Skin(TextFormatting.GOLD+"Nomad", "Nomad Genji Overwatch", "Aireters", "https://www.planetminecraft.com/skin/-nomad-genji-overwatch/"),
			new Skin(TextFormatting.GOLD+"Oni", "Oni Genji Skin", "DaDerpNarwhal", "http://www.minecraftskins.com/skin/11298711/oni-genji-skin/")),
	HANZO("Hanzo", false, false, new Ability(KeyBind.ABILITY_2, true, true), 
			new Ability(KeyBind.ABILITY_1, true, true), 
			new Ability(KeyBind.NONE, false, false), 
			0, 0, new int[] {2,3,3,2}, new ItemHanzoBow(), Crosshair.BOW, 0xB6B589, false, EntityHanzo.class, 
			new Skin("Classic", "Overwatch- Hanzo", "Ringoster", "https://www.planetminecraft.com/skin/overwatch--hanzo/"), 
			new Skin(TextFormatting.GOLD+"Cyber Ninja", "Cyber Ninja Hanzo", "Arctrooper7802", "http://www.minecraftskins.com/skin/11071427/cyber-ninja-hanzo/"), 
			new Skin(TextFormatting.GOLD+"Lone Wolf", "Hanzo, Lone Wolf | Overwatch", "Cayde - 6", "https://www.planetminecraft.com/skin/hanzo-lone-wolf-overwatch/"), 
			new Skin(TextFormatting.GOLD+"Okami", "Okami Hanzo (OW)", "SublimePNG", "https://www.planetminecraft.com/skin/okami-hanzo-ow/")),
	MCCREE("McCree", false, false, new Ability(KeyBind.ABILITY_2, false, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			6, 0, new int[] {2,3,3,2}, new ItemMcCreeGun(), Crosshair.CIRCLE_SMALL, 0xAF595C, false, EntityMcCree.class, 
			new Skin("Classic", "im yer huckleberry | Jesse McCree", "PlantyBox", "https://www.planetminecraft.com/skin/im-yer-huckleberry-jesse-mccree/"),
			new Skin("Classic", "it's high noon", "HazelOrb", "https://www.planetminecraft.com/skin/its-high-noon/"),
			new Skin(TextFormatting.GOLD+"Riverboat", "Overwatch - McCree (Riverboat)", "Ford", "https://www.planetminecraft.com/skin/overwatch-mccree-riverboat/"),
			new Skin(TextFormatting.GOLD+"Blackwatch", "BlackWatch McCree", "12TheDoctor12", "http://www.minecraftskins.com/skin/10858794/blackwatch-mccree/"),
			new Skin(TextFormatting.GOLD+"Lifeguard", "Lifeguard McCree", "OP_Beast", "https://www.planetminecraft.com/skin/lifeguard-mccree/")),
	REAPER("Reaper", false, false, new Ability(KeyBind.ABILITY_2, true, true), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			8, 0, new int[] {2,3,3,2}, new ItemReaperShotgun(), Crosshair.CIRCLE_BIG, 0x793E50, false, EntityReaper.class, 
			new Skin("Classic", "Reaper [Overwatch]", "Aegeah", "https://www.planetminecraft.com/skin/reaper-overwatch-3670094/"), 
			new Skin("Classic", "Reaper (PlayOfTheGame)", "_Phantom", "https://www.planetminecraft.com/skin/reaper-playofthegame-overwatch/"),
			new Skin("Classic", "Reaper | Overwatch", "Cayde - 6", "https://www.planetminecraft.com/skin/reaper-overwatch-3652548/"),
			new Skin(TextFormatting.DARK_PURPLE+"Shiver", "Reaper shiver holiday skin ( Overwatch)", "Hiccup415", "https://www.planetminecraft.com/skin/reaper-shiver-holiday-skin-overwatch/"), 
			new Skin(TextFormatting.GOLD+"Mariachi", "Mariachi skin Reaper (OverWatch)", "Roostinator", "https://www.planetminecraft.com/skin/mariachi-skin-overwatch/"),
			new Skin(TextFormatting.GOLD+"Blackwatch Reyes", "Blackwatch Reyes", "Razmoto", "https://www.planetminecraft.com/skin/blackwatch-reyes/"),
			new Skin(TextFormatting.GOLD+"Dracula", "Dracula Reaper Skin", "DaDerpNarwhal", "https://www.planetminecraft.com/skin/dracula-reaper-skin/")),
	REINHARDT("Reinhardt", false, false, new Ability(KeyBind.RMB, false, false), 
			new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, false, true), 
			0, 0, new int[] {4,6,6,4}, new ItemReinhardtHammer(), Crosshair.CIRCLE_SMALL, 0x919EA4, false, EntityReinhardt.class, 
			new Skin("Classic", "Overwatch Reinhardt","Kohicup", "https://www.planetminecraft.com/skin/overwatch-reinhardt/"),
			new Skin(TextFormatting.DARK_PURPLE+"Coldhardt", "ColdHardt", "jay_zx_jc", "https://www.planetminecraft.com/skin/coldhardt/"),
			new Skin(TextFormatting.GOLD+"Lionhardt", "LionHardt Reinhardt", "ReinhardtWillhelm", "http://www.minecraftskins.com/skin/8764321/lionhardt-reinhardt/"),
			new Skin(TextFormatting.GOLD+"Stonehardt", "Reinhardt - Overwatch", "Baccup", "https://www.planetminecraft.com/skin/reinhardt---overwatch/"),
			new Skin(TextFormatting.GOLD+"Balderich", "Balderich", "TheGuardian755", "http://www.minecraftskins.com/skin/10356345/balderich/")),
	SOLDIER76("Soldier76", false, false, new Ability(KeyBind.RMB, true, false), 
			new Ability(KeyBind.ABILITY_2, false, false), 
			new Ability(KeyBind.NONE, true, true), 
			25, 0, new int[] {2,3,3,2}, new ItemSoldier76Gun(), Crosshair.PLUS, 0x6A7895, false, EntitySoldier76.class, 
			new Skin("Classic", "Soldier 76 (Overwatch)", "sixfootblue", "https://www.planetminecraft.com/skin/soldier-76-overwatch-3819528/"),
			new Skin("Classic", "Soldier 76", "Knap", "https://www.planetminecraft.com/skin/soldier-76-3820018/"),
			new Skin(TextFormatting.DARK_AQUA+"Smoke", "smoke update", "Shadowstxr", "http://www.minecraftskins.com/skin/9559771/smoke-update/"),
			new Skin(TextFormatting.DARK_PURPLE+"Golden", "Golden Soldier 76", "riddler55", "http://www.minecraftskins.com/skin/10930005/golden-soldier-76/"),
			new Skin(TextFormatting.DARK_PURPLE+"Bone", "Soldier 76 Bone SKin", "BagelSki", "http://www.minecraftskins.com/skin/9737491/soldier-76-bone-skin/"),
			new Skin(TextFormatting.GOLD+"Strike Commander", "Strike Commander Morrison - Soldier 76 - Overwatch", "Obvial", "https://www.planetminecraft.com/skin/strike-commander-morrison-3938568/"),
			new Skin(TextFormatting.GOLD+"Grill Master: 76", "Grill Master 76 (Soldier 76 Summer Games 2017)", "InfamousHN", "https://www.planetminecraft.com/skin/grill-master-76-soldier-76-summer-games-2017/")),
	TRACER("Tracer", false, false, new Ability(KeyBind.ABILITY_2, false, false), 
			new Ability(KeyBind.ABILITY_1, true, false, 3, 60), 
			new Ability(KeyBind.NONE, false, false), 
			40, 0, new int[] {2,2,2,2}, new ItemTracerPistol(), Crosshair.CIRCLE_SMALL, 0xD89441, true, EntityTracer.class, 
			new Skin("Classic", "Tracer- Overwatch", "Ringoster", "https://www.planetminecraft.com/skin/tracer--overwatch-feat-19-transparency/"),
			new Skin(TextFormatting.GOLD+"Graffiti", "Graffiti Tracer (Overwatch)", "RyutoMatsuki", "https://www.planetminecraft.com/skin/graffiti-tracer-overwatch-better-in-preview-3982890/"),
			new Skin(TextFormatting.GOLD+"Slipstream", "Overwatch - Slipstream Tracer", "WeegeeTheLucario", "https://www.planetminecraft.com/skin/slipstream-tracer/"),
			new Skin(TextFormatting.GOLD+"Ultraviolet", "[Overwatch] Tracer ~Ultraviolet Skin~", "Vamp1re_", "https://www.planetminecraft.com/skin/overwatch-tracer-ultraviolet-skin/"),
			new Skin(TextFormatting.GOLD+"Cadet Oxton", "Overwatch - Cadet Oxton", "WeegeeTheLucario", "https://www.planetminecraft.com/skin/overwatch-cadet-oxton/"),
			new Skin(TextFormatting.GOLD+"Jingle", "Tracer Jingle", "salmanalansarii", "http://www.minecraftskins.com/skin/10175651/tracer-jingle/")),
	BASTION("Bastion", true, false, new Ability(KeyBind.RMB, false, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			25, 300, new int[] {2,3,3,2}, new ItemBastionGun(), Crosshair.PLUS, 0x7A8D79, false, EntityBastion.class,
			new Skin("Classic", "Bastion- Overwatch", "Ringoster", "https://www.planetminecraft.com/skin/bastion--overwatch/"),
			new Skin(TextFormatting.DARK_PURPLE+"Omnic Crisis", "Bastion Omnic Crisis", "LegitNickname", "http://www.minecraftskins.com/skin/10155984/bastion-omnic-crisis/"),
			new Skin(TextFormatting.DARK_PURPLE+"Blizzcon 2016", "Blizcon Bastion HD", "LegitNickname", "http://www.minecraftskins.com/skin/10221741/blizcon-bastion-hd/"),
			new Skin(TextFormatting.DARK_PURPLE+"Tombstone", "HD tombstone bastion", "LegitNickname", "http://www.minecraftskins.com/skin/10225172/hd-tombstone-bastion/"),
			new Skin(TextFormatting.GOLD+"Overgrown", "The last Bastion", "MikKurt", "http://www.minecraftskins.com/skin/10601249/the-last-bastion/")), 
	MEI("Mei", false, false, new Ability(KeyBind.ABILITY_2, false, false), 
			new Ability(KeyBind.ABILITY_1, true, true), 
			new Ability(KeyBind.NONE, false, false), 
			200, 0, new int[] {2,3,3,2}, new ItemMeiBlaster(), Crosshair.CIRCLE_SMALL, 0x6BA8E7, true, EntityMei.class, 
			new Skin("Classic", "A-Mei-Zing! ...get it? 'cause Mei..", "oEffy", "https://www.planetminecraft.com/skin/a-mei-zing-get-it-cause-mei/"),
			new Skin(TextFormatting.GOLD+"Mei-rry", "Mei-Rry", "KevinAguirre2", "http://www.minecraftskins.com/skin/11709782/mei-rry/", "A-Mei-Zing! ...get it? 'cause Mei..", "oEffy", "https://www.planetminecraft.com/skin/a-mei-zing-get-it-cause-mei/"),
			new Skin(TextFormatting.GOLD+"Luna", "Mei v2", "nikita505n", "http://www.minecraftskins.com/skin/11711832/mei-v2/"),
			new Skin(TextFormatting.GOLD+"Jiangshi", "Mei Jiangshi", "KevinAguirre2", "http://www.minecraftskins.com/skin/11720409/mei-jiangshi/", "A-Mei-Zing! ...get it? 'cause Mei..", "oEffy", "https://www.planetminecraft.com/skin/a-mei-zing-get-it-cause-mei/")),
	WIDOWMAKER("Widowmaker", false, false, new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, false, false), 
			new Ability(KeyBind.NONE, false, false), 
			30, 0, new int[] {2,3,3,2}, new ItemWidowmakerRifle(), Crosshair.CIRCLE_SMALL, 0x9A68A3, true, EntityWidowmaker.class, 
			new Skin("Classic", "Widowmaker - Overwatch: 1.8 Skin, Female", "sir-connor", "https://www.planetminecraft.com/skin/widowmaker---overwatch-18-skin-female/"),
			new Skin("Classic", "Widowmaker (Overwatch) ... ONE SHOT, ONE KILL", "KAWAI_Murderer", "https://www.planetminecraft.com/skin/widowmaker-overwatch-one-shot-one-kill/"),
			new Skin(TextFormatting.DARK_PURPLE+"Winter", "Winter Widowmaker", "Nudle", "https://www.planetminecraft.com/skin/winter-widowmaker/"),
			new Skin(TextFormatting.GOLD+"Huntress", "Ouh La La", "Katalisa", "https://www.planetminecraft.com/skin/huntress-widowmaker/"),
			new Skin(TextFormatting.GOLD+"Cote d'Azur", "Widowmaker: Cote d'Azur", "Althestane", "https://www.planetminecraft.com/skin/widowmaker-c-te-d-azur/"),
			new Skin(TextFormatting.GOLD+"Talon", "Widowmaker Talon Skin - IISavageDreamzII", "StarryDreamz", "https://www.planetminecraft.com/skin/widowmaker-talon-skin-iisavagedreamzii/")),
	MERCY("Mercy", true, true, new Ability(KeyBind.NONE, false, false), 
			new Ability(KeyBind.ABILITY_2, false, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			0, 20, new int[] {2,2,2,2}, new ItemMercyWeapon(), Crosshair.CIRCLE_SMALL, 0xEBE8BB, true, EntityMercy.class, 
			new Skin("Classic", "Overwatch | Mercy", "Efflorescence", "https://www.planetminecraft.com/skin/-overwatch-mercy-/"),
			new Skin("Classic", "Mercy", "FireBoltCreeeper", "https://www.planetminecraft.com/skin/mercy-3684205/"),
			new Skin(TextFormatting.GOLD+"Imp", "Imp Mercy Overwatch", "Aireters", "https://www.planetminecraft.com/skin/-imp-mercy-overwatch/"),
			new Skin(TextFormatting.GOLD+"Winged Victory", "Mercy (Winged Victory) - Overwatch", "Benenwren", "https://www.planetminecraft.com/skin/overwatch-mercy-winged-victory/"),
			new Skin(TextFormatting.GOLD+"Witch", "Witch Mercy [OVERWATCH]", "Nudle", "https://www.planetminecraft.com/skin/witch-mercy-overwatch-mind-the-collar-oops/"),
			new Skin(TextFormatting.GOLD+"Combat Medic", "Combat Medit Ziegler", "Noire_", "https://www.planetminecraft.com/skin/combat-medic-ziegler-3967530/")),
	JUNKRAT("Junkrat", false, false, new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false, 2, 160), 
			new Ability(KeyBind.NONE, false, false), 
			5, 0, new int[] {2,2,2,2}, new ItemJunkratLauncher(), Crosshair.CIRCLE_SMALL, 0xEABB51, true, EntityJunkrat.class, 
			new Skin("Classic", "Overwatch- Junkrat", "Ringoster", "https://www.planetminecraft.com/skin/overwatch--junkrat/"),
			new Skin("Classic", "Everything's coming up explodey! Overwatch - Junkrat", "_Phantom", "https://www.planetminecraft.com/skin/everything-s-coming-up-explodey-overwatch-junkrat/"),
			new Skin(TextFormatting.GOLD+"Scarecrow", "Scarecrow Junkrat- Overwatch", "-CenturianDoctor-", "https://www.planetminecraft.com/skin/scarecrow-junkrat--overwatch/"),
			new Skin(TextFormatting.GOLD+"Dr. Junkenstein", "Dr. Jamison Junkenstein [OVERWATCH]", "Nudle", "https://www.planetminecraft.com/skin/dr-jamison-junkenstein-overwatch/")),
	SOMBRA("Sombra", false, false, new Ability(KeyBind.RMB, false, false), 
			new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, true), 
			60, 0, new int[] {2,2,2,2}, new ItemSombraMachinePistol(), Crosshair.CIRCLE_SMALL, 0x745ABB, true, EntitySombra.class, 
			new Skin("Classic", "Boop!", "Nutellah", "https://www.planetminecraft.com/skin/boop-3851181/"),
			new Skin("Classic", "Virtuality - Sombra [Contest | Overwatch]", "Orbiter", "https://www.planetminecraft.com/skin/virtuality-sombra-contest-overwatch/"),
			new Skin(TextFormatting.DARK_AQUA+"Mar", "Sombra with Mar skin", "XxbalintgamerxX", "http://www.minecraftskins.com/skin/9944115/sombra-with-mar-skin/"),
			new Skin(TextFormatting.GOLD+"Augmented", "Sombra ONLINE - Augmented", "Grinshire", "https://www.planetminecraft.com/skin/sombra-online-augmented/"),
			new Skin(TextFormatting.GOLD+"Cyberspace", "Cyberspace Sombra", "oophelia", "https://www.planetminecraft.com/skin/cyberspace-sombra-3958304/"),
			new Skin(TextFormatting.GOLD+"Tulum", "Sombra Scuba skin ~ Elec", "Elec", "https://www.planetminecraft.com/skin/sombra-scuba-skin-elec-3999189/")),
	LUCIO("Lucio", true, false, new Ability(KeyBind.RMB, true, false), 
			new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			20, 20, new int[] {2,2,2,2}, new ItemLucioSoundAmplifier(), Crosshair.CIRCLE_SMALL, 0x91D618, true, EntityLucio.class, 
			new Skin("Classic", "Lúcio", "Drazile", "https://www.planetminecraft.com/skin/jet-set-tiesto/"),
			new Skin(TextFormatting.DARK_AQUA+"Roxo", "lucio roxo", "electricgeek", "http://www.minecraftskins.com/skin/9502279/lucio-roxo/"),
			new Skin(TextFormatting.DARK_PURPLE+"Andes", "Lucio Andes", "Stuphie", "http://www.minecraftskins.com/skin/10880715/lucio-andes/"),
			new Skin(TextFormatting.GOLD+"HippityHop", "Overwatch - Lúcio", "Drzzter", "https://www.planetminecraft.com/skin/overwatch---lcio-3766449/"),
			new Skin(TextFormatting.GOLD+"Ribbit", "Lucio Overwatch Ribbit", "DoctorMacaroni", "http://www.minecraftskins.com/skin/8719310/lucio-overwatch-ribbit/"),
			new Skin(TextFormatting.GOLD+"Slapshot", "Lucio Slapshot", "BoyBow", "http://www.minecraftskins.com/skin/10709362/lucio-slapshot/"),
			new Skin(TextFormatting.GOLD+"Jazzy", "Jazzy Lucio", "Noire_", "https://www.planetminecraft.com/skin/jazzy-lucio/")),
	ZENYATTA("Zenyatta", false, false, new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			20, 20, new int[] {2,2,2,2}, new ItemZenyattaWeapon(), Crosshair.CIRCLE_SMALL, 0xEDE582, true, EntityZenyatta.class, 
			new Skin("Classic", "Zenyatta (OverWatch)", "Kill3rCreeper", "https://www.planetminecraft.com/skin/zenyatta-overwatch/"),
			new Skin(TextFormatting.DARK_PURPLE+"Ascendant", "Zenyatta Ascendance skin", "brainman", "http://www.minecraftskins.com/skin/10621836/zenyatta-ascendance-skin/"),
			new Skin(TextFormatting.GOLD+"Djinnyatta", "Djinnyatta", "brainman", "http://www.minecraftskins.com/skin/11033097/djinnyatta/"),
			new Skin(TextFormatting.GOLD+"Ifrit", "Zenyatta Ifrit Skin", "brainman", "http://www.minecraftskins.com/skin/10626002/zenyatta-ifrit-skin/"),
			new Skin(TextFormatting.GOLD+"Nutcracker", "Overwatch - Nutcracker Zenyatta", "Drzzter", "https://www.planetminecraft.com/skin/overwatch---nutcracker-zenyatta/"),
			new Skin(TextFormatting.GOLD+"Cultist", "Zenyatta Cultist", "XxLucarioTheNinjaxX", "https://www.planetminecraft.com/skin/zenyatta-cultist/")),
	MOIRA("Moira", false, false, new Ability(KeyBind.NONE, true, false), 
			new Ability(KeyBind.ABILITY_2, true, true), 
			new Ability(KeyBind.ABILITY_1, true, true), 
			0, 0, new int[] {2,2,2,2}, new ItemMoiraWeapon(), Crosshair.CIRCLE_SMALL, 0x7D3E51, true, EntityMoira.class, 
			new Skin("Classic", "MOIRA!!!!!!", "Aegeah", "https://www.planetminecraft.com/skin/moira/"),
			new Skin("Classic", "Moira - Overwatch (Healer version)", "Elec", "https://www.planetminecraft.com/skin/moira-overwatch-healer-version/"),
			new Skin(TextFormatting.GOLD+"Moon", "Moira Moon", "KevinAguirre2", "http://www.minecraftskins.com/skin/11786311/moira-moon/"));

	public static final Handler VOICE_COOLDOWN = new Handler(Identifier.VOICE_COOLDOWN, false) {};

	public Ability ability1;
	public Ability ability2;
	public Ability ability3;

	public final Class heroClass;
	public String name;
	/**index from top of ability_overlay.png for this hero*/
	public int overlayIndex;
	/**index for alternate weapon*/
	public int altWeaponIndex;
	/**if weapon has alt (with different ability icons)*/
	public boolean hasAltWeapon;
	/**if mouse wheel can scroll between weapons*/
	public boolean switchAltWithScroll;
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

	public ModSoundEvents reloadSound;
	public boolean smallArms;
	public Skin[] skinInfo;
	public String[] skinCredits;
	public HashMap<String, Integer> skins = Maps.newHashMap();
	protected Crosshair crosshair;
	public Color color;

	protected static enum Crosshair {
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
		public String originalSkinName;
		public String originalAuthor;
		public String originalAddress;

		private Skin(String owName, String skinName, String author, String address) {
			this(owName, skinName, author, address, null, null, null);
		}

		private Skin(String owName, String skinName, String author, String address, String originalSkinName, String originalAuthor, String originalAddress) {
			this.owName = owName;
			this.skinName = skinName;
			this.author = author;
			this.address = address;
			this.originalSkinName = originalSkinName;
			this.originalAuthor = originalAuthor;
			this.originalAddress = originalAddress;
		}

		/**(skin name) by (author)*/
		public String getCreditText() {
			return TextFormatting.getTextWithoutFormattingCodes(this.skinName+" by "+this.author);
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

	private EnumHero(String name, boolean hasAltWeapon, boolean switchAltWithScroll, Ability ability1, Ability ability2, Ability ability3,
			int mainAmmo, int altAmmo, int[] armorReductionAmounts, ItemMWWeapon weapon, Crosshair crosshair, 
			int color, boolean smallArms, Class heroClass, Skin... skinInfo) {
		this.heroClass = heroClass;
		this.overlayIndex = IndexCounter.index++;
		this.name = name;
		this.hasAltWeapon = hasAltWeapon;
		this.switchAltWithScroll = switchAltWithScroll;
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
		this.color = new Color(color);
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
			if (Minewatch.proxy.getClientUUID() != null && 
					Minewatch.proxy.getClientUUID().toString().equals(uuid.toString()) && 
					!this.skinInfo[skin].getCreditText().equals(prop.getString())) {
				prop.set(this.skinInfo[skin].getCreditText());
				Config.config.save();
			}
		}
	}

	public Ability getAbility(int ability) {
		if (ability == 1)
			return this.ability1;
		else if (ability == 2)
			return this.ability2;
		else if (ability == 3)
			return this.ability3;
		else return this.ability1;
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