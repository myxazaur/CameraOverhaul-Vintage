package ua.myxazaur.vintagecam.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static ua.myxazaur.vintagecam.CameraOverhaul.MOD_ID;

@Config(modid = MOD_ID)
public class COConfig {
    @Config.Name("Camera Config")
    @Config.Comment("Camera effect configuration")
    public static final CameraConfig cameraConfig = new CameraConfig();

    public static class CameraConfig {
        @Config.Comment("Enable camera effects")
        public boolean enabled = true;

        @Config.Comment("Enable effects in third-person view")
        public boolean enableInThirdPerson = true;

        @Config.Comment("Vertical pitch effect intensity from jumping/falling")
        @Config.RangeDouble(min = 0.0, max = 10.0)
        public float verticalPitchFactor = 5.0F;

        @Config.Comment("Vertical movement smoothing factor")
        @Config.RangeDouble(min = 0.0, max = 2.0)
        public float verticalSmoothingFactor = 1.5F;

        @Config.Comment("Forward movement pitch effect intensity")
        @Config.RangeDouble(min = 0.0, max = 10.0)
        public float forwardPitchFactor = 2.0F;

        @Config.Comment("Horizontal movement smoothing factor")
        @Config.RangeDouble(min = 0.0, max = 3.0)
        public float horizontalSmoothingFactor = 3.0F;

        @Config.Comment("Strafing roll effect intensity")
        @Config.RangeDouble(min = 0.0, max = 10.0)
        public float strafeRollFactor = 3.0F;

        @Config.Comment("Turning roll effect intensity")
        @Config.RangeDouble(min = 0.0, max = 5.0)
        public float turningRollIntensity = 3.5F;

        @Config.Comment("Turning roll accumulation factor")
        @Config.RangeDouble(min = 0.0, max = 0.01)
        public float turningRollAccumulation = 0.01F;

        @Config.Comment("Turning roll smoothing factor")
        @Config.RangeDouble(min = 0.0, max = 3.0)
        public float turningRollSmoothing = 1.5F;

        @Config.Comment("Camera sway intensity")
        @Config.RangeDouble(min = 0.0, max = 2.0)
        public float swayIntensity = 0.5F;

        @Config.Comment("Camera sway frequency")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public float swayFrequency = 0.025F;

        @Config.Comment("Delay before sway effect starts (In ticks)")
        @Config.RangeDouble(min = 0.0)
        public float swayFadeDelay = 20.0F;

        @Config.Comment("Sway fade-in duration")
        @Config.RangeDouble(min = 0.0, max = 5.0)
        public float swayFadeInLength = 1.0F;

        @Config.Comment("Sway fade-out duration")
        @Config.RangeDouble(min = 0.0, max = 5.0)
        public float swayFadeOutLength = 0.5F;

        @Config.Comment("Base trauma amount from explosions/lightning bolt strikes")
        @Config.RangeDouble(min = 0.0, max = 5.0)
        public float explosionTraumaBase = 0.8F;

        @Config.Comment("Speed at which explosion trauma recovers")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public float explosionRecoverySpeed = 0.05F;

        @Config.Comment("Maximum angle of camera shake from explosions/lightning bolt strikes")
        @Config.RangeDouble(min = 0.0, max = 180.0)
        public float explosionMaxAngle = 30.0F;

        @Config.Comment("Frequency of camera shake oscillations from explosions/lightning bolt strikes")
        @Config.RangeDouble(min = 0.0, max = 30.0)
        public float explosionFrequency = 5.0F;
    }

    @Mod.EventBusSubscriber(modid = MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(MOD_ID)) {
                ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}