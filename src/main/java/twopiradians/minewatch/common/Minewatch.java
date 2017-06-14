package twopiradians.minewatch.common;

import java.io.File;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import twopiradians.minewatch.client.key.KeyToggleMode;
import twopiradians.minewatch.creativetab.MinewatchTab;

@Mod(modid = Minewatch.MODID, version = Minewatch.VERSION, name = Minewatch.MODNAME, guiFactory = "twopiradians.minewatch.client.gui.config.GuiFactory", updateJSON = "https://raw.githubusercontent.com/2piradians/Minewatch/1.11.2/update.json")
public class Minewatch
{
    public static final String MODNAME = "Minewatch";
    public static final String MODID = "minewatch";
    public static final String VERSION = "2.1";
    @Mod.Instance(MODID)
    public static Minewatch instance;
    public static MinewatchTab tab = new MinewatchTab("tabMinewatch");
    @SidedProxy(clientSide = "twopiradians.minewatch.client.ClientProxy", serverSide = "twopiradians.minewatch.common.CommonProxy")
	public static CommonProxy proxy;
    public static SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
	public static KeyToggleMode keyMode = new KeyToggleMode();
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
}
