package twopiradians.minewatch.common;

import java.io.File;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import twopiradians.minewatch.common.command.CommandDev;
import twopiradians.minewatch.common.command.CommandMinewatch;
import twopiradians.minewatch.common.hero.RankManager;
import twopiradians.minewatch.creativetab.ArmorWeaponsTab;
import twopiradians.minewatch.creativetab.IMinewatchTab;
import twopiradians.minewatch.creativetab.MapMakingTab;
import twopiradians.minewatch.creativetab.MinewatchTab;

//PORT search for scala library
// 1.10.2: en_US.lang - change entity.blah.name -> entity.minewatch.blah.name
// 1.12.1: add ", acceptedMinecraftVersions="[1.12,1.13)" to @Mod

//Regex: starting with ~819 errors, File Name Patterns: "!Minewatch.java", do in this order
//1.10.2: 
//(Minecraft.getMinecraft\(\)|mc)(.player) -> $1.thePlayer (fixes ~101 errors) 
//(MathHelper.)(floor|sqrt|clamp) -> $1$2_double (fixes ~37 errors)
//((Minecraft.getMinecraft\(\)\.)|(mc\.))world -> $1theWorld (fixes ~14 errors)
//^(?!import)(.*)world(\W.*)$ -> $1worldObj$2 (fixes ~487 errors - repeat until no matches found)
//spawnEntity -> spawnEntityInWorld (fixes ~34 errors)
//ItemStack.EMPTY -> null (fixes ~12 errors)
//(EntityRegistry.registerModEntity\().*?\),\s -> $1 (fixes ~29 errors)
//\.sendMessage\( -> .addChatMessage (fixes ~17 errors)

//1.12:
//.addCoord( -> .expand(
//.expand( -> .grow(
//.move( -> .offset(
@Mod(modid = Minewatch.MODID, version = Minewatch.VERSION, name = Minewatch.MODNAME, guiFactory = "twopiradians.minewatch.client.gui.config.GuiFactory", updateJSON = "https://raw.githubusercontent.com/Furgl/Global-Mod-Info/master/Minewatch/update.json")
public class Minewatch {
	
    public static final String MODNAME = "Minewatch";
    public static final String MODID = "minewatch";
    public static final String VERSION = "3.9";
    @Mod.Instance(MODID)
    public static Minewatch instance;
    public static MinewatchTab tabMinewatch = new MinewatchTab("tabMinewatch");
    public static IMinewatchTab tabArmorWeapons = new ArmorWeaponsTab("tabMinewatchArmorWeapons");
    public static IMinewatchTab tabMapMaking = new MapMakingTab("tabMinewatchMapMaking");
    @SidedProxy(clientSide = "twopiradians.minewatch.client.ClientProxy", serverSide = "twopiradians.minewatch.common.CommonProxy")
	public static CommonProxy proxy;
	public static Logger logger;
    public static SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
	public static File configFile;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
         proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
    
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandMinewatch());
		event.registerServerCommand(new CommandDev());
		RankManager.lookUpRanks();
	}

}