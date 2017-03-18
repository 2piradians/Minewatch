package twopiradians.minewatch.common;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import twopiradians.minewatch.creativetab.MinewatchTab;

@Mod(modid = Minewatch.MODID, version = Minewatch.VERSION, name = Minewatch.MODNAME)
public class Minewatch
{
    public static final String MODNAME = "Minewatch";
    public static final String MODID = "minewatch";
    public static final String VERSION = "1.0";
    @Mod.Instance(MODID)
    public static Minewatch instance;
    public static MinewatchTab tab = new MinewatchTab("tabMinewatch");
    @SidedProxy(clientSide = "twopiradians.minewatch.client.ClientProxy", serverSide = "twopiradians.minewatch.common.CommonProxy")
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
