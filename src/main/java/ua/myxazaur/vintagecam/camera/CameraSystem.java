package ua.myxazaur.vintagecam.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.lwjgl.opengl.GL11;
import ua.myxazaur.vintagecam.utils.*;

import static ua.myxazaur.vintagecam.config.COConfig.cameraConfig;

public class CameraSystem {
    private final ShakeSystem shakeSystem = new ShakeSystem();
    private final CameraUtils.ConfigContext config = new CameraUtils.ConfigContext();
    private final CameraUtils.Transform transform = new CameraUtils.Transform();
    private final Vector3d prevVelocity = new Vector3d();
    private final Vector3d prevEulerRot = new Vector3d();
    private float lastActionTime = 0.0F;
    private boolean wasFirstPerson = true;
    private double prevVerticalPitchOffset = 0.0;
    private double prevForwardPitchOffset = 0.0;
    private double prevStrafingRollOffset = 0.0;
    private double turningRollTargetOffset = 0.0;
    private double cameraSwayFactor = 0.0;
    private double cameraSwayFactorTarget = 0.0;

    private static final double BASE_VERTICAL_PITCH_SMOOTHING = 0.05;
    private static final double BASE_FORWARD_PITCH_SMOOTHING = 0.008;
    private static final double BASE_STRAFING_ROLL_SMOOTHING = 0.015;
    private static final double BASE_TURNING_ROLL_SMOOTHING = 0.15;
    private static final double CAMERASWAY_FADING_SMOOTHNESS = 3.0;

    public void updateCamera(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        if (player == null || !cameraConfig.enabled ||
                (!cameraConfig.enableInThirdPerson && mc.gameSettings.thirdPersonView != 0)) {
            return;
        }

        float time = (float) mc.world.getWorldTime() + partialTicks;
        boolean isFirstPerson = mc.gameSettings.thirdPersonView == 0;

        updateConfigContext(player);
        transform.eulerRot.set(0.0, 0.0, 0.0);

        Vector3d velocity = new Vector3d(player.motionX, player.motionY, player.motionZ);
        Vector3d eulerRot = new Vector3d(player.rotationPitch, player.rotationYaw, 0.0);
        if (!velocity.equals(prevVelocity) || !eulerRot.equals(prevEulerRot)) {
            lastActionTime = time;
        }

        applyCameraEffects(player, isFirstPerson, eulerRot, time, partialTicks);
        applyRotations();

        prevVelocity.set(velocity);
        prevEulerRot.set(eulerRot);
        wasFirstPerson = isFirstPerson;
    }

    private void applyCameraEffects(EntityPlayerSP player, boolean isFirstPerson, Vector3d eulerRot, float time, float partialTicks) {
        verticalVelocityPitchOffset(player, partialTicks);
        forwardVelocityPitchOffset(player, partialTicks);
        strafingRollOffset(player, partialTicks);
        turningRollOffset(player, isFirstPerson, eulerRot, partialTicks);
        noiseOffset(time, partialTicks);

        shakeSystem.update(partialTicks);

        if (shakeSystem.isShaking()) {
            Vector3d shakeOffset = shakeSystem.getShakeOffset();
            transform.eulerRot.add(shakeOffset);
        }
    }

    private void applyRotations() {
        GL11.glRotatef((float) transform.eulerRot.z, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef((float) transform.eulerRot.x, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float) transform.eulerRot.y, 0.0F, 1.0F, 0.0F);
    }

    private void updateConfigContext(EntityPlayerSP player) {
        config.reset();
        if (player.isRiding()) {
            config.applyRidingModifiers();
        } else if (player.isInWater()) {
            // maybe Aqua Acrobatics integration later?
            config.applySwimmingModifiers();
        } else if (player.capabilities.isFlying) {
            config.applyFlyingModifiers();
        }
    }

