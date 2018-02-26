package twopiradians.minewatch.common.hero;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityAna;
import twopiradians.minewatch.common.entity.hero.EntityBastion;
import twopiradians.minewatch.common.entity.hero.EntityDoomfist;
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
import twopiradians.minewatch.common.hero.RankManager.Rank;
import twopiradians.minewatch.common.item.ItemMWToken;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.item.weapon.ItemBastionGun;
import twopiradians.minewatch.common.item.weapon.ItemDoomfistWeapon;
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
	ANA("Ana", Type.SUPPORT, true, true, new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			10, 10, new int[] {2,3,3,2}, new ItemAnaRifle(), Crosshair.CIRCLE_SMALL, 0x6E8AB1, true, EntityAna.class,
			new Skin(Skin.Type.COMMON, "Classic", "Overwatch - Ana", "Drzzter", "https://www.planetminecraft.com/skin/overwatch---ana-shrike/"), 
			new Skin(Skin.Type.COMMON, "Classic", "Until The End - Ana [Overwatch]", "Orbiter", "https://www.planetminecraft.com/skin/until-the-end-ana-overwatch/"),
			new Skin(Skin.Type.EPIC, "Ghoul", "Ana Ghoul Skin", "DaDerpNarwhal", "http://www.minecraftskins.com/skin/11300611/ana-ghoul-skin/"), 
			new Skin(Skin.Type.EPIC, "Merciful", "Ana Merciful", "QuantumQuark", "http://www.minecraftskins.com/skin/11038160/ana-merciful/"), 
			new Skin(Skin.Type.LEGENDARY, "Captain Amari", "Captain Amari", "yana2princess", "http://www.minecraftskins.com/skin/11380464/captain-amari/"),
			new Skin(Skin.Type.EPIC, "Fusion", "Ana [Philadelphia Fusion]", "MeoWero", "https://www.planetminecraft.com/skin/ana-philadelphia-fusion/"),
			new Skin(Skin.Type.LEGENDARY, "Wadjet", "Wadjet Ana", "MrStiv2", "https://www.planetminecraft.com/skin/wadjet-ana/"),
			new Skin(Skin.Type.LEGENDARY, "Wasteland", "WasteLand Ana", "MrStiv2", "https://www.planetminecraft.com/skin/wasteland-ana/"),
			new Skin(Skin.Type.LEGENDARY, "Corsair", "Ana [Corsair]", "MeoWero", "https://www.planetminecraft.com/skin/ana-corsair-4088406/")), 
	GENJI("Genji", Type.OFFENSE, false, false, new Ability(KeyBind.ABILITY_2, true, true), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			24, 0, new int[] {2,3,3,2}, new ItemGenjiShuriken(), Crosshair.CIRCLE_SMALL, 0x95EF42, false, EntityGenji.class, 
			new Skin(Skin.Type.COMMON, "Classic", "Overwatch- Genji", "Ringoster", "https://www.planetminecraft.com/skin/genji-3709302/"), 
			new Skin(Skin.Type.RARE, "Carbon Fiber", "Genji: Carbon Fiber", "EP_Schnellnut", "https://www.planetminecraft.com/skin/genji-carbon-fiber/"), 
			new Skin(Skin.Type.LEGENDARY, "Young Genji", "Young Genji", "Aegeah", "https://www.planetminecraft.com/skin/young-genji/"), 
			new Skin(Skin.Type.LEGENDARY, "Blackwatch", "GENJI - BLACKWATCH! [Overwatch]", "Thinkingz", "https://www.planetminecraft.com/skin/genji---blackwatch-overwatch/"), 
			new Skin(Skin.Type.LEGENDARY, "Sentai", "Sentai Genji", "Blastronaut360", "http://www.minecraftskins.com/skin/11247630/sentai-genji/"),
			new Skin(Skin.Type.LEGENDARY, "Nomad", "Nomad Genji Overwatch", "Aireters", "https://www.planetminecraft.com/skin/-nomad-genji-overwatch/"),
			new Skin(Skin.Type.LEGENDARY, "Oni", "Oni Genji Skin", "DaDerpNarwhal", "http://www.minecraftskins.com/skin/11298711/oni-genji-skin/"),
			new Skin(Skin.Type.LEGENDARY, "Baihu", "Genji [Baihu]", "MeoWero", "https://www.planetminecraft.com/skin/genji-baihu/")),
	HANZO("Hanzo", Type.DEFENSE, false, false, new Ability(KeyBind.ABILITY_2, true, true), 
			new Ability(KeyBind.ABILITY_1, true, true), 
			new Ability(KeyBind.NONE, false, false), 
			0, 0, new int[] {2,3,3,2}, new ItemHanzoBow(), Crosshair.BOW, 0xB6B589, false, EntityHanzo.class, 
			new Skin(Skin.Type.COMMON, "Classic", "Overwatch- Hanzo", "Ringoster", "https://www.planetminecraft.com/skin/overwatch--hanzo/"), 
			new Skin(Skin.Type.LEGENDARY, "Cyber Ninja", "Cyber Ninja Hanzo", "Arctrooper7802", "http://www.minecraftskins.com/skin/11071427/cyber-ninja-hanzo/"), 
			new Skin(Skin.Type.LEGENDARY, "Lone Wolf", "Hanzo, Lone Wolf | Overwatch", "Cayde - 6", "https://www.planetminecraft.com/skin/hanzo-lone-wolf-overwatch/"), 
			new Skin(Skin.Type.LEGENDARY, "Okami", "Okami Hanzo (OW)", "SublimePNG", "https://www.planetminecraft.com/skin/okami-hanzo-ow/"),
			new Skin(Skin.Type.LEGENDARY, "Casual", "Casual Hanzo", "gab51299", "https://www.planetminecraft.com/skin/casual-hanzo/"),
			new Skin(Skin.Type.EPIC, "Demon", "Demon Hanzo", "Estevaosp", "https://www.planetminecraft.com/skin/demon-hanzo-4069126/", "Overwatch- Hanzo", "Ringoster", "https://www.planetminecraft.com/skin/overwatch--hanzo/"),
			new Skin(Skin.Type.LEGENDARY, "Kabuki", "Kabuki Hanzo", "Spartaculs", "https://www.planetminecraft.com/skin/kabuki-hanzo/")),
	MCCREE("McCree", Type.OFFENSE, false, false, new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			6, 0, new int[] {2,3,3,2}, new ItemMcCreeGun(), Crosshair.CIRCLE_SMALL, 0xAF595C, false, EntityMcCree.class, 
			new Skin(Skin.Type.COMMON, "Classic", "im yer huckleberry | Jesse McCree", "PlantyBox", "https://www.planetminecraft.com/skin/im-yer-huckleberry-jesse-mccree/"),
			new Skin(Skin.Type.COMMON, "Classic", "it's high noon", "HazelOrb", "https://www.planetminecraft.com/skin/its-high-noon/"),
			new Skin(Skin.Type.LEGENDARY, "Riverboat", "Overwatch - McCree (Riverboat)", "Ford", "https://www.planetminecraft.com/skin/overwatch-mccree-riverboat/"),
			new Skin(Skin.Type.LEGENDARY, "Blackwatch", "BlackWatch McCree", "12TheDoctor12", "http://www.minecraftskins.com/skin/10858794/blackwatch-mccree/"),
			new Skin(Skin.Type.LEGENDARY, "Lifeguard", "Lifeguard McCree", "OP_Beast", "https://www.planetminecraft.com/skin/lifeguard-mccree/")),
	REAPER("Reaper", Type.OFFENSE, false, false, new Ability(KeyBind.ABILITY_2, true, true), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			8, 0, new int[] {2,3,3,2}, new ItemReaperShotgun(), Crosshair.CIRCLE_BIG, 0x793E50, false, EntityReaper.class, 
			new Skin(Skin.Type.COMMON, "Classic", "Reaper [Overwatch]", "Aegeah", "https://www.planetminecraft.com/skin/reaper-overwatch-3670094/"), 
			new Skin(Skin.Type.COMMON, "Classic", "Reaper (PlayOfTheGame)", "_Phantom", "https://www.planetminecraft.com/skin/reaper-playofthegame-overwatch/"),
			new Skin(Skin.Type.COMMON, "Classic", "Reaper | Overwatch", "Cayde - 6", "https://www.planetminecraft.com/skin/reaper-overwatch-3652548/"),
			new Skin(Skin.Type.EPIC, "Shiver", "Reaper shiver holiday skin ( Overwatch)", "Hiccup415", "https://www.planetminecraft.com/skin/reaper-shiver-holiday-skin-overwatch/"), 
			new Skin(Skin.Type.LEGENDARY, "Mariachi", "Mariachi skin Reaper (OverWatch)", "Roostinator", "https://www.planetminecraft.com/skin/mariachi-skin-overwatch/"),
			new Skin(Skin.Type.LEGENDARY, "Blackwatch Reyes", "Blackwatch Reyes", "Razmoto", "https://www.planetminecraft.com/skin/blackwatch-reyes/"),
			new Skin(Skin.Type.LEGENDARY, "Dracula", "Dracula Reaper Skin", "DaDerpNarwhal", "https://www.planetminecraft.com/skin/dracula-reaper-skin/"),
			new Skin(Skin.Type.LEGENDARY, "Blackwatch Reyes", "Gabriel Reyes [Reaper] from Overwatch", "Sargumilien", "https://www.planetminecraft.com/skin/gabriel-reyes-reaper-from-overwatch/")),
	REINHARDT("Reinhardt", Type.TANK, false, false, new Ability(KeyBind.RMB, false, false), 
			new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			0, 0, new int[] {4,6,6,4}, new ItemReinhardtHammer(), Crosshair.CIRCLE_SMALL, 0x919EA4, false, EntityReinhardt.class, 
			new Skin(Skin.Type.COMMON, "Classic", "Overwatch Reinhardt","Kohicup", "https://www.planetminecraft.com/skin/overwatch-reinhardt/"),
			new Skin(Skin.Type.EPIC, "Coldhardt", "ColdHardt", "jay_zx_jc", "https://www.planetminecraft.com/skin/coldhardt/"),
			new Skin(Skin.Type.LEGENDARY, "Lionhardt", "LionHardt Reinhardt", "ReinhardtWillhelm", "http://www.minecraftskins.com/skin/8764321/lionhardt-reinhardt/"),
			new Skin(Skin.Type.LEGENDARY, "Stonehardt", "Reinhardt - Overwatch", "Baccup", "https://www.planetminecraft.com/skin/reinhardt---overwatch/"),
			new Skin(Skin.Type.LEGENDARY, "Balderich", "Balderich", "TheGuardian755", "http://www.minecraftskins.com/skin/10356345/balderich/"),
			new Skin(Skin.Type.LEGENDARY, "Blackhardt", "Blackhardt Reinhardt", "SpookiOrange", "https://www.planetminecraft.com/skin/blackhardt-reinhardt/")),
	SOLDIER76("Soldier76", Type.OFFENSE, false, false, new Ability(KeyBind.RMB, true, false), 
			new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.NONE, true, true), 
			25, 0, new int[] {2,3,3,2}, new ItemSoldier76Gun(), Crosshair.PLUS, 0x6A7895, false, EntitySoldier76.class, 
			new Skin(Skin.Type.COMMON, "Classic", "Soldier 76 (Overwatch)", "sixfootblue", "https://www.planetminecraft.com/skin/soldier-76-overwatch-3819528/"),
			new Skin(Skin.Type.COMMON, "Classic", "Soldier 76", "Knap", "https://www.planetminecraft.com/skin/soldier-76-3820018/"),
			new Skin(Skin.Type.RARE, "Smoke", "smoke update", "Shadowstxr", "http://www.minecraftskins.com/skin/9559771/smoke-update/"),
			new Skin(Skin.Type.EPIC, "Golden", "Golden Soldier 76", "riddler55", "http://www.minecraftskins.com/skin/10930005/golden-soldier-76/"),
			new Skin(Skin.Type.EPIC, "Bone", "Soldier 76 Bone SKin", "BagelSki", "http://www.minecraftskins.com/skin/9737491/soldier-76-bone-skin/"),
			new Skin(Skin.Type.LEGENDARY, "Strike Commander", "Strike Commander Morrison - Soldier 76 - Overwatch", "Obvial", "https://www.planetminecraft.com/skin/strike-commander-morrison-3938568/"),
			new Skin(Skin.Type.LEGENDARY, "Grill Master: 76", "Grill Master 76 (Soldier 76 Summer Games 2017)", "InfamousHN", "https://www.planetminecraft.com/skin/grill-master-76-soldier-76-summer-games-2017/")),
	TRACER("Tracer", Type.OFFENSE, false, false, new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false, 3, 60), 
			new Ability(KeyBind.NONE, false, false), 
			40, 0, new int[] {2,2,2,2}, new ItemTracerPistol(), Crosshair.CIRCLE_SMALL, 0xD89441, true, EntityTracer.class, 
			new Skin(Skin.Type.COMMON, "Classic", "Tracer- Overwatch", "Ringoster", "https://www.planetminecraft.com/skin/tracer--overwatch-feat-19-transparency/"),
			new Skin(Skin.Type.LEGENDARY, "Graffiti", "Graffiti Tracer (Overwatch)", "RyutoMatsuki", "https://www.planetminecraft.com/skin/graffiti-tracer-overwatch-better-in-preview-3982890/"),
			new Skin(Skin.Type.LEGENDARY, "Slipstream", "Overwatch - Slipstream Tracer", "WeegeeTheLucario", "https://www.planetminecraft.com/skin/slipstream-tracer/"),
			new Skin(Skin.Type.LEGENDARY, "Ultraviolet", "[Overwatch] Tracer ~Ultraviolet Skin~", "Vamp1re_", "https://www.planetminecraft.com/skin/overwatch-tracer-ultraviolet-skin/"),
			new Skin(Skin.Type.LEGENDARY, "Cadet Oxton", "Overwatch - Cadet Oxton", "WeegeeTheLucario", "https://www.planetminecraft.com/skin/overwatch-cadet-oxton/"),
			new Skin(Skin.Type.LEGENDARY, "Jingle", "Tracer Jingle", "salmanalansarii", "http://www.minecraftskins.com/skin/10175651/tracer-jingle/")),
	BASTION("Bastion", Type.DEFENSE, true, false, new Ability(KeyBind.RMB, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			25, 300, new int[] {2,3,3,2}, new ItemBastionGun(), Crosshair.PLUS, 0x7A8D79, false, EntityBastion.class,
			new Skin(Skin.Type.COMMON, "Classic", "Bastion- Overwatch", "Ringoster", "https://www.planetminecraft.com/skin/bastion--overwatch/"),
			new Skin(Skin.Type.EPIC, "Omnic Crisis", "Bastion Omnic Crisis", "LegitNickname", "http://www.minecraftskins.com/skin/10155984/bastion-omnic-crisis/"),
			new Skin(Skin.Type.EPIC, "Blizzcon 2016", "Blizcon Bastion HD", "LegitNickname", "http://www.minecraftskins.com/skin/10221741/blizcon-bastion-hd/"),
			new Skin(Skin.Type.EPIC, "Tombstone", "HD tombstone bastion", "LegitNickname", "http://www.minecraftskins.com/skin/10225172/hd-tombstone-bastion/"),
			new Skin(Skin.Type.LEGENDARY, "Overgrown", "The last Bastion", "MikKurt", "http://www.minecraftskins.com/skin/10601249/the-last-bastion/"),
			new Skin(Skin.Type.LEGENDARY, "Dune Buggy", "Dunebuggy Bastion", "SpookiOrange", "https://www.planetminecraft.com/skin/dunebuggy-bastion/")), 
	MEI("Mei", Type.DEFENSE, false, false, new Ability(KeyBind.ABILITY_2, false, false), 
			new Ability(KeyBind.ABILITY_1, true, true), 
			new Ability(KeyBind.NONE, false, false), 
			200, 0, new int[] {2,3,3,2}, new ItemMeiBlaster(), Crosshair.CIRCLE_SMALL, 0x6BA8E7, true, EntityMei.class, 
			new Skin(Skin.Type.COMMON, "Classic", "A-Mei-Zing! ...get it? 'cause Mei..", "oEffy", "https://www.planetminecraft.com/skin/a-mei-zing-get-it-cause-mei/"),
			new Skin(Skin.Type.LEGENDARY, "Mei-rry", "Mei-Rry", "KevinAguirre2", "http://www.minecraftskins.com/skin/11709782/mei-rry/", "A-Mei-Zing! ...get it? 'cause Mei..", "oEffy", "https://www.planetminecraft.com/skin/a-mei-zing-get-it-cause-mei/"),
			new Skin(Skin.Type.LEGENDARY, "Luna", "Mei v2", "nikita505n", "http://www.minecraftskins.com/skin/11711832/mei-v2/"),
			new Skin(Skin.Type.LEGENDARY, "Jiangshi", "Mei Jiangshi", "KevinAguirre2", "http://www.minecraftskins.com/skin/11720409/mei-jiangshi/", "A-Mei-Zing! ...get it? 'cause Mei..", "oEffy", "https://www.planetminecraft.com/skin/a-mei-zing-get-it-cause-mei/")),
	WIDOWMAKER("Widowmaker", Type.DEFENSE, false, false, new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			30, 0, new int[] {2,3,3,2}, new ItemWidowmakerRifle(), Crosshair.CIRCLE_SMALL, 0x9A68A3, true, EntityWidowmaker.class, 
			new Skin(Skin.Type.COMMON, "Classic", "Widowmaker - Overwatch: 1.8 Skin, Female", "sir-connor", "https://www.planetminecraft.com/skin/widowmaker---overwatch-18-skin-female/"),
			new Skin(Skin.Type.COMMON, "Classic", "Widowmaker (Overwatch) ... ONE SHOT, ONE KILL", "KAWAI_Murderer", "https://www.planetminecraft.com/skin/widowmaker-overwatch-one-shot-one-kill/"),
			new Skin(Skin.Type.EPIC, "Winter", "Winter Widowmaker", "Nudle", "https://www.planetminecraft.com/skin/winter-widowmaker/"),
			new Skin(Skin.Type.LEGENDARY, "Huntress", "Ouh La La", "Katalisa", "https://www.planetminecraft.com/skin/huntress-widowmaker/"),
			new Skin(Skin.Type.LEGENDARY, "Cote d'Azur", "Widowmaker: Cote d'Azur", "Althestane", "https://www.planetminecraft.com/skin/widowmaker-c-te-d-azur/"),
			new Skin(Skin.Type.LEGENDARY, "Talon", "Widowmaker Talon Skin - IISavageDreamzII", "StarryDreamz", "https://www.planetminecraft.com/skin/widowmaker-talon-skin-iisavagedreamzii/"),
			new Skin(Skin.Type.LEGENDARY, "Nova", "Widowmaker [Nova]", "MeoWero", "https://www.planetminecraft.com/skin/widowmaker-nova/")),
	MERCY("Mercy", Type.SUPPORT, true, true, new Ability(KeyBind.NONE, false, false), 
			new Ability(KeyBind.ABILITY_2, false, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			0, 20, new int[] {2,2,2,2}, new ItemMercyWeapon(), Crosshair.CIRCLE_SMALL, 0xEBE8BB, true, EntityMercy.class, 
			new Skin(Skin.Type.COMMON, "Classic", "Overwatch | Mercy", "Efflorescence", "https://www.planetminecraft.com/skin/-overwatch-mercy-/"),
			new Skin(Skin.Type.COMMON, "Classic", "Mercy", "FireBoltCreeeper", "https://www.planetminecraft.com/skin/mercy-3684205/"),
			new Skin(Skin.Type.LEGENDARY, "Imp", "Imp Mercy Overwatch", "Aireters", "https://www.planetminecraft.com/skin/-imp-mercy-overwatch/"),
			new Skin(Skin.Type.LEGENDARY, "Winged Victory", "Mercy (Winged Victory) - Overwatch", "Benenwren", "https://www.planetminecraft.com/skin/overwatch-mercy-winged-victory/"),
			new Skin(Skin.Type.LEGENDARY, "Witch", "Witch Mercy [OVERWATCH]", "Nudle", "https://www.planetminecraft.com/skin/witch-mercy-overwatch-mind-the-collar-oops/"),
			new Skin(Skin.Type.LEGENDARY, "Combat Medic", "Combat Medit Ziegler", "Noire_", "https://www.planetminecraft.com/skin/combat-medic-ziegler-3967530/"),
			new Skin(Skin.Type.LEGENDARY, "Devil", "MercyDevil", "MetalGearDva1", "https://www.planetminecraft.com/skin/mercydevil/")),
	JUNKRAT("Junkrat", Type.DEFENSE, false, false, new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false, 2, 160), 
			new Ability(KeyBind.NONE, false, false), 
			5, 0, new int[] {2,2,2,2}, new ItemJunkratLauncher(), Crosshair.CIRCLE_SMALL, 0xEABB51, true, EntityJunkrat.class, 
			new Skin(Skin.Type.COMMON, "Classic" , "Overwatch- Junkrat", "Ringoster", "https://www.planetminecraft.com/skin/overwatch--junkrat/"),
			new Skin(Skin.Type.COMMON, "Classic" , "Everything's coming up explodey! Overwatch - Junkrat", "_Phantom", "https://www.planetminecraft.com/skin/everything-s-coming-up-explodey-overwatch-junkrat/"),
			new Skin(Skin.Type.LEGENDARY, "Scarecrow", "Scarecrow Junkrat- Overwatch", "-CenturianDoctor-", "https://www.planetminecraft.com/skin/scarecrow-junkrat--overwatch/"),
			new Skin(Skin.Type.LEGENDARY, "Dr. Junkenstein", "Dr. Jamison Junkenstein [OVERWATCH]", "Nudle", "https://www.planetminecraft.com/skin/dr-jamison-junkenstein-overwatch/"),
			new Skin(Skin.Type.EPIC, "Caution", "Junkrat Danger", "Athenas123", "http://www.minecraftskins.com/skin/11911251/junkrat-danger/", "Overwatch- Junkrat", "Ringoster", "https://www.planetminecraft.com/skin/overwatch--junkrat/"),
			new Skin(Skin.Type.LEGENDARY, "Beachrat", "Beachrat - Overwatch Junkrat Skin", "Beanie", "https://www.planetminecraft.com/skin/beachrat-overwatch-junkrat-skin/"),
			new Skin(Skin.Type.LEGENDARY, "Jester", "Junkrat Fool", "GEDEE", "http://www.minecraftskins.com/skin/11885436/junkrat-fool/")),
	SOMBRA("Sombra", Type.OFFENSE, false, false, new Ability(KeyBind.RMB, true, false), 
			new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, true), 
			60, 0, new int[] {2,2,2,2}, new ItemSombraMachinePistol(), Crosshair.CIRCLE_SMALL, 0x745ABB, true, EntitySombra.class, 
			new Skin(Skin.Type.COMMON, "Classic" , "Boop!", "Nutellah", "https://www.planetminecraft.com/skin/boop-3851181/"),
			new Skin(Skin.Type.COMMON, "Classic" , "Virtuality - Sombra [Contest | Overwatch]", "Orbiter", "https://www.planetminecraft.com/skin/virtuality-sombra-contest-overwatch/"),
			new Skin(Skin.Type.RARE, "Mar", "Sombra with Mar skin", "XxbalintgamerxX", "http://www.minecraftskins.com/skin/9944115/sombra-with-mar-skin/"),
			new Skin(Skin.Type.EPIC, "Peppermint", "Sombra Peppermint - Elec", "Elec", "https://www.planetminecraft.com/skin/sombra-peppermint-elec/"),
			new Skin(Skin.Type.LEGENDARY, "Augmented", "Sombra ONLINE - Augmented", "Grinshire", "https://www.planetminecraft.com/skin/sombra-online-augmented/"),
			new Skin(Skin.Type.LEGENDARY, "Cyberspace", "Cyberspace Sombra", "oophelia", "https://www.planetminecraft.com/skin/cyberspace-sombra-3958304/"),
			new Skin(Skin.Type.LEGENDARY, "Tulum", "Sombra Scuba skin ~ Elec", "Elec", "https://www.planetminecraft.com/skin/sombra-scuba-skin-elec-3999189/"),
			new Skin(Skin.Type.EPIC, "Glitch", "Sombra [Glitch]", "MeoWero", "https://www.planetminecraft.com/skin/sombra-glitch/")),
	LUCIO("Lucio", Type.SUPPORT, true, false, new Ability(KeyBind.RMB, true, false), 
			new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			20, 20, new int[] {2,2,2,2}, new ItemLucioSoundAmplifier(), Crosshair.CIRCLE_SMALL, 0x91D618, true, EntityLucio.class, 
			new Skin(Skin.Type.COMMON, "Classic" , "L�cio", "Drazile", "https://www.planetminecraft.com/skin/jet-set-tiesto/"),
			new Skin(Skin.Type.RARE, "Roxo", "lucio roxo", "electricgeek", "http://www.minecraftskins.com/skin/9502279/lucio-roxo/"),
			new Skin(Skin.Type.EPIC, "Andes", "Lucio Andes", "Stuphie", "http://www.minecraftskins.com/skin/10880715/lucio-andes/"),
			new Skin(Skin.Type.LEGENDARY, "HippityHop", "Overwatch - L�cio", "Drzzter", "https://www.planetminecraft.com/skin/overwatch---lcio-3766449/"),
			new Skin(Skin.Type.LEGENDARY, "Ribbit", "Lucio Overwatch Ribbit", "DoctorMacaroni", "http://www.minecraftskins.com/skin/8719310/lucio-overwatch-ribbit/"),
			new Skin(Skin.Type.LEGENDARY, "Slapshot", "Lucio Slapshot", "BoyBow", "http://www.minecraftskins.com/skin/10709362/lucio-slapshot/"),
			new Skin(Skin.Type.LEGENDARY, "Jazzy", "Jazzy Lucio", "Noire_", "https://www.planetminecraft.com/skin/jazzy-lucio/"),
			new Skin(Skin.Type.EPIC, "Auditiva", "Auditiva Lucio", "ZeroxTHF", "https://www.planetminecraft.com/skin/auditiva-lucio/")),
	ZENYATTA("Zenyatta", Type.SUPPORT, false, false, new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			new Ability(KeyBind.NONE, false, false), 
			20, 20, new int[] {2,2,2,2}, new ItemZenyattaWeapon(), Crosshair.CIRCLE_SMALL, 0xEDE582, true, EntityZenyatta.class, 
			new Skin(Skin.Type.COMMON, "Classic" , "Zenyatta (OverWatch)", "Kill3rCreeper", "https://www.planetminecraft.com/skin/zenyatta-overwatch/"),
			new Skin(Skin.Type.EPIC, "Ascendant", "Zenyatta Ascendance skin", "brainman", "http://www.minecraftskins.com/skin/10621836/zenyatta-ascendance-skin/"),
			new Skin(Skin.Type.LEGENDARY, "Djinnyatta", "Djinnyatta", "brainman", "http://www.minecraftskins.com/skin/11033097/djinnyatta/"),
			new Skin(Skin.Type.LEGENDARY, "Ifrit", "Zenyatta Ifrit Skin", "brainman", "http://www.minecraftskins.com/skin/10626002/zenyatta-ifrit-skin/"),
			new Skin(Skin.Type.LEGENDARY, "Nutcracker", "Overwatch - Nutcracker Zenyatta", "Drzzter", "https://www.planetminecraft.com/skin/overwatch---nutcracker-zenyatta/"),
			new Skin(Skin.Type.LEGENDARY, "Cultist", "Zenyatta Cultist", "XxLucarioTheNinjaxX", "https://www.planetminecraft.com/skin/zenyatta-cultist/"),
			new Skin(Skin.Type.LEGENDARY, "Sunyatta", "Zenyatta [Sunyatta]", "MeoWero", "https://www.planetminecraft.com/skin/zenyatta-sunyatta/")),
	MOIRA("Moira", Type.SUPPORT, false, false, new Ability(KeyBind.NONE, true, false), 
			new Ability(KeyBind.ABILITY_2, true, true), 
			new Ability(KeyBind.ABILITY_1, true, true), 
			0, 0, new int[] {2,2,2,2}, new ItemMoiraWeapon(), Crosshair.CIRCLE_SMALL, 0x7D3E51, true, EntityMoira.class, 
			new Skin(Skin.Type.COMMON, "Classic" , "MOIRA!!!!!!", "Aegeah", "https://www.planetminecraft.com/skin/moira/"),
			new Skin(Skin.Type.COMMON, "Classic" , "Moira - Overwatch (Healer version)", "Elec", "https://www.planetminecraft.com/skin/moira-overwatch-healer-version/"),
			new Skin(Skin.Type.LEGENDARY, "Moon", "Moira Moon", "KevinAguirre2", "http://www.minecraftskins.com/skin/11786311/moira-moon/")),
	DOOMFIST("Doomfist", Type.OFFENSE, false, false, new Ability(KeyBind.RMB, true, false), 
			new Ability(KeyBind.ABILITY_2, true, false), 
			new Ability(KeyBind.ABILITY_1, true, false), 
			4, 4, new int[] {2,2,2,2}, new ItemDoomfistWeapon(), Crosshair.CIRCLE_SMALL, 0x83524B, false, EntityDoomfist.class, 
			new Skin(Skin.Type.COMMON, "Classic" , "Doomfist", "gab51299", "https://www.planetminecraft.com/skin/doomfist/"),
			new Skin(Skin.Type.COMMON, "Classic" , "Doomfist", "HazelOrb", "https://www.planetminecraft.com/skin/doomfist-3979249/"),
			new Skin(Skin.Type.LEGENDARY, "Spirit", "Doomfist Spirit", "CorduroyCorn", "http://www.minecraftskins.com/skin/11239728/doomfist-spirit/"));

	public enum Type {
		OFFENSE, DEFENSE, TANK, SUPPORT;
	}

	public static final Handler VOICE_COOLDOWN = new Handler(Identifier.VOICE_COOLDOWN, false) {};
	public static final ArrayList<EnumHero> ORDERED_HEROES = Lists.newArrayList(DOOMFIST, GENJI, MCCREE, null, REAPER, SOLDIER76, SOMBRA, TRACER, BASTION, HANZO, JUNKRAT, MEI, null, WIDOWMAKER, null, null, REINHARDT, null, null, null, ANA, LUCIO, MERCY, MOIRA, null, ZENYATTA);;
	public static final ResourceLocation PORTRAIT_OVERLAY_0 = new ResourceLocation(Minewatch.MODID+":textures/gui/hero_select_portrait_overlay_0.png");
	public static final ResourceLocation PORTRAIT_OVERLAY_1 = new ResourceLocation(Minewatch.MODID+":textures/gui/hero_select_portrait_overlay_1.png");

	public Ability ability1;
	public Ability ability2;
	public Ability ability3;

	public final Class heroClass;
	public String name;
	public Type type;
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
	public ModSoundEvents selectSound;
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
		public enum Type {
			COMMON(null), RARE(TextFormatting.DARK_AQUA), EPIC(TextFormatting.DARK_PURPLE), LEGENDARY(TextFormatting.GOLD);

			@Nullable
			public TextFormatting format;

			private Type(@Nullable TextFormatting format) {
				this.format = format;
			}
		}

		public Type type;
		public String owName;
		public String skinName;
		public String author;
		public String address;
		public String originalSkinName;
		public String originalAuthor;
		public String originalAddress;

		private Skin(Type type, String owName, String skinName, String author, String address) {
			this(type, owName, skinName, author, address, null, null, null);
		}

		private Skin(Type type, String owName, String skinName, String author, String address, String originalSkinName, String originalAuthor, String originalAddress) {
			this.type = type;
			this.owName = owName;
			this.skinName = skinName;
			this.author = author;
			this.address = address;
			this.originalSkinName = originalSkinName;
			this.originalAuthor = originalAuthor;
			this.originalAddress = originalAddress;
		}

		/**type.format + owName.toUpperCase()*/
		public String getOWName() {
			if (type != null && type.format != null)
				return type.format + owName.toUpperCase();
			else
				return owName.toUpperCase();
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

	private EnumHero(String name, Type type, boolean hasAltWeapon, boolean switchAltWithScroll, Ability ability1, Ability ability2, Ability ability3,
			int mainAmmo, int altAmmo, int[] armorReductionAmounts, ItemMWWeapon weapon, Crosshair crosshair, 
			int color, boolean smallArms, Class heroClass, Skin... skinInfo) {
		this.heroClass = heroClass;
		this.type = type;
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

	@SideOnly(Side.CLIENT)
	public void displayInfoScreen(double width, double height) {
		GlStateManager.pushMatrix();
		GlStateManager.disableDepth();

		GlStateManager.scale(width/256d, height/256d, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/info_background.png"));
		GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 1920, 1080, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+this.name.toLowerCase()+"_info.png"));
		GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 1920, 1080, 0);

		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}

	/**Get formatted name - like Lúcio and Soldier: 76*/
	public String getFormattedName(boolean allCaps) {
		String name = this.name;
		if (this == EnumHero.SOLDIER76)
			name = "Soldier: 76";
		else if (this == EnumHero.LUCIO)
			name = allCaps ? "L\u00DACIO": "L\u00FAcio";

		if (allCaps)
			name = name.toUpperCase();
		return name;
	}

	@SideOnly(Side.CLIENT)
	public static void displayPortrait(EnumHero hero, double x, double y, boolean useAlpha, boolean oldPortrait) {
		Minecraft mc = Minecraft.getMinecraft();

		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);

		Rank rank = RankManager.getHighestRank(mc.player);
		GlStateManager.translate(x, y, 0);
		if (oldPortrait)
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/icon_background.png"));
		else
			mc.getTextureManager().bindTexture(rank.iconLoc);
		GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 240, 240, 0);

		if (useAlpha) {
			// draw stencil
			GlStateManager.alphaFunc(GL11.GL_GREATER, hero == JUNKRAT ? 0.4f : 0.25F);
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			GL11.glStencilMask(0xFF); // writing on
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT); // flush old data
			GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF); // always add to buffer
			GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_REPLACE); // replace on success
			GlStateManager.colorMask(false, false, false, false); // don't draw this
			mc.getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+hero.name.toLowerCase()+"_icon.png"));
			GuiUtils.drawTexturedModalRect(-7, -20, 0, 0, 240, 230, 0);
			GlStateManager.colorMask(true, true, true, true);
			GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // anything written to buffer will be drawn
			GL11.glStencilMask(0x00); // writing off
			GlStateManager.color(1f, 1f, 1, 0.8f);
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
		}

		mc.getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/"+hero.name.toLowerCase()+"_icon.png"));
		GuiUtils.drawTexturedModalRect(-7, -20, 0, 0, 240, 230, 0);

		if (useAlpha) {
			GlStateManager.color(1f, 1f, 1, 0.2f);
			mc.getTextureManager().bindTexture(PORTRAIT_OVERLAY_1);
			GuiUtils.drawTexturedModalRect(-7, -20, 0, (int) (-mc.player.ticksExisted*0.5d), 240, 230, 0);

			GlStateManager.color(1f, 1f, 1, 0.5f);
			mc.getTextureManager().bindTexture(PORTRAIT_OVERLAY_0);
			GuiUtils.drawTexturedModalRect(-7, -20, 0, -mc.player.ticksExisted*3, 240, 230, 0);

			GL11.glDisable(GL11.GL_STENCIL_TEST);
		}

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.popMatrix();
	}

}