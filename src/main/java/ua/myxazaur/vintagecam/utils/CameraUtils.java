package ua.myxazaur.vintagecam.utils;

import ua.myxazaur.vintagecam.config.COConfig;

public class CameraUtils {
    public static class Transform {
        public Vector3d eulerRot = new Vector3d(0.0, 0.0, 0.0);
    }

    public static class ConfigContext {
        public float verticalPitchFactor;
        public float verticalSmoothingFactor;
        public float forwardPitchFactor;
        public float horizontalSmoothingFactor;
        public float strafeRollFactor;
        public float turningRollAccumulation;
        public float turningRollIntensity;
        public float turningRollSmoothing;

        public ConfigContext() {
            reset();
        }

        public void reset() {
            verticalPitchFactor = COConfig.cameraConfig.verticalPitchFactor;
            verticalSmoothingFactor = COConfig.cameraConfig.verticalSmoothingFactor;
            forwardPitchFactor = COConfig.cameraConfig.forwardPitchFactor;
            horizontalSmoothingFactor = COConfig.cameraConfig.horizontalSmoothingFactor;
            strafeRollFactor = COConfig.cameraConfig.strafeRollFactor;
            turningRollAccumulation = COConfig.cameraConfig.turningRollAccumulation;
            turningRollIntensity = COConfig.cameraConfig.turningRollIntensity;
            turningRollSmoothing = COConfig.cameraConfig.turningRollSmoothing;
        }

        public void applySwimmingModifiers() {
            verticalPitchFactor *= 0.5F;
            forwardPitchFactor *= 0.5F;
            strafeRollFactor *= 0.5F;
        }

        public void applyFlyingModifiers() {
            verticalPitchFactor *= 0.7F;
            forwardPitchFactor *= 0.7F;
            strafeRollFactor *= 0.7F;
        }

        public void applyRidingModifiers() {
            verticalPitchFactor *= 0.3F;
            forwardPitchFactor *= 0.3F;
            strafeRollFactor *= 0.3F;
        }
    }
}