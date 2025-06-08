package ua.myxazaur.vintagecam;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.myxazaur.vintagecam.camera.CameraSystem;
import ua.myxazaur.vintagecam.config.COConfig;
import ua.myxazaur.vintagecam.vintagecam.Tags;

@Mod(
        modid = CameraOverhaul.MOD_ID,
        name = CameraOverhaul.NAME,
        version = Tags.VERSION
)
public class CameraOverhaul {
    public static final String MOD_ID = "vintagecam";
    public static final String NAME = "Camera Overhaul (Vintage)";

    private CameraSystem cameraSystem;

    public static final Logger LOGGER = LogManager.getLogger(CameraOverhaul.NAME);

    @Mod.Instance
    public static CameraOverhaul instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        cameraSystem = new CameraSystem();

        MinecraftForge.EVENT_BUS.register(new COConfig());
    }

    public CameraSystem getCameraSystem() {
        return cameraSystem;
    }
}