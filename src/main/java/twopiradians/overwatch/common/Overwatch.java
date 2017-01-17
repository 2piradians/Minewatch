package twopiradians.overwatch.common;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import twopiradians.overwatch.creativetab.OverwatchTab;

@Mod(modid = Overwatch.MODID, version = Overwatch.VERSION, name = Overwatch.MODNAME)
public class Overwatch
{
    public static final String MODNAME = "Overwatch";
    public static final String MODID = "overwatch";
    public static final String VERSION = "1.0";
    @Mod.Instance(MODID)
    public static Overwatch instance;
    public static OverwatchTab tab = new OverwatchTab("tabOverwatch");
    @SidedProxy(clientSide = "twopiradians.overwatch.client.ClientProxy", serverSide = "twopiradians.overwatch.common.CommonProxy")
	public static CommonProxy proxy;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
         proxy.preInit();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }
}
