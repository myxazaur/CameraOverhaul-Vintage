// Copyright 2020-2025 Mirsario & Contributors.
// Released under the GNU General Public License 3.0.
// See LICENSE.md for details.

package ua.myxazaur.cameraoverhaul.config;

import net.minecraftforge.common.config.*;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ua.myxazaur.cameraoverhaul.Tags;

@Config(modid = Tags.MOD_ID)
public final class CameraConfig
{
    public static General general = new General();
    public static Contextual walking = new Contextual();
    public static Contextual swimming = new Contextual();
    public static Contextual flying = new Contextual();
    public static Contextual mounts = new Contextual();
    public static Contextual vehicles = new Contextual();

    public static final class General {
        public boolean enabled = true;
        public boolean enableInThirdPerson = true;

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
        public double thunderTrauma = 0.05;
        public double handSwingTrauma = 0.03;
    }

    public static final class Contextual {
        public double strafingRollFactor = 10.0;
        public double forwardVelocityPitchFactor = 7.0;
        public double verticalVelocityPitchFactor = 2.5;
        public double horizontalVelocitySmoothingFactor = 1.0;
        public double verticalVelocitySmoothingFactor = 1.0;
    }

    static {
        // Flying
        flying.strafingRollFactor *= -1.0;
        // Swimming
        swimming.strafingRollFactor *= -3.0;
        swimming.forwardVelocityPitchFactor *= 3.0;
        swimming.verticalVelocityPitchFactor *= 3.0;
        // Mounts
        mounts.strafingRollFactor *= 2.0;
        mounts.forwardVelocityPitchFactor *= 0.5;
        // Vehicles
        vehicles.strafingRollFactor *= 0.5;
        vehicles.forwardVelocityPitchFactor *= 0.5;
        vehicles.verticalVelocityPitchFactor *= 2.0;
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