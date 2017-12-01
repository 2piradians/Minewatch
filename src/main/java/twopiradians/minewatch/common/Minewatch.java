package twopiradians.minewatch.common;

import java.io.File;

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
import twopiradians.minewatch.creativetab.MinewatchTab;

//PORT change json location, search for scala library
// 1.10.2: en_US.lang - change entity.blah.name -> entity.minewatch.blah.name
// 1.12.1: add ", acceptedMinecraftVersions="[1.12,1.13)" to @Mod
@Mod(modid = Minewatch.MODID, version = Minewatch.VERSION, name = Minewatch.MODNAME, guiFactory = "twopiradians.minewatch.client.gui.config.GuiFactory", updateJSON = "https://raw.githubusercontent.com/2piradians/Minewatch/1.11.2/update.json")
public class Minewatch {
	
    public static final String MODNAME = "Minewatch";
    public static final String MODID = "minewatch";
    public static final String VERSION = "3.6";
    @Mod.Instance(MODID)
    public static Minewatch instance;
    public static MinewatchTab tab = new MinewatchTab("tabMinewatch");
    @SidedProxy(clientSide = "twopiradians.minewatch.client.ClientProxy", serverSide = "twopiradians.minewatch.common.CommonProxy")
	public static CommonProxy proxy;
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
	}

}