package ua.myxazaur.cameraoverhaul.config;

import net.minecraftforge.common.config.*;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ua.myxazaur.cameraoverhaul.Tags;
import ua.myxazaur.cameraoverhaul.utils.MathUtils;

@Config(modid = Tags.MOD_ID)
public final class CameraConfig
{
    public static General general = new General();
    public static Contextual walking = new Contextual();
    public static Contextual sprinting = new Contextual(12.5, 9.5, 2.5, 1.0, 1.0, 0.6);
    public static Contextual flying = new Contextual(-10.0, 7.0, 2.5, 1.0, 1.0, 1.0);
    public static Contextual swimming = new Contextual(-30.0, 21.0, 7.5, 1.0, 1.0, 1.5);
    public static Contextual mounts = new Contextual(20.0, 3.5, 2.5, 1.0, 1.0, 1.0);
    public static Contextual vehicles = new Contextual(5.0, 3.5, 5.0, 1.0, 1.0, 0.0);

    public static final class General {
        public boolean enabled = true;
        public boolean enableInThirdPerson = true;
        public double contextTransitionSmoothing = 0.1;

        @Config.Comment({
                "List of entity class names to ignore for camera effects.",
                "Useful for compatibility with mods that use custom camera entities (e.g., mirrors).",
                "You can use partial class names - if the entity's class name CONTAINS any of these strings, it will be ignored.",
                "Examples: 'EntityMirror', 'mrcrayfish', 'Mirror'"
        })
        public String[] entityBlacklist = {
                "com.mrcrayfish.furniture.entity.EntityMirror"
        };

        @Config.Comment("If true, only EntityLivingBase and its subclasses will have camera effects applied.")
        public boolean onlyLivingEntities = true;

        // Turning Roll
        public double turningRollAccumulation = 1.0;
        public double turningRollIntensity = 1.25;
        public double turningRollSmoothing = 1.0;
        // Sway
        public double cameraSwayIntensity = 0.60;
        public double cameraSwayFrequency = 0.16;
        public double cameraSwayFadeInDelay = 0.15;
        public double cameraSwayFadeInLength = 5.0;
        public double cameraSwayFadeOutLength = 0.75;
        // ScreenShakes
        public double screenShakesMaxIntensity = 2.5;
        public double screenShakesMaxFrequency = 6.0;
        public double explosionTrauma = 1.00;
        public boolean scaleExplosionByStrength = true;
        public double thunderTrauma = 0.05;
        public double handSwingTrauma = 0.03;
    }

    public static final class Contextual implements Cloneable {
        public double strafingRollFactor;
        public double forwardVelocityPitchFactor;
        public double verticalVelocityPitchFactor;
        public double horizontalVelocitySmoothingFactor;
        public double verticalVelocitySmoothingFactor;
        public double mouseSmoothing;

        public Contextual() { this(10.0, 7.0, 2.5, 1.0, 1.0, 0.0); }

        public Contextual(double strafe, double forward, double vertical,
                          double hSmooth, double vSmooth, double mouseSmooth) {
            this.strafingRollFactor = strafe;
            this.forwardVelocityPitchFactor = forward;
            this.verticalVelocityPitchFactor = vertical;
            this.horizontalVelocitySmoothingFactor = hSmooth;
            this.verticalVelocitySmoothingFactor = vSmooth;
            this.mouseSmoothing = mouseSmooth;
        }

        public void lerp(Contextual a, Contextual b, double step) {
            strafingRollFactor = MathUtils.lerp(a.strafingRollFactor, b.strafingRollFactor, step);
            forwardVelocityPitchFactor = MathUtils.lerp(a.forwardVelocityPitchFactor, b.forwardVelocityPitchFactor, step);
            verticalVelocityPitchFactor = MathUtils.lerp(a.verticalVelocityPitchFactor, b.verticalVelocityPitchFactor, step);
            horizontalVelocitySmoothingFactor = MathUtils.lerp(a.horizontalVelocitySmoothingFactor, b.horizontalVelocitySmoothingFactor, step);
            verticalVelocitySmoothingFactor = MathUtils.lerp(a.verticalVelocitySmoothingFactor, b.verticalVelocitySmoothingFactor, step);
            mouseSmoothing = MathUtils.lerp(a.mouseSmoothing, b.mouseSmoothing, step);
        }

        public Contextual clone() {
            try { return (Contextual)super.clone(); }
            catch (CloneNotSupportedException e) { return null; }
        }
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static class ConfigSyncHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Tags.MOD_ID)) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}