    private void verticalVelocityPitchOffset(EntityPlayerSP player, float deltaTime) {
        double multiplier = config.verticalPitchFactor;
        double smoothing = BASE_VERTICAL_PITCH_SMOOTHING * config.verticalSmoothingFactor;

        double targetOffset = player.motionY * multiplier;
        double currentOffset = MathUtils.damp(prevVerticalPitchOffset, targetOffset, smoothing, deltaTime);

        transform.eulerRot.x += currentOffset;
        prevVerticalPitchOffset = currentOffset;
    }

    private void forwardVelocityPitchOffset(EntityPlayerSP player, float deltaTime) {
        double multiplier = config.forwardPitchFactor;
        double smoothing = BASE_FORWARD_PITCH_SMOOTHING * config.horizontalSmoothingFactor;

        double targetOffset = player.moveForward * multiplier;
        double currentOffset = MathUtils.damp(prevForwardPitchOffset, targetOffset, smoothing, deltaTime);

        transform.eulerRot.x += currentOffset;
        prevForwardPitchOffset = currentOffset;
    }

    private void strafingRollOffset(EntityPlayerSP player, float deltaTime) {
        double multiplier = config.strafeRollFactor;
        double smoothing = BASE_STRAFING_ROLL_SMOOTHING * config.horizontalSmoothingFactor;

        double target = -player.moveStrafing * multiplier;
        double offset = MathUtils.damp(prevStrafingRollOffset, target, smoothing, deltaTime);

        transform.eulerRot.z += offset;
        prevStrafingRollOffset = offset;
    }

    private void turningRollOffset(EntityPlayerSP player, boolean isFirstPerson, Vector3d eulerRot, float deltaTime) {
        double decaySmoothing = BASE_TURNING_ROLL_SMOOTHING * config.turningRollSmoothing;
        double intensity = config.turningRollIntensity;
        double accumulation = config.turningRollAccumulation;

        double yawDelta = prevEulerRot.y - eulerRot.y;
        if (isFirstPerson != wasFirstPerson) {
            yawDelta = 0.0;
        }

        turningRollTargetOffset = MathUtils.damp(turningRollTargetOffset, 0, decaySmoothing, deltaTime);
        turningRollTargetOffset = MathUtils.clamp(turningRollTargetOffset + yawDelta * accumulation, -0.5, 0.5);
        double turningRollOffset = MathUtils.clamp01(turningEasing(Math.abs(turningRollTargetOffset))) *
                intensity * Math.signum(turningRollTargetOffset);

        transform.eulerRot.z += turningRollOffset;
    }

    private void noiseOffset(float time, float deltaTime) {
        float noiseX = time * cameraConfig.swayFrequency;

        if (time - lastActionTime < cameraConfig.swayFadeDelay) {
            cameraSwayFactorTarget = 0.0;
        } else if (cameraSwayFactor == cameraSwayFactorTarget) {
            cameraSwayFactorTarget = 1.0;
        }

        double fadeLength = cameraSwayFactorTarget > 0 ? cameraConfig.swayFadeInLength : cameraConfig.swayFadeOutLength;
        double fadeStep = fadeLength > 0.0 ? deltaTime / fadeLength : 1.0;
        cameraSwayFactor = MathUtils.stepTowards(cameraSwayFactor, cameraSwayFactorTarget, fadeStep);

        double scaledIntensity = cameraConfig.swayIntensity * Math.pow(cameraSwayFactor, CAMERASWAY_FADING_SMOOTHNESS);
        Vector3d target = new Vector3d(scaledIntensity, scaledIntensity, 0.0);
        Vector3d noise = new Vector3d(
                SimplexNoise.noise2(420L, noiseX, 0.0),
                SimplexNoise.noise2(1337L, noiseX, 0.0),
                SimplexNoise.noise2(6969L, noiseX, 0.0)
        );

        transform.eulerRot.add(noise.mul(target, new Vector3d()));
    }

    private static double turningEasing(double x) {
        return x < 0.5 ? (4 * x * x * x) : (1 - Math.pow(-2 * x + 2, 3) / 2);
    }

    public ShakeSystem getShakeSystem() {
        return shakeSystem;
    }

}