package modmuss50.HardCoreMapRest.proxy;

import java.io.File;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import modmuss50.HardCoreMapRest.GuiTweaker;
import modmuss50.HardCoreMapRest.MapReset;
import modmuss50.HardCoreMapRest.TemplateSaveLoader;
import modmuss50.HardCoreMapRest.server.PacketOpen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

	@Override
	public void preinit(){
		super.preinit();
		MapReset.networkWrapper.registerMessage(PacketOpen.class, PacketOpen.class, 0, Side.CLIENT);		
	}
	
	@Override
	public void init(){
		super.init();
		MapReset.saveLoader = new TemplateSaveLoader(new File(Minecraft.getMinecraft().mcDataDir, "maps"));

		if (FMLCommonHandler.instance().getSide().isClient()) {
			MinecraftForge.EVENT_BUS.register(new GuiTweaker());
		}				
	}
}
