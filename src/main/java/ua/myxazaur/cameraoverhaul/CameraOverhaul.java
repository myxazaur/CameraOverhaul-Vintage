package ua.myxazaur.cameraoverhaul;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import ua.myxazaur.cameraoverhaul.camera.CameraContext;
import ua.myxazaur.cameraoverhaul.camera.CameraSystem;

@Mod(
        modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        clientSideOnly = true,
        guiFactory = "ua.myxazaur.cameraoverhaul.config.CameraConfigGuiFactory"
)
public class CameraOverhaul
{
    public static Logger log;
    public static CameraSystem camera;
    public static CameraContext context;

    public static boolean aquaAcrobatics = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        camera = new CameraSystem();
        context = new CameraContext();

        aquaAcrobatics = Loader.isModLoaded("aquaacrobatics");
    }
}
